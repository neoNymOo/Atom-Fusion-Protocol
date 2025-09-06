package com.nymoo.afp.common.util;

import com.nymoo.afp.common.entity.EntityExoskeleton;
import com.nymoo.afp.common.item.ArmorExo;
import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.item.ItemFusionCore;
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
 * Включает несколько небольших оптимизаций:
 *  - ARMOR_SLOTS как EnumSet (минимизация перебора всех EntityEquipmentSlot),
 *  - ранние выходы при несоответствии условий,
 *  - минимальное копирование ItemStack'ов (копируем только при реальной передаче).
 *
 * Поведение сохранено идентичным.
 */
public class UtilEntityExoskeleton {
    public static final SoundEvent EXO_CLICK_SOUND;
    public static final SoundEvent POWERON_SOUND;

    // Ограничиваемся 4 слотами брони: HEAD, CHEST, LEGS, FEET
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
     * Обработка взаимодействия: либо установка части, либо снятие.
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
     * Делаем быстрые проверки и только при успехе — копирование и модификация стека.
     */
    private static void tryInstallPart(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot slot, ItemStack heldStack) {
        if (!(heldStack.getItem() instanceof IPowerArmor)) return;

        ItemArmor heldArmor = (ItemArmor) heldStack.getItem();
        if (heldArmor.armorType != slot) return;

        ItemStack slotStack = entity.getItemStackFromSlot(slot);
        if (slotStack.isEmpty()) return;

        // Слот должен быть базовой "exo"-бронёй
        if (!"exo".equals(((IPowerArmor) slotStack.getItem()).getPowerArmorType())) return;

        String newType = ((IPowerArmor) heldStack.getItem()).getPowerArmorType();
        if (!isArmorCompatible(entity, newType)) return;

        // Ставим копию предмета в слот экзоскелета
        entity.setItemStackToSlot(slot, heldStack.copy());

        // Уменьшаем стек в руке (если не креатив)
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
     * Снятие части брони с экзоскелета.
     * Сохранена логика: если нагрудник soldered — попытка входа, если базовая exo — проверяем возможность входа.
     */
    private static void tryUninstallPart(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot slot) {
        ItemStack chestplateStack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplateStack.isEmpty()) return;

        // Если нагрудник припаян — запрещаем снятие (и при клике по CHEST пробуем войти).
        if (hasBooleanTag(chestplateStack, "soldered")) {
            if (slot == EntityEquipmentSlot.CHEST) {
                tryEnterExoskeleton(world, player, entity);
            }
            return;
        }

        ItemStack slotStack = entity.getItemStackFromSlot(slot);
        if (slotStack.isEmpty()) return;

        String armorType = ((IPowerArmor) slotStack.getItem()).getPowerArmorType();
        if ("exo".equals(armorType)) {
            // Для CHEST проверяем, все ли слоты exo -> тогда попытка входа
            if (slot == EntityEquipmentSlot.CHEST) {
                boolean allExo = true;
                for (EntityEquipmentSlot armorSlot : ARMOR_SLOTS) {
                    ItemStack stack = entity.getItemStackFromSlot(armorSlot);
                    if (stack.isEmpty() || !"exo".equals(((IPowerArmor) stack.getItem()).getPowerArmorType())) {
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

        // Рука должна быть пуста
        if (!player.getHeldItem(hand).isEmpty()) return;

        // Перемещаем копию предмета в руку игрока
        player.setHeldItem(hand, slotStack.copy());

        // Заменяем в слоте на базовую exo-часть
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
     * Попытка входа в экзоскелет (копируем броню на игрока и убираем сущность).
     * Сохранены все проверки (расстояние, угол, пустые слоты у игрока).
     */
    private static void tryEnterExoskeleton(World world, EntityPlayer player, EntityExoskeleton.Exoskeleton entity) {
        if (player.isSneaking()) return;

        // Проверка дистанции (squared)
        double distanceSq = player.getDistanceSq(entity);
        if (distanceSq > 1.0D) return;

        // Угол
        float yawDiff = MathHelper.wrapDegrees(player.rotationYaw - entity.rotationYaw);
        if (yawDiff <= -55.0F || yawDiff >= 55.0F) return;

        // Проверка, что у игрока нет брони
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            if (!player.getItemStackFromSlot(slot).isEmpty()) return;
        }

        // Копируем броню с экзоскелета на игрока
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            player.setItemStackToSlot(slot, entity.getItemStackFromSlot(slot).copy());
        }

        // Если в нагруднике есть fusion core — проигрываем звук включения
        if (hasBooleanTag(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), "fusion_energy")) {
            world.playSound(null, entity.posX, entity.posY, entity.posZ, POWERON_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        // Синхронизация позиции/вращения и удаление сущности
        player.rotationYaw = entity.rotationYaw;
        player.rotationPitch = entity.rotationPitch;
        player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
        entity.setDead();
    }

    /**
     * Выход из экзоскелета: создаём сущность экзоскелета и переносим броню.
     * Логика сохранена.
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
     * Установка ядерного блока (fusion core) в нагрудник экзоскелета.
     */
    public static void tryInstallFusionCore(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot clickedSlot) {
        ItemStack chestplate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplate.isEmpty()) return;

        ItemStack heldStack = player.getHeldItem(hand);
        if (heldStack.isEmpty() || heldStack.getItem() != ItemFusionCore.itemFusionCore) return;

        int energy = 0;
        NBTTagCompound heldTag = heldStack.getTagCompound();
        if (heldTag != null && heldTag.hasKey("fusion_energy")) {
            energy = heldTag.getInteger("fusion_energy");
        }

        NBTTagCompound chestTag = chestplate.getTagCompound();
        if (chestTag == null) {
            chestTag = new NBTTagCompound();
            chestplate.setTagCompound(chestTag);
        }
        chestTag.setInteger("fusion_energy", energy);

        // Уменьшаем стек в руке
        heldStack.shrink(1);
        if (heldStack.isEmpty()) {
            player.setHeldItem(hand, ItemStack.EMPTY);
        }

        world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Снятие ядерного блока из нагрудника экзоскелета (перенос в руку игрока).
     */
    public static void tryUninstallFusionCore(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot clickedSlot) {
        ItemStack chestplate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestplate.isEmpty()) return;

        NBTTagCompound chestTag = chestplate.getTagCompound();
        if (chestTag == null) return;

        int energy = chestTag.getInteger("fusion_energy");

        ItemStack newCore = new ItemStack(ItemFusionCore.itemFusionCore);
        NBTTagCompound newTag = new NBTTagCompound();
        newTag.setInteger("fusion_energy", energy);
        newCore.setTagCompound(newTag);

        // Ставим в главную руку (предполагается, что она уже была пуста)
        player.setHeldItem(EnumHand.MAIN_HAND, newCore);

        chestTag.removeTag("fusion_energy");

        world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Совместимость брони: если новая часть не "exo", все имеющиеся части (не-"exo") должны иметь одинаковый тип.
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
     * Проверка свободного пространства позади игрока.
     * Проверяем загрузку блоков и отсутствие suffocation.
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
     * Быстрая проверка булевого тега в NBT.
     */
    public static boolean hasBooleanTag(ItemStack stack, String tagName) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(tagName);
    }
}
