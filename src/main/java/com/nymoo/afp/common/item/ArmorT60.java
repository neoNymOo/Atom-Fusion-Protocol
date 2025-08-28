package com.nymoo.afp.common.item;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.config.AFPConfig;
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
        AFPConfig.ArmorSet config = AFPConfig.getArmorSet("T-60");

        return EnumHelper.addArmorMaterial(
                "t60",
                "minecraft:diamond",
                config.durability,
                new int[]{config.bootsProtection, config.leggingsProtection, config.chestplateProtection, config.helmetProtection},
                config.enchantability,
                SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                config.toughness
        );
    }
}