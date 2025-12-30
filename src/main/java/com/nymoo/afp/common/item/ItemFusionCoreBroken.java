package com.nymoo.afp.common.item;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.tab.TabEquipment;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Элемент мода для регистрации ядра синтеза.
 * Ядро синтеза используется как источник энергии для силовой брони.
 */
@ModElementRegistry.ModElement.Tag
public class ItemFusionCoreBroken extends ModElementRegistry.ModElement {
    /**
     * Зарегистрированный предмет ядра синтеза
     */
    @GameRegistry.ObjectHolder("afp:fusion_core_broken")
    public static final Item itemFusionCore = null;

    public ItemFusionCoreBroken(ModElementRegistry instance) {
        super(instance, 3);
    }

    @Override
    public void initElements() {
        elements.items.add(() -> new ItemCustom());
    }

    /**
     * Регистрирует модель предмета ядра синтеза для клиентской части.
     *
     * @param event Событие регистрации моделей
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemFusionCore, 0, new ModelResourceLocation("afp:fusion_core_broken", "inventory"));
    }

    /**
     * Пользовательская реализация предмета ядра синтеза.
     * Хранит информацию об уровне истощения энергии и отображает ее в подсказке.
     */
    public static class ItemCustom extends Item {
        public ItemCustom() {
            setMaxDamage(0);
            maxStackSize = 1;
            setTranslationKey("fusion_core_broken");
            setRegistryName("fusion_core_broken");
            setCreativeTab(TabEquipment.tab);
        }
    }
}