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

/**
 * Миксин для изменения механики поворота сущностей.
 * Влияет на скорость поворота игрока при ношении силовой брони с разряженным ядром синтеза.
 */
@Mixin(Entity.class)
public abstract class MixinEntity {

    /**
     * Модифицирует скорость поворота по горизонтали для игроков в силовой броне.
     * Замедляет поворот при отсутствии энергии в ядре синтеза.
     *
     * @param yaw Исходная скорость поворота по горизонтали
     * @return Модифицированная скорость поворота по горизонтали
     */
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

    /**
     * Модифицирует скорость поворота по вертикали для игроков в силовой броне.
     * Замедляет поворот при отсутствии энергии в ядре синтеза.
     *
     * @param pitch Исходная скорость поворота по вертикали
     * @return Модифицированная скорость поворота по вертикали
     */
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