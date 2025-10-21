package com.nymoo.afp.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/**
 * Серверный прокси для мода Atom Fusion Protocol.
 * Реализует сервер-специфичные методы инициализации без загрузки клиентских ресурсов.
 */
public class ServerProxy implements IProxy {

    /**
     * Выполняет предварительную инициализацию на сервере.
     * Серверная версия не загружает графические ресурсы.
     *
     * @param event Событие предварительной инициализации Forge
     */
    @Override
    public void preInit(FMLPreInitializationEvent event) {
    }

    /**
     * Выполняет основную инициализацию на сервере.
     * Регистрирует сервер-специфичные обработчики и системы.
     *
     * @param event Событие инициализации Forge
     */
    @Override
    public void init(FMLInitializationEvent event) {
    }

    /**
     * Выполняет пост-инициализацию на сервере.
     * Выполняет финальную настройку серверных компонентов после загрузки всех модов.
     *
     * @param event Событие пост-инициализации Forge
     */
    @Override
    public void postInit(FMLPostInitializationEvent event) {
    }

    /**
     * Обрабатывает запуск сервера.
     * Регистрирует серверные команды и инициализирует сервер-специфичные функции.
     *
     * @param event Событие запуска сервера Forge
     */
    @Override
    public void serverStarting(FMLServerStartingEvent event) {
    }
}