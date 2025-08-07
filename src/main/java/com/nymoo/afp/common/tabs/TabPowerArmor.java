package com.nymoo.afp.common.tabs;

import com.nymoo.afp.ElementsAFP;
import com.nymoo.afp.common.items.ArmorX02;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ElementsAFP.ModElement.Tag
public class TabPowerArmor extends ElementsAFP.ModElement {
    public static CreativeTabs tab;

    public TabPowerArmor(ElementsAFP instance) {
        super(instance, 1);
    }

    @Override
    public void initElements() {
        tab = new CreativeTabs("tabpowerarmor") {
            @SideOnly(Side.CLIENT)
            @Override
            public ItemStack createIcon() {
                return new ItemStack(ArmorX02.helmet);
            }
        };
    }
}