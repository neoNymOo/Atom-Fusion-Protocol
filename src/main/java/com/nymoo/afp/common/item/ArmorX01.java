package com.nymoo.afp.common.item;

import com.nymoo.afp.ModElementRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

@ModElementRegistry.ModElement.Tag
public class ArmorX01 extends AbstractPowerArmor {
    @GameRegistry.ObjectHolder("afp:x01_helmet")
    public static Item helmet;
    @GameRegistry.ObjectHolder("afp:x01_chestplate")
    public static Item chestplate;
    @GameRegistry.ObjectHolder("afp:x01_leggings")
    public static Item leggings;
    @GameRegistry.ObjectHolder("afp:x01_boots")
    public static Item boots;

    public ArmorX01(ModElementRegistry instance) {
        super(instance, 4, "x01", true);
    }

    @Override
    protected ItemArmor.ArmorMaterial getArmorMaterial() {
        return EnumHelper.addArmorMaterial(
                "x01",
                "minecraft:diamond",
                67,
                new int[]{19, 24, 28, 19},
                0,
                SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                4f
        );
    }
}
