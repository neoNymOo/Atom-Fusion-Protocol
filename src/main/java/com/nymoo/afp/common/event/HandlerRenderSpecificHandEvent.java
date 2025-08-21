package com.nymoo.afp.common.event;

import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.render.model.armor.PowerArmorModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class HandlerRenderSpecificHandEvent {
    private static final Map<Item, ResourceLocation> textureCache = new HashMap<>();
    @SubscribeEvent
    public static void onRenderSpecificHandEvent(RenderSpecificHandEvent e) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;

        if (e.getHand() != EnumHand.MAIN_HAND) return;

        ItemStack heldStack = e.getItemStack();
        if (!heldStack.isEmpty()) return;

        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplateStack.isEmpty() || !(chestplateStack.getItem() instanceof IPowerArmor)) return;

        EnumHandSide primaryHand = player.getPrimaryHand();
        boolean isRightArm = primaryHand == EnumHandSide.RIGHT;

        float swingProgress = e.getSwingProgress();
        float equipProgress = e.getEquipProgress();

        float f = isRightArm ? 1.0F : -1.0F;
        float f1 = MathHelper.sqrt(swingProgress);
        float f2 = -0.3F * MathHelper.sin(f1 * (float) Math.PI);
        float f3 = 0.4F * MathHelper.sin(f1 * (float) Math.PI * 2F);
        float f4 = -0.4F * MathHelper.sin(swingProgress * (float) Math.PI);
        GlStateManager.translate(f * (f2 + 0.64000005F), f3 + -0.6F + equipProgress * -0.6F, f4 + -0.71999997F);
        GlStateManager.rotate(f * 45.0F, 0.0F, 1.0F, 0.0F);
        float f5 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f6 = MathHelper.sin(f1 * (float) Math.PI);
        GlStateManager.rotate(f * f6 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f * f5 * -20.0F, 0.0F, 0.0F, 1.0F);

        GlStateManager.translate(f * -1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(f * 120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f * -135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(f * 5.6F, 0.0F, 0.0F);

        ModelBiped bipedModel = chestplateStack.getItem().getArmorModel(player, chestplateStack, EntityEquipmentSlot.CHEST, null);
        if (bipedModel == null) return;
        if (!(bipedModel instanceof PowerArmorModel)) return;
        PowerArmorModel model = (PowerArmorModel) bipedModel;

        Item armorItem = chestplateStack.getItem();
        ResourceLocation texture = textureCache.get(armorItem);
        if (texture == null) {
            String regPath = armorItem.getRegistryName().getPath();
            String armorType = regPath.substring(0, regPath.length() - "_chestplate".length());
            texture = new ResourceLocation("afp", "textures/armor/" + armorType + "/" + armorType + ".png");
            textureCache.put(armorItem, texture);
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        model.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);

        if (isRightArm) {
            model.rightArm.offsetY = 0.0F;
            model.rightArm.rotateAngleX = 0.0F;
            model.rightArm.render(0.0625F);
        } else {
            model.leftArm.offsetY = 0.0F;
            model.leftArm.rotateAngleX = 0.0F;
            model.leftArm.render(0.0625F);
        }

        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull();

        e.setCanceled(true);
    }
}