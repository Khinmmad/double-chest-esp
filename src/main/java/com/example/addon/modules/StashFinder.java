package com.example.addon.modules;

import com.example.addon.AddonCategory;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class StashFinder extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> minContainers = sgGeneral.add(new IntSetting.Builder()
        .name("min-containers")
        .description("Número mínimo de contenedores en un chunk para considerarlo un stash.")
        .defaultValue(4).min(1).max(64).sliderRange(1, 32)
        .build()
    );

    private final Setting<Integer> chunksPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("chunks-per-tick")
        .description("Cuántos chunks de la cola se analizan por tick. Más bajo = más suave al moverse.")
        .defaultValue(8).min(1).max(64).sliderRange(1, 32)
        .build()
    );

    private final Setting<Boolean> countChests = sgGeneral.add(new BoolSetting.Builder()
        .name("count-chests")
        .description("Contar cofres y cofres trampa.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> countBarrels = sgGeneral.add(new BoolSetting.Builder()
        .name("count-barrels")
        .description("Contar barriles.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> countShulkers = sgGeneral.add(new BoolSetting.Builder()
        .name("count-shulkers")
        .description("Contar shulker boxes.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> countHoppers = sgGeneral.add(new BoolSetting.Builder()
        .name("count-hoppers")
        .description("Contar hoppers.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> playSound = sgGeneral.add(new BoolSetting.Builder()
        .name("play-sound")
        .description("Reproducir un sonido cuando se detecta un stash.")
        .defaultValue(true)
        .build()
    );

    // Chunks ya anunciados (no repetir aviso).
    private final Set<Long> notifiedChunks = new HashSet<>();
    // Cola de chunks pendientes de analizar + set para evitar duplicados.
    private final ArrayDeque<Long> pending = new ArrayDeque<>();
    private final Set<Long> pendingSet = new HashSet<>();

    public StashFinder() {
        super(AddonCategory.DCE, "stash-finder",
              "Notifica cuando un chunk contiene muchos contenedores (detector de stashes).");
    }

    @Override
    public void onActivate() {
        notifiedChunks.clear();
        pending.clear();
        pendingSet.clear();
    }

    @Override
    public void onDeactivate() {
        notifiedChunks.clear();
        pending.clear();
        pendingSet.clear();
    }

    // Al cargar un chunk solo lo ENCOLAMOS: el trabajo real se reparte en onTick
    // para no procesar cientos de chunks de golpe al moverse/teletransportarse.
    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        long key = event.chunk().getPos().pack();
        if (notifiedChunks.contains(key)) return;
        if (pendingSet.add(key)) pending.add(key);
    }

    // Procesa un número limitado de chunks por tick (cola amortiguada).
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.level == null || pending.isEmpty()) return;

        int budget = chunksPerTick.get();
        while (budget-- > 0 && !pending.isEmpty()) {
            long key = pending.poll();
            pendingSet.remove(key);
            if (notifiedChunks.contains(key)) continue;

            int cx = ChunkPos.getX(key);
            int cz = ChunkPos.getZ(key);
            LevelChunk chunk = mc.level.getChunk(cx, cz);

            int count = 0;
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (matches(be)) count++;
            }
            if (count == 0) continue; // chunk vacío/no cargado todavía: lo ignoramos.

            if (count >= minContainers.get()) {
                notifiedChunks.add(key);
                BlockPos center = new BlockPos(cx * 16 + 8, 64, cz * 16 + 8);

                ChatUtils.sendMsg(Component.literal(
                    "§dStashFinder §7| §fChunk con §a" + count +
                    " §fcontenedores cerca de §b" + center.toShortString()
                ));

                if (playSound.get()) {
                    Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(
                            SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f
                        )
                    );
                }
            }
        }
    }

    private boolean matches(BlockEntity be) {
        if (countChests.get() && be instanceof ChestBlockEntity) return true;
        if (countBarrels.get() && be instanceof BarrelBlockEntity) return true;
        if (countShulkers.get() && be instanceof ShulkerBoxBlockEntity) return true;
        if (countHoppers.get() && be instanceof HopperBlockEntity) return true;
        return false;
    }
}
