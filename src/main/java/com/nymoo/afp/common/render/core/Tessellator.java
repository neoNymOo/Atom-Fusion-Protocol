package com.nymoo.afp.common.render.core;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

/**
 * Кастомный тесселятор для построения геометрии.
 * Обертка над стандартным BufferBuilder Minecraft для упрощения работы с вершинами.
 */
@SideOnly(Side.CLIENT)
public class Tessellator {
    public static final Tessellator instance = new Tessellator(2097152);

    public boolean defaultTexture = false;
    public boolean hasColor;
    public boolean hasNormals;

    private int rawBufferSize = 0;
    private int[] rawBuffer;
    private int vertexCount;
    private int color;
    private boolean hasTexture;
    private boolean hasBrightness;
    private int rawBufferIndex;
    private boolean isColorDisabled;
    private double xOffset;
    private double yOffset;
    private double zOffset;
    private int r, g, b, a;
    private float normalTestX, normalTestY, normalTestZ;

    private int[] vertexStateBuffer = null;
    private Integer[] sortIndices = null;

    private Tessellator(int size) {
        this.rawBufferSize = size;
        this.rawBuffer = new int[size];
    }

    public Tessellator() {
        this(2097152);
    }

    /**
     * Завершает построение и отрисовывает буфер.
     *
     * @return Статус выполнения
     */
    public int draw() {
        hasColor = false;
        hasNormals = false;
        this.xOffset = 0;
        this.yOffset = 0;
        this.zOffset = 0;
        net.minecraft.client.renderer.Tessellator.getInstance().draw();
        return 1;
    }

    /**
     * Получает текущее состояние вершин с сортировкой по глубине.
     *
     * @param x Координата X камеры
     * @param y Координата Y камеры
     * @param z Координата Z камеры
     * @return Объект состояния вершин
     */
    public TessellatorVertexState getVertexState(float x, float y, float z) {
        int total = this.rawBufferIndex;
        int segSize = 32;
        int segCount = total / segSize;

        if (vertexStateBuffer == null || vertexStateBuffer.length < total) {
            vertexStateBuffer = new int[total];
        }
        if (sortIndices == null || sortIndices.length < segCount) {
            sortIndices = new Integer[segCount];
        }

        for (int i = 0; i < segCount; i++) sortIndices[i] = i * segSize;

        float ox = x + (float) this.xOffset;
        float oy = y + (float) this.yOffset;
        float oz = z + (float) this.zOffset;

        Arrays.sort(sortIndices, 0, segCount, new QuadComparator(this.rawBuffer, ox, oy, oz));

        for (int i = 0; i < segCount; i++) {
            System.arraycopy(this.rawBuffer, sortIndices[i], vertexStateBuffer, i * segSize, segSize);
        }

        int remainingStart = segCount * segSize;
        if (remainingStart < total) {
            System.arraycopy(this.rawBuffer, remainingStart, vertexStateBuffer, remainingStart, total - remainingStart);
        }

        System.arraycopy(vertexStateBuffer, 0, this.rawBuffer, 0, total);
        return new TessellatorVertexState(Arrays.copyOf(vertexStateBuffer, total), this.rawBufferIndex, this.vertexCount, this.hasTexture, this.hasBrightness, this.hasNormals, this.hasColor);
    }

    /**
     * Восстанавливает состояние тесселятора из сохраненного объекта.
     *
     * @param state Сохраненное состояние вершин
     */
    public void setVertexState(TessellatorVertexState state) {
        while (state.getRawBuffer().length > rawBufferSize && rawBufferSize > 0) {
            rawBufferSize <<= 1;
        }
        if (rawBufferSize > rawBuffer.length) {
            rawBuffer = new int[rawBufferSize];
        }
        System.arraycopy(state.getRawBuffer(), 0, this.rawBuffer, 0, state.getRawBuffer().length);
        this.rawBufferIndex = state.getRawBufferIndex();
        this.vertexCount = state.getVertexCount();
        this.hasTexture = state.getHasTexture();
        this.hasBrightness = state.getHasBrightness();
        this.hasColor = state.getHasColor();
        this.hasNormals = state.getHasNormals();
    }

