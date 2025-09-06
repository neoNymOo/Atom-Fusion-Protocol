package com.nymoo.afp.common.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Отрисовывает индикатор прогресса удержания для взаимодействия с экзоскелетом
 * Основные особенности:
 *  - Прогресс-бар из сегментов в центре экрана
 *  - Оптимизированное использование StringBuilder для минимизации аллокаций
 *  - Точное восстановление состояний рендера
 */
@Mod.EventBusSubscriber
public class HandlerRenderGameOverlayEvent {

    // Переиспользуемый builder для формирования строки прогресса
    private static final StringBuilder LOADING_BAR_BUILDER = new StringBuilder(16);

    /**
     * Обработчик события отрисовки интерфейса
     * Отображает прогресс-бар только при активном процессе удержания
     */
    @SubscribeEvent
    public static void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (HandlerClientTickEvent.startHoldTime == 0L) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        // Показываем только от первого лица
        if (mc.gameSettings.thirdPersonView != 0) return;

        // Вычисляем прогресс от 0.0 до 1.0
        long now = System.currentTimeMillis();
        float elapsed = (now - HandlerClientTickEvent.startHoldTime) / 1000.0F;
        float progress = Math.min(elapsed / HandlerClientTickEvent.MAX_HOLD_TIME, 1.0F);

        if (progress < 1.0F && progress > 0.0F) {
            drawLoadingBar(event, progress, mc);
        }
    }

    /**
     * Отрисовывает индикатор прогресса в виде сегментированной полосы
     * Использует специальные символы для визуализации заполнения
     */
    private static void drawLoadingBar(RenderGameOverlayEvent.Post event, float progress, Minecraft mc) {
        if (progress <= 0.0F) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;

        final String filledChar = "▮";
        final String emptyChar = "▯";
        final int totalSegments = 13;

        // Вычисляем количество заполненных сегментов
        int filledSegments = (int) (progress * totalSegments);
        filledSegments = Math.min(filledSegments, totalSegments);

        // Формируем строку прогресса
        LOADING_BAR_BUILDER.setLength(0);
        for (int i = 0; i < filledSegments; i++) {
            LOADING_BAR_BUILDER.append(filledChar);
        }
        for (int i = filledSegments; i < totalSegments; i++) {
            LOADING_BAR_BUILDER.append(emptyChar);
        }
        String text = LOADING_BAR_BUILDER.toString();

        // Позиционируем и отрисовываем текст
        int width = mc.fontRenderer.getStringWidth(text);
        int x = centerX - width / 2;
        int y = centerY + 4;

        GlStateManager.enableBlend();

        // Устанавливаем режим смешивания как в оригинальном коде
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.fontRenderer.drawString(text, x, y, 0xFFFFFF);

        // Восстанавливаем стандартный режим смешивания
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}