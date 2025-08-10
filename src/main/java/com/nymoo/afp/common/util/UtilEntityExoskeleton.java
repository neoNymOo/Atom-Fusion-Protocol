package com.nymoo.afp.common.util;

import com.nymoo.afp.common.entity.EntityExoskeleton;
import com.nymoo.afp.common.item.AbstractPowerArmor;
import com.nymoo.afp.common.item.ArmorExo;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UtilEntityExoskeleton {
    private static final SoundEvent EXO_CLICK_SOUND;
    private static final SoundEvent POWERON_SOUND;
    private static final EntityEquipmentSlot[] ARMOR_SLOTS = EntityEquipmentSlot.values();
    private static final ItemStack BASE_HELMET = new ItemStack(ArmorExo.helmet);
    private static final ItemStack BASE_CHESTPLATE = new ItemStack(ArmorExo.body);
    private static final ItemStack BASE_LEGGINGS = new ItemStack(ArmorExo.legs);
    private static final ItemStack BASE_BOOTS = new ItemStack(ArmorExo.boots);
    private static final String SEALED_TAG = "sealed";
    private static final String CORE_TAG = "core";

    static {
        EXO_CLICK_SOUND = SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("afp", "exo_click"));
        POWERON_SOUND = SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("afp", "poweron"));
    }

    public static void handleInteraction(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot clickedSlot) {
        ItemStack heldStack = player.getHeldItem(hand);
        if (!heldStack.isEmpty()) {
            tryInstallPart(world, player, hand, entity, clickedSlot, heldStack);
        } else {
            tryUninstallPart(world, player, hand, entity, clickedSlot);
        }
    }

    private static void tryInstallPart(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot slot, ItemStack heldStack) {
        ResourceLocation registryName = heldStack.getItem().getRegistryName();
        if (registryName == null) return;

        String path = registryName.getPath();
        int underscoreIndex = path.indexOf('_');
        if (underscoreIndex == -1) return;

        String armorType = path.substring(0, underscoreIndex);
        String armorPart = path.substring(underscoreIndex + 1);
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

        if (EXO_CLICK_SOUND != null) {
            world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    private static void tryUninstallPart(World world, EntityPlayer player, EnumHand hand, EntityExoskeleton.Exoskeleton entity, EntityEquipmentSlot slot) {
        ItemStack currentArmor = entity.getItemStackFromSlot(slot);
        ItemStack chestArmor = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (!isSealed(chestArmor)) {
            for (EntityEquipmentSlot armorSlot : ARMOR_SLOTS) {
                if (armorSlot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && !player.getItemStackFromSlot(armorSlot).isEmpty()) {
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

        if (EXO_CLICK_SOUND != null) {
            world.playSound(null, entity.posX, entity.posY, entity.posZ, EXO_CLICK_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    private static void tryEnterExoskeleton(World world, EntityPlayer player, EntityExoskeleton.Exoskeleton entity) {
        entity.setDead();

        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            if (slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR) continue;

            ItemStack armorStack = entity.getItemStackFromSlot(slot);
            if (!armorStack.isEmpty()) {
                player.setItemStackToSlot(slot, armorStack.copy());
                entity.setItemStackToSlot(slot, getBaseArmorForSlot(slot));
            }
        }

        SoundEvent sound = isCore(entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST)) ? EXO_CLICK_SOUND : POWERON_SOUND;
        if (sound != null) {
            world.playSound(null, entity.posX, entity.posY, entity.posZ, sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        player.rotationYaw = entity.rotationYaw;
        player.rotationPitch = entity.rotationPitch;
        player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
    }

    public static void tryExitExoskeleton(World world, EntityPlayer player) {
        player.setInvisible(false);

        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if ((chestplate.isEmpty() && !(chestplate.getItem() instanceof IPowerArmor) || player.isRiding())) {
            return;
        }

        EnumFacing facing = player.getHorizontalFacing();
        if (!isSpaceBehindPlayerClear(world, player, facing)) {
            return;
        }

        EntityExoskeleton.Exoskeleton exoskeleton = new EntityExoskeleton.Exoskeleton(world);
        exoskeleton.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        world.spawnEntity(exoskeleton);

        double px = player.posX;
        double py = player.posY;
        double pz = player.posZ;
        EnumFacing opposite = facing.getOpposite();
        px += opposite.getXOffset() * 0.5;
        pz += opposite.getZOffset() * 0.5;
        player.setPositionAndUpdate(px, py, pz);

        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            if (slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR) continue;

            ItemStack armorStack = player.getItemStackFromSlot(slot);
            if (!armorStack.isEmpty()) {
                exoskeleton.setItemStackToSlot(slot, armorStack.copy());
                player.setItemStackToSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private static boolean isBaseExoArmor(ItemStack stack, EntityEquipmentSlot slot) {
        Item item = stack.getItem();
        if (slot == EntityEquipmentSlot.HEAD) return item == ArmorExo.helmet;
        if (slot == EntityEquipmentSlot.CHEST) return item == ArmorExo.body;
        if (slot == EntityEquipmentSlot.LEGS) return item == ArmorExo.legs;
        if (slot == EntityEquipmentSlot.FEET) return item == ArmorExo.boots;
        return false;
    }

    private static boolean isSealed(ItemStack stack) {
        if (stack.isEmpty()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(SEALED_TAG);
    }

    private static boolean isCore(ItemStack stack) {
        if (stack.isEmpty()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(CORE_TAG);
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

    // Исправлено: метод сделан публичным для доступа из EntityExoskeleton
    public static ItemStack getBaseArmorForSlot(EntityEquipmentSlot slot) {
        switch(slot) {
            case HEAD: return BASE_HELMET.copy();
            case CHEST: return BASE_CHESTPLATE.copy();
            case LEGS: return BASE_LEGGINGS.copy();
            case FEET: return BASE_BOOTS.copy();
            default: return ItemStack.EMPTY;
        }
    }

    private static boolean checkArmorCompatibility(EntityExoskeleton.Exoskeleton entity, String newType) {
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            if (slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR) continue;

            ItemStack armorStack = entity.getItemStackFromSlot(slot);
            if (armorStack.isEmpty() || isBaseExoArmor(armorStack, slot)) continue;

            ResourceLocation armorLoc = armorStack.getItem().getRegistryName();
            if (armorLoc == null) continue;

            String armorPath = armorLoc.getPath();
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
        if (!world.isBlockLoaded(lowerPos) || world.getBlockState(lowerPos).causesSuffocation()) {
            return false;
        }

        BlockPos upperPos = lowerPos.up();
        return world.isBlockLoaded(upperPos) && !world.getBlockState(upperPos).causesSuffocation();
    }
}