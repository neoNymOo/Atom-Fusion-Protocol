package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class HandlerLivingHurtEvent {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer entity = (EntityPlayer) event.getEntity();
            ItemStack chestplateStack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (event.getSource() == DamageSource.FALL && !chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
                event.setAmount(event.getAmount() * AFPConfig.powerArmorFallDamageMultiplier);
            }
        }
    }
}