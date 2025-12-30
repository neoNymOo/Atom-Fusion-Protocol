package com.nymoo.afp.common.render.core;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Грань 3D модели (полигон).
 * Содержит информацию о вершинах, нормалях и текстурных координатах для рендеринга.
 */
public class Face {
    /**
     * Массив вершин, образующих грань
     */
    public Vertex[] vertices;
    /**
     * Массив нормалей для каждой вершины
     */
    public Vertex[] vertexNormals;
    /**
     * Нормаль всей грани (используется, если нет нормалей вершин)
     */
    public Vertex faceNormal;
    /**
     * Текстурные координаты (UV) для наложения текстуры
     */
    public TextureCoordinate[] textureCoordinates;

    private float averageU = 0F;
    private float averageV = 0F;
    private boolean hasTexture = false;
    private boolean hasNormals = false;

    /**
     * Инициализирует параметры грани.
     * Рассчитывает средние значения UV и проверяет наличие компонентов.
     */
    public void init() {
        if (textureCoordinates != null && textureCoordinates.length > 0) {
            hasTexture = true;
            float uSum = 0F;
            float vSum = 0F;
            for (TextureCoordinate tc : textureCoordinates) {
                uSum += tc.u;
                vSum += tc.v;
            }
            averageU = uSum / textureCoordinates.length;
            averageV = vSum / textureCoordinates.length;
        }

        if (vertexNormals != null && vertexNormals.length > 0) {
            hasNormals = true;
        }
    }

    /**
     * Добавляет грань в буфер тесселятора для отрисовки.
     *
     * @param tessellator Тесселятор для построения геометрии
     */
    @SideOnly(Side.CLIENT)
    public void addFaceForRender(Tessellator tessellator) {
        addFaceForRender(tessellator, 0.0000F);
    }

    /**
     * Добавляет грань в буфер тесселятора с смещением текстуры.
     *
     * @param tessellator   Тесселятор для построения геометрии
     * @param textureOffset Смещение текстурных координат
     */
    @SideOnly(Side.CLIENT)
    public void addFaceForRender(Tessellator tessellator, float textureOffset) {
        float offsetU, offsetV;

        for (int i = 0; i < vertices.length; ++i) {
            if (hasNormals) {
                tessellator.setNormal(vertexNormals[i].x, vertexNormals[i].y, vertexNormals[i].z);
            } else if (faceNormal != null) {
                tessellator.setNormal(faceNormal.x, faceNormal.y, faceNormal.z);
            }

            if (hasTexture) {
                offsetU = textureOffset;
                offsetV = textureOffset;

                if (textureCoordinates[i].u > averageU) offsetU = -offsetU;
                if (textureCoordinates[i].v > averageV) offsetV = -offsetV;

                tessellator.addVertexWithUV(vertices[i].x, vertices[i].y, vertices[i].z,
                        textureCoordinates[i].u + offsetU,
                        textureCoordinates[i].v + offsetV);
            } else {
                tessellator.addVertexWithUV(vertices[i].x, vertices[i].y, vertices[i].z, 0, 0);
            }
        }
    }

    /**
     * Добавляет грань в буфер с разделением по высоте (для анимации или эффектов).
     *
     * @param tessellator   Тесселятор для построения геометрии
     * @param textureOffset Смещение текстуры
     * @param splitHeight   Высота разделения модели
     * @param scale         Масштаб смещения
     */
    @SideOnly(Side.CLIENT)
    public void addFaceForRenderSplit(Tessellator tessellator, float textureOffset, float splitHeight, float scale) {
        float offsetU, offsetV;

        for (int i = 0; i < vertices.length; ++i) {
            if (hasNormals) {
                tessellator.setNormal(vertexNormals[i].x, vertexNormals[i].y, vertexNormals[i].z);
            }

            float yPos = vertices[i].y;
            float yOffset = (yPos >= splitHeight) ? scale : 0;

            if (hasTexture) {
                offsetU = textureOffset;
                offsetV = textureOffset;

                if (textureCoordinates[i].u > averageU) offsetU = -offsetU;
                if (textureCoordinates[i].v > averageV) offsetV = -offsetV;

                tessellator.addVertexWithUV(vertices[i].x, yPos + yOffset, vertices[i].z,
                        textureCoordinates[i].u + offsetU,
                        textureCoordinates[i].v + offsetV);
            } else {
                tessellator.addVertexWithUV(vertices[i].x, yPos + yOffset, vertices[i].z, 0, 0);
            }
        }
    }

    /**
     * Вычисляет нормаль грани на основе векторного произведения сторон.
     *
     * @return Нормализованный вектор нормали
     */
    public Vertex calculateFaceNormal() {
        Vec3 v1 = Vec3.createVectorHelper(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y, vertices[1].z - vertices[0].z);
        Vec3 v2 = Vec3.createVectorHelper(vertices[2].x - vertices[0].x, vertices[2].y - vertices[0].y, vertices[2].z - vertices[0].z);
        Vec3 normalVector = v1.crossProduct(v2).normalize();
        return new Vertex((float) normalVector.xCoord, (float) normalVector.yCoord, (float) normalVector.zCoord);
    }
}