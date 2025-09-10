package com.nymoo.afp.common.util;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.mixin.impl.EntityAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class UtilPowerArmor {
    // Cached servo step sounds
    private static final SoundEvent[] SERVO_SOUNDS = {
            SoundEvent.REGISTRY.getObject(new ResourceLocation("afp:servo_step1")),
            SoundEvent.REGISTRY.getObject(new ResourceLocation("afp:servo_step2")),
            SoundEvent.REGISTRY.getObject(new ResourceLocation("afp:servo_step3"))
    };

    private static final String NBT_KEY_NEXT_STEP_DISTANCE = "afp_nextStepDistance";
    private static final String NBT_KEY_PREV_STEP_MODIFIED = "prev_step_modified";
    private static final String NBT_KEY_FUSION_DEPLETION = "fusion_depletion";
    private static final String NBT_KEY_JETPACK = "jetpack";
    private static final float MAX_DEPLETION = 288000f;
    private static final float STEP_DELTA_THRESHOLD = 0.06f; // Approx equivalent to sqrt(distSq) > 0.1, since delta ≈ 0.6 * dist

    /**
     * Plays a servo step sound if the player is on ground and a step threshold is crossed.
     */
    public static void handleStepSound(World world, EntityPlayer player) {
        if (!AFPConfig.playServoStepSound || world.isRemote || !player.onGround) {
            return;
        }

        EntityAccessor acc = (EntityAccessor) player;
        float nextStepDistance = acc.getNextStepDistance();
        float distanceWalkedOnStepModified = acc.getDistanceWalkedOnStepModified();

        NBTTagCompound data = player.getEntityData();
        float lastStepDistance = data.getFloat(NBT_KEY_NEXT_STEP_DISTANCE);
        if (lastStepDistance == 0) {
            // Initialize on first call
            data.setFloat(NBT_KEY_NEXT_STEP_DISTANCE, nextStepDistance);
            return;
        }

        // Get block under feet
        BlockPos posUnder = new BlockPos(MathHelper.floor(player.posX), MathHelper.floor(player.posY - 0.2D), MathHelper.floor(player.posZ));
        IBlockState block = world.getBlockState(posUnder);

        // Play sound if step threshold crossed and not on air
        if (block.getMaterial() != Material.AIR && lastStepDistance <= distanceWalkedOnStepModified) {
            playServoStepSound(world, player.posX, player.posY, player.posZ);
        }

        // Update stored threshold
        data.setFloat(NBT_KEY_NEXT_STEP_DISTANCE, nextStepDistance);
    }

    // Plays a random servo step sound
    public static void playServoStepSound(World world, double x, double y, double z) {
        int soundIndex = (int) (Math.random() * SERVO_SOUNDS.length);
        world.playSound(null, x, y, z, SERVO_SOUNDS[soundIndex], SoundCategory.PLAYERS, AFPConfig.servoVolume, 1.0f);
    }

    /**
     * Handles energy depletion logic for power armor based on player actions.
     */
    public static void handleEnergyDepletion(World world, EntityPlayer player, ItemStack itemStack) {
        if (world.isRemote) {
            return;
        }

        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            itemStack.setTagCompound(nbt);
        }

        float currentDepletion = nbt.getFloat(NBT_KEY_FUSION_DEPLETION);
        if (currentDepletion >= MAX_DEPLETION) {
            // Max depletion reached; no further processing
            return;
        }

        EntityAccessor acc = (EntityAccessor) player;
        float distanceWalkedOnStepModified = acc.getDistanceWalkedOnStepModified();
        float prevStepModified = nbt.getFloat(NBT_KEY_PREV_STEP_MODIFIED);

        float rate = 1.0f; // Base rate

        // Sprinting
        if (player.isSprinting()) {
            rate += 0.5f;
        }

        // Jumping (without jetpack)
        boolean hasJetpack = nbt.getBoolean(NBT_KEY_JETPACK);
        if (!player.onGround && player.motionY > 0.0D && !hasJetpack) {
            rate += 0.7f;
        }

        // In water
        if (player.isInWater()) {
            rate += 0.3f;
        }

        // Using item or swinging (attack)
        if (player.isHandActive() || player.isSwingInProgress) {
            rate += 0.4f;
        }

        // Recent hurt or attack
        if (player.hurtTime > 0 || (player.ticksExisted - player.getLastAttackedEntityTime() < 20)) {
            rate += 1.0f;
        }

        // Jetpack operation
        if (hasJetpack) {
            rate += 2.0f;
        }

        // Horizontal ground movement (non-sprinting) using step fields
        float stepDelta = distanceWalkedOnStepModified - prevStepModified;
        if (player.onGround && !player.isSprinting() && stepDelta > STEP_DELTA_THRESHOLD) {
            rate += 0.2f;
        }

        // Update depletion
        currentDepletion += rate;
        if (currentDepletion > MAX_DEPLETION) {
            currentDepletion = MAX_DEPLETION;
        }
        nbt.setFloat(NBT_KEY_FUSION_DEPLETION, currentDepletion);

        // Update previous step modified for next tick
        nbt.setFloat(NBT_KEY_PREV_STEP_MODIFIED, distanceWalkedOnStepModified);
    }
}