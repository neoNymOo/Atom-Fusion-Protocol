package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.entity.EntityExoskeleton;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Обработчик событий установки цели атаки для мобов.
 * Предотвращает нацеливание мобов на экзоскелет, делая его нейтральным для всех существ.
 */
@Mod.EventBusSubscriber
public class HandlerLivingSetAttackTargetEvent {

    /**
     * Обрабатывает событие установки цели атаки для сущностей.
     * Если целью является экзоскелет, сбрасывает цель атаки у атакующей сущности.
     * Это делает экзоскелет неагрессивным для всех мобов.
     *
     * @param event Событие установки цели атаки, содержащее атакующую сущность и цель
     */
    @SubscribeEvent
    public static void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof EntityExoskeleton.Exoskeleton) {
            EntityLivingBase entity = event.getEntityLiving();
            if (entity instanceof EntityLiving) {
                ((EntityLiving) entity).setAttackTarget(null);
            }
        }
    }
}