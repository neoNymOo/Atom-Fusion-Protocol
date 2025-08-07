package com.nymoo.afp.common.mixins;

import com.nymoo.afp.common.utils.PowerArmorUtil;
import net.minecraft.entity.player.EntityPlayer;
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

    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true)
    private void onIsItemValid(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        System.out.println("AAAAAAAAAAAAAAAAAAAAAA");
        if (this.inventory instanceof InventoryPlayer && this.slotIndex >= 36 && this.slotIndex <= 39) {
            if (PowerArmorUtil.isWearingPowerArmor(stack)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "canTakeStack", at = @At("HEAD"), cancellable = true)
    private void onCanTakeStack(EntityPlayer player, CallbackInfoReturnable<Boolean> cir) {
        System.out.println("AAAAAAAAAAAAAAAAAAAAAA");
        if (this.inventory instanceof InventoryPlayer && this.slotIndex >= 36 && this.slotIndex <= 39) {
            ItemStack stack = this.getStack();
            if (!stack.isEmpty() && PowerArmorUtil.isWearingPowerArmor(stack)) {
                cir.setReturnValue(false);
            }
        }
    }
}