package com.nymoo.afp.common.tab;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.item.ArmorX02;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ModElementRegistry.ModElement.Tag
public class TabPowerArmor extends ModElementRegistry.ModElement {
    public static CreativeTabs tab;

    public TabPowerArmor(ModElementRegistry instance) {
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