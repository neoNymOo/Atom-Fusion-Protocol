package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class HandlerLivingFallEvent {

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer entity = (EntityPlayer) event.getEntity();
            ItemStack chestplateStack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
                if (event.getDistance() <= AFPConfig.powerArmorFallThreshold) {
                    event.setCanceled(true);
                    entity.fallDistance = 0.0F;
                } else {
                    event.setDamageMultiplier(event.getDamageMultiplier() * AFPConfig.powerArmorFallDamageMultiplier);
                }
            }
        }
    }
}
