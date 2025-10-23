package com.nymoo.afp.common.handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.WeakHashMap;

/**
 * Обработчик событий урона.
 * Сохраняет количество полученного урона для последующего использования в расчете отбрасывания.
 */
@Mod.EventBusSubscriber
public class HandlerLivingDamageEvent {
    private static final WeakHashMap<Entity, Float> lastDamageMap = new WeakHashMap<>();

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof EntityPlayer && event.getAmount() > 0) {
            lastDamageMap.put(event.getEntity(), event.getAmount());
        }
    }

    // Геттер для доступа к данным урона
    public static float getAndRemoveDamage(Entity entity) {
        Float damage = lastDamageMap.remove(entity);
        return damage != null ? damage : 0f;
    }
}