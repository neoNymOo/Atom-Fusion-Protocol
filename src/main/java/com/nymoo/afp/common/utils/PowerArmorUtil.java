package com.nymoo.afp.common.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class PowerArmorUtil {
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
        if (world.isRemote) return; // Только на серверной стороне

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
            if (player.onGround && lastStepDistance <= distanceWalkedOnStepModified) {
                playServoStepSound(world, player.posX, player.posY, player.posZ);
            }

            // Обновление состояния шага
            data.setFloat(key, nextStepDistance);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Воспроизводит случайный звук шага сервопривода
     * @param world Мир для воспроизведения
     * @param x Координата X
     * @param y Координата Y
     * @param z Координата Z
     */
    public static void playServoStepSound(World world, double x, double y, double z) {
        // Выбираем случайный звук из доступных вариантов
        int soundIndex = (int) (Math.random() * SERVO_SOUNDS.length);
        // Воспроизводим звук для всех игроков в радиусе
        world.playSound(null, x, y, z, SERVO_SOUNDS[soundIndex], SoundCategory.PLAYERS, 0.55f, 1.0f);
    }

    /**
     * Проверяет, носит ли игрок силовую броню
     * @param stack Предмет для проверки
     * @return true если надет нагрудник силовой брони
     */
    public static boolean isWearingPowerArmor(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation regName = stack.getItem().getRegistryName();
        if (regName == null) {
            return false;
        }
        String path = regName.getPath();
        return path.startsWith("x03_") ||
                path.startsWith("x02_") ||
                path.startsWith("x01_") ||
                path.startsWith("t60_") ||
                path.startsWith("t51_") ||
                path.startsWith("t45_") ||
                path.startsWith("exo_");
    }
}