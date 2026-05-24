package com.example.addon;

import com.example.addon.modules.BarrelESP;
import com.example.addon.modules.ChestTracer;
import com.example.addon.modules.CoordsMark;
import com.example.addon.modules.DeathCoords;
import com.example.addon.modules.DoubleChestESP;
import com.example.addon.modules.ItemTracer;
import com.example.addon.modules.MobProximityAlert;
import com.example.addon.modules.OreESP;
import com.example.addon.modules.ShulkerESP;
import com.example.addon.modules.SpawnerESP;
import com.example.addon.modules.StashFinder;
import com.example.addon.modules.ToolBreakAlert;
import com.example.addon.modules.TrialChamberESP;
import com.example.addon.modules.ValuableBlockESP;
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
        modules.add(new TrialChamberESP());
        modules.add(new ItemTracer());
        // Detección (survival)
        modules.add(new OreESP());
        modules.add(new SpawnerESP());
        modules.add(new ValuableBlockESP());
        // Utilidad / QoL
        modules.add(new DeathCoords());
        modules.add(new ToolBreakAlert());
        modules.add(new MobProximityAlert());
        modules.add(new CoordsMark());
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
