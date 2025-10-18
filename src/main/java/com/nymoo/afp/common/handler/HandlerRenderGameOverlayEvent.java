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
 * Handles rendering of game overlays, specifically the progress bar for hold actions and fade effects.
 * Integrates with HandlerClientTickEvent for hold progress and fade timing.
 */
@Mod.EventBusSubscriber
public class HandlerRenderGameOverlayEvent {

    // Reusable StringBuilder for building the loading bar to reduce allocations
    private static final StringBuilder LOADING_BAR_BUILDER = new StringBuilder(16);

    /**
     * Handles rendering the game overlay.
     * Renders progress bar if hold action is in progress and fade effect if active.
     *
     * @param event The render event.
     */
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            return;
        }

        // Render only in first-person view
        if (mc.gameSettings.thirdPersonView != 0) {
            return;
        }

        ScaledResolution scaledRes = new ScaledResolution(mc);

        // Render hold progress bar if active
        if (HandlerClientTickEvent.startHoldTime != 0L) {
            long now = System.currentTimeMillis();
            float elapsed = (now - HandlerClientTickEvent.startHoldTime) / 1000.0F;
            float progress = Math.min(elapsed / HandlerClientTickEvent.currentMaxHoldTime, 1.0F);

            if (progress > 0.0F && progress < 1.0F) {
                drawProgressBar(progress, mc, scaledRes);
            }
        }

        // Render fade effect if active
        if (HandlerClientTickEvent.fadeStartTime != 0L) {
            long now = System.currentTimeMillis();
            if (now < HandlerClientTickEvent.fadeStartTime) {
                return; // Delay not elapsed
            }

            float elapsed = (now - HandlerClientTickEvent.fadeStartTime) / 1000.0F;
            float totalInHold = HandlerClientTickEvent.FADE_DURATION_IN + HandlerClientTickEvent.FADE_HOLD;
            float totalDuration = totalInHold + HandlerClientTickEvent.FADE_DURATION_OUT;

            if (elapsed >= totalDuration) {
                HandlerClientTickEvent.fadeStartTime = 0L;
                return;
            }

            float alpha;
            if (elapsed < HandlerClientTickEvent.FADE_DURATION_IN) {
                alpha = elapsed / HandlerClientTickEvent.FADE_DURATION_IN;
            } else if (elapsed < totalInHold) {
                alpha = 1.0F;
            } else {
                alpha = 1.0F - (elapsed - totalInHold) / HandlerClientTickEvent.FADE_DURATION_OUT;
            }

            drawFade(alpha, mc, scaledRes);
        }
    }

    /**
     * Draws the progress bar on screen using special characters for segments.
     *
     * @param progress Current progress (0.0 to 1.0).
     * @param mc The Minecraft instance.
     * @param scaledRes The scaled resolution.
     */
    private static void drawProgressBar(float progress, Minecraft mc, ScaledResolution scaledRes) {
        int centerX = scaledRes.getScaledWidth() / 2;
        int centerY = scaledRes.getScaledHeight() / 2;

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
        String barText = LOADING_BAR_BUILDER.toString();

        int textWidth = mc.fontRenderer.getStringWidth(barText);
        int x = centerX - textWidth / 2;
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
        mc.fontRenderer.drawString(barText, x, y, 0xFFFFFF);

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Draws a black fade overlay with specified alpha.
     *
     * @param alpha Opacity (0.0 to 1.0).
     * @param mc The Minecraft instance.
     * @param scaledRes The scaled resolution.
     */
    private static void drawFade(float alpha, Minecraft mc, ScaledResolution scaledRes) {
        if (alpha <= 0.0F) {
            return;
        }

        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(0.0F, 0.0F, 0.0F, alpha);
        GlStateManager.disableTexture2D();

        int width = scaledRes.getScaledWidth();
        int height = scaledRes.getScaledHeight();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
        buffer.pos(0.0D, height, -90.0D).endVertex();
        buffer.pos(width, height, -90.0D).endVertex();
        buffer.pos(width, 0.0D, -90.0D).endVertex();
        buffer.pos(0.0D, 0.0D, -90.0D).endVertex();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}