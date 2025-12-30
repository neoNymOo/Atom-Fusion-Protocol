package com.nymoo.afp.common.mixin.impl; // Замените на ваш пакет

import com.nymoo.afp.common.item.IPowerArmor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;


@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow public abstract Item getItem();
    @Shadow public abstract void setItemDamage(int meta);
    @Shadow public abstract int getMaxDamage();

    /**
     * Метод attemptDamageItem возвращает true, если предмет должен сломаться (исчезнуть),
     * и false, если он просто получил урон, но остался цел.
     */
    @Inject(method = "attemptDamageItem", at = @At("RETURN"), cancellable = true)
    public void preventArmorBreakage(int amount, Random rand, EntityPlayerMP p, CallbackInfoReturnable<Boolean> cir) {
        // Проверяем, решил ли майнкрафт, что предмет сломался (cir.getReturnValue() == true)
        if (cir.getReturnValue()) {
            // Получаем текущий предмет (так как мы внутри ItemStack, this - это стак)
            ItemStack stack = (ItemStack) (Object) this;

            // Проверка: является ли предмет нашей силовой броней
            if (stack.getItem() instanceof IPowerArmor) {

                // 1. Устанавливаем урон на максимум (прочность 0, но предмет существует)
                this.setItemDamage(this.getMaxDamage());

                // 2. Говорим игре, что предмет НЕ сломался
                cir.setReturnValue(false);
            }
        }
    }
}