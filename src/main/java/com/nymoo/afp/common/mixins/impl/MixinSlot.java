package com.nymoo.afp.common.mixins.impl;

import com.nymoo.afp.common.utils.PowerArmorUtil;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class MixinSlot {

    @Shadow public IInventory inventory;
    @Shadow public int slotIndex;
    @Shadow public abstract ItemStack getStack();

    @Inject(method = "canTakeStack", at = @At("HEAD"), cancellable = true)
    private void onCanTakeStack(CallbackInfoReturnable<Boolean> cir) {
        if (!(this.inventory instanceof InventoryPlayer)) return;
        if (this.slotIndex < 36 || this.slotIndex > 39) return;

        if (!this.getStack().isEmpty() && PowerArmorUtil.isPowerArmor(this.getStack())) {
            cir.setReturnValue(false);
        }
    }
}