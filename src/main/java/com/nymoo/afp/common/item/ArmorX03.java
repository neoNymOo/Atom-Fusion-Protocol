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
public class ArmorX03 extends AbstractPowerArmor {
    @GameRegistry.ObjectHolder("afp:x03_helmet")
    public static Item helmet;
    @GameRegistry.ObjectHolder("afp:x03_chestplate")
    public static Item chestplate;
    @GameRegistry.ObjectHolder("afp:x03_leggings")
    public static Item leggings;
    @GameRegistry.ObjectHolder("afp:x03_boots")
    public static Item boots;

    public ArmorX03(ModElementRegistry instance) {
        super(instance, 2, "x03", true);
    }

    @Override
    protected ItemArmor.ArmorMaterial getArmorMaterial() {
        AFPConfig.ArmorSet config = AFPConfig.getArmorSet("X-03");

        return EnumHelper.addArmorMaterial(
                "x03",
                "minecraft:diamond",
                config.durability,
                new int[]{config.bootsProtection, config.leggingsProtection, config.chestplateProtection, config.helmetProtection},
                config.enchantability,
                SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                config.toughness
        );
    }
}