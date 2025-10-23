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
 * Уменьшает силу отбрасывания для игроков в силовой броне согласно конфигурации.
 */
@Mod.EventBusSubscriber
public class HandlerLivingKnockBackEvent {

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            ItemStack chestplateStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (!chestplateStack.isEmpty() && chestplateStack.getItem() instanceof IPowerArmor) {
                String type = ((IPowerArmor) chestplateStack.getItem()).getPowerArmorType();
                AFPConfig.ArmorSet set = AFPConfig.getArmorSet(type);

                if (set != null) {
                    float armorMult = set.knockbackMultiplier;
                    float damage = HandlerLivingDamageEvent.getAndRemoveDamage(player);
                    float damageMult = damage / AFPConfig.knockbackDamageScale;
                    float finalMult = armorMult * damageMult;
                    event.setStrength(event.getOriginalStrength() * finalMult);
                }
            }
        }
    }
}