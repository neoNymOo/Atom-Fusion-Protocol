package com.nymoo.afp.common.render.core;

import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.Map;

/**
 * Центральный загрузчик для продвинутых моделей.
 * Управляет регистрацией обработчиков и загрузкой моделей различных форматов (например, OBJ).
 */
@SideOnly(Side.CLIENT)
public class AdvancedModelLoader {
    /**
     * Реестр загрузчиков моделей, привязанный к расширениям файлов
     */
    private static final Map<String, IModelCustomLoader> instances = Maps.newHashMap();

    static {
        registerModelHandler(new ObjModelLoader());
    }

    /**
     * Регистрирует новый обработчик моделей.
     *
     * @param modelHandler Экземпляр загрузчика, реализующий интерфейс IModelCustomLoader
     */
    public static void registerModelHandler(IModelCustomLoader modelHandler) {
        for (String suffix : modelHandler.getSuffixes()) {
            instances.put(suffix, modelHandler);
        }
    }

    /**
     * Загружает модель по указанному ресурсу.
     * Определяет тип загрузчика по расширению файла.
     *
     * @param resource Путь к файлу модели
     * @return Экземпляр загруженной модели
     * @throws IllegalArgumentException Если имя ресурса некорректно или формат не поддерживается
     * @throws ModelFormatException     Если произошла ошибка при парсинге модели
     */
    public static IModelCustom loadModel(ResourceLocation resource) throws IllegalArgumentException, ModelFormatException {
        String name = resource.getPath();
        int i = name.lastIndexOf('.');
        if (i == -1) {
            throw new IllegalArgumentException("The resource name " + resource + " is not valid");
        }
        String suffix = name.substring(i + 1);
        IModelCustomLoader loader = instances.get(suffix);
        if (loader == null) {
            throw new IllegalArgumentException("The resource name " + resource + " is not supported");
        }

        return loader.loadInstance(resource);
    }

    /**
     * Возвращает список поддерживаемых расширений файлов моделей.
     *
     * @return Коллекция строк с суффиксами файлов
     */
    public static Collection<String> getSupportedSuffixes() {
        return instances.keySet();
    }
}