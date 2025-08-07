package com.nymoo.afp.common.items;

import com.nymoo.afp.ElementsAFP;
import com.nymoo.afp.common.render.model.armor.ModelPowerArmor;
import com.nymoo.afp.common.tabs.TabPowerArmor;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ElementsAFP.ModElement.Tag
public class ArmorX02 extends ElementsAFP.ModElement {
    @GameRegistry.ObjectHolder("afp:x02_helmet")
    public static final Item helmet = null;
    @GameRegistry.ObjectHolder("afp:x02_chestplate")
    public static final Item body = null;
    @GameRegistry.ObjectHolder("afp:x02_leggings")
    public static final Item legs = null;
    @GameRegistry.ObjectHolder("afp:x02_boots")
    public static final Item boots = null;

    @SideOnly(Side.CLIENT)
    private ModelPowerArmor helmetModel;
    @SideOnly(Side.CLIENT)
    private ModelPowerArmor chestplateModel;
    @SideOnly(Side.CLIENT)
    private ModelPowerArmor chestplateModelJet;
    @SideOnly(Side.CLIENT)
    private ModelPowerArmor leggingsModel;
    @SideOnly(Side.CLIENT)
    private ModelPowerArmor bootsModel;

    public ArmorX02(ElementsAFP instance) {
        super(instance, 2);
    }

    @Override
    public void initElements() {
        ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial("x02", "minecraft:diamond", 12, new int[]{17, 22, 27, 17}, 0,
                (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("")), 4f);

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.HEAD) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                if (helmetModel == null) {
                    helmetModel = new ModelPowerArmor(0, "x02", false);
                }
                return helmetModel;
            }
        }.setTranslationKey("x02_helmet").setRegistryName("x02_helmet").setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.CHEST) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                boolean jetpack = stack.hasTagCompound() && stack.getTagCompound().getBoolean("jetpack");

                if (jetpack) {
                    if (chestplateModelJet == null) {
                        chestplateModelJet = new ModelPowerArmor(1, "x02", true);
                    }
                    return chestplateModelJet;
                } else {
                    if (chestplateModel == null) {
                        chestplateModel = new ModelPowerArmor(1, "x02", false);
                    }
                    return chestplateModel;
                }
            }
        }.setTranslationKey("x02_chestplate").setRegistryName("x02_chestplate").setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.LEGS) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                if (leggingsModel == null) {
                    leggingsModel = new ModelPowerArmor(2, "x02", false);
                }
                return leggingsModel;
            }
        }.setTranslationKey("x02_leggings").setRegistryName("x02_leggings").setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.FEET) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                if (bootsModel == null) {
                    bootsModel = new ModelPowerArmor(3, "x02", false);
                }
                return bootsModel;
            }
        }.setTranslationKey("x02_boots").setRegistryName("x02_boots").setCreativeTab(TabPowerArmor.tab));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(helmet, 0, new ModelResourceLocation("afp:x02/x02_helmet", "inventory"));
        ModelLoader.setCustomModelResourceLocation(legs, 0, new ModelResourceLocation("afp:x02/x02_leggings", "inventory"));
        ModelLoader.setCustomModelResourceLocation(boots, 0, new ModelResourceLocation("afp:x02/x02_boots", "inventory"));

        ModelLoader.setCustomMeshDefinition(body, stack -> {
            boolean jetpack = stack.getTagCompound() != null && stack.getTagCompound().getBoolean("jetpack");
            String modelPath = jetpack ? "afp:x02/x02_j_chestplate" : "afp:x02/x02_chestplate";
            return new ModelResourceLocation(modelPath, "inventory");
        });

        ModelLoader.registerItemVariants(
                body,
                new ModelResourceLocation("afp:x02/x02_chestplate", "inventory"),
                new ModelResourceLocation("afp:x02/x02_j_chestplate", "inventory")
        );
    }
}