package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Обработчик событий отбрасывания существ.
 * Управляет модификацией отбрасывания для игроков в силовой броне.
 */
@Mod.EventBusSubscriber
public class HandlerLivingKnockBackEvent {

    /**
     * Обрабатывает событие отбрасывания живого существа.
     * Уменьшает силу отбрасывания для игроков в силовой броне согласно конфигурации.
     *
     * @param event Событие отбрасывания существа
     */
    @SubscribeEvent
    public static void onHandlerLivingKnockBackEvent(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
                event.setStrength(event.getStrength() * AFPConfig.powerArmorKnockbackMultiplier);
            }
        }
    }
}