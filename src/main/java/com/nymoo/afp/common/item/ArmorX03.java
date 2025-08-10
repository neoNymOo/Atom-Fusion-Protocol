package com.nymoo.afp.common.item;

import com.nymoo.afp.ModElementRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

@ModElementRegistry.ModElement.Tag
public class ArmorX03 extends AbstractPowerArmor {
    @GameRegistry.ObjectHolder("afp:x03_helmet") public static Item helmet;
    @GameRegistry.ObjectHolder("afp:x03_chestplate") public static Item body;
    @GameRegistry.ObjectHolder("afp:x03_leggings") public static Item legs;
    @GameRegistry.ObjectHolder("afp:x03_boots") public static Item boots;

    public ArmorX03(ModElementRegistry instance) {
        super(instance, 1, "x03", true);
    }

    @Override
    protected ItemArmor.ArmorMaterial getArmorMaterial() {
        return EnumHelper.addArmorMaterial(
                "x03",
                "minecraft:diamond",
                69,
                new int[]{20, 24, 30, 19},
                0,
                (SoundEvent) SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                4f
        );
    }
}