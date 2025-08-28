package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockDispenser.class)
public abstract class MixinBlockDispenser {
    @Inject(method = "getBehavior", at = @At("HEAD"), cancellable = true)
    private void onGetBehavior(ItemStack stack, CallbackInfoReturnable<IBehaviorDispenseItem> cir) {
        if (AFPConfig.canDispenserEquipPowerArmor) return;

        if (stack.getItem() instanceof IPowerArmor) {
            cir.setReturnValue(IBehaviorDispenseItem.DEFAULT_BEHAVIOR);
        }
    }
}