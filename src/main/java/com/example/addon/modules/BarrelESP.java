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
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

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

    private final List<Box> found = new ArrayList<>();
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
        if (mc.world == null || mc.player == null) return;
        if (++ticker < updateDelay.get()) return;
        ticker = 0;

        found.clear();
        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        BlockPos from = new BlockPos(center.getX() - r, mc.world.getBottomY(), center.getZ() - r);
        BlockPos to   = new BlockPos(center.getX() + r, mc.world.getTopY() - 1, center.getZ() + r);

        for (BlockPos pos : BlockPos.iterate(from, to)) {
            if (mc.world.getBlockState(pos).getBlock() != Blocks.BARREL) continue;

            if (ignoreVillagerWorkstations.get()) {
                Box neighborhood = new Box(pos).expand(3.0);
                boolean hasVillager = !mc.world
                    .getEntitiesByClass(net.minecraft.entity.passive.VillagerEntity.class, neighborhood, v -> true)
                    .isEmpty();
                if (hasVillager) continue;
            }

            found.add(new Box(pos.getX(), pos.getY(), pos.getZ(),
                              pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0));
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;
        for (Box b : found) {
            event.renderer.box(b, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }
}
