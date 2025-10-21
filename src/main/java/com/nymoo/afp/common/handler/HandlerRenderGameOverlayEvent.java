package com.nymoo.afp.common.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Обработчик рендеринга игрового интерфейса.
 * Отвечает за отображение прогресс-бара взаимодействий и эффектов затемнения экрана.
 */
@Mod.EventBusSubscriber
public class HandlerRenderGameOverlayEvent {
    /**
     * Построитель строки для отрисовки прогресс-бара
     */
    private static final StringBuilder BAR_BUILDER = new StringBuilder(16);

    /**
     * Обрабатывает событие рендеринга игрового интерфейса после всех элементов.
     * Отображает прогресс-бар удержания и эффекты затемнения экрана.
     *
     * @param event Событие рендеринга игрового интерфейса
     */
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.gameSettings.thirdPersonView != 0) return;

        ScaledResolution resolution = new ScaledResolution(minecraft);

        if (HandlerClientTickEvent.startHoldTime != 0L) {
            long currentTime = System.currentTimeMillis();
            float progress = Math.min((currentTime - HandlerClientTickEvent.startHoldTime) / 1000.0F / HandlerClientTickEvent.currentMaxHoldTime, 1.0F);
            if (progress > 0.0F && progress < 1.0F) drawProgressBar(progress, minecraft, resolution);
        }

        if (HandlerClientTickEvent.fadeStartTime != 0L) {
            long currentTime = System.currentTimeMillis();
            if (currentTime < HandlerClientTickEvent.fadeStartTime) return;

            float elapsedTime = (currentTime - HandlerClientTickEvent.fadeStartTime) / 1000.0F;
            float totalInHold = HandlerClientTickEvent.FADE_DURATION_IN + HandlerClientTickEvent.FADE_HOLD;
            float totalDuration = totalInHold + HandlerClientTickEvent.FADE_DURATION_OUT;

            if (elapsedTime >= totalDuration) {
                HandlerClientTickEvent.fadeStartTime = 0L;
                return;
            }

            float alpha = (elapsedTime < HandlerClientTickEvent.FADE_DURATION_IN) ? elapsedTime / HandlerClientTickEvent.FADE_DURATION_IN :
                    (elapsedTime < totalInHold) ? 1.0F :
                            1.0F - (elapsedTime - totalInHold) / HandlerClientTickEvent.FADE_DURATION_OUT;
            drawFade(alpha, resolution);
        }
    }

    /**
     * Отрисовывает прогресс-бар удержания в центре экрана.
     * Использует символьное представление для отображения прогресса.
     *
     * @param progress   Текущий прогресс от 0.0 до 1.0
     * @param minecraft  Экземпляр клиента Minecraft
     * @param resolution Масштабированное разрешение экрана
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
     * Отрисовывает эффект затемнения экрана с заданной прозрачностью.
     * Используется для плавных переходов при входе/выходе из экзоскелета.
     *
     * @param alpha      Уровень прозрачности затемнения от 0.0 до 1.0
     * @param resolution Масштабированное разрешение экрана
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
}