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
 * Handles rendering of the game overlay, specifically the progress bar for hold actions.
 * Interacts with HandlerClientTickEvent to get hold progress.
 */
@Mod.EventBusSubscriber
public class HandlerRenderGameOverlayEvent {

    // Reusable StringBuilder for building the loading bar string to minimize allocations
    private static final StringBuilder LOADING_BAR_BUILDER = new StringBuilder(16);

    /**
     * Event handler for rendering the game overlay.
     * Checks if a hold action is in progress and renders the progress bar if applicable.
     *
     * @param event The render event.
     */
    @SubscribeEvent
    public static void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        // Only render in first-person view
        if (mc.gameSettings.thirdPersonView != 0) return;

        ScaledResolution sr = new ScaledResolution(mc);

        // Render hold progress bar if active
        if (HandlerClientTickEvent.startHoldTime != 0L) {
            long now = System.currentTimeMillis();
            float elapsed = (now - HandlerClientTickEvent.startHoldTime) / 1000.0F;
            float progress = Math.min(elapsed / HandlerClientTickEvent.currentMaxHoldTime, 1.0F);

            if (progress < 1.0F && progress > 0.0F) {
                drawLoadingBar(event, progress, mc, sr);
            }
        }

        // Render fade effect if active
        if (HandlerClientTickEvent.fadeStartTime != 0L) {
            long now = System.currentTimeMillis();
            if (now < HandlerClientTickEvent.fadeStartTime) {
                return; // Delay not over yet
            }
            float elapsed = (now - HandlerClientTickEvent.fadeStartTime) / 1000.0F;
            float totalInHold = HandlerClientTickEvent.FADE_DURATION_IN + HandlerClientTickEvent.FADE_HOLD;
            float total = totalInHold + HandlerClientTickEvent.FADE_DURATION_OUT;

            if (elapsed >= total) {
                HandlerClientTickEvent.fadeStartTime = 0L;
                return;
            }

            float alpha = 0f;
            if (elapsed < HandlerClientTickEvent.FADE_DURATION_IN) {
                alpha = elapsed / HandlerClientTickEvent.FADE_DURATION_IN;
            } else if (elapsed < totalInHold) {
                alpha = 1f;
            } else {
                alpha = 1f - (elapsed - totalInHold) / HandlerClientTickEvent.FADE_DURATION_OUT;
            }

            drawFadeOverlay(alpha, mc, sr);
        }
    }

    /**
     * Draws the loading bar on the screen.
     * Uses special characters for filled and empty segments.
     *
     * @param event    The render event.
     * @param progress The current progress (0.0 to 1.0).
     * @param mc       The Minecraft instance.
     * @param sr       The scaled resolution.
     */
    private static void drawLoadingBar(RenderGameOverlayEvent.Post event, float progress, Minecraft mc, ScaledResolution sr) {
        if (progress <= 0.0F) return;

        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;

        final String filledChar = "▮";
        final String emptyChar = "▯";
        final int totalSegments = 13;

        int filledSegments = (int) (progress * totalSegments);
        filledSegments = Math.min(filledSegments, totalSegments);

        LOADING_BAR_BUILDER.setLength(0);
        for (int i = 0; i < filledSegments; i++) {
            LOADING_BAR_BUILDER.append(filledChar);
        }
        for (int i = filledSegments; i < totalSegments; i++) {
            LOADING_BAR_BUILDER.append(emptyChar);
        }
        String text = LOADING_BAR_BUILDER.toString();

        int width = mc.fontRenderer.getStringWidth(text);
        int x = centerX - width / 2;
        int y = centerY + 4;

        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.fontRenderer.drawString(text, x, y, 0xFFFFFF);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Draws a black fade overlay on the screen with the given alpha.
     *
     * @param alpha The opacity of the overlay (0.0 to 1.0).
     * @param mc    The Minecraft instance.
     * @param sr    The scaled resolution.
     */
    private static void drawFadeOverlay(float alpha, Minecraft mc, ScaledResolution sr) {
        if (alpha <= 0.0F) return;

        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(0.0F, 0.0F, 0.0F, alpha);
        GlStateManager.disableTexture2D();

        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
        bufferbuilder.pos(0.0D, height, -90.0D).endVertex();
        bufferbuilder.pos(width, height, -90.0D).endVertex();
        bufferbuilder.pos(width, 0.0D, -90.0D).endVertex();
        bufferbuilder.pos(0.0D, 0.0D, -90.0D).endVertex();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}