package com.nymoo.afp.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.nymoo.afp.common.util.UtilEntityExoskeleton.tryExitExoskeleton;

@Mod.EventBusSubscriber
public class HandlerPlayerDeath {
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            World world = player.getEntityWorld();

            tryExitExoskeleton(world, player, true);
        }
    }
}