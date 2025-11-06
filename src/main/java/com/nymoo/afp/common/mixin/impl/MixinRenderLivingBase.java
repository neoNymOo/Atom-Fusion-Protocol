package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Миксин для модификации рендеринга живых сущностей.
 * Отключает рендер базовой модели игрока при ношении силовой брони,
 * чтобы избежать вылезания скина из-под брони.
 */
@Mixin(RenderLivingBase.class)
public abstract class MixinRenderLivingBase {

    /**
     * Отключает рендер базовой модели игрока, если на нём надета силовая броня.
     * Это предотвратит отображение скина под бронёй, но сохранит рендер слоёв (включая саму броню).
     *
     * @param entity    Сущность для рендеринга
     * @param limbSwing Параметры анимации
     * @param ci        Колбэк для отмены рендера
     */
    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    private void onRenderModel(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo ci) {
        if (entity instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer) entity;
            ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (!chestplate.isEmpty() && chestplate.getItem() instanceof IPowerArmor) {
                ci.cancel();
            }
        }
    }
}