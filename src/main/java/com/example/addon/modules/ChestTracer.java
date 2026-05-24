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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
        if (mc.level == null || mc.player == null) return;

        DoubleChestESP dce = Modules.get().get(DoubleChestESP.class);
        if (dce == null || !dce.isActive()) return;

        // Origen en la mira: partimos de la posición de la cámara y la desplazamos
        // 1 bloque hacia delante en la dirección de la vista. Si partiéramos del ojo
        // exacto, la línea se proyectaría como un punto y no se vería.
        Vec3 eye  = mc.gameRenderer.getMainCamera().position();
        Vec3 look = mc.player.getViewVector(event.tickDelta);
        double sx = eye.x + look.x;
        double sy = eye.y + look.y;
        double sz = eye.z + look.z;
        SettingColor c = color.get();

        for (AABB b : dce.getDetectedBoxes()) {
            double cx = (b.minX + b.maxX) / 2.0;
            double cy = (b.minY + b.maxY) / 2.0;
            double cz = (b.minZ + b.maxZ) / 2.0;
            event.renderer.line(sx, sy, sz, cx, cy, cz, c);
        }
    }
}
