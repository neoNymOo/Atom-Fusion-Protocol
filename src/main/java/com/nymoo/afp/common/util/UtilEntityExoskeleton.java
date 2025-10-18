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
 * Utility class for exoskeleton entity operations.
 * Provides methods for installing/uninstalling parts, fusion cores, entering/exiting.
 * Called from event handlers and network message handlers.
 * Extended with methods for installing/uninstalling fusion cores on other players' chestplates.
 */
public class UtilEntityExoskeleton {
    public static final SoundEvent EXO_CLICK_SOUND;
    public static final SoundEvent POWERON_SOUND;
    // Set of armor slots for efficient iteration
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
     * Handles general interaction with exoskeleton.
     * Delegates to install or uninstall based on held item.
     *
     * @param world       The world.
     * @param player      The player.
     * @param hand        The hand.
     * @param entity      The exoskeleton.
     * @param clickedSlot The clicked slot.
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
     * Attempts to install an armor part into the exoskeleton.
     * Checks compatibility and replaces base 'exo' part.
     *
     * @param world     The world.
     * @param player    The player.
     * @param hand      The hand.
     * @param entity    The exoskeleton.
     * @param slot      The slot.
     * @param heldStack The held stack.
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
     * Attempts to uninstall an armor part from the exoskeleton.
     * Replaces with base 'exo' part and gives to player.
     *
     * @param world  The world.
     * @param player The player.
     * @param hand   The hand.
     * @param entity The exoskeleton.
     * @param slot   The slot.
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
     * Attempts to enter the exoskeleton.
     * Transfers armor to player, plays sound if powered, updates position, removes entity.
     * Called from network handler on server.
     *
     * @param world  The world.
     * @param player The player.
     * @param entity The exoskeleton.
     */
    public static void tryEnterExoskeleton(World world, EntityPlayer player, EntityExoskeleton.Exoskeleton entity) {
        if (player.isSneaking()) return;
        double distanceSq = player.getDistanceSq(entity);
        if (distanceSq > 1.0D) return;
        float yawDiff = MathHelper.wrapDegrees(player.rotationYaw - entity.rotationYaw);
        if (yawDiff <= -55.0F || yawDiff >= 55.0F) return;
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            if (!player.getItemStackFromSlot(slot).isEmpty()) return;
        }
        for (EntityEquipmentSlot slot : ARMOR_SLOTS) {
            player.setItemStackToSlot(slot, entity.getItemStackFromSlot(slot).copy());
        }
        ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (hasBooleanTag(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), "fusion_depletion") && chestplateStack.getTagCompound().getFloat("fusion_depletion") < 288000) {
            world.playSound(null, entity.posX, entity.posY, entity.posZ, POWERON_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        player.rotationYaw = entity.rotationYaw;
        player.rotationPitch = entity.rotationPitch;
        player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
        entity.setDead();
    }

    /**
     * Attempts to exit the exoskeleton.
     * Creates new exoskeleton entity, transfers armor, updates player position.
     * Called from keybind handler on server.
     *
     * @param world       The world.
     * @param player      The player.
     * @param isDeathExit Whether exiting due to death.
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
     * Attempts to install a fusion core into the exoskeleton chestplate.
     * Transfers energy and plays sound.
     *
     * @param world       The world.
     * @param player      The player.
     * @param hand        The hand.
     * @param entity      The exoskeleton.
     * @param clickedSlot The slot.
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
     * Attempts to uninstall a fusion core from the exoskeleton chestplate.
     * Creates new core item and plays sound.
     *
     * @param world       The world.
     * @param player      The player.
     * @param hand        The hand.
     * @param entity      The exoskeleton.
     * @param clickedSlot The slot.
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
     * Attempts to install a fusion core into another player's chestplate.
     * Transfers energy and plays sound at the target's position.
     *
     * @param world  The world.
     * @param player The interacting player.
     * @param hand   The hand.
     * @param target The target player.
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
     * Attempts to uninstall a fusion core from another player's chestplate.
     * Creates new core item, gives to player, and plays sound at the target's position.
     *
     * @param world  The world.
     * @param player The interacting player.
     * @param hand   The hand.
     * @param target The target player.
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
     * Checks if the new armor type is compatible with existing parts.
     * All non-'exo' parts must match the new type.
     *
     * @param entity  The exoskeleton.
     * @param newType The new type.
     * @return True if compatible.
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
     * Checks if there is space behind the player for exiting.
     *
     * @param world  The world.
     * @param player The player.
     * @param facing The facing direction.
     * @return True if space is clear.
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
     * Checks for a boolean NBT tag on the stack.
     *
     * @param stack   The stack.
     * @param tagName The tag name.
     * @return True if present and true.
     */
    public static boolean hasBooleanTag(ItemStack stack, String tagName) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(tagName);
    }
}