package com.nymoo.afp.common.handler;

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

/**
 * Обработчик рендеринга конкретной руки для отображения рук силовой брони.
 * Заменяет стандартную модель руки на модель руки из силовой брони при ее ношении.
 */
@Mod.EventBusSubscriber
public class HandlerRenderSpecificHandEvent {
    /**
     * Кэш текстур для оптимизации загрузки ресурсов
     */
    private static final Map<Item, ResourceLocation> textureCache = new HashMap<>();

    /**
     * Обрабатывает событие рендеринга конкретной руки.
     * Заменяет рендер пустой руки на рендер руки силовой брони при соответствующих условиях.
     *
     * @param event Событие рендеринга конкретной руки
     */
    @SubscribeEvent
    public static void onRenderSpecificHandEvent(RenderSpecificHandEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;

        if (event.getHand() != EnumHand.MAIN_HAND) return;

        ItemStack heldStack = event.getItemStack();
        if (!heldStack.isEmpty()) return;

        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplateStack.isEmpty() || !(chestplateStack.getItem() instanceof IPowerArmor)) return;

        EnumHandSide primaryHand = player.getPrimaryHand();
        boolean isRightArm = primaryHand == EnumHandSide.RIGHT;

        float swingProgress = event.getSwingProgress();
        float equipProgress = event.getEquipProgress();

        // Применение трансформаций для позиционирования руки
        float handSideMultiplier = isRightArm ? 1.0F : -1.0F;
        float swingSqrt = MathHelper.sqrt(swingProgress);
        float swingX = -0.3F * MathHelper.sin(swingSqrt * (float) Math.PI);
        float swingY = 0.4F * MathHelper.sin(swingSqrt * (float) Math.PI * 2F);
        float swingZ = -0.4F * MathHelper.sin(swingProgress * (float) Math.PI);
        GlStateManager.translate(handSideMultiplier * (swingX + 0.64000005F), swingY + -0.6F + equipProgress * -0.6F, swingZ + -0.71999997F);
        GlStateManager.rotate(handSideMultiplier * 45.0F, 0.0F, 1.0F, 0.0F);
        float swingSin = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float swingSqrtSin = MathHelper.sin(swingSqrt * (float) Math.PI);
        GlStateManager.rotate(handSideMultiplier * swingSqrtSin * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(handSideMultiplier * swingSin * -20.0F, 0.0F, 0.0F, 1.0F);

        // Дополнительные трансформации для точного позиционирования
        GlStateManager.translate(handSideMultiplier * -1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(handSideMultiplier * 120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(handSideMultiplier * -135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(handSideMultiplier * 5.6F, 0.0F, 0.0F);

        // Получение модели брони
        ModelBiped bipedModel = chestplateStack.getItem().getArmorModel(player, chestplateStack, EntityEquipmentSlot.CHEST, null);
        if (bipedModel == null) return;
        if (!(bipedModel instanceof PowerArmorModel)) return;
        PowerArmorModel armorModel = (PowerArmorModel) bipedModel;

        // Загрузка текстуры брони
        Item armorItem = chestplateStack.getItem();
        ResourceLocation texture = textureCache.get(armorItem);
        if (texture == null) {
            String registryPath = armorItem.getRegistryName().getPath();
            String armorType = registryPath.substring(0, registryPath.length() - "_chestplate".length());
            texture = new ResourceLocation("afp", "textures/armor/" + armorType + "/" + armorType + ".png");
            textureCache.put(armorItem, texture);
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        // Настройка состояния OpenGL для рендеринга
        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        // Установка углов поворота модели
        armorModel.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);

        // Рендер соответствующей руки
        if (isRightArm) {
            armorModel.rightArm.offsetY = 0.0F;
            armorModel.rightArm.rotateAngleX = 0.0F;
            armorModel.rightArm.render(0.0625F);
        } else {
            armorModel.leftArm.offsetY = 0.0F;
            armorModel.leftArm.rotateAngleX = 0.0F;
            armorModel.leftArm.render(0.0625F);
        }

        // Восстановление состояния OpenGL
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull();

        event.setCanceled(true);
    }
}