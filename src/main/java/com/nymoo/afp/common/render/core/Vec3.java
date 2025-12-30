package com.nymoo.afp.common.render.core;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Вектор в трехмерном пространстве.
 * Используется для математических вычислений (нормали, позиции, направления).
 */
public class Vec3 {
    public double xCoord;
    public double yCoord;
    public double zCoord;

    public Vec3(Vec3d vec) {
        this.xCoord = vec.x;
        this.yCoord = vec.y;
        this.zCoord = vec.z;
    }

    public Vec3(BlockPos vec) {
        this.xCoord = vec.getX();
        this.yCoord = vec.getY();
        this.zCoord = vec.getZ();
    }

    public Vec3(double x, double y, double z) {
        this.xCoord = x == -0.0D ? 0.0D : x;
        this.yCoord = y == -0.0D ? 0.0D : y;
        this.zCoord = z == -0.0D ? 0.0D : z;
    }

    public static Vec3 createVectorHelper(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    public static Vec3 createVectorHelper(Entity e) {
        return new Vec3(e.posX, e.posY, e.posZ);
    }

    public Vec3 setComponents(double x, double y, double z) {
        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
        return this;
    }

    public Vec3 subtract(Vec3 other) {
        return createVectorHelper(this.xCoord - other.xCoord, this.yCoord - other.yCoord, this.zCoord - other.zCoord);
    }

    /**
     * Нормализует вектор (приводит длину к 1).
     */
    public Vec3 normalize() {
        double d0 = MathHelper.sqrt(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
        return d0 < 1.0E-4D ? createVectorHelper(0.0D, 0.0D, 0.0D) : createVectorHelper(this.xCoord / d0, this.yCoord / d0, this.zCoord / d0);
    }

    /**
     * Вычисляет векторное произведение двух векторов.
     */
    public Vec3 crossProduct(Vec3 other) {
        return createVectorHelper(this.yCoord * other.zCoord - this.zCoord * other.yCoord, this.zCoord * other.xCoord - this.xCoord * other.zCoord, this.xCoord * other.yCoord - this.yCoord * other.xCoord);
    }

    public double dotProduct(Vec3 v) {
        return this.xCoord * v.xCoord + this.yCoord * v.yCoord + this.zCoord * v.zCoord;
    }

    public Vec3 addVector(double x, double y, double z) {
        return createVectorHelper(this.xCoord + x, this.yCoord + y, this.zCoord + z);
    }

    public double distanceTo(Vec3 v) {
        return MathHelper.sqrt(squareDistanceTo(v));
    }

    public double squareDistanceTo(Vec3 v) {
        return (v.xCoord - xCoord) * (v.xCoord - xCoord) + (v.yCoord - yCoord) * (v.yCoord - yCoord) + (v.zCoord - zCoord) * (v.zCoord - zCoord);
    }

    public String toString() {
        return "(" + this.xCoord + ", " + this.yCoord + ", " + this.zCoord + ")";
    }
}