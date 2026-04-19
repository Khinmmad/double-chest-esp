package com.example.addon;

import com.example.addon.modules.BarrelESP;
import com.example.addon.modules.ChestTracer;
import com.example.addon.modules.DoubleChestESP;
import com.example.addon.modules.ShulkerESP;
import com.example.addon.modules.StashFinder;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class AddonMain extends MeteorAddon {

    public static final Logger LOG  = LogUtils.getLogger();
    public static final String NAME = "DoubleChestESP Addon";

    @Override
    public void onInitialize() {
        LOG.info("Initializing {} v1.0.0", NAME);

        // Registra todos los módulos en la categoría propia
        Modules modules = Modules.get();
        modules.add(new DoubleChestESP());
        modules.add(new BarrelESP());
        modules.add(new ShulkerESP());
        modules.add(new ChestTracer());
        modules.add(new StashFinder());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(AddonCategory.DCE);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }

    @Override
    public String getWebsite() {
        return "https://github.com/IsraelZermeno/double-chest-esp";
    }
}
