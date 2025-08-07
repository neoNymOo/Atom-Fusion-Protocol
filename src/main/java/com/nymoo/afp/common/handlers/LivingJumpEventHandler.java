package com.nymoo.afp.common.handlers;

import com.nymoo.afp.common.utils.PowerArmorUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LivingJumpEventHandler {
    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (PowerArmorUtil.isPowerArmor(chestplate)) {
                PowerArmorUtil.playServoStepSound(player.world, player.posX, player.posY, player.posZ);
            }
        }
    }
}