package com.example.addon;

import meteordevelopment.meteorclient.systems.modules.Category;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class AddonCategory {
    public static final Category DCE = new Category("DCE", () -> new ItemStack(Items.CHEST));
}
