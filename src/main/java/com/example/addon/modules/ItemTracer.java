package com.example.addon.modules;

import com.example.addon.AddonCategory;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Localiza ítems dropeados en el suelo y dibuja:
 * <ul>
 *   <li>Líneas trazadoras desde la vista del jugador hasta cada ítem.</li>
 *   <li>ESP (caja semitransparente visible a través de paredes) alrededor
 *       de cada ítem, con color configurable.</li>
 * </ul>
 * <p>
 * Optimizaciones de rendimiento:
 * <ul>
 *   <li>El escaneo de entidades se hace cada N ticks, no cada frame.</li>
 *   <li>Se cachean solo los IDs de entidad; las posiciones se interpolan
 *       al renderizar para mantener suavidad sin re-escanear.</li>
 *   <li>La comprobación de rango usa distancia al cuadrado (sin sqrt).</li>
 *   <li>El filtrado por whitelist se hace al escanear, no al renderizar.</li>
 *   <li>El AABB del ESP se construye inline sin crear objetos extra por frame.</li>
 * </ul>
 */
public class ItemTracer extends Module {

    // ── Settings ─────────────────────────────────────────────────────────

    private final SettingGroup sgGeneral  = settings.getDefaultGroup();
    private final SettingGroup sgTracer   = settings.createGroup("Tracer");
    private final SettingGroup sgESP      = settings.createGroup("ESP");
    private final SettingGroup sgNametag  = settings.createGroup("Nametag");

