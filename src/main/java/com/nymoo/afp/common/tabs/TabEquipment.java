package com.nymoo.afp.common.tabs;

import com.nymoo.afp.ElementsAFP;
import com.nymoo.afp.common.items.ArmorT45;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ElementsAFP.ModElement.Tag
public class TabEquipment extends ElementsAFP.ModElement {
    public static CreativeTabs tab;

    public TabEquipment(ElementsAFP instance) {
        super(instance, 2);
    }

    @Override
    public void initElements() {
        tab = new CreativeTabs("tabequipment") {
            @SideOnly(Side.CLIENT)
            @Override
            public ItemStack createIcon() {
                return new ItemStack(ArmorT45.helmet);
            }
        };
    }
}