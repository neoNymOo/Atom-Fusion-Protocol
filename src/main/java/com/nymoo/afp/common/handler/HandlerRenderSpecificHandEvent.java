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
 * Обработчик рендеринга руки от первого лица.
 *
 * Назначение:
 * - Подменяет стандартную руку на руку экзоскелета (силовой брони), когда на игроке надет нагрудник
 *   и основная рука пуста.
 * - Подбирает корректную текстуру по предмету нагрудника и применяет трансформации для позы взмаха/доставания.
 *
 * Детали:
 * - Кэширует пути текстур в {@link #textureCache}, чтобы не создавать {@link ResourceLocation} каждый кадр.
 * - Рендер выполняется только для основной руки, при пустом предмете в ней.
 */
@Mod.EventBusSubscriber
public class HandlerRenderSpecificHandEvent {

    // Кэш соответствия предмета нагрудника и пути к его текстуре
    private static final Map<Item, ResourceLocation> textureCache = new HashMap<>();

    /**
     * Перехватывает {@link RenderSpecificHandEvent} и рисует руку экзоскелета вместо стандартной.
     *
     * Вход:
     * - event: содержит руку, прогресс взмаха/доставания и текущий ItemStack.
     *
     * Логика:
     * 1) Обрабатываем только MAIN_HAND и только при пустой руке.
     * 2) Проверяем, что на груди надета силовая броня (IPowerArmor).
     * 3) Применяем матрицы смещения/поворота под анимацию взмаха.
     * 4) Выбираем модель брони ({@link PowerArmorModel}) и биндим её текстуру.
     * 5) Рендерим соответствующую руку (левую/правую).
     * 6) Отменяем стандартный рендер, чтобы не было дубля.
     *
     * Результат:
     * - Рука экзоскелета отображается в кадре, стандартная рука скрыта.
     *
     * @param event событие рендеринга руки
     */
    @SubscribeEvent
    public static void onRenderSpecificHandEvent(RenderSpecificHandEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }

        // Обрабатываем только основную руку
        if (event.getHand() != EnumHand.MAIN_HAND) {
            return;
        }

        // Прерываем, если в руке есть предмет
        ItemStack heldStack = event.getItemStack();
        if (!heldStack.isEmpty()) {
            return;
        }

        // Требуется нагрудник силовой брони
        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplateStack.isEmpty() || !(chestplateStack.getItem() instanceof IPowerArmor)) {
            return;
        }

        // Вычисляем сторону ведущей руки и параметры анимации
        EnumHandSide primaryHand = player.getPrimaryHand();
        boolean isRightArm = primaryHand == EnumHandSide.RIGHT;
        float swingProgress = event.getSwingProgress();
        float equipProgress = event.getEquipProgress();

        // Трансформации под анимацию взмаха/доставания
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

        // Дополнительные смещения для корректного положения руки в кадре
        GlStateManager.translate(handSideMultiplier * -1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(handSideMultiplier * 120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(handSideMultiplier * -135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(handSideMultiplier * 5.6F, 0.0F, 0.0F);

        // Получаем модель брони и убеждаемся, что это PowerArmorModel
        ModelBiped bipedModel = chestplateStack.getItem().getArmorModel(player, chestplateStack, EntityEquipmentSlot.CHEST, null);
        if (bipedModel == null || !(bipedModel instanceof PowerArmorModel)) {
            return;
        }
        PowerArmorModel armorModel = (PowerArmorModel) bipedModel;

        // Разрешаем текстуру нагрудника через кэш
        Item armorItem = chestplateStack.getItem();
        ResourceLocation texture = textureCache.computeIfAbsent(armorItem, item -> {
            String registryPath = item.getRegistryName().getPath();
            String armorType = registryPath.substring(0, registryPath.length() - "_chestplate".length());
            return new ResourceLocation("afp", "textures/armor/" + armorType + "/" + armorType + ".png");
        });
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        // GL-состояние: сглаживание + прозрачность
        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        // Настройка позы модели под текущее состояние игрока
        armorModel.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);

        // Рендер нужной руки (сброс смещений для чистого кадра)
        if (isRightArm) {
            armorModel.rightArm.offsetY = 0.0F;
            armorModel.rightArm.rotateAngleX = 0.0F;
            armorModel.rightArm.render(0.0625F);
        } else {
            armorModel.leftArm.offsetY = 0.0F;
            armorModel.leftArm.rotateAngleX = 0.0F;
            armorModel.leftArm.render(0.0625F);
        }

        // Возврат GL-состояния
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull();

        // Блокируем стандартный рендер руки
        event.setCanceled(true);
    }
}
