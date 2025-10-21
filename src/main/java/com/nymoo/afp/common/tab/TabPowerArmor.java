package com.nymoo.afp.common.tab;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.item.ArmorX02;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Элемент мода для регистрации творческой вкладки силовой брони.
 * Создает отдельную вкладку в творческом меню для наборов силовой брони мода Atom Fusion Protocol.
 */
@ModElementRegistry.ModElement.Tag
public class TabPowerArmor extends ModElementRegistry.ModElement {
    /**
     * Творческая вкладка для силовой брони мода
     */
    public static CreativeTabs tab;

    public TabPowerArmor(ModElementRegistry instance) {
        super(instance, 1);
    }

    @Override
    public void initElements() {
        tab = new CreativeTabs("tabpowerarmor") {
            /**
             * Создает иконку для творческой вкладки силовой брони.
             * Использует шлем X-02 в качестве визуального представления вкладки.
             *
             * @return Стек предмета для отображения в качестве иконки
             */
            @SideOnly(Side.CLIENT)
            @Override
            public ItemStack createIcon() {
                return new ItemStack(ArmorX02.helmet);
            }
        };
    }
}