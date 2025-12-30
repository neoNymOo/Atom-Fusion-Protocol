package com.nymoo.afp.proxy;

import com.nymoo.afp.common.render.model.armor.PowerArmorModel;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Клиентский прокси для мода Atom Fusion Protocol.
 * Обрабатывает клиент-специфичную инициализацию, включая предзагрузку моделей брони.
 */
public class ClientProxy implements IProxy {

    /**
     * Выполняет основную инициализацию на клиенте.
     *
     * @param event Событие инициализации Forge
     */
    @Override
    public void init(FMLInitializationEvent event) {
    }

    /**
     * Выполняет предварительную инициализацию на клиенте.
     * Предзагружает модели силовой брони для всех типов и слотов.
     *
     * @param event Событие предварительной инициализации Forge
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void preInit(FMLPreInitializationEvent event) {
        String[] armorTypes = {"x03", "x02", "x01", "t60", "t51", "t45", "x03_broken", "x02_broken", "x01_broken", "t60_broken", "t51_broken", "t45_broken"};
        for (String armorType : armorTypes) {
            boolean isBroken = armorType.endsWith("_broken");
            try {
                // Предзагрузка моделей для всех слотов брони
                new PowerArmorModel(0, armorType, false, isBroken); // Шлем
                new PowerArmorModel(1, armorType, false, isBroken); // Нагрудник
                if (!isBroken && !armorType.equals("exo")) {
                    new PowerArmorModel(1, armorType, true, isBroken); // Нагрудник с реактивным ранцем (кроме exo)
                }
                new PowerArmorModel(2, armorType, false, isBroken); // Поножи
                new PowerArmorModel(3, armorType, false, isBroken); // Ботинки
            } catch (Exception exception) {
                System.err.println("Failed to preload model for " + armorType + ": " + exception.getMessage());
            }
        }
    }

    /**
     * Выполняет пост-инициализацию на клиенте.
     *
     * @param event Событие пост-инициализации Forge
     */
    @Override
    public void postInit(FMLPostInitializationEvent event) {
    }

    /**
     * Обрабатывает запуск сервера на клиенте.
     *
     * @param event Событие запуска сервера Forge
     */
    @Override
    public void serverStarting(FMLServerStartingEvent event) {
    }
}