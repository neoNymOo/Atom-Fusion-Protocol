package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.config.AFPConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Обработчик отрисовки интерфейса в игре.
 * Отвечает за прогресс-бар, затемнение экрана и индикаторы силовой брони.
 */
@Mod.EventBusSubscriber
public class HandlerRenderGameOverlayEvent {
    // Построитель строки для прогресс-бара.
    private static final StringBuilder BAR_BUILDER = new StringBuilder(16);
//    // Текстура виньетки.
//    private static final ResourceLocation VIGNETTE_TEXTURE = new ResourceLocation("afp:textures/misc/vignette.png");
//    // Текстура интерфейса силовой брони.
//    private static final ResourceLocation PA_INTERFACE_TEXTURE = new ResourceLocation("afp:textures/misc/power_armor_interface.png");

    /**
     * Обрабатывает пре-рендеринг для скрытия стандартных элементов и отрисовки фона.
     * Проверяет наличие силовой брони, отключает глубину и рисует виньетку с оверлеем.
     * @param event событие рендеринга интерфейса.
     */
    @SubscribeEvent
    public static void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.gameSettings.thirdPersonView != 0) return;
        ScaledResolution resolution = new ScaledResolution(minecraft);
        EntityPlayer player = minecraft.player;
        if (player == null) return;
        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        boolean hasChestplate = !chestplate.isEmpty() && chestplate.getItem() instanceof IPowerArmor;
        if (!hasChestplate) return;
        RenderGameOverlayEvent.ElementType type = event.getType();
        if (type == RenderGameOverlayEvent.ElementType.ALL) {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableAlpha();
//            drawVignette(minecraft, resolution);
//            drawFullScreenOverlay(minecraft, resolution);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        } else if (type == RenderGameOverlayEvent.ElementType.HEALTH || type == RenderGameOverlayEvent.ElementType.FOOD) {
            event.setCanceled(true);
        }
    }

    /**
     * Обрабатывает пост-рендеринг для отрисовки прогресс-бара, затемнения и HUD силовой брони.
     * Вычисляет прогресс и альфу, вызывает методы отрисовки.
     * @param event событие рендеринга интерфейса.
     */
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.gameSettings.thirdPersonView != 0) return;
        ScaledResolution resolution = new ScaledResolution(minecraft);
        long currentTime = System.currentTimeMillis();
        // Отрисовка прогресс-бара взаимодействия.
        if (HandlerClientTickEvent.startHoldTime != 0L) {
            float progress = Math.min((currentTime - HandlerClientTickEvent.startHoldTime) / 1000.0F / HandlerClientTickEvent.currentMaxHoldTime, 1.0F);
            if (progress > 0.0F && progress < 1.0F) drawProgressBar(progress, minecraft, resolution);
        }
        // Затемнение экрана при входе/выходе из экзоскелета.
        if (HandlerClientTickEvent.fadeStartTime != 0L) {
            if (currentTime >= HandlerClientTickEvent.fadeStartTime) {
                float elapsedTime = (currentTime - HandlerClientTickEvent.fadeStartTime) / 1000.0F;
                float totalInHold = AFPConfig.fadeDurationIn + AFPConfig.fadeHold;
                float totalDuration = totalInHold + AFPConfig.fadeDurationOut;
                if (elapsedTime < totalDuration) {
                    float alpha = (elapsedTime < AFPConfig.fadeDurationIn)
                            ? elapsedTime / AFPConfig.fadeDurationIn
                            : (elapsedTime < totalInHold)
                            ? 1.0F
                            : 1.0F - (elapsedTime - totalInHold) / AFPConfig.fadeDurationOut;
                    drawFade(alpha, resolution);
                } else {
                    HandlerClientTickEvent.fadeStartTime = 0L;
                }
            }
        }
