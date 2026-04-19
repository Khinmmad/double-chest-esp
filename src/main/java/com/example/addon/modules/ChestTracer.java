package com.example.addon.modules;

import com.example.addon.AddonCategory;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ChestTracer extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("Color de la línea trazadora.")
        .defaultValue(new SettingColor(255, 165, 0, 200))
        .build()
    );

    public ChestTracer() {
        super(AddonCategory.DCE, "chest-tracer",
              "Dibuja líneas desde el jugador a cada cofre doble detectado por DoubleChestESP.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        DoubleChestESP dce = Modules.get().get(DoubleChestESP.class);
        if (dce == null || !dce.isActive()) return;

        Vec3d pPos = mc.player.getPos().add(0, mc.player.getStandingEyeHeight() * 0.5, 0);
        SettingColor c = color.get();

        for (Box b : dce.getDetectedBoxes()) {
            double cx = (b.minX + b.maxX) / 2.0;
            double cy = (b.minY + b.maxY) / 2.0;
            double cz = (b.minZ + b.maxZ) / 2.0;
            event.renderer.line(pPos.x, pPos.y, pPos.z, cx, cy, cz, c);
        }
    }
}
