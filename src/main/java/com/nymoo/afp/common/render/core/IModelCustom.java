package com.nymoo.afp.common.render.core;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Интерфейс для кастомных моделей.
 * Определяет методы для рендеринга всей модели или её отдельных частей.
 */
@SideOnly(Side.CLIENT)
public interface IModelCustom {
    String getType();

    void renderAll();

    void renderOnly(String... groupNames);

    void renderPart(String partName);

    void renderAllExcept(String... excludedGroupNames);

    void tessellateAll(Tessellator tes);

    void tessellatePart(Tessellator tes, String name);

    void tessellateOnly(Tessellator tes, String... names);

    void tessellateAllExcept(Tessellator tes, String... excluded);
}