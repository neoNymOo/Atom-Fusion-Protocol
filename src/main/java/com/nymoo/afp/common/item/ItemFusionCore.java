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
public class ItemFusionCore extends ModElementRegistry.ModElement {
    /**
     * Зарегистрированный предмет ядра синтеза
     */
    @GameRegistry.ObjectHolder("afp:fusion_core")
    public static final Item itemFusionCore = null;

    public ItemFusionCore(ModElementRegistry instance) {
        super(instance, 2);
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
        ModelLoader.setCustomModelResourceLocation(itemFusionCore, 0, new ModelResourceLocation("afp:fusion_core", "inventory"));
    }

    /**
     * Пользовательская реализация предмета ядра синтеза.
     * Хранит информацию об уровне истощения энергии и отображает ее в подсказке.
     */
    public static class ItemCustom extends Item {
        public ItemCustom() {
            setMaxDamage(0);
            maxStackSize = 1;
            setTranslationKey("fusion_core");
            setRegistryName("fusion_core");
            setCreativeTab(TabEquipment.tab);
        }

        /**
         * Добавляет информацию в подсказку предмета.
         * Отображает описание и текущий уровень истощения энергии ядра.
         *
         * @param stack   Стек предмета
         * @param world   Мир, в котором отображается подсказка
         * @param tooltip Список строк подсказки для добавления
         * @param flagIn  Флаг режима подсказки
         */
        @SideOnly(Side.CLIENT)
        @Override
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
            super.addInformation(stack, worldIn, tooltip, flagIn);

            tooltip.add(TextFormatting.GRAY + new TextComponentTranslation("tooltip.afp.fusion_core.description").getFormattedText());

            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            NBTTagCompound nbt = stack.getTagCompound();
            if (!nbt.hasKey("fusion_depletion")) {
                nbt.setInteger("fusion_depletion", 0);
            }

            int depletion = nbt.getInteger("fusion_depletion");
            tooltip.add(TextFormatting.GRAY + new TextComponentTranslation("tooltip.afp.fusion_core.depletion", depletion).getFormattedText());
        }
    }
}