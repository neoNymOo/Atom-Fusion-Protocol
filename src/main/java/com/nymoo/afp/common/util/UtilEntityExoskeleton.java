package com.nymoo.afp.common.util;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.entity.EntityExoskeleton;
import com.nymoo.afp.common.item.ArmorExo;
import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.item.ItemFusionCore;
import net.minecraft.entity.Entity;
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
 * Утилитарный класс для операций с сущностью экзоскелета.
 * Предоставляет методы для установки/снятия частей, ядер синтеза, входа/выхода из экзоскелета.
 * Расширен методами для установки/снятия ядер синтеза на нагрудниках других игроков.
 *
 * В эту локальную копию добавлены ссылки на AFPConfig для конфигурируемых порогов дистанции,
 * угла и максимальной разрядки, а также корректная проверка наличия энергии.
 */
public class UtilEntityExoskeleton {
    /**
     * Звук клика экзоскелета
     */
    public static final SoundEvent EXO_CLICK_SOUND;
    /**
     * Звук включения питания
     */
    public static final SoundEvent POWERON_SOUND;
    /**
     * Набор слотов брони для эффективной итерации
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
     * Обрабатывает общее взаимодействие с экзоскелетом.
     * Делегирует установку или снятие в зависимости от предмета в руке.
     *
     * @param world       Мир, в котором происходит взаимодействие
     * @param player      Игрок, выполняющий взаимодействие
     * @param hand        Рука, используемая для взаимодействия
     * @param entity      Экзоскелет, с которым взаимодействуют
     * @param clickedSlot Кликнутый слот экипировки
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
     * Пытается установить часть брони в экзоскелет.
     * Проверяет совместимость и заменяет базовую часть 'exo'.
     *
     * @param world     Мир, в котором происходит установка
     * @param player    Игрок, устанавливающий часть
     * @param hand      Рука, используемая для установки
     * @param entity    Экзоскелет, в который устанавливают часть
     * @param slot      Слот для установки
     * @param heldStack Устанавливаемый предмет
     */
    private static void tryInstallPart(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot slot, ItemStack heldStack) {
        if (!(heldStack.getItem() instanceof IPowerArmor)) return;
        ItemArmor heldArmor = (ItemArmor) heldStack.getItem();
        if (heldArmor.armorType != slot) return;
        ItemStack slotStack = entity.getItemStackFromSlot(slot);
        if (slotStack.isEmpty()) return;
        if (!"exo".equals(((IPowerArmor) slotStack.getItem()).getPowerArmorType())) return;
        String newType = ((IPowerArmor) heldStack.getItem()).getPowerArmorType();
        if (!isArmorCompatible(entity, newType)) return;
        entity.setItemStackToSlot(slot, heldStack.copy());
        if (!player.isCreative()) {
            heldStack.shrink(1);
            if (heldStack.isEmpty()) {
                player.setHeldItem(hand, ItemStack.EMPTY);
            }
        }
        world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Пытается снять часть брони с экзоскелета.
     * Заменяет на базовую часть 'exo' и отдает игроку.
     *
     * @param world  Мир, в котором происходит снятие
     * @param player Игрок, снимающий часть
     * @param hand   Рука, используемая для снятия
     * @param entity Экзоскелет, с которого снимают часть
     * @param slot   Слот для снятия
     */
    private static void tryUninstallPart(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot slot) {
        ItemStack chestplateStack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplateStack.isEmpty()) return;
        if (hasBooleanTag(chestplateStack, "soldered")) {
            return;
        }
        ItemStack slotStack = entity.getItemStackFromSlot(slot);
        if (slotStack.isEmpty()) return;
        String armorType = ((IPowerArmor) slotStack.getItem()).getPowerArmorType();
        if ("exo".equals(armorType)) {
            return;
        }
        if (!player.getHeldItem(hand).isEmpty()) return;
        player.setHeldItem(hand, slotStack.copy());
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
        world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Пытается войти в экзоскелет.
     * Передает броню игроку, воспроизводит звук при наличии питания, обновляет позицию, удаляет сущность.
     * Вызывается из сетевого обработчика на сервере.
     *
     * @param world  Мир, в котором происходит вход
     * @param player Игрок, входящий в экзоскелет
     * @param entity Экзоскелет, в который входят
     */
    public static void tryEnterExoskeleton(World world, EntityPlayer player, EntityExoskeleton.Exoskeleton entity) {
        if (player.isSneaking()) return;
        double distanceSquared = player.getDistanceSq(entity);
        // Используем расстояние из конфигурации (квадрат расстояния)
        if (distanceSquared > AFPConfig.exoskeletonEntryDistance) return;
        float yawDifference = MathHelper.wrapDegrees(player.rotationYaw - entity.rotationYaw);
        // Используем предельный угол из конфигурации
        if (yawDifference <= -AFPConfig.exoskeletonEntryYaw || yawDifference >= AFPConfig.exoskeletonEntryYaw) return;
        // игрок должен снять всю броню перед входом
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            if (!player.getItemStackFromSlot(slot).isEmpty()) return;
        }
        // переносим все предметы экзоскелета на игрока
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            player.setItemStackToSlot(slot, entity.getItemStackFromSlot(slot).copy());
        }
        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        // если в нагруднике есть тег fusion_depletion и его значение меньше максимального порога, воспроизводим звук включения
        if (!chestplateStack.isEmpty() && chestplateStack.hasTagCompound()) {
            NBTTagCompound tag = chestplateStack.getTagCompound();
            if (tag.hasKey("fusion_depletion") && tag.getFloat("fusion_depletion") < AFPConfig.maxDepletion) {
                world.playSound(null, entity.posX, entity.posY, entity.posZ, POWERON_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
        player.rotationYaw = entity.rotationYaw;
        player.rotationPitch = entity.rotationPitch;
        player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
        entity.setDead();
    }

    /**
     * Пытается выйти из экзоскелета.
     * Создает новую сущность экзоскелета, передает броню, обновляет позицию игрока.
     * Вызывается из обработчика клавиши на сервере.
     *
     * @param world       Мир, в котором происходит выход
     * @param player      Игрок, выходящий из экзоскелета
     * @param isDeathExit Флаг выхода по причине смерти
     */
    public static void tryExitExoskeleton(World world, EntityPlayer player, Boolean isDeathExit) {
        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplate.isEmpty() || !(chestplate.getItem() instanceof IPowerArmor)) return;
        if (!isDeathExit) {
            if (player.isRiding()) return;
            if (!isSpaceBehindPlayerClear(world, player, player.getHorizontalFacing())) return;
        }
        EntityExoskeleton.Exoskeleton entity = new EntityExoskeleton.Exoskeleton(world);
        entity.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        if (!isDeathExit) {
            EnumFacing opposite = player.getHorizontalFacing().getOpposite();
            player.posX += opposite.getXOffset() * 0.5D;
            player.posZ += opposite.getZOffset() * 0.5D;
            player.setPositionAndUpdate(player.posX, player.posY, player.posZ);
        }
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            entity.setItemStackToSlot(slot, player.getItemStackFromSlot(slot).copy());
            player.setItemStackToSlot(slot, ItemStack.EMPTY);
        }
        world.spawnEntity(entity);
    }

