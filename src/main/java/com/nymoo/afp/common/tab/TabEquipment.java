package com.nymoo.afp.common.tab;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.item.ItemFusionCore;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ModElementRegistry.ModElement.Tag
public class TabEquipment extends ModElementRegistry.ModElement {
    public static CreativeTabs tab;

    public TabEquipment(ModElementRegistry instance) {
        super(instance, 2);
    }

    @Override
    public void initElements() {
        tab = new CreativeTabs("tabequipment") {
            @SideOnly(Side.CLIENT)
            @Override
            public ItemStack createIcon() {
                return new ItemStack(ItemFusionCore.itemFusionCore);
            }
        };
    }
}