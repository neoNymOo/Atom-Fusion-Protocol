package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.render.model.armor.PowerArmorModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {

    private static final Map<Item, ResourceLocation> ARMOR_TEXTURE_CACHE = new HashMap<>();

    @Inject(method = "renderRightArm", at = @At("HEAD"), cancellable = true)
    private void onRenderRightArm(AbstractClientPlayer player, CallbackInfo ci) {
        renderCustomArm(player, true, ci);
    }

    @Inject(method = "renderLeftArm", at = @At("HEAD"), cancellable = true)
    private void onRenderLeftArm(AbstractClientPlayer player, CallbackInfo ci) {
        renderCustomArm(player, false, ci);
    }

    private void renderCustomArm(AbstractClientPlayer player, boolean isRightArm, CallbackInfo ci) {
        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplate.isEmpty()) return;
        Item item = chestplate.getItem();
        if (!(item instanceof IPowerArmor)) return;

        ModelBiped armorModel = item.getArmorModel(player, chestplate, EntityEquipmentSlot.CHEST, null);
        if (!(armorModel instanceof PowerArmorModel)) return;
        PowerArmorModel model = (PowerArmorModel) armorModel;

        ResourceLocation texture = ARMOR_TEXTURE_CACHE.get(item);
        if (texture == null) {
            String regPath = item.getRegistryName().getPath();
            String armorType = regPath.replace("_chestplate", "");
            texture = new ResourceLocation("afp", "textures/armor/" + armorType + "/" + armorType + ".png");
            ARMOR_TEXTURE_CACHE.put(item, texture);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F);

        GlStateManager.enableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        if (isRightArm) {
            model.rightArm.render(0.0625F);
        } else {
            model.leftArm.render(0.0625F);
        }
        GlStateManager.disableBlend();
        ci.cancel();
    }
}