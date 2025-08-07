package com.nymoo.afp.common.mixins.impl;

import com.nymoo.afp.common.utils.PowerArmorUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemArmor.class)
public abstract class MixinItemArmor {

    @Inject(method = "onItemRightClick", at = @At("HEAD"), cancellable = true)
    public void onRightClick(World world, EntityPlayer player, EnumHand hand, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        ItemStack stack = player.getHeldItem(hand);
        if (PowerArmorUtil.isPowerArmor(stack)) {
            cir.setReturnValue(new ActionResult<>(EnumActionResult.FAIL, stack));
        }
    }
}