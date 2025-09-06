package com.nymoo.afp.common.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
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
     * @param event The render event.
     */
    @SubscribeEvent
    public static void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (HandlerClientTickEvent.startHoldTime == 0L) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        // Only render in first-person view
        if (mc.gameSettings.thirdPersonView != 0) return;

        long now = System.currentTimeMillis();
        float elapsed = (now - HandlerClientTickEvent.startHoldTime) / 1000.0F;
        float progress = Math.min(elapsed / HandlerClientTickEvent.currentMaxHoldTime, 1.0F);

        if (progress < 1.0F && progress > 0.0F) {
            drawLoadingBar(event, progress, mc);
        }
    }

    /**
     * Draws the loading bar on the screen.
     * Uses special characters for filled and empty segments.
     * @param event The render event.
     * @param progress The current progress (0.0 to 1.0).
     * @param mc The Minecraft instance.
     */
    private static void drawLoadingBar(RenderGameOverlayEvent.Post event, float progress, Minecraft mc) {
        if (progress <= 0.0F) return;

        ScaledResolution sr = new ScaledResolution(mc);
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
}