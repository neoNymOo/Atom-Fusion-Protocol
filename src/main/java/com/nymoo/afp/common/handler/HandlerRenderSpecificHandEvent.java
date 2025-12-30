package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.item.ArmorExo;
import com.nymoo.afp.common.render.model.armor.PowerArmorModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * Обработчик рендеринга руки от первого лица.
 *
 * Назначение:
 * - Подменяет стандартную руку на руку экзоскелета (силовой брони) при наличии нагрудника и пустой основной руке.
 * - Биндит корректную текстуру и применяет трансформации для анимации взмаха/доставания.
 */
@Mod.EventBusSubscriber
public class HandlerRenderSpecificHandEvent {
    /**
     * Перехватывает рендер основной руки и рисует модель экзоскелета вместо стандартной.
     *
     * Вход:
     * - event: событие рендера конкретной руки (прогресс взмаха, предмет в руке и т.д.).
     *
     * Логика:
     * 1) Обрабатывает только MAIN_HAND и только при пустой руке игрока.
     * 2) Проверяет наличие нагрудника, реализующего IPowerArmor.
     * 3) Вычисляет параметры анимации и применяет трансформации OpenGL.
     * 4) Выбирает модель и текстуру (ванильный рендер при exo/сломанной броне или модель PowerArmor).
     * 5) Рендерит соответствующую руку и блокирует стандартный рендер.
     *
     * Результат:
     * - На экране отображается рука экзоскелета или ванильная рука+экзомодель в зависимости от типа брони.
     *
     * @param event событие рендеринга конкретной руки
     */
    @SubscribeEvent
    public static void onRenderSpecificHandEvent(RenderSpecificHandEvent event) {
        // Получаем клиента и игрока
        AbstractClientPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }

        // Обрабатываем только основную руку
        if (event.getHand() != EnumHand.MAIN_HAND) {
            return;
        }

        // Предмет в руке — рендер экзорычи невозможен
        // heldStack: предмет в основной руке игрока
        ItemStack heldStack = event.getItemStack();
        if (!heldStack.isEmpty()) {
            return;
        }

