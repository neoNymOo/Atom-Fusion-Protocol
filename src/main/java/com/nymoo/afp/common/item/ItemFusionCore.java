package com.nymoo.afp.common.item;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.tab.TabEquipment;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ModElementRegistry.ModElement.Tag
public class ItemFusionCore extends ModElementRegistry.ModElement {
    @GameRegistry.ObjectHolder("afp:fusion_core")
    public static final Item itemFusionCore = null;

    public ItemFusionCore(ModElementRegistry instance) {
        super(instance, 2);
    }

    @Override
    public void initElements() {
        elements.items.add(() -> new ItemCustom());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemFusionCore, 0, new ModelResourceLocation("afp:fusion_core", "inventory"));
    }

    public static class ItemCustom extends Item {
        public ItemCustom() {
            setMaxDamage(0);
            maxStackSize = 1;
            setTranslationKey("fusion_core");
            setRegistryName("fusion_core");
            setCreativeTab(TabEquipment.tab);
        }
    }
}