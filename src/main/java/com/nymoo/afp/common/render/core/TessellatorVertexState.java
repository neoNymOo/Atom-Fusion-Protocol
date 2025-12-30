package com.nymoo.afp.common.render.core;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Состояние вершин тесселятора.
 * Хранит снимок буфера и флагов для последующего восстановления.
 */
@SideOnly(Side.CLIENT)
public class TessellatorVertexState {
    private final int[] rawBuffer;
    private final int rawBufferIndex;
    private final int vertexCount;
    private final boolean hasTexture;
    private final boolean hasBrightness;
    private final boolean hasNormals;
    private final boolean hasColor;

    public TessellatorVertexState(int[] buf, int idx, int count, boolean tex, boolean bright, boolean norm, boolean col) {
        this.rawBuffer = buf;
        this.rawBufferIndex = idx;
        this.vertexCount = count;
        this.hasTexture = tex;
        this.hasBrightness = bright;
        this.hasNormals = norm;
        this.hasColor = col;
    }

    public int[] getRawBuffer() {
        return this.rawBuffer;
    }

    public int getRawBufferIndex() {
        return this.rawBufferIndex;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public boolean getHasTexture() {
        return this.hasTexture;
    }

    public boolean getHasBrightness() {
        return this.hasBrightness;
    }

    public boolean getHasNormals() {
        return this.hasNormals;
    }

    public boolean getHasColor() {
        return this.hasColor;
    }
}