    /**
     * Пытается установить ядро синтеза в нагрудник экзоскелета.
     * Передает энергию и воспроизводит звук.
     *
     * @param world       Мир, в котором происходит установка
     * @param player      Игрок, устанавливающий ядро
     * @param hand        Рука, используемая для установки
     * @param entity      Экзоскелет, в который устанавливают ядро
     * @param clickedSlot Кликнутый слот
     */
    public static void tryInstallFusionCore(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot clickedSlot) {
        ItemStack chestplate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplate.isEmpty()) return;
        ItemStack heldStack = player.getHeldItem(hand);
        if (heldStack.isEmpty() || heldStack.getItem() != ItemFusionCore.itemFusionCore) return;
        float energy = 0;
        NBTTagCompound heldTag = heldStack.getTagCompound();
        if (heldTag != null && heldTag.hasKey("fusion_depletion")) {
            energy = heldTag.getFloat("fusion_depletion");
        }
        NBTTagCompound chestTag = chestplate.getTagCompound();
        if (chestTag == null) {
            chestTag = new NBTTagCompound();
            chestplate.setTagCompound(chestTag);
        }
        chestTag.setFloat("fusion_depletion", energy);
        heldStack.shrink(1);
        if (heldStack.isEmpty()) {
            player.setHeldItem(hand, ItemStack.EMPTY);
        }
        world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Пытается снять ядро синтеза с нагрудника экзоскелета.
     * Создает новый предмет ядра и воспроизводит звук.
     *
     * @param world       Мир, в котором происходит снятие
     * @param player      Игрок, снимающий ядро
     * @param hand        Рука, используемая для снятия
     * @param entity      Экзоскелет, с которого снимают ядро
     * @param clickedSlot Кликнутый слот
     */
    public static void tryUninstallFusionCore(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot clickedSlot) {
        ItemStack chestplate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplate.isEmpty()) return;
        NBTTagCompound chestTag = chestplate.getTagCompound();
        if (chestTag == null) return;
        float energy = chestTag.getFloat("fusion_depletion");
        ItemStack newCore = new ItemStack(ItemFusionCore.itemFusionCore);
        NBTTagCompound newTag = new NBTTagCompound();
        newTag.setFloat("fusion_depletion", energy);
        newCore.setTagCompound(newTag);
        player.setHeldItem(EnumHand.MAIN_HAND, newCore);
        chestTag.removeTag("fusion_depletion");
        world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Пытается установить ядро синтеза в нагрудник другого игрока.
     * Передает энергию и воспроизводит звук на позиции цели.
     *
     * @param world  Мир, в котором происходит установка
     * @param player Игрок, устанавливающий ядро
     * @param hand   Рука, используемая для установки
     * @param target Игрок-цель для установки ядра
     */
    public static void tryInstallFusionCoreOnPlayer(World world, EntityPlayer player, EnumHand hand, EntityPlayer target) {
        ItemStack chestplate = target.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplate.isEmpty()) return;
        ItemStack heldStack = player.getHeldItem(hand);
        if (heldStack.isEmpty() || heldStack.getItem() != ItemFusionCore.itemFusionCore) return;
        float energy = 0;
        NBTTagCompound heldTag = heldStack.getTagCompound();
        if (heldTag != null && heldTag.hasKey("fusion_depletion")) {
            energy = heldTag.getFloat("fusion_depletion");
        }
        NBTTagCompound chestTag = chestplate.getTagCompound();
        if (chestTag == null) {
            chestTag = new NBTTagCompound();
            chestplate.setTagCompound(chestTag);
        }
        chestTag.setFloat("fusion_depletion", energy);
        heldStack.shrink(1);
        if (heldStack.isEmpty()) {
            player.setHeldItem(hand, ItemStack.EMPTY);
        }
        world.playSound(null, target.posX, target.posY, target.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Пытается снять ядро синтеза с нагрудника другого игрока.
     * Создает новый предмет ядра, отдает игроку и воспроизводит звук на позиции цели.
     *
     * @param world  Мир, в котором происходит снятие
     * @param player Игрок, снимающий ядро
     * @param hand   Рука, используемая для снятия
     * @param target Игрок-цель для снятия ядра
     */
    public static void tryUninstallFusionCoreFromPlayer(World world, EntityPlayer player, EnumHand hand, EntityPlayer target) {
        ItemStack chestplate = target.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplate.isEmpty()) return;
        NBTTagCompound chestTag = chestplate.getTagCompound();
        if (chestTag == null) return;
        float energy = chestTag.getFloat("fusion_depletion");
        ItemStack newCore = new ItemStack(ItemFusionCore.itemFusionCore);
        NBTTagCompound newTag = new NBTTagCompound();
        newTag.setFloat("fusion_depletion", energy);
        newCore.setTagCompound(newTag);
        player.setHeldItem(EnumHand.MAIN_HAND, newCore);
        chestTag.removeTag("fusion_depletion");
        world.playSound(null, target.posX, target.posY, target.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Проверяет совместимость нового типа брони с существующими частями.
     * Все части не типа 'exo' должны соответствовать новому типу.
     *
     * @param entity  Экзоскелет для проверки
     * @param newType Новый тип брони
     * @return true если совместимо
     */
    private static boolean isArmorCompatible(EntityExoskeleton.Exoskeleton entity, String newType) {
        if ("exo".equals(newType)) return true;
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorStack = entity.getItemStackFromSlot(slot);
            if (armorStack.isEmpty()) continue;
            String armorType = ((IPowerArmor) armorStack.getItem()).getPowerArmorType();
            if ("exo".equals(armorType) || armorType.equals(newType)) continue;
            return false;
        }
        return true;
    }

    /**
     * Проверяет наличие свободного места позади игрока для выхода.
     *
     * @param world  Мир для проверки
     * @param player Игрок для проверки
     * @param facing Направление взгляда игрока
     * @return true если место свободно
     */
    public static boolean isSpaceBehindPlayerClear(World world, EntityPlayer player, EnumFacing facing) {
        BlockPos lowerPos = new BlockPos(player).offset(facing.getOpposite());
        BlockPos upperPos = lowerPos.up();
        return world.isBlockLoaded(lowerPos) &&
                world.isBlockLoaded(upperPos) &&
                !world.getBlockState(lowerPos).causesSuffocation() &&
                !world.getBlockState(upperPos).causesSuffocation();
    }

    /**
     * Проверяет наличие булевого NBT-тега на предмете.
     *
     * @param stack   Предмет для проверки
     * @param tagName Имя тега
     * @return true если тег присутствует и имеет значение true
     */
    public static boolean hasBooleanTag(ItemStack stack, String tagName) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(tagName);
    }
}