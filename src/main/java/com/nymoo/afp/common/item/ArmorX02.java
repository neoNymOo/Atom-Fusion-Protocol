package com.nymoo.afp.common.item;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.config.AFPConfig;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

@ModElementRegistry.ModElement.Tag
public class ArmorX02 extends AbstractPowerArmor {
    @GameRegistry.ObjectHolder("afp:x02_helmet")
    public static Item helmet;
    @GameRegistry.ObjectHolder("afp:x02_chestplate")
    public static Item chestplate;
    @GameRegistry.ObjectHolder("afp:x02_leggings")
    public static Item leggings;
    @GameRegistry.ObjectHolder("afp:x02_boots")
    public static Item boots;

    public ArmorX02(ModElementRegistry instance) {
        super(instance, 3, "x02", true);
    }

    @Override
    protected ItemArmor.ArmorMaterial getArmorMaterial() {
        AFPConfig.ArmorSet config = AFPConfig.getArmorSet("X-02");

        return EnumHelper.addArmorMaterial(
                "x02",
                "minecraft:diamond",
                config.durability,
                new int[]{config.bootsProtection, config.leggingsProtection, config.chestplateProtection, config.helmetProtection},
                config.enchantability,
                SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                config.toughness
        );
    }
}