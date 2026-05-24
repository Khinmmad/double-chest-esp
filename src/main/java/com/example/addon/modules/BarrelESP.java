package com.example.addon.modules;

import com.example.addon.AddonCategory;
import com.example.addon.NametagHelper;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
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
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BarrelESP extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender  = settings.createGroup("Render");

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Radio horizontal en bloques.")
        .defaultValue(40).min(5).max(200).sliderRange(5, 100)
        .build()
    );

    private final Setting<Integer> updateDelay = sgGeneral.add(new IntSetting.Builder()
        .name("update-delay")
        .description("Ticks entre cada búsqueda.")
        .defaultValue(20).min(1).max(100).sliderRange(1, 60)
        .build()
    );

    private final Setting<Boolean> ignoreVillagerWorkstations = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-villager-workstations")
        .description("Ignora barriles cerca de aldeanos (workstations). Aproximación: filtra si hay aldeanos a 3 bloques.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> nametags = sgGeneral.add(new BoolSetting.Builder()
        .name("nametags")
        .description("Mostrar el nombre y la distancia sobre cada barril.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("Both, Sides o Lines.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("Relleno del box.")
        .defaultValue(new SettingColor(139, 90, 43, 55))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Contorno del box.")
        .defaultValue(new SettingColor(139, 90, 43, 230))
        .build()
    );

    private final List<AABB> found = new ArrayList<>();
    private int ticker = 0;

    public BarrelESP() {
        super(AddonCategory.DCE, "barrel-esp", "Resalta barriles a través de paredes.");
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
                    if (!(entry.getValue() instanceof BarrelBlockEntity)) continue;

                    BlockPos pos = entry.getKey();
                    if (Math.abs(pos.getX() - center.getX()) > r) continue;
                    if (Math.abs(pos.getZ() - center.getZ()) > r) continue;

                    if (ignoreVillagerWorkstations.get()) {
                        AABB neighborhood = new AABB(pos).inflate(3.0);
                        boolean hasVillager = !mc.level
                            .getEntitiesOfClass(Villager.class, neighborhood, v -> true)
                            .isEmpty();
                        if (hasVillager) continue;
                    }

                    found.add(new AABB(pos.getX(), pos.getY(), pos.getZ(),
                                       pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0));
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;
        for (AABB b : found) {
            event.renderer.box(b, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (!nametags.get() || mc.player == null) return;
        double px = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();
        for (AABB b : found) {
            double cx = (b.minX + b.maxX) / 2.0, cy = b.maxY + 0.2, cz = (b.minZ + b.maxZ) / 2.0;
            NametagHelper.render(cx, cy, cz, NametagHelper.label("Barril", cx, cy, cz, px, py, pz), 1.0);
        }
    }
}
