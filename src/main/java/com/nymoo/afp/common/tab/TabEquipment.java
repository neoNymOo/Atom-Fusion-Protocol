package com.nymoo.afp.common.tab;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.item.ItemFusionCore;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Элемент мода для регистрации творческой вкладки оборудования.
 * Создает отдельную вкладку в творческом меню для предметов мода Atom Fusion Protocol.
 */
@ModElementRegistry.ModElement.Tag
public class TabEquipment extends ModElementRegistry.ModElement {
    /**
     * Творческая вкладка для оборудования мода
     */
    public static CreativeTabs tab;

    public TabEquipment(ModElementRegistry instance) {
        super(instance, 2);
    }

    @Override
    public void initElements() {
        tab = new CreativeTabs("tabequipment") {
            /**
             * Создает иконку для творческой вкладки.
             * Использует ядро синтеза в качестве визуального представления вкладки.
             *
             * @return Стек предмета для отображения в качестве иконки
             */
            @SideOnly(Side.CLIENT)
            @Override
            public ItemStack createIcon() {
                return new ItemStack(ItemFusionCore.itemFusionCore);
            }
        };
    }
}