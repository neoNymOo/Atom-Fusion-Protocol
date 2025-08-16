package com.nymoo.afp.common.util;

import com.nymoo.afp.common.entity.EntityExoskeleton;
import com.nymoo.afp.common.item.ArmorExo;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Утилитный класс для работы с экзоскелетом.
 * Содержит методы для установки/снятия частей брони, входа/выхода в/из экзоскелет(а),
 */
public class UtilEntityExoskeleton {

    /**
     * Звук щелчка при установке/снятии части экзоскелета
     */
    public static final SoundEvent EXO_CLICK_SOUND;
    /**
     * Звук включения при входе в экзоскелет с ядерным блоком
     */
    public static final SoundEvent POWERON_SOUND;

    /**
     * Набор слотов брони для оптимизации циклов (избегает проверки всех слотов EntityEquipmentSlot)
     */
    private static final EnumSet<EntityEquipmentSlot> ARMOR_SLOTS = EnumSet.of(
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET
    );

    static {
        EXO_CLICK_SOUND = SoundEvent.REGISTRY.getObject(new ResourceLocation("afp", "exo_click"));
        POWERON_SOUND = SoundEvent.REGISTRY.getObject(new ResourceLocation("afp", "poweron"));
    }

    /**
     * Главный метод обработки взаимодействия игрока с экзоскелетом.
     * Если в руке предмет — пытаемся установить его в экзоскелет.
     * Если рука пустая — пытаемся снять часть брони с экзоскелета.
     */
    public static void handleInteraction(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot clickedSlot) {
        ItemStack heldStack = player.getHeldItem(hand);
        if (!heldStack.isEmpty()) {
            tryInstallPart(world, player, hand, entity, clickedSlot, heldStack);
        } else {
            tryUninstallPart(world, player, hand, entity, clickedSlot);
        }
    }

    /**
     * Установка брони в экзоскелет.
     * Проверяет: тип предмета (должен быть IPowerArmor), соответствие слота, наличие базовой "exo"-брони в слоте,
     * совместимость типов с уже установленной броней. Если все проверки пройдены, устанавливает копию предмета,
     * уменьшает стек в руке (если не креатив) и воспроизводит звук.
     */
    private static void tryInstallPart(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot slot, ItemStack heldStack) {
        // Предмет должен быть силовой броней (IPowerArmor)
        if (!(heldStack.getItem() instanceof IPowerArmor)) {
            return;
        }
        ItemArmor heldArmor = (ItemArmor) heldStack.getItem();

        // Слот брони должен совпадать с типом предмета
        if (heldArmor.armorType != slot) {
            return;
        }

        // Получаем текущий предмет в слоте экзоскелета
        ItemStack slotStack = entity.getItemStackFromSlot(slot);

        // Слот не должен быть пустым и должен содержать базовую "exo"-броню
        if (slotStack.isEmpty() || !"exo".equals(getArmorType(slotStack))) {
            return;
        }

        // Проверяем совместимость новой брони с уже установленными частями
        String newType = getArmorType(heldStack);
        if (!isArmorCompatible(entity, newType)) {
            return;
        }

        // Устанавливаем копию брони в экзоскелет
        entity.setItemStackToSlot(slot, heldStack.copy());

        // Если игрок не в креативе, уменьшаем стек в руке
        if (!player.isCreative()) {
            heldStack.shrink(1);
            if (heldStack.isEmpty()) {
                player.setHeldItem(hand, ItemStack.EMPTY);
            }
        }

        // Воспроизводим звук установки
        world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Снятие брони с экзоскелета.
     * Если нагрудник припаян ("soldered"), то снятие невозможно, и для слота CHEST выполняется попытка входа в экзоскелет.
     * Если броня базовая "exo", то для слота CHEST проверяется, все ли слоты базовые, и если да — попытка входа.
     * В противном случае, перемещает броню в пустую руку игрока и заменяет на базовую "exo"-броню.
     */
    private static void tryUninstallPart(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot slot) {
        // Проверяем наличие нагрудника в экзоскелете
        ItemStack chestplateStack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplateStack.isEmpty()) {
            return;
        }

        // Если нагрудник припаян, запрещаем снятие и пытаемся войти (только для клика по CHEST)
        if (hasBooleanTag(chestplateStack, "soldered")) {
            if (slot == EntityEquipmentSlot.CHEST) {
                tryEnterExoskeleton(world, player, entity);
            }
            return;
        }

        // Получаем предмет из слота экзоскелета
        ItemStack slotStack = entity.getItemStackFromSlot(slot);
        if (slotStack.isEmpty()) {
            return;
        }

        // Если это базовая "exo"-броня, проверяем возможность входа (для CHEST, если все слоты "exo")
        String armorType = getArmorType(slotStack);
        if ("exo".equals(armorType)) {
            if (slot == EntityEquipmentSlot.CHEST) {
                boolean allExo = true;
                for (EntityEquipmentSlot armorSlot : ARMOR_SLOTS) {
                    ItemStack stack = entity.getItemStackFromSlot(armorSlot);
                    if (stack.isEmpty() || !"exo".equals(getArmorType(stack))) {
                        allExo = false;
                        break;
                    }
                }
                if (allExo) {
                    tryEnterExoskeleton(world, player, entity);
                }
            }
            return;
        }

        // Рука игрока должна быть пустой для снятия
        if (!player.getHeldItem(hand).isEmpty()) {
            return;
        }

        // Перемещаем копию предмета в руку игрока
        player.setHeldItem(hand, slotStack.copy());

        // Заменяем в слоте экзоскелета на базовую "exo"-броню
        ItemStack exoItem = ItemStack.EMPTY;
        switch (slot) {
            case HEAD:
                exoItem = new ItemStack(ArmorExo.helmet);
                break;
            case CHEST:
                exoItem = new ItemStack(ArmorExo.chestplate);
                break;
            case LEGS:
                exoItem = new ItemStack(ArmorExo.leggings);
                break;
            case FEET:
                exoItem = new ItemStack(ArmorExo.boots);
                break;
        }
        entity.setItemStackToSlot(slot, exoItem);

        // Воспроизводим звук снятия
        world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Вход в экзоскелет.
     * Проверяет расстояние (не более 1 блока) и угол обзора (игрок должен быть в 110-градусном секторе спереди экзоскелета).
     * Если проверки пройдены, копирует всю броню на игрока, синхронизирует позицию и поворот, воспроизводит звук (если есть core)
     * и удаляет сущность экзоскелета.
     */
    private static void tryEnterExoskeleton(World world, EntityPlayer player, EntityExoskeleton.Exoskeleton entity) {
        // Проверка расстояния (не более 1 блока)
        double distance = player.getDistance(entity);
        if (distance > 1.0D) {
            return;
        }

        // Проверка угла (игрок в 110-градусном секторе спереди: разница yaw в пределах -55..55 градусов)
        float yawDiff = MathHelper.wrapDegrees(player.rotationYaw - entity.rotationYaw);
        if (yawDiff <= -55.0F || yawDiff >= 55.0F) {
            return;
        }

        // Копируем броню с экзоскелета на игрока
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            player.setItemStackToSlot(slot, entity.getItemStackFromSlot(slot).copy());
        }

        // Если в нагруднике есть ядерный блок ("core"), воспроизводим звук включения
        if (hasBooleanTag(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), "core")) {
            world.playSound(null, entity.posX, entity.posY, entity.posZ, POWERON_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        // Синхронизируем поворот и позицию игрока с экзоскелетом
        player.rotationYaw = entity.rotationYaw;
        player.rotationPitch = entity.rotationPitch;
        player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);

        // Удаляем сущность экзоскелета
        entity.setDead();
    }

