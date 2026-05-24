package com.example.addon.modules;

import com.example.addon.AddonCategory;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
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

public class DeathCoords extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> copyToClipboard = sgGeneral.add(new BoolSetting.Builder()
        .name("copy-to-clipboard")
        .description("Copia las coordenadas de muerte al portapapeles.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> playSound = sgGeneral.add(new BoolSetting.Builder()
        .name("play-sound")
        .description("Reproduce un sonido al morir.")
        .defaultValue(true)
        .build()
    );

    private boolean dead = false;

    public DeathCoords() {
        super(AddonCategory.DCE, "death-coords",
              "Al morir, anuncia y copia tus coordenadas para volver por tus cosas.");
    }

    @Override
    public void onActivate() {
        dead = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (mc.player.getHealth() <= 0.0f) {
            if (!dead) {
                dead = true;
                BlockPos pos = mc.player.blockPosition();
                String coords = pos.getX() + ", " + pos.getY() + ", " + pos.getZ();

                ChatUtils.sendMsg(Component.literal(
                    "§c☠ Moriste en §f" + coords +
                    (copyToClipboard.get() ? " §7(copiado al portapapeles)" : "")
                ));

                if (copyToClipboard.get()) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(coords);
                }
                if (playSound.get()) {
                    Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), 0.7f, 1.0f)
                    );
                }
            }
        } else {
            dead = false;
        }
    }
}
