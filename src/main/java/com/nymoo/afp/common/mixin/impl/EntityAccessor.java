package com.nymoo.afp.common.mixin.impl;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Миксин для доступа к приватным полям класса Entity.
 * Предоставляет геттеры для внутренних полей, связанных с обработкой шагов и движения.
 */
@Mixin(Entity.class)
public interface EntityAccessor {
    /**
     * Получает расстояние до следующего шага сущности.
     * Используется для расчёта момента воспроизведения звуков шагов.
     *
     * @return Расстояние до следующего шага
     */
    @Accessor("nextStepDistance")
    int getNextStepDistance();

    /**
     * Получает модифицированное пройденное расстояние для расчета шагов.
     * Учитывает модификации, связанные с различными типами движения.
     *
     * @return Модифицированное пройденное расстояние
     */
    @Accessor("distanceWalkedOnStepModified")
    float getDistanceWalkedOnStepModified();
}