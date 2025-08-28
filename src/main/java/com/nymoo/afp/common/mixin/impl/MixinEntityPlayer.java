package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {
    @Inject(method = "getAIMoveSpeed", at = @At("RETURN"), cancellable = true)
    private void onGetAIMoveSpeed(CallbackInfoReturnable<Float> cir) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
            float originalSpeed = cir.getReturnValue();
            float modifiedSpeed = originalSpeed * AFPConfig.powerArmorSpeedMultiplier;
            cir.setReturnValue(modifiedSpeed);
        }
    }
}