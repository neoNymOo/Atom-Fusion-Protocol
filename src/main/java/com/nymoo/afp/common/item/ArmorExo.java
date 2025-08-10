package com.nymoo.afp.common.item;

import com.nymoo.afp.ModElementRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

@ModElementRegistry.ModElement.Tag
public class ArmorExo extends AbstractPowerArmor {
    @GameRegistry.ObjectHolder("afp:exo_helmet") public static Item helmet;
    @GameRegistry.ObjectHolder("afp:exo_chestplate") public static Item body;
    @GameRegistry.ObjectHolder("afp:exo_leggings") public static Item legs;
    @GameRegistry.ObjectHolder("afp:exo_boots") public static Item boots;

    public ArmorExo(ModElementRegistry instance) {
        super(instance, 7, "exo", false);
    }

    @Override
    protected ItemArmor.ArmorMaterial getArmorMaterial() {
        return EnumHelper.addArmorMaterial(
                "exo",
                "minecraft:diamond",
                34,
                new int[]{2, 5, 6, 2},
                0,
                (SoundEvent) SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                0f
        );
    }
}