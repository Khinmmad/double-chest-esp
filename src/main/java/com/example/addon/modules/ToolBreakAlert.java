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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ToolBreakAlert extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> threshold = sgGeneral.add(new IntSetting.Builder()
        .name("durability-threshold")
        .description("Avisa cuando la durabilidad restante baja de este valor.")
        .defaultValue(25).min(1).max(200).sliderRange(5, 100)
        .build()
    );

    private final Setting<Boolean> playSound = sgGeneral.add(new BoolSetting.Builder()
        .name("play-sound")
        .description("Reproduce un sonido al avisar.")
        .defaultValue(true)
        .build()
    );

    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private final boolean[] warned = new boolean[SLOTS.length];
    private int ticker = 0;

    public ToolBreakAlert() {
        super(AddonCategory.DCE, "tool-break-alert",
              "Avisa cuando una herramienta, elytra o armadura está a punto de romperse.");
    }

    @Override
    public void onActivate() {
        ticker = 0;
        for (int i = 0; i < warned.length; i++) warned[i] = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (++ticker < 10) return; // ~2 chequeos por segundo
        ticker = 0;

        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = mc.player.getItemBySlot(SLOTS[i]);
            if (stack.isEmpty() || !stack.isDamageableItem()) {
                warned[i] = false;
                continue;
            }

            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            if (remaining <= threshold.get()) {
                if (!warned[i]) {
                    warned[i] = true;
                    ChatUtils.sendMsg(Component.literal(
                        "§e⚠ §f" + stack.getHoverName().getString() +
                        " §7casi roto §8(§c" + remaining + "§8 de durabilidad)"
                    ));
                    if (playSound.get()) {
                        Minecraft.getInstance().getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), 1.6f, 0.8f)
                        );
                    }
                }
            } else {
                warned[i] = false;
            }
        }
    }
}
