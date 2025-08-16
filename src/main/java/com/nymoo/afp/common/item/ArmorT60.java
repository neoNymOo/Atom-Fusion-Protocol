package com.nymoo.afp.common.item;

import com.nymoo.afp.ModElementRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

@ModElementRegistry.ModElement.Tag
public class ArmorT60 extends AbstractPowerArmor {
    @GameRegistry.ObjectHolder("afp:t60_helmet")
    public static Item helmet;
    @GameRegistry.ObjectHolder("afp:t60_chestplate")
    public static Item chestplate;
    @GameRegistry.ObjectHolder("afp:t60_leggings")
    public static Item leggings;
    @GameRegistry.ObjectHolder("afp:t60_boots")
    public static Item boots;

    public ArmorT60(ModElementRegistry instance) {
        super(instance, 5, "t60", true);
    }

    @Override
    protected ItemArmor.ArmorMaterial getArmorMaterial() {
        return EnumHelper.addArmorMaterial(
                "t60",
                "minecraft:diamond",
                21,
                new int[]{15, 22, 27, 17},
                0,
                SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                4f
        );
    }
}