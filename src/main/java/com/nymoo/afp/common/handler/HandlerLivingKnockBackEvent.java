package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class HandlerLivingKnockBackEvent {
    @SubscribeEvent
    public static void onHandlerLivingKnockBackEvent(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) event.getEntity();
            ItemStack chestplateStack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
                event.setStrength(0.0F);
            }
        }
    }
}
