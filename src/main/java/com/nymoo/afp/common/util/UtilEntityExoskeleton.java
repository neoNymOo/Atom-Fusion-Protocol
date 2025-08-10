package com.nymoo.afp.common.util;

import com.nymoo.afp.common.entity.EntityExoskeleton;
import com.nymoo.afp.common.item.ArmorExo;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UtilEntityExoskeleton {
    private static final SoundEvent EXO_CLICK_SOUND = SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("afp", "exo_click"));
    private static final SoundEvent POWERON_SOUND = SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("afp", "poweron"));
    private static final EntityEquipmentSlot[] ARMOR_SLOTS = {EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};

    public static void handleInteraction(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot clickedSlot) {
        ItemStack heldStack = player.getHeldItem(hand);
        if (!heldStack.isEmpty()) {
            tryInstallPart(world, player, hand, entity, clickedSlot, heldStack);
        } else {
            tryUninstallPart(world, player, hand, entity, clickedSlot);
        }
    }

    private static void tryInstallPart(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot slot, ItemStack heldStack) {
        String itemId = heldStack.getItem().getRegistryName().toString();
        int colonIndex = itemId.indexOf(':');
        if (colonIndex == -1) return;
        String itemPath = itemId.substring(colonIndex + 1);
        int underscoreIndex = itemPath.indexOf('_');
        if (underscoreIndex == -1) return;
        String armorType = itemPath.substring(0, underscoreIndex);
        String armorPart = itemPath.substring(underscoreIndex + 1);
        if (!getSlotPart(slot).equals(armorPart)) return;
        ItemStack currentArmor = entity.getItemStackFromSlot(slot);
        if (currentArmor.isEmpty() || !isBaseExoArmor(currentArmor, slot)) return;
        if (!checkArmorCompatibility(entity, armorType)) return;
        ItemStack newArmor = heldStack.copy();
        newArmor.setCount(1);
        entity.setItemStackToSlot(slot, newArmor);
        if (!player.isCreative()) {
            heldStack.shrink(1);
            if (heldStack.isEmpty()) {
                player.setHeldItem(hand, ItemStack.EMPTY);
            }
        }
        world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    private static void tryUninstallPart(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot slot) {
        ItemStack currentArmor = entity.getItemStackFromSlot(slot);
        ItemStack chestArmor = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (!isSealed(chestArmor)) {
            for (EntityEquipmentSlot armorSlot : ARMOR_SLOTS) {
                if (!player.getItemStackFromSlot(armorSlot).isEmpty()) {
                    return;
                }
            }
            if (slot == EntityEquipmentSlot.CHEST && !player.isRiding()) {
                tryEnterExoskeleton(world, player, entity);
            }
            return;
        }

        if (isBaseExoArmor(currentArmor, slot)) {
            return;
        }

        player.setHeldItem(hand, currentArmor.copy());
        entity.setItemStackToSlot(slot, getBaseArmorForSlot(slot));
        world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    private static void tryEnterExoskeleton(World world, EntityPlayer player, EntityExoskeleton.Exoskeleton entity) {
        entity.setDead();

        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorStack = entity.getItemStackFromSlot(slot);
            if (!armorStack.isEmpty()) {
                player.setItemStackToSlot(slot, armorStack.copy());
                entity.setItemStackToSlot(slot, getBaseArmorForSlot(slot));
            }
        }

        if (isCore(entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST))) {
            world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
        } else {
            world.playSound(null, entity.posX, entity.posY, entity.posZ, POWERON_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        player.rotationYaw = entity.rotationYaw;
        player.rotationPitch = entity.rotationPitch;
        player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
    }

    public static void tryExitExoskeleton(World world, EntityPlayer player) {
        // Проверка наличия нагрудника
        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!chestplate.isEmpty() && chestplate.getItem() instanceof IPowerArmor || player.isRiding()) {
            return;
        }

        // Проверка свободного пространства сзади игрока
        EnumFacing facing = player.getHorizontalFacing();
        if (!isSpaceBehindPlayerClear(world, player, facing)) {
            return;
        }
        double px = player.posX;
        double py = player.posY;
        double pz = player.posZ;

        // Создание сущности экзоскелета
        EntityExoskeleton.Exoskeleton exoskeleton = new EntityExoskeleton.Exoskeleton(world);
        exoskeleton.setLocationAndAngles(
                px,
                py,
                pz,
                player.rotationYaw,
                player.rotationPitch
        );
        //здесь если игрок при выходи был sneaking то сущность также становится sneaking
        world.spawnEntity(exoskeleton);

        // Перемещение игрока
        switch (facing) {
            case NORTH:
                pz += 0.5;
                break;
            case SOUTH:
                pz -= 0.5;
                break;
            case WEST:
                px += 0.5;
                break;
            case EAST:
                px -= 0.5;
                break;
        }

        player.setPositionAndUpdate(px, py, pz);

        // Перенос брони с игрока на экзоскелет
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorStack = player.getItemStackFromSlot(slot);
            if (!armorStack.isEmpty()) {
                exoskeleton.setItemStackToSlot(slot, armorStack.copy());
                player.setItemStackToSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private static void tryInstallCore(World world, EntityPlayer player, EntityExoskeleton.Exoskeleton entity) {
        //это метод-заготовка на будущее
    }

    private static void tryUninstallCore(World world, EntityPlayer player, EntityExoskeleton.Exoskeleton entity) {
        //это метод-заготовка на будущее
    }

    private static boolean isBaseExoArmor(ItemStack stack, EntityEquipmentSlot slot) {
        Item item = stack.getItem();
        switch(slot) {
            case HEAD: return item == ArmorExo.helmet;
            case CHEST: return item == ArmorExo.body;
            case LEGS: return item == ArmorExo.legs;
            case FEET: return item == ArmorExo.boots;
            default: return false;
        }
    }

    private static boolean isSealed(ItemStack stack) {
        if (stack.isEmpty()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean("sealed");
    }

    private static boolean isCore(ItemStack stack) {
        if (stack.isEmpty()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean("core");
    }

    private static String getSlotPart(EntityEquipmentSlot slot) {
        switch(slot) {
            case HEAD: return "helmet";
            case CHEST: return "chestplate";
            case LEGS: return "leggings";
            case FEET: return "boots";
            default: return "";
        }
    }

    private static ItemStack getBaseArmorForSlot(EntityEquipmentSlot slot) {
        switch(slot) {
            case HEAD: return new ItemStack(ArmorExo.helmet);
            case CHEST: return new ItemStack(ArmorExo.body);
            case LEGS: return new ItemStack(ArmorExo.legs);
            case FEET: return new ItemStack(ArmorExo.boots);
            default: return ItemStack.EMPTY;
        }
    }

    private static boolean checkArmorCompatibility(EntityExoskeleton.Exoskeleton entity, String newType) {
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorStack = entity.getItemStackFromSlot(slot);
            if (armorStack.isEmpty() || isBaseExoArmor(armorStack, slot)) continue;
            String armorId = armorStack.getItem().getRegistryName().toString();
            int colonIndex = armorId.indexOf(':');
            if (colonIndex == -1) continue;
            String armorPath = armorId.substring(colonIndex + 1);
            int underscoreIndex = armorPath.indexOf('_');
            if (underscoreIndex == -1) continue;
            String armorType = armorPath.substring(0, underscoreIndex);
            if (!armorType.equals(newType)) return false;
        }
        return true;
    }

    private static boolean isSpaceBehindPlayerClear(World world, EntityPlayer player, EnumFacing facing) {
        EnumFacing backward = facing.getOpposite();
        BlockPos playerPos = new BlockPos(player.posX, player.posY, player.posZ);

        BlockPos lowerPos = playerPos.offset(backward);
        if (!world.isBlockLoaded(lowerPos) ||
                world.getBlockState(lowerPos).isFullBlock()) {
            return false;
        }

        BlockPos upperPos = lowerPos.up();
        if (!world.isBlockLoaded(upperPos) ||
                world.getBlockState(upperPos).isFullBlock()) {
            return false;
        }

        return true;
    }
}