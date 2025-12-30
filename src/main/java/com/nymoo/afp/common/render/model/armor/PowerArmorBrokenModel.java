package com.nymoo.afp.common.render.model.armor;

import com.nymoo.afp.common.entity.EntityExoskeletonBroken.ExoskeletonBroken;
import com.nymoo.afp.common.render.core.AdvancedModelLoader;
import com.nymoo.afp.common.render.core.IModelCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

/**
 * Рендерер сломанных частей силовой брони.
 * Оптимизирован с использованием кэширования для предотвращения повторной загрузки ресурсов.
 */
@SideOnly(Side.CLIENT)
public class PowerArmorBrokenModel {

    /**
     * Кэш загруженных моделей для избежания чтения с диска каждый кадр.
     * Ключ - ID предмета, Значение - объект модели.
     */
    private static final Map<String, IModelCustom> MODEL_CACHE = new HashMap<>();

    /**
     * Кэш текстур для моделей.
     * Ключ - ID предмета, Значение - путь к текстуре.
     */
    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new HashMap<>();

    public PowerArmorBrokenModel() {
    }

    /**
     * Основной метод отрисовки модели.
     * Определяет тип брони по предмету в руке, загружает (или берет из кэша) модель и рендерит её.
     *
     * @param entityIn Сущность для рендера
     * @param scale Масштаб рендеринга
     */
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!(entityIn instanceof ExoskeletonBroken)) {
            return;
        }

        ExoskeletonBroken entity = (ExoskeletonBroken) entityIn;
        ItemStack mainHand = entity.getHeldItemMainhand();
        if (mainHand.isEmpty()) {
            return;
        }

        String id = mainHand.getItem().getRegistryName().getPath();
        if (id == null || id.isEmpty()) {
            return;
        }

        // Быстрая проверка кэша перед тяжелыми операциями со строками
        IModelCustom model = MODEL_CACHE.get(id);
        ResourceLocation texture = TEXTURE_CACHE.get(id);

        // Если модели нет в кэше, пробуем загрузить
        if (model == null) {
            String[] split = id.split("_");
            // Проверка формата ID: type_part_broken (минимум 3 части)
            if (split.length < 3 || !split[2].equals("broken")) {
                return;
            }

            String type = split[0];
            String part = split[1];

            try {
                ResourceLocation modelRes = new ResourceLocation("afp:models/armor/" + type + "/" + type + "_" + part + "_broken.obj");
                model = AdvancedModelLoader.loadModel(modelRes);
                MODEL_CACHE.put(id, model);

                texture = new ResourceLocation("afp:textures/models/armor/" + type + "/" + type + "_full_broken.png");
                TEXTURE_CACHE.put(id, texture);
            } catch (Exception e) {
                // В случае ошибки загрузки можно добавить заглушку или логирование, чтобы не пытаться грузить каждый кадр
                System.err.println("Failed to load broken armor model for ID: " + id);
                e.printStackTrace();
                return;
            }
        }

        if (model == null || texture == null) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glShadeModel(GL11.GL_SMOOTH);

        // Применение вращения сущности (корректировка на 180 градусов)
        GL11.glRotatef(180.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);

        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        model.renderAll();

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glPopMatrix();
    }

    /**
     * Класс рендера сущности сломанного экзоскелета.
     * Связывает сущность с логикой отрисовки модели.
     */
    @SideOnly(Side.CLIENT)
    public static class RenderExoskeletonBroken extends Render<ExoskeletonBroken> {
        private static final PowerArmorBrokenModel MODEL = new PowerArmorBrokenModel();

        public RenderExoskeletonBroken(RenderManager renderManager) {
            super(renderManager);
            this.shadowSize = 0.5F;
        }

        @Override
        public void doRender(ExoskeletonBroken entity, double x, double y, double z, float entityYaw, float partialTicks) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            // Масштаб 0.0625F соответствует 1/16 блока (стандарт Minecraft)
            MODEL.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
            GlStateManager.popMatrix();
        }

        @Override
        protected ResourceLocation getEntityTexture(ExoskeletonBroken entity) {
            return null; // Текстура управляется внутри модели
        }
    }
}