        // chestplateStack: предмет в слоте нагрудника (должен быть силовой бронёй)
        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplateStack.isEmpty() || !(chestplateStack.getItem() instanceof IPowerArmor)) {
            return;
        }

        // Определяем сторону основной руки и параметры анимации
        // primaryHand: ведущая рука игрока
        EnumHandSide primaryHand = player.getPrimaryHand();
        // isRightArm: флаг правой руки
        boolean isRightArm = primaryHand == EnumHandSide.RIGHT;
        // swingProgress: прогресс взмаха/атаки
        float swingProgress = event.getSwingProgress();
        // equipProgress: прогресс доставания/экипировки
        float equipProgress = event.getEquipProgress();

        // Вычисления трансформаций для анимации взмаха
        float handSideMultiplier = isRightArm ? 1.0F : -1.0F;
        float swingSqrt = MathHelper.sqrt(swingProgress);
        float swingX = -0.3F * MathHelper.sin(swingSqrt * (float) Math.PI);
        float swingY = 0.4F * MathHelper.sin(swingSqrt * (float) Math.PI * 2F);
        float swingZ = -0.4F * MathHelper.sin(swingProgress * (float) Math.PI);

        // Применяем базовые трансляции и повороты для позиции руки в кадре
        GlStateManager.translate(handSideMultiplier * (swingX + 0.64000005F), swingY + -0.6F + equipProgress * -0.6F, swingZ + -0.71999997F);
        GlStateManager.rotate(handSideMultiplier * 45.0F, 0.0F, 1.0F, 0.0F);

        // Дополнительные повороты, усиливающие анимацию
        float swingSin = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float swingSqrtSin = MathHelper.sin(swingSqrt * (float) Math.PI);
        GlStateManager.rotate(handSideMultiplier * swingSqrtSin * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(handSideMultiplier * swingSin * -20.0F, 0.0F, 0.0F, 1.0F);

        // Финальные смещения для корректного кадрирования руки
        GlStateManager.translate(handSideMultiplier * -1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(handSideMultiplier * 120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(handSideMultiplier * -135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(handSideMultiplier * 5.6F, 0.0F, 0.0F);

        // Получаем модель брони, предоставленную предметом нагрудника
        ModelBiped bipedModel = chestplateStack.getItem().getArmorModel(player, chestplateStack, EntityEquipmentSlot.CHEST, null);
        if (bipedModel == null) {
            return;
        }

        // Определяем тип брони и флаги состояния
        IPowerArmor powerArmor = (IPowerArmor) chestplateStack.getItem();
        String type = powerArmor.getPowerArmorType();
        // isBroken: броня поломана
        boolean isBroken = type.endsWith("_broken");
        // isExo: тип экзоскелета (ванильный рендер + модель)
        boolean isExo = type.equals("exo");

        // Если броня сломана или это exo — сначала рендерим ванильную руку игрока
        if (isBroken || isExo) {
            // Биндим скин игрока и рендерим vanilla руку (левая/правая)
            ResourceLocation skin = player.getLocationSkin();
            Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
            GlStateManager.disableCull();
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            boolean isSlim = "slim".equals(player.getSkinType());
            RenderPlayer renderPlayer = new RenderPlayer(renderManager, isSlim);
            if (isRightArm) {
                renderPlayer.renderRightArm(player);
            } else {
                renderPlayer.renderLeftArm(player);
            }
            GlStateManager.enableCull();

            // Затем биндим текстуру экзо-модели и готовим модель к рендеру
            ResourceLocation exoTexture = new ResourceLocation("afp", "textures/models/armor/exo/exo_full.png");
            Minecraft.getMinecraft().getTextureManager().bindTexture(exoTexture);
            GlStateManager.disableCull();

            // Выбираем подходящую бипед-модель: либо модель предмета, либо вспомогательная ArmorExo
            ModelBiped biped;
            if (isExo) {
                biped = (ModelBiped) bipedModel;
            } else {
                biped = ArmorExo.getBipedModel(player, EntityEquipmentSlot.CHEST);
            }

            // Настраиваем позу модели и рендерим нужную руку
            biped.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
            if (isRightArm) {
                biped.bipedRightArm.offsetY = 0.0F;
                biped.bipedRightArm.rotateAngleX = 0.0F;
                biped.bipedRightArm.render(0.0625F);
            } else {
                biped.bipedLeftArm.offsetY = 0.0F;
                biped.bipedLeftArm.rotateAngleX = 0.0F;
                biped.bipedLeftArm.render(0.0625F);
            }
            GlStateManager.enableCull();
        }

        // Если броня — чистый exo, отрабатываем ванильный рендер и выходим (не рисуем PowerArmorModel)
        if (isExo) {
            event.setCanceled(true);
            return;
        }

        // Ожидаем модель типа PowerArmorModel для продолжения
        if (!(bipedModel instanceof PowerArmorModel)) {
            return;
        }

        PowerArmorModel armorModel = (PowerArmorModel) bipedModel;

        // Подготавливаем текстуру брони (учёт сломанной версии)
        // baseType: базовый ключ типа брони (например "exo" или другое)
        String baseType = armorModel.getBaseArmorType();
        String textureSuffix = armorModel.isBroken() ? "_broken" : "";
        ResourceLocation texture = new ResourceLocation("afp", "textures/models/armor/" + baseType + "/" + baseType + "_full" + textureSuffix + ".png");
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        // Настройка GL: отключаем отсечение, включаем смешивание и сглаживание
        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        // Применяем позу модели под текущего игрока
        armorModel.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);

        // Рендерим нужную руку модели экзоскелета (сброс локальных смещений)
        if (isRightArm) {
            armorModel.rightArm.offsetY = 0.0F;
            armorModel.rightArm.rotateAngleX = 0.0F;
            armorModel.rightArm.render(0.0625F);
        } else {
            armorModel.leftArm.offsetY = 0.0F;
            armorModel.leftArm.rotateAngleX = 0.0F;
            armorModel.leftArm.render(0.0625F);
        }

        // Восстанавливаем GL-состояние
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull();

        // Блокируем стандартный рендер руки, чтобы не было дубля
        event.setCanceled(true);
    }
}
