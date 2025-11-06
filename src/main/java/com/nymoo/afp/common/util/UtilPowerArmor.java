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

/**
 * Утилитарный класс для управления звуками шагов и энергопотреблением силовой брони.
 * Содержит методы для воспроизведения звуков сервоприводов и расчета расхода энергии.
 */
public class UtilPowerArmor {
    /**
     * Обрабатывает воспроизведение звуков шагов сервоприводов силовой брони.
     * Определяет момент шага и воспроизводит случайный звук сервопривода.
     *
     * @param world  Мир, в котором находится игрок
     * @param player Игрок в силовой броне
     */
    public static void handleStepSound(World world, EntityPlayer player) {
        if (!AFPConfig.playServoStepSound || world.isRemote || !player.onGround || player.isInWater()) {
            return;
        }
        EntityAccessor accessor = (EntityAccessor) player;
        float nextStepDistance = accessor.getNextStepDistance();
        float distanceWalkedOnStepModified = accessor.getDistanceWalkedOnStepModified();
        NBTTagCompound entityData = player.getEntityData();
        float lastStepDistance = entityData.getFloat("afp_nextStepDistance");
        if (lastStepDistance == 0) {
            entityData.setFloat("afp_nextStepDistance", nextStepDistance);
            return;
        }
        BlockPos positionUnderPlayer = new BlockPos(MathHelper.floor(player.posX), MathHelper.floor(player.posY - 0.2D), MathHelper.floor(player.posZ));
        IBlockState blockState = world.getBlockState(positionUnderPlayer);
        if (blockState.getMaterial() != Material.AIR && lastStepDistance <= distanceWalkedOnStepModified) {
            playServoStepSound(world, player.posX, player.posY, player.posZ);
        }
        entityData.setFloat("afp_nextStepDistance", nextStepDistance);
    }

    /**
     * Воспроизводит случайный звук шага сервопривода в указанных координатах.
     * Выбирает один из трех доступных звуков шага с заданной громкостью.
     *
     * @param world Мир для воспроизведения звука
     * @param x     Координата X воспроизведения звука
     * @param y     Координата Y воспроизведения звука
     * @param z     Координата Z воспроизведения звука
     */
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
     * Обрабатывает логику расхода энергии силовой брони на основе действий игрока.
     * Учитывает различные факторы: бег, прыжки, нахождение в воде, использование предметов и т.д.
     *
     * @param world     Мир, в котором находится игрок
     * @param player    Игрок в силовой броне
     * @param itemStack Предмет силовой брони для обновления уровня энергии
     */
    public static void handleEnergyDepletion(World world, EntityPlayer player, ItemStack itemStack) {
        if (world.isRemote) {
            return;
        }
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            itemStack.setTagCompound(tagCompound);
        }
        float currentDepletion = tagCompound.getFloat("fusion_depletion");
        if (!tagCompound.hasKey("fusion_depletion")) {
            return;
        }
        if (currentDepletion >= AFPConfig.maxDepletion) {
            return;
        }
        EntityAccessor accessor = (EntityAccessor) player;
        float distanceWalkedOnStepModified = accessor.getDistanceWalkedOnStepModified();
        float previousStepModified = tagCompound.getFloat("prev_step_modified");
        float depletionRate = AFPConfig.baseDepletionRate;
        if (player.isSprinting()) {
            depletionRate += AFPConfig.sprintDepletionAdder;
        }
        boolean hasJetpack = tagCompound.getBoolean("jetpack");
        if (!player.onGround && player.motionY > 0.0D && !hasJetpack) {
            depletionRate += AFPConfig.jumpDepletionAdder;
        }
        if (player.isInWater()) {
            depletionRate += AFPConfig.waterDepletionAdder;
        }
        if (player.isHandActive() || player.isSwingInProgress) {
            depletionRate += AFPConfig.useItemDepletionAdder;
        }
        if (player.hurtTime > 0 || (player.ticksExisted - player.getLastAttackedEntityTime() < 20)) {
            depletionRate += AFPConfig.hurtDepletionAdder;
        }
        if (hasJetpack) {
            depletionRate += AFPConfig.jetpackDepletionAdder;
        }
        float stepDelta = distanceWalkedOnStepModified - previousStepModified;
        if (player.onGround && !player.isSprinting() && stepDelta > AFPConfig.stepDeltaThreshold) {
            depletionRate += AFPConfig.walkDepletionAdder;
        }
        currentDepletion += depletionRate;
        if (currentDepletion > AFPConfig.maxDepletion) {
            currentDepletion = AFPConfig.maxDepletion;
        }
        tagCompound.setFloat("fusion_depletion", currentDepletion);
        tagCompound.setFloat("prev_step_modified", distanceWalkedOnStepModified);
    }
}