    /**
     * Выход из экзоскелета (плановый или при смерти игрока).
     * Проверяет, что нагрудник — силовая броня и игрок не верхом. Затем проверяет свободное пространство позади.
     * Если все ок, создает сущность экзоскелета, перемещает игрока вперед, переносит броню и спавнит сущность.
     */
    public static void tryExitExoskeleton(World world, EntityPlayer player, Boolean isDeathExit) {
        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        // Проверка: нагрудник должен быть силовой броней и игрок не верхом
        if (chestplate.isEmpty() || !(chestplate.getItem() instanceof IPowerArmor)) {
            return;
        }

        //Проверка свободного пространства позади игрока и он не всадник
        if (!isDeathExit) {
            if (player.isRiding()) return;
            if (!isSpaceBehindPlayerClear(world, player, player.getHorizontalFacing())) return;
        }

        // Создаем новую сущность экзоскелета на позиции игрока
        EntityExoskeleton.Exoskeleton entity = new EntityExoskeleton.Exoskeleton(world);
        entity.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);

        // Смещаем игрока вперед, чтобы освободить место для экзоскелета
        if (!isDeathExit) {
            EnumFacing opposite = player.getHorizontalFacing().getOpposite();
            player.posX += opposite.getXOffset() * 0.5D;
            player.posZ += opposite.getZOffset() * 0.5D;
            player.setPositionAndUpdate(player.posX, player.posY, player.posZ);
        }

        // Переносим броню с игрока на экзоскелет и очищаем слоты игрока
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            entity.setItemStackToSlot(slot, player.getItemStackFromSlot(slot).copy());
            player.setItemStackToSlot(slot, ItemStack.EMPTY);
        }

        // Спавним сущность экзоскелета в мире
        world.spawnEntity(entity);
    }

    /**
     * Проверка совместимости брони по типам.
     * Все установленные части (кроме базовых "exo") должны иметь один и тот же тип.
     */
    private static boolean isArmorCompatible(EntityExoskeleton.Exoskeleton entity, String newType) {
        if ("exo".equals(newType)) {
            return true;
        }
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorStack = entity.getItemStackFromSlot(slot);
            if (armorStack.isEmpty()) {
                continue;
            }
            String armorType = getArmorType(armorStack);
            if ("exo".equals(armorType) || armorType.equals(newType)) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Проверка свободного пространства позади игрока (2 блока: нижний и верхний).
     * Блоки должны быть загружены и не вызывать удушье.
     */
    private static boolean isSpaceBehindPlayerClear(World world, EntityPlayer player, EnumFacing facing) {
        BlockPos lowerPos = new BlockPos(player).offset(facing.getOpposite());
        BlockPos upperPos = lowerPos.up();
        return world.isBlockLoaded(lowerPos) &&
                world.isBlockLoaded(upperPos) &&
                !world.getBlockState(lowerPos).causesSuffocation() &&
                !world.getBlockState(upperPos).causesSuffocation();
    }

    /**
     * Получает тип брони из имени предмета (первая часть registry name до "_").
     * Например: "exo_helmet" -> "exo". Если стек пустой, возвращает пустую строку.
     */
    public static String getArmorType(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        String path = stack.getItem().getRegistryName().getPath();
        int underscoreIndex = path.indexOf('_');
        return underscoreIndex > 0 ? path.substring(0, underscoreIndex) : path;
    }

    /**
     * Универсальный метод проверки наличия булевого тега в NBT предмета.
     * Возвращает false, если NBT отсутствует или тег не true.
     */
    private static boolean hasBooleanTag(ItemStack stack, String tagName) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(tagName);
    }
}