package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Миксин для модификации поведения игрока при использовании силовой брони.
 * Изменяет механику движения и взаимодействия с окружающей средой.
 */
@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {

    /**
     * Модифицирует скорость движения игрока при ношении силовой брони.
     * Скорость уменьшается в зависимости от уровня энергии в ядре синтеза.
     *
     * @param cir Колбэк для возврата модифицированного значения скорости
     */
    @Inject(method = "getAIMoveSpeed", at = @At("RETURN"), cancellable = true)
    private void onGetAIMoveSpeed(CallbackInfoReturnable<Float> cir) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        // Проверка наличия силовой брони
        if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
            NBTTagCompound nbt = chestplateStack.getTagCompound();

            // Обработка случая с разряженным или отсутствующим ядром синтеза
            if (nbt == null || !nbt.hasKey("fusion_depletion") || nbt.getFloat("fusion_depletion") >= 288000) {
                float originalSpeed = cir.getReturnValue();
                float modifiedSpeed = originalSpeed * 0.1f;
                cir.setReturnValue(modifiedSpeed);
                return;
            }

            // Применение модификатора скорости из конфигурации
            float originalSpeed = cir.getReturnValue();
            float modifiedSpeed = originalSpeed * AFPConfig.powerArmorSpeedMultiplier;
            cir.setReturnValue(modifiedSpeed);
        }
    }

    /**
     * Отключает эффект толчка водой для игрока в силовой броне.
     *
     * @param cir Колбэк для возврата результата проверки толчка водой
     */
    @Inject(method = "isPushedByWater", at = @At("HEAD"), cancellable = true)
    public void onIsPushedByWater(CallbackInfoReturnable<Boolean> cir) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        // Отключение толчка водой при наличии силовой брони
        if (!chestplate.isEmpty() && chestplate.getItem() instanceof IPowerArmor) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "travel", at = @At("RETURN"))
    private void onTravel(float strafe, float vertical, float forward, CallbackInfo ci) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !(chestplate.getItem() instanceof IPowerArmor)) return;

        NBTTagCompound nbt = chestplate.getTagCompound();
        float multiplier;
        if (nbt == null || !nbt.hasKey("fusion_depletion") || nbt.getFloat("fusion_depletion") >= 288000) {
            multiplier = 0.1f;
        } else {
            multiplier = AFPConfig.powerArmorSpeedMultiplier;
        }

        // если в воде — уменьшить горизонтальную скорость после выполнения travel
        if (player.isInWater()) {
            player.motionX *= multiplier;
            player.motionZ *= multiplier;
        }
    }
}