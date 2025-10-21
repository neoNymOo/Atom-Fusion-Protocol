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
 * Класс обрабатывает рендер оверлея для прогресс-бара и затухания.
 */
@Mod.EventBusSubscriber
public class HandlerRenderGameOverlayEvent {
    // Строитель строки для бара.
    private static final StringBuilder BAR_BUILDER = new StringBuilder(16);

    /**
     * Рендерит оверлей после элементов.
     * @param event Событие.
     */
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings.thirdPersonView != 0) return;

        ScaledResolution res = new ScaledResolution(mc);

        if (HandlerClientTickEvent.startHoldTime != 0L) {
            long now = System.currentTimeMillis();
            float progress = Math.min((now - HandlerClientTickEvent.startHoldTime) / 1000.0F / HandlerClientTickEvent.currentMaxHoldTime, 1.0F);
            if (progress > 0.0F && progress < 1.0F) drawProgressBar(progress, mc, res);
        }

        if (HandlerClientTickEvent.fadeStartTime != 0L) {
            long now = System.currentTimeMillis();
            if (now < HandlerClientTickEvent.fadeStartTime) return;

            float elapsed = (now - HandlerClientTickEvent.fadeStartTime) / 1000.0F;
            float totalInHold = HandlerClientTickEvent.FADE_DURATION_IN + HandlerClientTickEvent.FADE_HOLD;
            float total = totalInHold + HandlerClientTickEvent.FADE_DURATION_OUT;

            if (elapsed >= total) {
                HandlerClientTickEvent.fadeStartTime = 0L;
                return;
            }

            float alpha = (elapsed < HandlerClientTickEvent.FADE_DURATION_IN) ? elapsed / HandlerClientTickEvent.FADE_DURATION_IN :
                    (elapsed < totalInHold) ? 1.0F :
                            1.0F - (elapsed - totalInHold) / HandlerClientTickEvent.FADE_DURATION_OUT;
            drawFade(alpha, res);
        }
    }

    /**
     * Рисует прогресс-бар.
     * @param progress Прогресс.
     * @param mc Minecraft.
     * @param res Разрешение.
     */
    private static void drawProgressBar(float progress, Minecraft mc, ScaledResolution res) {
        int centerX = res.getScaledWidth() / 2;
        int centerY = res.getScaledHeight() / 2;

        final String filled = "▮";
        final String empty = "▯";
        final int segments = 13;
        int filledCount = (int) (progress * segments);

        BAR_BUILDER.setLength(0);
        for (int i = 0; i < filledCount; i++) BAR_BUILDER.append(filled);
        for (int i = filledCount; i < segments; i++) BAR_BUILDER.append(empty);

        String bar = BAR_BUILDER.toString();
        int width = mc.fontRenderer.getStringWidth(bar);
        int x = centerX - width / 2;
        int y = centerY + 4;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.enableAlpha();
        mc.fontRenderer.drawString(bar, x, y, 0xFFFFFF);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    /**
     * Рисует затухание.
     * @param alpha Альфа.
     * @param res Разрешение.
     */
    private static void drawFade(float alpha, ScaledResolution res) {
        if (alpha <= 0.0F) return;

        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(0.0F, 0.0F, 0.0F, alpha);
        GlStateManager.disableTexture2D();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
        buf.pos(0, res.getScaledHeight(), -90).endVertex();
        buf.pos(res.getScaledWidth(), res.getScaledHeight(), -90).endVertex();
        buf.pos(res.getScaledWidth(), 0, -90).endVertex();
        buf.pos(0, 0, -90).endVertex();
        tess.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}