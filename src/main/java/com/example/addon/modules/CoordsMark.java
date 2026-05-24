package com.example.addon.modules;

import com.example.addon.AddonCategory;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class CoordsMark extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Keybind> markKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("mark-key")
        .description("Tecla para marcar tu posición actual (con el módulo activo).")
        .defaultValue(Keybind.none())
        .build()
    );

    private final Setting<Boolean> copyToClipboard = sgGeneral.add(new BoolSetting.Builder()
        .name("copy-to-clipboard")
        .description("Copia las coordenadas marcadas al portapapeles.")
        .defaultValue(true)
        .build()
    );

    private boolean wasPressed = false;

    public CoordsMark() {
        super(AddonCategory.DCE, "coords-mark",
              "Marca tu posición actual con una tecla y la manda al chat/portapapeles.");
    }

    @Override
    public void onActivate() {
        wasPressed = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        boolean pressed = markKey.get().isPressed();
        if (pressed && !wasPressed) {
            BlockPos pos = mc.player.blockPosition();
            String coords = pos.getX() + ", " + pos.getY() + ", " + pos.getZ();

            ChatUtils.sendMsg(Component.literal(
                "§b📍 Waypoint: §f" + coords +
                (copyToClipboard.get() ? " §7(copiado)" : "")
            ));
            if (copyToClipboard.get()) {
                Minecraft.getInstance().keyboardHandler.setClipboard(coords);
            }
        }
        wasPressed = pressed;
    }
}
