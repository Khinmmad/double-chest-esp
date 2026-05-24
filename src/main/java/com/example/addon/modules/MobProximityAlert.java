package com.example.addon.modules;

import com.example.addon.AddonCategory;
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
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;

public class MobProximityAlert extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Distancia (bloques) para avisar de un hostil.")
        .defaultValue(12).min(2).max(48).sliderRange(2, 32)
        .build()
    );

    private final Setting<Integer> cooldown = sgGeneral.add(new IntSetting.Builder()
        .name("cooldown")
        .description("Ticks de espera entre avisos (evita spam).")
        .defaultValue(60).min(5).max(200).sliderRange(20, 120)
        .build()
    );

    private final Setting<Boolean> chatMessage = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-message")
        .description("Mostrar también un mensaje en el chat.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> playSound = sgGeneral.add(new BoolSetting.Builder()
        .name("play-sound")
        .description("Reproducir un sonido de aviso.")
        .defaultValue(true)
        .build()
    );

    private int cd = 0;

    public MobProximityAlert() {
        super(AddonCategory.DCE, "mob-proximity-alert",
              "Avisa cuando un mob hostil (o un creeper) se acerca demasiado.");
    }

    @Override
    public void onActivate() {
        cd = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.level == null || mc.player == null) return;
        if (cd > 0) { cd--; return; }

        int r = range.get();
        Entity nearest = null;
        double nearestSq = (double) r * r;
        boolean creeper = false;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof Monster)) continue;
            double dSq = e.distanceToSqr(mc.player);
            if (dSq <= nearestSq) {
                nearestSq = dSq;
                nearest = e;
                creeper = e instanceof Creeper;
            }
        }

        if (nearest == null) return;

        cd = cooldown.get();
        double dist = Math.sqrt(nearestSq);
        String name = creeper ? "§a¡Creeper!" : "§c" + nearest.getName().getString();

        if (chatMessage.get()) {
            ChatUtils.sendMsg(Component.literal(
                "§6⚠ Hostil cerca: " + name + " §7a §f" + String.format("%.1f", dist) + "§7 bloques"
            ));
        }
        if (playSound.get()) {
            float pitch = creeper ? 0.6f : 1.2f;
            Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), pitch, 1.0f)
            );
        }
    }
}
