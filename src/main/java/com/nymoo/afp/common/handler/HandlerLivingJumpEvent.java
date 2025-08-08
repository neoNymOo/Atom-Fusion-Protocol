package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.util.UtilPowerArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HandlerLivingJumpEvent {
    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (UtilPowerArmor.isPowerArmor(chestplate)) {
                UtilPowerArmor.playServoStepSound(player.world, player.posX, player.posY, player.posZ);
            }
        }
    }
}