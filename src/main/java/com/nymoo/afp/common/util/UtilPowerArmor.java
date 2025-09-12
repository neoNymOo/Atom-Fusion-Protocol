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
    /**

     Plays a servo step sound if the player is on ground and a step threshold is crossed.
     */
    public static void handleStepSound(World world, EntityPlayer player) {
        if (!AFPConfig.playServoStepSound || world.isRemote || !player.onGround) {
            return;
        }
        EntityAccessor acc = (EntityAccessor) player;
        float nextStepDistance = acc.getNextStepDistance();
        float distanceWalkedOnStepModified = acc.getDistanceWalkedOnStepModified();
        NBTTagCompound data = player.getEntityData();
        float lastStepDistance = data.getFloat("afp_nextStepDistance");
        if (lastStepDistance == 0) {
        // Initialize on first call
            data.setFloat("afp_nextStepDistance", nextStepDistance);
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
        data.setFloat("afp_nextStepDistance", nextStepDistance);
    }

    // Plays a random servo step sound
    public static void playServoStepSound(World world, double x, double y, double z) {
        SoundEvent[] servoSounds = {
                SoundEvent.REGISTRY.getObject(new ResourceLocation("afp:servo_step1")),
                SoundEvent.REGISTRY.getObject(new ResourceLocation("afp:servo_step2")),
                SoundEvent.REGISTRY.getObject(new ResourceLocation("afp:servo_step3"))
        };
        int soundIndex = (int) (Math.random() * servoSounds.length);
        world.playSound(null, x, y, z, servoSounds[soundIndex], SoundCategory.PLAYERS, AFPConfig.servoVolume, 1.0f);
    }
    /**

     Handles energy depletion logic for power armor based on player actions.
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
        float currentDepletion = nbt.getFloat("fusion_depletion");
        if (currentDepletion >= AFPConfig.maxDepletion) {
        // Max depletion reached; no further processing
            return;
        }
        EntityAccessor acc = (EntityAccessor) player;
        float distanceWalkedOnStepModified = acc.getDistanceWalkedOnStepModified();
        float prevStepModified = nbt.getFloat("prev_step_modified");
        float rate = AFPConfig.baseDepletionRate;
        // Sprinting
        if (player.isSprinting()) {
            rate += AFPConfig.sprintDepletionAdder;
        }
        // Jumping (without jetpack)
        boolean hasJetpack = nbt.getBoolean("jetpack");
        if (!player.onGround && player.motionY > 0.0D && !hasJetpack) {
            rate += AFPConfig.jumpDepletionAdder;
        }
        // In water
        if (player.isInWater()) {
            rate += AFPConfig.waterDepletionAdder;
        }
        // Using item or swinging (attack)
        if (player.isHandActive() || player.isSwingInProgress) {
            rate += AFPConfig.useItemDepletionAdder;
        }
        // Recent hurt or attack
        if (player.hurtTime > 0 || (player.ticksExisted - player.getLastAttackedEntityTime() < 20)) {
            rate += AFPConfig.hurtDepletionAdder;
        }
        // Jetpack operation
        if (hasJetpack) {
            rate += AFPConfig.jetpackDepletionAdder;
        }
        // Horizontal ground movement (non-sprinting) using step fields
        float stepDelta = distanceWalkedOnStepModified - prevStepModified;
        if (player.onGround && !player.isSprinting() && stepDelta > AFPConfig.stepDeltaThreshold) {
            rate += AFPConfig.walkDepletionAdder;
        }
        // Update depletion
        currentDepletion += rate;
        if (currentDepletion > AFPConfig.maxDepletion) {
            currentDepletion = AFPConfig.maxDepletion;
        }
        nbt.setFloat("fusion_depletion", currentDepletion);
        // Update previous step modified for next tick
        nbt.setFloat("prev_step_modified", distanceWalkedOnStepModified);
    }
}