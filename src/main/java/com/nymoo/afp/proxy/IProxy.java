package com.nymoo.afp.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/**
 * Интерфейс прокси для разделения клиентской и серверной логики мода.
 * Определяет методы жизненного цикла инициализации, которые должны быть реализованы
 * как на клиенте, так и на сервере.
 */
public interface IProxy {

    /**
     * Выполняет предварительную инициализацию мода.
     * Вызывается на самой ранней стадии загрузки Forge.
     *
     * @param event Событие предварительной инициализации Forge
     */
    void preInit(FMLPreInitializationEvent event);

    /**
     * Выполняет основную инициализацию мода.
     * Вызывается после предварительной инициализации всех модов.
     *
     * @param event Событие инициализации Forge
     */
    void init(FMLInitializationEvent event);

    /**
     * Выполняет пост-инициализацию мода.
     * Вызывается после инициализации всех модов для финальной настройки.
     *
     * @param event Событие пост-инициализации Forge
     */
    void postInit(FMLPostInitializationEvent event);

    /**
     * Выполняет инициализацию серверной части мода.
     * Вызывается при запуске сервера для регистрации команд и сервер-специфичных функций.
     *
     * @param event Событие запуска сервера Forge
     */
    void serverStarting(FMLServerStartingEvent event);
}