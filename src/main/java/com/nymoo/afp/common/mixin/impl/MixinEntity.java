package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.handler.HandlerClientTickEvent;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Миксин для изменения поведения сущностей.
 * Модифицирует поворот игрока и предотвращает столкновения при ношении силовой брони.
 */
@Mixin(Entity.class)
public abstract class MixinEntity {

    /**
     * Модифицирует скорость поворота по горизонтали.
     * Замедляет поворот при взаимодействии с бронёй или разряженном ядре синтеза.
     *
     * @param yaw Исходная скорость поворота по горизонтали
     * @return Модифицированная скорость поворота по горизонтали
     */
    @ModifyVariable(method = "turn", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float onTurnModifyYaw(float yaw) {
        if (!((Object) this instanceof EntityPlayer)) {
            return yaw;
        }
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (player.world.isRemote && (HandlerClientTickEvent.currentMode == 2 || HandlerClientTickEvent.currentMode == 3)) {
            return yaw * 0.05f;
        }
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
     * Модифицирует скорость поворота по вертикали.
     * Замедляет поворот при взаимодействии с бронёй или разряженном ядре синтеза.
     *
     * @param pitch Исходная скорость поворота по вертикали
     * @return Модифицированная скорость поворота по вертикали
     */
    @ModifyVariable(method = "turn", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float onTurnModifyPitch(float pitch) {
        if (!((Object) this instanceof EntityPlayer)) {
            return pitch;
        }
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (player.world.isRemote && (HandlerClientTickEvent.currentMode == 2 || HandlerClientTickEvent.currentMode == 3)) {
            return pitch * 0.05f;
        }
        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
            NBTTagCompound nbt = chestplateStack.getTagCompound();
            if (nbt == null || !nbt.hasKey("fusion_depletion") || nbt.getFloat("fusion_depletion") >= 288000) {
                return pitch * 0.05f;
            }
        }
        return pitch;
    }

    /**
     * Предотвращает отталкивание игрока при столкновении с сущностями.
     * Активно при ношении силовой брони.
     *
     * @param entityIn Сущность, столкнувшаяся с игроком
     * @param ci       Колбэк для отмены события
     */
    @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
    private void preventCollisionPush(Entity entityIn, CallbackInfo ci) {
        if (!((Object) this instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) (Object) this;
        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
            ci.cancel();
        }
    }
}