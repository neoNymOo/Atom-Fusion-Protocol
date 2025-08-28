package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.config.AFPConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.nymoo.afp.common.util.UtilEntityExoskeleton.tryExitExoskeleton;

@Mod.EventBusSubscriber
public class HandlerLivingDeathEvent {
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