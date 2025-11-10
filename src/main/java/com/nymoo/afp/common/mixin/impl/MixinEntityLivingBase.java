package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Миксин для модификации поведения EntityLivingBase при использовании силовой брони.
 * Реализует ограничения прыжков и плавный отскок от дна в зависимости от заряда ядра.
 * Все жёстко прописанные значения заменены ссылками на AFPConfig.
 */
@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

    /**
     * Таймер для управления плавным отскоком от дна.
     * Уменьшается каждый тик и определяет продолжительность эффекта.
     */
    @Unique
    private int afpSmoothBounceTimer = 0;

    /**
     * Обрабатывает прыжок игрока в силовой броне.
     * Если ядро синтеза разряжено (fusion_depletion >= maxDepletion), прыжок отменяется.
     *
     * @param ci Колбэк для отмены стандартного поведения
     */
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) (Object) this;
            ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
                NBTTagCompound nbt = chestplateStack.getTagCompound();
                // Отменяем прыжок, если ядро разряжено или отсутствует
                if (nbt == null || !nbt.hasKey("fusion_depletion") || nbt.getFloat("fusion_depletion") >= AFPConfig.maxDepletion) {
                    ci.cancel();
                }
            }
        }
    }

    /**
     * Обрабатывает подъем со дна в воде/лаве для игрока в силовой броне.
     * Если ядро разряжено, отменяет подъем. Иначе инициирует плавный отскок
     * с параметрами из конфигурации.
     *
     * @param ci Колбэк для отмены стандартного поведения
     */
    @Inject(method = "handleJumpWater", at = @At("HEAD"), cancellable = true)
    private void onHandleJumpWater(CallbackInfo ci) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;

        if (self instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) self;
            ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
                NBTTagCompound nbt = chestplateStack.getTagCompound();
                boolean discharged = nbt == null || !nbt.hasKey("fusion_depletion") || nbt.getFloat("fusion_depletion") >= AFPConfig.maxDepletion;

                if (discharged) {
                    // Если разряжено, отменяем прыжок в воде
                    ci.cancel();
                    return;
                }

                // Запускаем плавный отскок: минимальная вертикальная скорость и таймер
                if (self.onGround) {
                    // устанавливаем вертикальную скорость не менее заданной конфигурацией
                    self.motionY = Math.max(self.motionY, AFPConfig.powerArmorWaterJumpMotion);
                    self.isAirBorne = true;
                    this.afpSmoothBounceTimer = AFPConfig.powerArmorBounceTimer;
                }

                // Отменяем дальнейшую обработку, чтобы наша логика применилась
                ci.cancel();
            }
        }
    }

    /**
     * Обрабатывает движение сущности и применяет плавный отскок при активном таймере.
     * Добавляет вертикальную скорость и сглаживает горизонтальное движение в соответствии с конфигом.
     *
     * @param strafe   Боковое движение
     * @param vertical Вертикальное движение
     * @param forward  Переднее движение
     * @param ci       Колбэк для управления вызовом
     */
    @Inject(method = "travel", at = @At("HEAD"))
    private void onTravel(float strafe, float vertical, float forward, CallbackInfo ci) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;

        if (this.afpSmoothBounceTimer > 0) {
            // Ускоряемся к целевой скорости
            double add = AFPConfig.powerArmorBounceAcceleration;
            double desired = Math.min(AFPConfig.powerArmorBounceTarget, self.motionY + add);
            if (desired > self.motionY) {
                self.motionY = desired;
                self.isAirBorne = true;
            }
            // Применяем горизонтальное трение
            self.motionX *= AFPConfig.powerArmorBounceFriction;
            self.motionZ *= AFPConfig.powerArmorBounceFriction;
            this.afpSmoothBounceTimer--;
        }
    }
}