package com.example.addon.modules;

import com.example.addon.AddonCategory;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

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

    private final Set<Long> notifiedChunks = new HashSet<>();

    public StashFinder() {
        super(AddonCategory.DCE, "stash-finder",
              "Notifica cuando un chunk contiene muchos contenedores (detector de stashes).");
    }

    @Override
    public void onActivate() {
        notifiedChunks.clear();
    }

    @Override
    public void onDeactivate() {
        notifiedChunks.clear();
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        if (mc.world == null) return;

        WorldChunk chunk = event.chunk();
        ChunkPos cp = chunk.getPos();
        long key = cp.toLong();
        if (notifiedChunks.contains(key)) return;

        int count = 0;
        int bottomY = mc.world.getBottomY();
        int topY    = mc.world.getTopYInclusive();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = bottomY; y <= topY; y++) {
                    BlockPos pos = new BlockPos(cp.getStartX() + x, y, cp.getStartZ() + z);
                    Block b = chunk.getBlockState(pos).getBlock();
                    if (matches(b)) count++;
                }
            }
        }

        if (count >= minContainers.get()) {
            notifiedChunks.add(key);
            BlockPos centerBlock = new BlockPos(cp.getStartX() + 8, 64, cp.getStartZ() + 8);

            ChatUtils.sendMsg(Text.literal(
                "§dStashFinder §7| §fChunk con §a" + count +
                " §fcontenedores cerca de §b" + centerBlock.toShortString()
            ));

            if (playSound.get()) {
                MinecraftClient.getInstance().getSoundManager().play(
                    net.minecraft.client.sound.PositionedSoundInstance.master(
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f
                    )
                );
            }
        }
    }

    private boolean matches(Block b) {
        if (countChests.get() && (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST)) return true;
        if (countBarrels.get() && b == Blocks.BARREL) return true;
        if (countShulkers.get() && b instanceof ShulkerBoxBlock) return true;
        if (countHoppers.get() && b == Blocks.HOPPER) return true;
        return false;
    }
}
