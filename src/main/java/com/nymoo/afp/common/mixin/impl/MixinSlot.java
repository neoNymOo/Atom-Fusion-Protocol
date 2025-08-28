package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
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

    @Shadow
    public IInventory inventory;

    @Shadow
    public abstract ItemStack getStack();

    @Inject(method = "canTakeStack", at = @At("HEAD"), cancellable = true)
    private void onCanTakeStack(CallbackInfoReturnable<Boolean> cir) {
        if (AFPConfig.canPlayerUnequipPowerArmor) return;

        if (!(this.inventory instanceof InventoryPlayer)) return;
        InventoryPlayer inv = (InventoryPlayer) this.inventory;
        EntityPlayer player = inv.player;

        ItemStack stack = this.getStack();
        if (!stack.isEmpty() && stack.getItem() instanceof IPowerArmor) {
            for (EntityEquipmentSlot equipSlot : new EntityEquipmentSlot[]{
                    EntityEquipmentSlot.HEAD,
                    EntityEquipmentSlot.CHEST,
                    EntityEquipmentSlot.LEGS,
                    EntityEquipmentSlot.FEET}) {
                if (player.getItemStackFromSlot(equipSlot) == stack) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
    }
}