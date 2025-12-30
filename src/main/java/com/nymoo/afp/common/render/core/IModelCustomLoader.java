package com.nymoo.afp.common.render.core;

import net.minecraft.util.ResourceLocation;

/**
 * Интерфейс загрузчика моделей.
 * Отвечает за создание экземпляров моделей из файлов ресурсов.
 */
public interface IModelCustomLoader {
    String getType();

    String[] getSuffixes();

    IModelCustom loadInstance(ResourceLocation resource) throws ModelFormatException;
}