//        // Отрисовка HUD силовой брони.
//        renderPowerArmorHUD(minecraft, resolution);
    }

    /**
     * Рисует прогресс-бар в центре экрана с символами заполнения.
     * Вычисляет заполненные сегменты, строит строку и отображает текст.
     * @param progress прогресс от 0 до 1.
     * @param minecraft клиент Minecraft.
     * @param resolution разрешение экрана.
     */
    private static void drawProgressBar(float progress, Minecraft minecraft, ScaledResolution resolution) {
        int centerX = resolution.getScaledWidth() / 2;
        int centerY = resolution.getScaledHeight() / 2;
        final String filledSegment = "▮";
        final String emptySegment = "▯";
        final int totalSegments = 13;
        int filledSegmentsCount = (int) (progress * totalSegments);
        BAR_BUILDER.setLength(0);
        for (int i = 0; i < filledSegmentsCount; i++) BAR_BUILDER.append(filledSegment);
        for (int i = filledSegmentsCount; i < totalSegments; i++) BAR_BUILDER.append(emptySegment);
        String progressBar = BAR_BUILDER.toString();
        int barWidth = minecraft.fontRenderer.getStringWidth(progressBar);
        int xPosition = centerX - barWidth / 2;
        int yPosition = centerY + 4;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.enableAlpha();
        minecraft.fontRenderer.drawString(progressBar, xPosition, yPosition, 0xFFFFFF);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    /**
     * Рисует затемнение экрана с заданной прозрачностью.
     * Отключает текстуры, рисует квад, восстанавливает состояние.
     * @param alpha прозрачность от 0 до 1.
     * @param resolution разрешение экрана.
     */
    private static void drawFade(float alpha, ScaledResolution resolution) {
        if (alpha <= 0.0F) return;
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(0.0F, 0.0F, 0.0F, alpha);
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
        bufferBuilder.pos(0, resolution.getScaledHeight(), -90).endVertex();
        bufferBuilder.pos(resolution.getScaledWidth(), resolution.getScaledHeight(), -90).endVertex();
        bufferBuilder.pos(resolution.getScaledWidth(), 0, -90).endVertex();
        bufferBuilder.pos(0, 0, -90).endVertex();
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // TODO: доделать интерфейс силовой брони
//    /**
//     * Отрисовывает HUD силовой брони с индикаторами.
//     * Проверяет наличие брони, вычисляет углы стрелок, рисует их.
//     * @param minecraft клиент Minecraft.
//     * @param resolution разрешение экрана.
//     */
//    private static void renderPowerArmorHUD(Minecraft minecraft, ScaledResolution resolution) {
//        EntityPlayer player = minecraft.player;
//        if (player == null) return;
//        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
//        boolean hasChestplate = !chestplate.isEmpty() && chestplate.getItem() instanceof IPowerArmor;
//        if (!hasChestplate) return;
//        GlStateManager.disableTexture2D();
//        GlStateManager.enableBlend();
//        GlStateManager.tryBlendFuncSeparate(
//                GlStateManager.SourceFactor.SRC_ALPHA,
//                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
//                GlStateManager.SourceFactor.ONE,
//                GlStateManager.DestFactor.ZERO);
//        float r = 176.0F / 255.0F;
//        float g = 0.0F;
//        float b = 0.0F;
//        float a = 1.0F;
//        GlStateManager.color(r, g, b, a);
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder bufferBuilder = tessellator.getBuffer();
//        bufferBuilder.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
//        // Индикатор энергии.
//        float depletion = getFloatTag(chestplate, "fusion_depletion");
//        float depletionArrowRotation = lerpAngle(-91.0F, 133.0F, (AFPConfig.maxDepletion - depletion) / AFPConfig.maxDepletion);
//        int depletionArrowX = (int) (resolution.getScaledWidth() * 26.0F / 29.0F);
//        int depletionArrowY = (int) (resolution.getScaledHeight() * 25.0F / 30.0F);
//        drawArrow(bufferBuilder, depletionArrowX, depletionArrowY, depletionArrowRotation, 2, 30);
//        // Индикатор голода.
//        float hunger = player.getFoodStats().getFoodLevel();
//        float hungerArrowRotation = lerpAngle(-92.0F, 86.0F, hunger / 20.0F) + 90.0F;
//        int hungerArrowX = (int) (resolution.getScaledWidth() * 6.0F / 29.0F);
//        int hungerArrowY = (int) (resolution.getScaledHeight() * 21.0F / 24.0F);
//        drawArrow(bufferBuilder, hungerArrowX, hungerArrowY, hungerArrowRotation, 2, 12);
//        // Индикатор здоровья.
//        float health = player.getHealth();
//        float maxHealth = player.getMaxHealth();
//        float healthArrowRotation = lerpAngle(-125.0F, 120.0F, (maxHealth - health) / maxHealth);
//        int healthArrowX = (int) (resolution.getScaledWidth() * 3.0F / 37.0F);
//        int healthArrowY = (int) (resolution.getScaledHeight() * 19.0F / 22.0F);
//        drawArrow(bufferBuilder, healthArrowX, healthArrowY, healthArrowRotation, 2, 19);
//        // Индикатор радиации.
//        float rads = 0.0F;
//        if (AtomFusionProtocol.IS_HBM_LOADED) {
//            double hbmRads = HbmLivingProps.getRadiation(player);
//            rads = (float) Math.min(hbmRads / 1000.0 * 10000.0, 10000.0F);
//        }
//        float radsArrowRotation = lerpAngle(-90.0F, 90.0F, (10_000.0F - rads) / 10_000.0F);
//        int radsArrowX = (int) (resolution.getScaledWidth() * 1492.0F / 2000.0F);
//        int radsArrowY = (int) (resolution.getScaledHeight() * 892.0F / 1000.0F);
//        System.out.println("Radiation: " + rads + " rads");
//        drawArrow(bufferBuilder, radsArrowX, radsArrowY, radsArrowRotation, 2, 12);
//        tessellator.draw();
//        GlStateManager.disableBlend();
//        GlStateManager.enableTexture2D();
//    }

//    /**
//     * Интерполирует угол между минимумом и максимумом по доле.
//     * Ограничивает долю в [0, 1], возвращает угол.
//     * @param min минимальный угол.
//     * @param max максимальный угол.
//     * @param t доля от 0 до 1.
//     * @return интерполированный угол.
//     */
//    private static float lerpAngle(float min, float max, float t) {
//        if (t < 0.0F) t = 0.0F;
//        if (t > 1.0F) t = 1.0F;
//        return min + t * (max - min);
//    }

//    /**
//     * Рисует стрелку с поворотом вокруг центра.
//     * Вычисляет координаты точек, добавляет вершины в буфер.
//     * @param bufferBuilder буфер для вершин.
//     * @param x центр X.
//     * @param y центр Y.
//     * @param rotation угол поворота в градусах.
//     * @param width ширина стрелки.
//     * @param height длина стрелки.
//     */
//    private static void drawArrow(BufferBuilder bufferBuilder, int x, int y, float rotation, int width, int height) {
//        float theta = (float) Math.toRadians(-rotation);
//        float cos = (float) Math.cos(theta);
//        float sin = (float) Math.sin(theta);
//        float halfWidth = width / 2.0F;
//        float[][] points = {
//                {-halfWidth, 0},
//                {halfWidth, 0},
//                {halfWidth, -height},
//                {-halfWidth, -height}
//        };
//        for (float[] p : points) {
//            float px = p[0];
//            float py = p[1];
//            float rx = px * cos - py * sin + x;
//            float ry = px * sin + py * cos + y;
//            bufferBuilder.pos(rx, ry, 0).endVertex();
//        }
//    }

//    /**
//     * Читает числовой тег из предмета.
//     * Поддерживает float, int, long; возвращает 0 если тега нет.
//     * @param stack предмет.
//     * @param tagName имя тега.
//     * @return значение тега.
//     */
//    private static float getFloatTag(ItemStack stack, String tagName) {
//        if (stack.isEmpty() || !stack.hasTagCompound()) return 0.0F;
//        NBTTagCompound tag = stack.getTagCompound();
//        if (tag == null) return 0.0F;
//        if (tag.hasKey(tagName, 99)) {
//            return tag.getFloat(tagName);
//        }
//        if (tag.hasKey(tagName, 3) || tag.hasKey(tagName, 4)) {
//            return (float) tag.getDouble(tagName);
//        }
//        return 0.0F;
//    }

//    /**
//     * Рисует полноэкранный оверлей интерфейса силовой брони.
//     * Связывает текстуру и рисует прямоугольник на весь экран.
//     * @param minecraft клиент Minecraft.
//     * @param resolution разрешение экрана.
//     */
//    private static void drawFullScreenOverlay(Minecraft minecraft, ScaledResolution resolution) {
//        minecraft.getTextureManager().bindTexture(PA_INTERFACE_TEXTURE);
//        minecraft.ingameGUI.drawModalRectWithCustomSizedTexture(
//                0, 0,
//                0, 0,
//                resolution.getScaledWidth(),
//                resolution.getScaledHeight(),
//                resolution.getScaledWidth(),
//                resolution.getScaledHeight());
//    }

//    /**
//     * Рисует виньетку на экране.
//     * Связывает текстуру и рисует прямоугольник на весь экран.
//     * @param minecraft клиент Minecraft.
//     * @param resolution разрешение экрана.
//     */
//    private static void drawVignette(Minecraft minecraft, ScaledResolution resolution) {
//        minecraft.getTextureManager().bindTexture(VIGNETTE_TEXTURE);
//        minecraft.ingameGUI.drawModalRectWithCustomSizedTexture(
//                0, 0,
//                0, 0,
//                resolution.getScaledWidth(),
//                resolution.getScaledHeight(),
//                resolution.getScaledWidth(),
//                resolution.getScaledHeight());
//    }
}