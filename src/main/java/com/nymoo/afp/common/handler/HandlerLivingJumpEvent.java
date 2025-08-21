package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.util.UtilPowerArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class HandlerLivingJumpEvent {
    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) event.getEntity();
            ItemStack chestplateStack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
                UtilPowerArmor.playServoStepSound(entity.world, entity.posX, entity.posY, entity.posZ);
            }
        }
    }
}