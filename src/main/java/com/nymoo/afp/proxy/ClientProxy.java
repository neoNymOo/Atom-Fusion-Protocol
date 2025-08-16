package com.nymoo.afp.proxy;

import com.nymoo.afp.AtomFusionProtocol;
import com.nymoo.afp.common.item.AbstractPowerArmor;
import com.nymoo.afp.common.render.model.armor.PowerArmorModel;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class ClientProxy implements IProxy {
    @Override
    public void init(FMLInitializationEvent event) {
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        for (com.nymoo.afp.ModElementRegistry.ModElement element : AtomFusionProtocol.instance.elements.getElements()) {
            if (element instanceof AbstractPowerArmor) {
                AbstractPowerArmor apa = (AbstractPowerArmor) element;
                String armorType = apa.getArmorName();
                boolean hasJetpack = apa.hasJetpack();
                new PowerArmorModel(0, armorType, false);
                new PowerArmorModel(1, armorType, false);
                if (hasJetpack) {
                    new PowerArmorModel(1, armorType, true);
                }
                new PowerArmorModel(2, armorType, false);
                new PowerArmorModel(3, armorType, false);
            }
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
    }
}
