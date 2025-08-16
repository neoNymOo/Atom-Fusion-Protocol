package com.nymoo.afp.common.item;

import com.nymoo.afp.ModElementRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

@ModElementRegistry.ModElement.Tag
public class ArmorT45 extends AbstractPowerArmor {
    @GameRegistry.ObjectHolder("afp:t45_helmet")
    public static Item helmet;
    @GameRegistry.ObjectHolder("afp:t45_chestplate")
    public static Item chestplate;
    @GameRegistry.ObjectHolder("afp:t45_leggings")
    public static Item leggings;
    @GameRegistry.ObjectHolder("afp:t45_boots")
    public static Item boots;

    public ArmorT45(ModElementRegistry instance) {
        super(instance, 7, "t45", true);
    }

    @Override
    protected ItemArmor.ArmorMaterial getArmorMaterial() {
        return EnumHelper.addArmorMaterial(
                "t45",
                "minecraft:diamond",
                69,
                new int[]{12, 17, 19, 14},
                0,
                SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                4f
        );
    }
}