package com.nymoo.afp.common.item;

import com.nymoo.afp.ModElementRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

@ModElementRegistry.ModElement.Tag
public class ArmorX02 extends AbstractPowerArmor {
    @GameRegistry.ObjectHolder("afp:x02_helmet") public static Item helmet;
    @GameRegistry.ObjectHolder("afp:x02_chestplate") public static Item body;
    @GameRegistry.ObjectHolder("afp:x02_leggings") public static Item legs;
    @GameRegistry.ObjectHolder("afp:x02_boots") public static Item boots;

    public ArmorX02(ModElementRegistry instance) {
        super(instance, 2, "x02", true);
    }

    @Override
    protected ItemArmor.ArmorMaterial getArmorMaterial() {
        return EnumHelper.addArmorMaterial(
                "x02",
                "minecraft:diamond",
                12,
                new int[]{17, 22, 27, 17},
                0,
                (SoundEvent) SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                4f
        );
    }
}