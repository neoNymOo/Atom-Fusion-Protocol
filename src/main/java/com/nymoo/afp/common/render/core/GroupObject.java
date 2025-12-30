package com.nymoo.afp.common.render.core;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

/**
 * Группа объектов модели.
 * Объединяет набор граней под одним именем для раздельного рендеринга частей (например, "Рука", "Шлем").
 */
@SideOnly(Side.CLIENT)
public class GroupObject {
    /**
     * Имя группы объектов
     */
    public String name;
    /**
     * Список граней, входящих в группу
     */
    public ArrayList<Face> faces = new ArrayList<Face>();
    /**
     * Режим отрисовки OpenGL (GL_TRIANGLES или GL_QUADS)
     */
    public int glDrawingMode;

    private VertexBuffer vbo;
    private boolean vboInitialized = false;

    public GroupObject() {
        this("");
    }

    public GroupObject(String name) {
        this(name, -1);
    }

    public GroupObject(String name, int glDrawingMode) {
        this.name = name;
        this.glDrawingMode = glDrawingMode;
    }

    /**
     * Отрисовывает группу объектов.
     * Использует VBO для оптимизации, если он скомпилирован, иначе использует прямой рендеринг.
     */
    @SideOnly(Side.CLIENT)
    public void render() {
        if (faces.isEmpty()) return;

        if (!vboInitialized) {
            compileVBO();
        }

        if (vbo != null) {
            GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GlStateManager.glEnableClientState(GL11.GL_NORMAL_ARRAY);

            vbo.bindBuffer();

            // Настройка указателей вершин с использованием GL11 для корректной передачи смещений
            // Шаг (Stride) для формата POSITION_TEX_NORMAL составляет 24 байта
            GL11.glVertexPointer(3, GL11.GL_FLOAT, 24, 0L);
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 24, 12L);
            GL11.glNormalPointer(GL11.GL_BYTE, 24, 20L);

            vbo.drawArrays(glDrawingMode);

            vbo.unbindBuffer();

            GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GlStateManager.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        } else {
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawing(glDrawingMode);
            render(tessellator);
            tessellator.draw();
        }
    }

    /**
     * Компилирует геометрию группы в Vertex Buffer Object (VBO).
     * Повышает производительность рендеринга статических моделей.
     */
    @SideOnly(Side.CLIENT)
    private void compileVBO() {
        net.minecraft.client.renderer.Tessellator mcTessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.BufferBuilder buffer = mcTessellator.getBuffer();

        buffer.begin(glDrawingMode, DefaultVertexFormats.POSITION_TEX_NORMAL);

        Tessellator tessellator = Tessellator.instance;
        // Сброс состояния тесселятора перед заполнением буфера
        tessellator.hasColor = false;
        tessellator.hasNormals = false;
        tessellator.setTranslation(0, 0, 0);

        for (Face face : faces) {
            face.addFaceForRender(tessellator);
        }

        buffer.finishDrawing();

        this.vbo = new VertexBuffer(DefaultVertexFormats.POSITION_TEX_NORMAL);
        this.vbo.bufferData(buffer.getByteBuffer());

        this.vboInitialized = true;

        buffer.reset();
    }

    /**
     * Отрисовывает группу через переданный тесселятор.
     *
     * @param tessellator Тесселятор для построения геометрии
     */
    @SideOnly(Side.CLIENT)
    public void render(Tessellator tessellator) {
        if (faces.size() > 0) {
            for (Face face : faces) {
                face.addFaceForRender(tessellator);
            }
        }
    }

    /**
     * Отрисовывает группу с разделением по высоте (обертка).
     *
     * @param splitHeight Высота разделения
     * @param scale       Масштаб смещения
     */
    @SideOnly(Side.CLIENT)
    public void renderSplit(float splitHeight, float scale) {
        if (faces.size() > 0) {
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawing(glDrawingMode);
            renderSplit(tessellator, splitHeight, scale);
            tessellator.draw();
        }
    }

    /**
     * Отрисовывает группу с разделением по высоте через тесселятор.
     *
     * @param tessellator Тесселятор для построения геометрии
     * @param splitHeight Высота разделения
     * @param scale       Масштаб смещения
     */
    @SideOnly(Side.CLIENT)
    public void renderSplit(Tessellator tessellator, float splitHeight, float scale) {
        if (faces.size() > 0) {
            for (Face face : faces) {
                face.addFaceForRenderSplit(tessellator, 0F, splitHeight, scale);
            }
        }
    }

    /**
     * Освобождает ресурсы OpenGL (удаляет VBO).
     */
    public void deleteGlResources() {
        if (vbo != null) {
            vbo.deleteGlBuffers();
            vbo = null;
            vboInitialized = false;
        }
    }
}