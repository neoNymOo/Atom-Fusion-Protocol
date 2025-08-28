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
public class ArmorT51 extends AbstractPowerArmor {
    @GameRegistry.ObjectHolder("afp:t51_helmet")
    public static Item helmet;
    @GameRegistry.ObjectHolder("afp:t51_chestplate")
    public static Item chestplate;
    @GameRegistry.ObjectHolder("afp:t51_leggings")
    public static Item leggings;
    @GameRegistry.ObjectHolder("afp:t51_boots")
    public static Item boots;

    public ArmorT51(ModElementRegistry instance) {
        super(instance, 6, "t51", true);
    }

    @Override
    protected ItemArmor.ArmorMaterial getArmorMaterial() {
        AFPConfig.ArmorSet config = AFPConfig.getArmorSet("T-51");

        return EnumHelper.addArmorMaterial(
                "t51",
                "minecraft:diamond",
                config.durability,
                new int[]{config.bootsProtection, config.leggingsProtection, config.chestplateProtection, config.helmetProtection},
                config.enchantability,
                SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                config.toughness
        );
    }
}