    // ── General ──────────────────────────────────────────────────────────

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Radio máximo de detección en bloques (limitado por hasta dónde el servidor envía las entidades).")
        .defaultValue(96).min(8).max(256).sliderRange(8, 192)
        .build()
    );

    private final Setting<Integer> updateDelay = sgGeneral.add(new IntSetting.Builder()
        .name("update-delay")
        .description("Ticks entre cada escaneo de entidades (1 = cada tick, más preciso/responsivo).")
        .defaultValue(2).min(1).max(60).sliderRange(1, 20)
        .build()
    );

    private final Setting<Boolean> useWhitelist = sgGeneral.add(new BoolSetting.Builder()
        .name("use-whitelist")
        .description("Solo trazar los ítems de la lista blanca.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<Item>> whitelist = sgGeneral.add(new ItemListSetting.Builder()
        .name("whitelist")
        .description("Ítems a trazar (si use-whitelist está activado).")
        .visible(useWhitelist::get)
        .build()
    );

    private final Setting<Boolean> showItemCount = sgGeneral.add(new BoolSetting.Builder()
        .name("show-count")
        .description("Muestra la cantidad de ítems detectados en la info del módulo.")
        .defaultValue(true)
        .build()
    );

    // ── Tracer ───────────────────────────────────────────────────────────

    private final Setting<Boolean> enableTracer = sgTracer.add(new BoolSetting.Builder()
        .name("enable-tracer")
        .description("Dibuja líneas trazadoras desde tu vista a cada ítem.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> tracerColor = sgTracer.add(new ColorSetting.Builder()
        .name("tracer-color")
        .description("Color de las líneas trazadoras.")
        .defaultValue(new SettingColor(0, 255, 170, 200))
        .visible(enableTracer::get)
        .build()
    );

    // ── ESP (visible a través de paredes) ────────────────────────────────

    private final Setting<Boolean> enableESP = sgESP.add(new BoolSetting.Builder()
        .name("enable-esp")
        .description("Dibuja una caja ESP alrededor de cada ítem (visible a través de paredes).")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> espShapeMode = sgESP.add(new EnumSetting.Builder<ShapeMode>()
        .name("esp-shape-mode")
        .description("Both, Sides o Lines para la caja ESP.")
        .defaultValue(ShapeMode.Both)
        .visible(enableESP::get)
        .build()
    );

    private final Setting<SettingColor> espSideColor = sgESP.add(new ColorSetting.Builder()
        .name("esp-side-color")
        .description("Relleno de la caja ESP.")
        .defaultValue(new SettingColor(0, 255, 170, 40))
        .visible(enableESP::get)
        .build()
    );

    private final Setting<SettingColor> espLineColor = sgESP.add(new ColorSetting.Builder()
        .name("esp-line-color")
        .description("Contorno de la caja ESP.")
        .defaultValue(new SettingColor(0, 255, 170, 200))
        .visible(enableESP::get)
        .build()
    );

    // ── Nametag (nombre / cantidad / distancia sobre el ítem) ─────────────

    private final Setting<Boolean> nametags = sgNametag.add(new BoolSetting.Builder()
        .name("nametags")
        .description("Muestra nombre, cantidad y distancia sobre cada ítem.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> nametagCount = sgNametag.add(new BoolSetting.Builder()
        .name("nametag-count")
        .description("Incluir la cantidad (xN) en el nametag.")
        .defaultValue(true)
        .visible(nametags::get)
        .build()
    );

    private final Setting<Boolean> nametagDistance = sgNametag.add(new BoolSetting.Builder()
        .name("nametag-distance")
        .description("Incluir la distancia en bloques en el nametag.")
        .defaultValue(true)
        .visible(nametags::get)
        .build()
    );

    private final Setting<Double> nametagScale = sgNametag.add(new DoubleSetting.Builder()
        .name("nametag-scale")
        .description("Tamaño del texto del nametag.")
        .defaultValue(1.0).min(0.25).max(3.0).sliderRange(0.5, 2.0)
        .visible(nametags::get)
        .build()
    );

    private final Setting<SettingColor> nametagColor = sgNametag.add(new ColorSetting.Builder()
        .name("nametag-color")
        .description("Color del texto del nametag.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(nametags::get)
        .build()
    );

    // ── Cache ────────────────────────────────────────────────────────────

    // Guardamos solo los IDs de las entidades que pasaron el filtro.
    // Al renderizar, buscamos la entidad por ID (O(1) en el mapa del nivel)
    // y usamos su posición interpolada para suavidad visual.
    private final List<Integer> cachedIds = new ArrayList<>();
    private int ticker = 0;

    // Tamaño de la caja ESP (radio desde el centro del ítem)
    private static final double ESP_HALF = 0.25;

    // ── Constructor ──────────────────────────────────────────────────────

    public ItemTracer() {
        super(AddonCategory.DCE, "item-tracer",
              "Tracer + ESP para ítems dropeados en el suelo (visible a través de paredes).");
    }

    // ── Lifecycle ────────────────────────────────────────────────────────

    @Override
    public void onActivate() {
        cachedIds.clear();
        ticker = 0;
    }

    @Override
    public void onDeactivate() {
        cachedIds.clear();
    }

    // ── Scan (throttled) ─────────────────────────────────────────────────

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.level == null || mc.player == null) return;
        if (++ticker < updateDelay.get()) return;
        ticker = 0;

        cachedIds.clear();

        double rangeSq = range.get() * (double) range.get();
        Vec3 playerPos = mc.player.position();

        boolean filtering = useWhitelist.get();
        List<Item> allowed = filtering ? whitelist.get() : null;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;

            // Distancia al cuadrado — evita sqrt
            double dx = entity.getX() - playerPos.x;
            double dy = entity.getY() - playerPos.y;
            double dz = entity.getZ() - playerPos.z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > rangeSq) continue;

            // Filtro de whitelist
            if (filtering && allowed != null && !allowed.isEmpty()) {
                ItemStack stack = itemEntity.getItem();
                if (!allowed.contains(stack.getItem())) continue;
            }

            cachedIds.add(entity.getId());
        }
    }

    // ── Render ───────────────────────────────────────────────────────────

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null || cachedIds.isEmpty()) return;

        boolean tracer = enableTracer.get();
        boolean esp    = enableESP.get();
        if (!tracer && !esp) return;

        // Precalcular origen del tracer (1 bloque delante del ojo)
        double sx = 0, sy = 0, sz = 0;
        if (tracer) {
            Vec3 eye  = mc.gameRenderer.getMainCamera().position();
            Vec3 look = mc.player.getViewVector(event.tickDelta);
            sx = eye.x + look.x;
            sy = eye.y + look.y;
            sz = eye.z + look.z;
        }

        SettingColor tc = tracer ? tracerColor.get() : null;
        SettingColor sc = esp ? espSideColor.get() : null;
        SettingColor lc = esp ? espLineColor.get() : null;
        ShapeMode sm    = esp ? espShapeMode.get() : null;
        float delta = event.tickDelta;

        for (int i = 0, n = cachedIds.size(); i < n; i++) {
            Entity entity = mc.level.getEntity(cachedIds.get(i));
            if (entity == null || entity.isRemoved()) continue;

            // Posición interpolada para suavidad visual
            double ex = entity.xOld + (entity.getX() - entity.xOld) * delta;
            double ey = entity.yOld + (entity.getY() - entity.yOld) * delta;
            double ez = entity.zOld + (entity.getZ() - entity.zOld) * delta;

            // Tracer: línea desde la vista al ítem
            if (tracer) {
                event.renderer.line(sx, sy, sz, ex, ey, ez, tc);
            }

            // ESP: caja alrededor del ítem (visible a través de paredes)
            if (esp) {
                event.renderer.box(
                    ex - ESP_HALF, ey,            ez - ESP_HALF,
                    ex + ESP_HALF, ey + ESP_HALF * 2, ez + ESP_HALF,
                    sc, lc, sm, 0
                );
            }
        }
    }

    // ── Nametags (texto 2D proyectado sobre cada ítem) ───────────────────

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (!nametags.get() || mc.level == null || mc.player == null || cachedIds.isEmpty()) return;

        TextRenderer text = TextRenderer.get();
        double s = nametagScale.get();
        float delta = event.tickDelta;

        for (int i = 0, n = cachedIds.size(); i < n; i++) {
            Entity entity = mc.level.getEntity(cachedIds.get(i));
            if (!(entity instanceof ItemEntity item) || entity.isRemoved()) continue;

            double ex = item.xOld + (item.getX() - item.xOld) * delta;
            double ey = item.yOld + (item.getY() - item.yOld) * delta;
            double ez = item.zOld + (item.getZ() - item.zOld) * delta;

            Vector3d pos = new Vector3d(ex, ey + 0.45, ez);
            if (!NametagUtils.to2D(pos, s)) continue;

            ItemStack stack = item.getItem();
            StringBuilder sb = new StringBuilder(stack.getHoverName().getString());
            if (nametagCount.get() && stack.getCount() > 1) sb.append(" x").append(stack.getCount());
            if (nametagDistance.get()) sb.append("  ").append((int) mc.player.distanceTo(item)).append("m");
            String str = sb.toString();

            NametagUtils.begin(pos);
            text.begin(s);
            double w = text.getWidth(str);
            text.render(str, -w / 2.0, 0, nametagColor.get());
            text.end();
            NametagUtils.end();
        }
    }

    // ── Info string ──────────────────────────────────────────────────────

    @Override
    public String getInfoString() {
        if (!showItemCount.get()) return null;
        return String.valueOf(cachedIds.size());
    }
}
