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
public class ArmorExo extends AbstractPowerArmor {
    @GameRegistry.ObjectHolder("afp:exo_helmet")
    public static Item helmet;
    @GameRegistry.ObjectHolder("afp:exo_chestplate")
    public static Item chestplate;
    @GameRegistry.ObjectHolder("afp:exo_leggings")
    public static Item leggings;
    @GameRegistry.ObjectHolder("afp:exo_boots")
    public static Item boots;

    public ArmorExo(ModElementRegistry instance) {
        super(instance, 8, "exo", false);
    }

    @Override
    protected ItemArmor.ArmorMaterial getArmorMaterial() {
        AFPConfig.ArmorSet config = AFPConfig.getArmorSet("Exo");

        return EnumHelper.addArmorMaterial(
                "exo",
                "minecraft:diamond",
                config.durability,
                new int[]{config.bootsProtection, config.leggingsProtection, config.chestplateProtection, config.helmetProtection},
                config.enchantability,
                SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                config.toughness
        );
    }
}