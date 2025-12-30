package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
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
     * Порядок проверок важен: сначала проверяем наличие предмета, затем instanceof, затем каст.
     */
    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    private void onRenderModel(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo ci) {
        // Работает только для клиентского игрока
        if (!(entity instanceof AbstractClientPlayer)) {
            return;
        }

        AbstractClientPlayer player = (AbstractClientPlayer) entity;

        // Берём нагрудник
        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        // Защищаемся: если нет предмета — выходим
        if (chestplate == null || chestplate.isEmpty()) {
            return;
        }

        // Только предметы, реализующие IPowerArmor
        if (!(chestplate.getItem() instanceof IPowerArmor)) {
            return;
        }

        // Безопасный каст и получение типа брони
        IPowerArmor armorItem = (IPowerArmor) chestplate.getItem();
        String armorType = armorItem != null ? armorItem.getPowerArmorType() : null;

        RenderLivingBase<?> self = (RenderLivingBase<?>) (Object) this;

        // Если это поломанная броня, отключаем рендер второго слоя скина
        if (armorType != null && armorType.endsWith("_broken")) {
            if (self.getMainModel() instanceof ModelPlayer) {
                ModelPlayer model = (ModelPlayer) self.getMainModel();
                model.bipedHeadwear.showModel = false;
                model.bipedBodyWear.showModel = false;
                model.bipedLeftArmwear.showModel = false;
                model.bipedRightArmwear.showModel = false;
                model.bipedLeftLegwear.showModel = false;
                model.bipedRightLegwear.showModel = false;
            }
        }

        // Если это силовая броня и она не типа "exo" и не помечена как broken — отменяем рендер базовой модели
        if (armorType != null && !"exo".equals(armorType) && !armorType.endsWith("_broken")) {
            try {
                ci.cancel();
            } catch (Throwable t) {
                // В редком случае — защититься от неожиданного исключения,
                // чтобы не нарушить дальнейший рендеринг (логируем в консоль).
                t.printStackTrace();
            }
        }
        GlStateManager.scale(1.055F, 1.055F, 1.055F);
        GlStateManager.translate(0.0F, -0.075F, 0.0F);
    }
}