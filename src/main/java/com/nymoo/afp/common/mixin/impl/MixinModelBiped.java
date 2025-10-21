package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.entity.EntityExoskeleton.Exoskeleton;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Миксин для изменения модели бипеда.
 * Управляет анимацией рук экзоскелета в зависимости от конфигурации.
 */
@Mixin(ModelBiped.class)
public abstract class MixinModelBiped {

    /**
     * Модифицирует углы поворота модели после установки основных параметров.
     * Фиксирует положение рук экзоскелета, если в конфигурации отключено движение рук.
     *
     * @param limbSwing       Время взмаха конечностей для анимации
     * @param limbSwingAmount Интенсивность взмаха конечностей
     * @param ageInTicks      Время существования сущности в тиках
     * @param netHeadYaw      Угол поворота головы по горизонтали
     * @param headPitch       Угол поворота головы по вертикали
     * @param scaleFactor     Масштабный фактор модели
     * @param entityIn        Сущность, для которой устанавливаются углы
     * @param ci              Колбэк для управления выполнением метода
     */
    @Inject(method = "setRotationAngles", at = @At("RETURN"))
    private void onSetRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo ci) {
        if (AFPConfig.canExoskeletonSwingArms) return;

        if (entityIn instanceof Exoskeleton) {
            ModelBiped self = (ModelBiped) (Object) this;

            if (self.bipedRightArm != null) {
                self.bipedRightArm.rotateAngleX = 0.0F;
                self.bipedRightArm.rotateAngleY = 0.0F;
                self.bipedRightArm.rotateAngleZ = 0.05F;
            }

            if (self.bipedLeftArm != null) {
                self.bipedLeftArm.rotateAngleX = 0.0F;
                self.bipedLeftArm.rotateAngleY = 0.0F;
                self.bipedLeftArm.rotateAngleZ = -0.05F;
            }
        }
    }
}