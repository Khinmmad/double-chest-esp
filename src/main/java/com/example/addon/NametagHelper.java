package com.example.addon;

import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import org.joml.Vector3d;

/**
 * Utilidad compartida para dibujar nametags (texto 2D proyectado) sobre una
 * posición del mundo. Debe llamarse SIEMPRE dentro de un Render2DEvent.
 */
public final class NametagHelper {

    private static final Color WHITE = new SettingColor(255, 255, 255, 255);

    private NametagHelper() {}

    public static void render(double x, double y, double z, String label, double scale) {
        Vector3d pos = new Vector3d(x, y, z);
        if (!NametagUtils.to2D(pos, scale)) return;

        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(pos);
        text.begin(scale);
        double w = text.getWidth(label);
        text.render(label, -w / 2.0, 0, WHITE);
        text.end();
        NametagUtils.end();
    }

    /** Etiqueta "Nombre  Dm" con la distancia (en bloques) del jugador al punto. */
    public static String label(String name, double x, double y, double z,
                               double px, double py, double pz) {
        double dx = x - px, dy = y - py, dz = z - pz;
        int dist = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
        return name + "  " + dist + "m";
    }
}
