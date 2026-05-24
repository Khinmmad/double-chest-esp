package com.example.addon.modules;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class OreESP extends BlockScanESP {

    private final Setting<Boolean> diamond = sgGeneral.add(new BoolSetting.Builder()
        .name("diamond").description("Diamante (y deepslate).").defaultValue(true).build());

    private final Setting<Boolean> netherite = sgGeneral.add(new BoolSetting.Builder()
        .name("netherite").description("Ancient debris.").defaultValue(true).build());

    private final Setting<Boolean> emerald = sgGeneral.add(new BoolSetting.Builder()
        .name("emerald").description("Esmeralda (y deepslate).").defaultValue(true).build());

    private final Setting<Boolean> gold = sgGeneral.add(new BoolSetting.Builder()
        .name("gold").description("Oro (normal, deepslate y nether).").defaultValue(false).build());

    private final Setting<Boolean> iron = sgGeneral.add(new BoolSetting.Builder()
        .name("iron").description("Hierro (y deepslate).").defaultValue(false).build());

    private final Setting<Boolean> redstone = sgGeneral.add(new BoolSetting.Builder()
        .name("redstone").description("Redstone (y deepslate).").defaultValue(false).build());

    private final Setting<Boolean> lapis = sgGeneral.add(new BoolSetting.Builder()
        .name("lapis").description("Lapislázuli (y deepslate).").defaultValue(false).build());

    private final Setting<Boolean> copper = sgGeneral.add(new BoolSetting.Builder()
        .name("copper").description("Cobre (y deepslate).").defaultValue(false).build());

    private final Setting<Boolean> coal = sgGeneral.add(new BoolSetting.Builder()
        .name("coal").description("Carbón (y deepslate).").defaultValue(false).build());

    private final Setting<Boolean> quartz = sgGeneral.add(new BoolSetting.Builder()
        .name("quartz").description("Cuarzo del nether.").defaultValue(false).build());

    private final Setting<SettingColor> side = sgRender.add(new ColorSetting.Builder()
        .name("side-color").description("Relleno del box.")
        .defaultValue(new SettingColor(0, 220, 255, 50)).build());

    private final Setting<SettingColor> line = sgRender.add(new ColorSetting.Builder()
        .name("line-color").description("Contorno del box.")
        .defaultValue(new SettingColor(0, 220, 255, 220)).build());

    public OreESP() {
        super("ore-esp", "Resalta minerales valiosos a través de paredes (estilo xray).");
    }

    @Override
    protected boolean matches(BlockState state) {
        Block b = state.getBlock();
        if (diamond.get()   && (b == Blocks.DIAMOND_ORE  || b == Blocks.DEEPSLATE_DIAMOND_ORE))  return true;
        if (netherite.get() &&  b == Blocks.ANCIENT_DEBRIS)                                       return true;
        if (emerald.get()   && (b == Blocks.EMERALD_ORE  || b == Blocks.DEEPSLATE_EMERALD_ORE))  return true;
        if (gold.get()      && (b == Blocks.GOLD_ORE     || b == Blocks.DEEPSLATE_GOLD_ORE || b == Blocks.NETHER_GOLD_ORE)) return true;
        if (iron.get()      && (b == Blocks.IRON_ORE     || b == Blocks.DEEPSLATE_IRON_ORE))     return true;
        if (redstone.get()  && (b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE)) return true;
        if (lapis.get()     && (b == Blocks.LAPIS_ORE    || b == Blocks.DEEPSLATE_LAPIS_ORE))    return true;
        if (copper.get()    && (b == Blocks.COPPER_ORE   || b == Blocks.DEEPSLATE_COPPER_ORE))   return true;
        if (coal.get()      && (b == Blocks.COAL_ORE     || b == Blocks.DEEPSLATE_COAL_ORE))     return true;
        if (quartz.get()    &&  b == Blocks.NETHER_QUARTZ_ORE)                                    return true;
        return false;
    }

    @Override protected SettingColor sideColor() { return side.get(); }
    @Override protected SettingColor lineColor() { return line.get(); }
}
