package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Миксин для изменения механики прыжка живых существ.
 * Ограничивает возможность прыжка для игроков в силовой броне с разряженным ядром синтеза.
 */
@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

    /**
     * Внедряется в начало метода jump для проверки возможности прыжка.
     * Отменяет прыжок, если игрок носит силовую броню с разряженным ядром синтеза.
     *
     * @param ci Колбэк для отмены выполнения прыжка
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
}