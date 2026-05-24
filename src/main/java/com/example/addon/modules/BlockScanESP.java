package com.example.addon.modules;

import com.example.addon.AddonCategory;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base para ESPs que escanean BLOQUES (no block entities), p.ej. minerales.
 * Para no provocar tirones: encola los chunks (al cargar, al actualizarse un
 * bloque relevante, o al activarse) y escanea solo unos pocos por tick,
 * saltando secciones de solo-aire y secciones cuya paleta no contiene el bloque.
 * Los resultados se cachean por chunk y se renderizan cada frame.
 */
public abstract class BlockScanESP extends Module {

    protected final SettingGroup sgGeneral = settings.getDefaultGroup();
    protected final SettingGroup sgRender  = settings.createGroup("Render");

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Radio de detección en bloques.")
        .defaultValue(48).min(16).max(160).sliderRange(16, 128)
        .build()
    );

    private final Setting<Integer> chunksPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("chunks-per-tick")
        .description("Chunks escaneados por tick. Más bajo = más suave al moverse.")
        .defaultValue(2).min(1).max(16).sliderRange(1, 8)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("Both, Sides o Lines.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final ArrayDeque<Long> queue = new ArrayDeque<>();
    private final Set<Long> queued = new HashSet<>();
    private final Map<Long, List<AABB>> found = new HashMap<>();

    protected BlockScanESP(String name, String description) {
        super(AddonCategory.DCE, name, description);
    }

    /** true si el bloque debe resaltarse. */
    protected abstract boolean matches(BlockState state);
    protected abstract SettingColor sideColor();
    protected abstract SettingColor lineColor();

    @Override
    public void onActivate() {
        clearAll();
        if (mc.level == null || mc.player == null) return;
        int cr = (range.get() >> 4) + 1;
        int pcx = mc.player.blockPosition().getX() >> 4;
        int pcz = mc.player.blockPosition().getZ() >> 4;
        for (int cx = pcx - cr; cx <= pcx + cr; cx++)
            for (int cz = pcz - cr; cz <= pcz + cr; cz++)
                enqueue(ChunkPos.pack(cx, cz));
    }

    @Override
    public void onDeactivate() {
        clearAll();
    }

    private void clearAll() {
        queue.clear();
        queued.clear();
        found.clear();
    }

    private void enqueue(long key) {
        if (queued.add(key)) queue.add(key);
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        enqueue(event.chunk().getPos().pack());
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        if (matches(event.oldState) || matches(event.newState)) {
            enqueue(ChunkPos.pack(event.pos.getX() >> 4, event.pos.getZ() >> 4));
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.level == null || mc.player == null) return;

        int budget = chunksPerTick.get();
        while (budget-- > 0 && !queue.isEmpty()) {
            long key = queue.poll();
            queued.remove(key);
            scanChunk(key);
        }

        // Poda chunks lejanos para no acumular memoria/render.
        int cr = (range.get() >> 4) + 2;
        int pcx = mc.player.blockPosition().getX() >> 4;
        int pcz = mc.player.blockPosition().getZ() >> 4;
        found.keySet().removeIf(k ->
            Math.abs(ChunkPos.getX(k) - pcx) > cr || Math.abs(ChunkPos.getZ(k) - pcz) > cr);
    }

    private void scanChunk(long key) {
        if (mc.level == null) return;
        int cx = ChunkPos.getX(key);
        int cz = ChunkPos.getZ(key);
        LevelChunk chunk = mc.level.getChunk(cx, cz);

        LevelChunkSection[] sections = chunk.getSections();
        int minSecY = chunk.getMinSectionY();
        int ox = cx << 4, oz = cz << 4;
        List<AABB> list = null;

        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection sec = sections[i];
            if (sec == null || sec.hasOnlyAir() || !sec.maybeHas(this::matches)) continue;

            int baseY = (minSecY + i) << 4;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (matches(sec.getBlockState(x, y, z))) {
                            if (list == null) list = new ArrayList<>();
                            double wx = ox + x, wy = baseY + y, wz = oz + z;
                            list.add(new AABB(wx, wy, wz, wx + 1.0, wy + 1.0, wz + 1.0));
                        }
                    }
                }
            }
        }

        if (list != null) found.put(key, list);
        else found.remove(key);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (found.isEmpty()) return;
        SettingColor sc = sideColor();
        SettingColor lc = lineColor();
        ShapeMode mode = shapeMode.get();
        for (List<AABB> list : found.values()) {
            for (AABB box : list) {
                event.renderer.box(box, sc, lc, mode, 0);
            }
        }
    }
}
