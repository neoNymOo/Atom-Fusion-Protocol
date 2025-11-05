package com.nymoo.afp.common.mixin.impl;

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
 * Реализует плавный отскок от дна и ограничения прыжков при разряженном ядре синтеза.
 */
@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

    /**
     * Целевая вертикальная скорость для плавного отскока.
     * Определяет максимальную скорость подъема при отталкивании от дна.
     */
    @Unique
    private static final double AFP_SMOOTH_BOUNCE_TARGET = 0.45D;
    /**
     * Таймер для управления плавным отскоком от дна.
     * Уменьшается каждый тик и определяет продолжительность эффекта.
     */
    @Unique
    private int afpSmoothBounceTimer = 0;

    /**
     * Обрабатывает прыжок игрока в силовой броне.
     * Отменяет прыжок если ядро синтеза разряжено.
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
                if (nbt == null || !nbt.hasKey("fusion_depletion") || nbt.getFloat("fusion_depletion") >= 288000) {
                    ci.cancel();
                }
            }
        }
    }

    /**
     * Обрабатывает подъем со дна в воде/лаве для игрока в силовой броне.
     * Реализует плавный отскок при заряженном ядре и блокирует подъем при разряженном.
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
                boolean discharged = nbt == null || !nbt.hasKey("fusion_depletion") || nbt.getFloat("fusion_depletion") >= 288000;

                if (discharged) {
                    ci.cancel();
                    return;
                }

                if (self.onGround) {
                    self.motionY = Math.max(self.motionY, 0.12D);
                    self.isAirBorne = true;
                    this.afpSmoothBounceTimer = 6;
                }

                ci.cancel();
            }
        }
    }

    /**
     * Обрабатывает движение сущности и применяет плавный отскок при активном таймере.
     * Добавляет вертикальную скорость и сглаживает горизонтальное движение.
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
            double add = 0.055D;
            double desired = Math.min(AFP_SMOOTH_BOUNCE_TARGET, self.motionY + add);

            if (desired > self.motionY) {
                self.motionY = desired;
                self.isAirBorne = true;
            }

            self.motionX *= 0.98;
            self.motionZ *= 0.98;
            this.afpSmoothBounceTimer--;
        }
    }
}