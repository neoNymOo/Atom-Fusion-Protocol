package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @ModifyVariable(method = "turn", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float onTurnModifyYaw(float yaw) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
            NBTTagCompound nbt = chestplateStack.getTagCompound();
            if (nbt == null || !nbt.hasKey("fusion_depletion") || nbt.getFloat("fusion_depletion") >= 288000) {
                return yaw * 0.05f;
            }
        }
        return yaw;
    }

    @ModifyVariable(method = "turn", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float onTurnModifyPitch(float pitch) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
            NBTTagCompound nbt = chestplateStack.getTagCompound();
            if (nbt == null || !nbt.hasKey("fusion_depletion") || nbt.getFloat("fusion_depletion") >= 288000) {
                return pitch * 0.05f;
            }
        }
        return pitch;
    }
}