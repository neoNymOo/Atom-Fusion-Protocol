package com.nymoo.afp.common.util;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class UtilPowerArmor {
    // Звуковые эффекты шагов сервоприводов
    private static final SoundEvent[] SERVO_SOUNDS = {
            SoundEvent.REGISTRY.getObject(new ResourceLocation("afp:servo_step1")),
            SoundEvent.REGISTRY.getObject(new ResourceLocation("afp:servo_step2")),
            SoundEvent.REGISTRY.getObject(new ResourceLocation("afp:servo_step3"))
    };

    // Reflection-поля для доступа к внутренним параметрам шага
    private static final Field nextStepDistanceField;
    private static final Field distanceWalkedOnStepModifiedField;

    static {
        // Инициализация reflection-полей
        nextStepDistanceField = ReflectionHelper.findField(Entity.class, "nextStepDistance", "field_70150_b");
        distanceWalkedOnStepModifiedField = ReflectionHelper.findField(Entity.class, "distanceWalkedOnStepModified", "field_82151_R");

        nextStepDistanceField.setAccessible(true);
        distanceWalkedOnStepModifiedField.setAccessible(true);
    }

    /**
     * Обрабатывает логику воспроизведения звуков шагов силовой брони
     * @param world Мир, в котором находится игрок
     * @param player Игрок в силовой броне
     */
    public static void handleStepSound(World world, EntityPlayer player) {
        if (world.isRemote || !player.onGround) return; // Только на серверной стороне и на земле

        try {
            // Получаем значения шаговых полей через reflection
            float nextStepDistance = nextStepDistanceField.getFloat(player);
            float distanceWalkedOnStepModified = distanceWalkedOnStepModifiedField.getFloat(player);

            // Используем NBT игрока для хранения состояния шагов
            NBTTagCompound data = player.getEntityData();
            String key = "afp_nextStepDistance";
            float lastStepDistance = data.getFloat(key);

            // Инициализация при первом вызове
            if (lastStepDistance == 0) {
                data.setFloat(key, nextStepDistance);
                return;
            }

            // Проверка условий для воспроизведения звука
            int px = MathHelper.floor(player.posX);
            int py = MathHelper.floor(player.posY - 0.2D);
            int pz = MathHelper.floor(player.posZ);
            IBlockState block = player.world.getBlockState(new BlockPos(px, py, pz));
            if (block.getMaterial() != Material.AIR && lastStepDistance <= distanceWalkedOnStepModified) {
                playServoStepSound(world, player.posX, player.posY, player.posZ);
            }

            // Обновление состояния шага
            data.setFloat(key, nextStepDistance);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void playServoStepSound(World world, double x, double y, double z) {
        int soundIndex = (int) (Math.random() * SERVO_SOUNDS.length);
        world.playSound(null, x, y, z, SERVO_SOUNDS[soundIndex], SoundCategory.PLAYERS, 0.55f, 1.0f);
    }
}