    /**
     * Начинает построение четырехугольников (Quads).
     */
    public void startDrawingQuads() {
        this.startDrawing(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
    }

    /**
     * Начинает построение цветных четырехугольников.
     */
    public void startDrawingQuadsColor() {
        startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
    }

    /**
     * Начинает построение в указанном режиме OpenGL.
     *
     * @param mode Режим отрисовки (например, GL_TRIANGLES)
     */
    public void startDrawing(int mode) {
        startDrawing(mode, DefaultVertexFormats.POSITION_TEX_NORMAL);
    }

    /**
     * Начинает построение с указанным форматом вершин.
     *
     * @param glMode Режим отрисовки
     * @param format Формат вершин
     */
    public void startDrawing(int glMode, VertexFormat format) {
        this.isColorDisabled = false;
        net.minecraft.client.renderer.Tessellator.getInstance().getBuffer().begin(glMode, format);
    }

    /**
     * Устанавливает текстурные координаты.
     */
    public void setTextureUV(double u, double v) {
        this.hasTexture = true;
    }

    /**
     * Устанавливает яркость.
     */
    public void setBrightness(int brightness) {
        this.hasBrightness = true;
    }

    /**
     * Устанавливает цвет (RGB float).
     */
    public void setColorOpaque_F(float r, float g, float b) {
        this.setColorOpaque((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F));
    }

    /**
     * Устанавливает цвет с прозрачностью (RGBA float).
     */
    public void setColorRGBA_F(float r, float g, float b, float a) {
        this.setColorRGBA((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F), (int) (a * 255.0F));
    }

    /**
     * Устанавливает непрозрачный цвет (RGB int).
     */
    public void setColorOpaque(int r, int g, int b) {
        this.setColorRGBA(r, g, b, 255);
    }

    /**
     * Устанавливает цвет с прозрачностью (RGBA int).
     */
    public void setColorRGBA(int r, int g, int b, int a) {
        if (!this.isColorDisabled) {
            this.hasColor = true;
            this.r = Math.max(0, Math.min(255, r));
            this.g = Math.max(0, Math.min(255, g));
            this.b = Math.max(0, Math.min(255, b));
            this.a = Math.max(0, Math.min(255, a));
        }
    }

    /**
     * Добавляет вершину с текстурными координатами.
     */
    public void addVertexWithUV(double x, double y, double z, double u, double v) {
        BufferBuilder buf = net.minecraft.client.renderer.Tessellator.getInstance().getBuffer();
        buf.pos(x + xOffset, y + yOffset, z + zOffset).tex(u, v);
        if (hasColor) buf.color(r, g, b, a);
        if (hasNormals) buf.normal(normalTestX, normalTestY, normalTestZ);
        buf.endVertex();
        vertexCount++;
    }

    /**
     * Добавляет вершину без текстурных координат.
     */
    public void addVertex(double x, double y, double z) {
        BufferBuilder buf = net.minecraft.client.renderer.Tessellator.getInstance().getBuffer();
        buf.pos(x + xOffset, y + yOffset, z + zOffset);
        if (hasColor) buf.color(r, g, b, a);
        if (hasNormals) buf.normal(normalTestX, normalTestY, normalTestZ);
        buf.endVertex();
        vertexCount++;
    }

    /**
     * Устанавливает нормаль для следующей вершины.
     */
    public void setNormal(float x, float y, float z) {
        this.hasNormals = true;
        normalTestX = x;
        normalTestY = y;
        normalTestZ = z;
    }

    /**
     * Устанавливает глобальное смещение для вершин.
     */
    public void setTranslation(double x, double y, double z) {
        this.xOffset = x;
        this.yOffset = y;
        this.zOffset = z;
    }

    /**
     * Добавляет смещение к текущему.
     */
    public void addTranslation(float x, float y, float z) {
        this.xOffset += x;
        this.yOffset += y;
        this.zOffset += z;
    }
}