package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Обработчик событий падения существ.
 * Обеспечивает специальную обработку падения для игроков в силовой броне.
 */
@Mod.EventBusSubscriber
public class HandlerLivingFallEvent {

    /**
     * Обрабатывает событие падения живого существа.
     * Для игроков в силовой броне применяет специальные правила:
     * - Отменяет падение ниже пороговой высоты
     * - Уменьшает урон от падения выше пороговой высоты
     *
     * @param event Событие падения существа
     */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
                if (event.getDistance() <= AFPConfig.powerArmorFallThreshold) {
                    event.setCanceled(true);
                    player.fallDistance = 0.0F;
                } else {
                    event.setDamageMultiplier(event.getDamageMultiplier() * AFPConfig.powerArmorFallDamageMultiplier);
                }
            }
        }
    }
}