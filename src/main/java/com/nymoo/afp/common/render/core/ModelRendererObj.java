package com.nymoo.afp.common.render.core;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Объект рендеринга модели.
 * Управляет позиционированием, вращением и отрисовкой конкретных частей модели.
 */
@SideOnly(Side.CLIENT)
public class ModelRendererObj {

    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public float offsetX;
    public float offsetY;
    public float offsetZ;

    String[] parts;
    IModelCustom model;

    public ModelRendererObj(IModelCustom model, String... parts) {
        this.model = model;
        this.parts = parts;
    }

    /**
     * Устанавливает смещение модели.
     */
    public ModelRendererObj setPosition(float x, float y, float z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    /**
     * Устанавливает точку вращения модели.
     */
    public ModelRendererObj setRotationPoint(float x, float y, float z) {
        this.rotationPointX = x;
        this.rotationPointY = y;
        this.rotationPointZ = z;
        return this;
    }

    /**
     * Копирует параметры трансформации в другой объект.
     */
    public void copyTo(ModelRendererObj obj) {
        obj.offsetX = offsetX;
        obj.offsetY = offsetY;
        obj.offsetZ = offsetZ;
        obj.rotateAngleX = rotateAngleX;
        obj.rotateAngleY = rotateAngleY;
        obj.rotateAngleZ = rotateAngleZ;
        obj.rotationPointX = rotationPointX;
        obj.rotationPointY = rotationPointY;
        obj.rotationPointZ = rotationPointZ;
    }

    /**
     * Выполняет рендеринг модели с учетом всех трансформаций.
     *
     * @param scale Масштаб отрисовки
     */
    @SideOnly(Side.CLIENT)
    public void render(float scale) {
        GlStateManager.pushMatrix();

        GlStateManager.translate(this.offsetX * scale, this.offsetY * scale, this.offsetZ * scale);

        if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
            GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

            if (this.rotateAngleZ != 0.0F)
                GlStateManager.rotate(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
            if (this.rotateAngleY != 0.0F)
                GlStateManager.rotate(this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
            if (this.rotateAngleX != 0.0F)
                GlStateManager.rotate(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);

            GlStateManager.translate(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);
        } else {
            if (this.rotateAngleZ != 0.0F)
                GlStateManager.rotate(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
            if (this.rotateAngleY != 0.0F)
                GlStateManager.rotate(this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
            if (this.rotateAngleX != 0.0F)
                GlStateManager.rotate(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
        }

        GlStateManager.scale(scale, scale, scale);

        if (parts != null && parts.length > 0)
            for (String part : parts)
                model.renderPart(part);
        else
            model.renderAll();

        GlStateManager.popMatrix();
    }
}