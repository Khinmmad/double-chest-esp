package com.example.addon.modules;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ValuableBlockESP extends BlockScanESP {

    private final Setting<Boolean> beds = sgGeneral.add(new BoolSetting.Builder()
        .name("beds").description("Camas (útil para explosiones en nether/end).").defaultValue(true).build());

    private final Setting<Boolean> endFrames = sgGeneral.add(new BoolSetting.Builder()
        .name("end-portal-frames").description("End portal frames (strongholds).").defaultValue(true).build());

    private final Setting<Boolean> anchors = sgGeneral.add(new BoolSetting.Builder()
        .name("respawn-anchors").description("Respawn anchors.").defaultValue(true).build());

    private final Setting<Boolean> budding = sgGeneral.add(new BoolSetting.Builder()
        .name("budding-amethyst").description("Budding amethyst (geodas).").defaultValue(false).build());

    private final Setting<SettingColor> side = sgRender.add(new ColorSetting.Builder()
        .name("side-color").description("Relleno del box.")
        .defaultValue(new SettingColor(255, 105, 180, 50)).build());

    private final Setting<SettingColor> line = sgRender.add(new ColorSetting.Builder()
        .name("line-color").description("Contorno del box.")
        .defaultValue(new SettingColor(255, 105, 180, 220)).build());

    public ValuableBlockESP() {
        super("valuable-block-esp", "Resalta bloques clave: camas, end portal frames, respawn anchors, etc.");
    }

    @Override
    protected boolean matches(BlockState state) {
        Block b = state.getBlock();
        if (beds.get()      && b instanceof BedBlock)        return true;
        if (endFrames.get() && b == Blocks.END_PORTAL_FRAME) return true;
        if (anchors.get()   && b == Blocks.RESPAWN_ANCHOR)   return true;
        if (budding.get()   && b == Blocks.BUDDING_AMETHYST)  return true;
        return false;
    }

    @Override protected SettingColor sideColor() { return side.get(); }
    @Override protected SettingColor lineColor() { return line.get(); }
}
