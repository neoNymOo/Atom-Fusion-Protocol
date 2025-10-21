package com.nymoo.afp.common.mixin.impl;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Миксин для изменения поведения раздатчика блоков.
 * Ограничивает возможность автоматической экипировки силовой брони через раздатчик.
 */
@Mixin(BlockDispenser.class)
public abstract class MixinBlockDispenser {

    /**
     * Внедряется в начало метода getBehavior для изменения поведения раздатчика.
     * Если в конфигурации отключена возможность экипировки брони через раздатчик,
     * возвращает стандартное поведение для предметов силовой брони.
     *
     * @param stack Стек предмета для проверки
     * @param cir   Колбэк для возврата измененного поведения
     */
    @Inject(method = "getBehavior", at = @At("HEAD"), cancellable = true)
    private void onGetBehavior(ItemStack stack, CallbackInfoReturnable<IBehaviorDispenseItem> cir) {
        if (AFPConfig.canDispenserEquipPowerArmor) return;

        if (stack.getItem() instanceof IPowerArmor) {
            cir.setReturnValue(IBehaviorDispenseItem.DEFAULT_BEHAVIOR);
        }
    }
}