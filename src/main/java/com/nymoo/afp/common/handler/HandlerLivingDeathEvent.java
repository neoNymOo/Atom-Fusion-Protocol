package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.config.AFPConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.nymoo.afp.common.util.UtilEntityExoskeleton.tryExitExoskeleton;

/**
 * Обработчик событий смерти существ.
 * Обеспечивает корректное взаимодействие с экзоскелетом при смерти игрока.
 */
@Mod.EventBusSubscriber
public class HandlerLivingDeathEvent {

    /**
     * Обрабатывает событие смерти живого существа.
     * Автоматически выходит из экзоскелета при смерти игрока, если включена соответствующая настройка.
     *
     * @param event Событие смерти существа
     */
    @SubscribeEvent
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        if (!AFPConfig.handlePlayerDeath) return;

        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            World world = player.getEntityWorld();

            boolean keepInventory = world.getGameRules().getBoolean("keepInventory");
            if (!keepInventory) {
                tryExitExoskeleton(world, player, true);
            }
        }
    }
}