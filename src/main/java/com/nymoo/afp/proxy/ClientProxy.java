package com.nymoo.afp.proxy;

import com.nymoo.afp.common.render.model.armor.PowerArmorModel;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientProxy implements IProxy {
    @Override
    public void init(FMLInitializationEvent event) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void preInit(FMLPreInitializationEvent event) {
        String[] armorTypes = {"x03", "x02", "x01", "t60", "t51", "t45", "exo"};
        for (String armorType : armorTypes) {
            try {
                new PowerArmorModel(0, armorType, false);
                new PowerArmorModel(1, armorType, false);
                if (!armorType.equals("exo")) {
                    new PowerArmorModel(1, armorType, true);
                }
                new PowerArmorModel(2, armorType, false);
                new PowerArmorModel(3, armorType, false);
            } catch (Exception e) {
                System.err.println("Failed to preload model for " + armorType + ": " + e.getMessage());
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