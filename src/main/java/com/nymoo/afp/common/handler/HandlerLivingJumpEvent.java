package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.util.UtilPowerArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HandlerLivingJumpEvent {
    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) event.getEntity();
            ItemStack chestplate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (!chestplate.isEmpty() && chestplate.getItem() instanceof IPowerArmor) {
                UtilPowerArmor.playServoStepSound(entity.world, entity.posX, entity.posY, entity.posZ);
            }
        }
    }
}