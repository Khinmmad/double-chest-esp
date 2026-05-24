package com.example.addon.modules;

import com.example.addon.AddonCategory;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrialChamberESP extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender  = settings.createGroup("Render");

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Radio horizontal en bloques.")
        .defaultValue(64).min(5).max(200).sliderRange(5, 128)
        .build()
    );

    private final Setting<Integer> updateDelay = sgGeneral.add(new IntSetting.Builder()
        .name("update-delay")
        .description("Ticks entre cada búsqueda.")
        .defaultValue(20).min(1).max(100).sliderRange(1, 60)
        .build()
    );

    private final Setting<Boolean> showSpawners = sgGeneral.add(new BoolSetting.Builder()
        .name("spawners")
        .description("Resaltar trial spawners (normales y ominosos).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showVaults = sgGeneral.add(new BoolSetting.Builder()
        .name("vaults")
        .description("Resaltar vaults / cofres de llaves (normales y ominosos).")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("Both, Sides o Lines.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> spawnerSide = sgRender.add(new ColorSetting.Builder()
        .name("spawner-side-color")
        .description("Relleno del box (trial spawner).")
        .defaultValue(new SettingColor(0, 200, 200, 55))
        .build()
    );

    private final Setting<SettingColor> spawnerLine = sgRender.add(new ColorSetting.Builder()
        .name("spawner-line-color")
        .description("Contorno del box (trial spawner).")
        .defaultValue(new SettingColor(0, 200, 200, 230))
        .build()
    );

    private final Setting<SettingColor> vaultSide = sgRender.add(new ColorSetting.Builder()
        .name("vault-side-color")
        .description("Relleno del box (vault).")
        .defaultValue(new SettingColor(255, 215, 0, 55))
        .build()
    );

    private final Setting<SettingColor> vaultLine = sgRender.add(new ColorSetting.Builder()
        .name("vault-line-color")
        .description("Contorno del box (vault).")
        .defaultValue(new SettingColor(255, 215, 0, 230))
        .build()
    );

    private static class Entry {
        final AABB box;
        final boolean vault;
        Entry(AABB box, boolean vault) { this.box = box; this.vault = vault; }
    }

    private final List<Entry> found = new ArrayList<>();
    private int ticker = 0;

    public TrialChamberESP() {
        super(AddonCategory.DCE, "trial-chamber-esp",
              "Resalta trial spawners y vaults (cofres de llaves) de las trial chambers a través de paredes.");
    }

    @Override
    public void onActivate() {
        found.clear();
        ticker = 0;
    }

    @Override
    public void onDeactivate() {
        found.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.level == null || mc.player == null) return;
        if (++ticker < updateDelay.get()) return;
        ticker = 0;

        found.clear();
        int r = range.get();
        BlockPos center = mc.player.blockPosition();

        int chunkRadius = (r >> 4) + 1;
        int pcx = center.getX() >> 4;
        int pcz = center.getZ() >> 4;

        for (int cx = pcx - chunkRadius; cx <= pcx + chunkRadius; cx++) {
            for (int cz = pcz - chunkRadius; cz <= pcz + chunkRadius; cz++) {
                LevelChunk chunk = mc.level.getChunk(cx, cz);

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntity be = entry.getValue();

                    boolean isSpawner = be instanceof TrialSpawnerBlockEntity;
                    boolean isVault   = be instanceof VaultBlockEntity;
                    if (!isSpawner && !isVault) continue;
                    if (isSpawner && !showSpawners.get()) continue;
                    if (isVault && !showVaults.get()) continue;

                    BlockPos pos = entry.getKey();
                    if (Math.abs(pos.getX() - center.getX()) > r) continue;
                    if (Math.abs(pos.getZ() - center.getZ()) > r) continue;

                    found.add(new Entry(
                        new AABB(pos.getX(), pos.getY(), pos.getZ(),
                                 pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0),
                        isVault
                    ));
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;
        for (Entry e : found) {
            SettingColor sc = e.vault ? vaultSide.get() : spawnerSide.get();
            SettingColor lc = e.vault ? vaultLine.get() : spawnerLine.get();
            event.renderer.box(e.box, sc, lc, shapeMode.get(), 0);
        }
    }
}
