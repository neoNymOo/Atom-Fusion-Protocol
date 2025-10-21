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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Миксин для изменения скорости движения игрока.
 * Влияет на скорость передвижения при ношении силовой брони в зависимости от уровня энергии.
 */
@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {

    /**
     * Модифицирует скорость движения игрока при ношении силовой брони.
     * Значительно замедляет движение при разряженном ядре синтеза и умеренно замедляет при наличии энергии.
     *
     * @param cir Колбэк для возврата модифицированной скорости движения
     */
    @Inject(method = "getAIMoveSpeed", at = @At("RETURN"), cancellable = true)
    private void onGetAIMoveSpeed(CallbackInfoReturnable<Float> cir) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
            NBTTagCompound nbt = chestplateStack.getTagCompound();
            if (nbt == null || !nbt.hasKey("fusion_depletion") || nbt.getFloat("fusion_depletion") >= 288000) {
                float originalSpeed = cir.getReturnValue();
                float modifiedSpeed = originalSpeed * 0.1f;
                cir.setReturnValue(modifiedSpeed);
                return;
            }

            float originalSpeed = cir.getReturnValue();
            float modifiedSpeed = originalSpeed * AFPConfig.powerArmorSpeedMultiplier;
            cir.setReturnValue(modifiedSpeed);
        }
    }
}