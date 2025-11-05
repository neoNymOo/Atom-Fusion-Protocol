package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.util.UtilPowerArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/**
 * Обработчик событий прыжка существ.
 * Обеспечивает воспроизведение звуков сервоприводов при прыжке в силовой броне.
 */
@Mod.EventBusSubscriber
public class HandlerLivingJumpEvent {
    /**
     * Обрабатывает событие прыжка живого существа.
     * Воспроизводит звук сервопривода при прыжке игрока в силовой броне, если включена соответствующая настройка.
     *
     * @param event Событие прыжка существа
     */
    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!AFPConfig.playServoJumpSound) return;
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            if (player.isInWater()) return;
            ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
                UtilPowerArmor.playServoStepSound(player.world, player.posX, player.posY, player.posZ);
            }
        }
    }
}