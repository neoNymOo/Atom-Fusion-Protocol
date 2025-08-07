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
public class ArmorX01 extends ElementsAFP.ModElement {
    @GameRegistry.ObjectHolder("afp:x01_helmet")
    public static final Item helmet = null;
    @GameRegistry.ObjectHolder("afp:x01_chestplate")
    public static final Item body = null;
    @GameRegistry.ObjectHolder("afp:x01_leggings")
    public static final Item legs = null;
    @GameRegistry.ObjectHolder("afp:x01_boots")
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

    public ArmorX01(ElementsAFP instance) {
        super(instance, 3);
    }

    @Override
    public void initElements() {
        ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial("x01", "minecraft:diamond", 67, new int[]{19, 24, 28, 19}, 0,
                (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("")), 4f);

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.HEAD) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                if (helmetModel == null) {
                    helmetModel = new ModelPowerArmor(0, "x01", false);
                }
                return helmetModel;
            }
        }.setTranslationKey("x01_helmet").setRegistryName("x01_helmet").setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.CHEST) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                boolean jetpack = stack.hasTagCompound() && stack.getTagCompound().getBoolean("jetpack");

                if (jetpack) {
                    if (chestplateModelJet == null) {
                        chestplateModelJet = new ModelPowerArmor(1, "x01", true);
                    }
                    return chestplateModelJet;
                } else {
                    if (chestplateModel == null) {
                        chestplateModel = new ModelPowerArmor(1, "x01", false);
                    }
                    return chestplateModel;
                }
            }
        }.setTranslationKey("x01_chestplate").setRegistryName("x01_chestplate").setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.LEGS) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                if (leggingsModel == null) {
                    leggingsModel = new ModelPowerArmor(2, "x01", false);
                }
                return leggingsModel;
            }
        }.setTranslationKey("x01_leggings").setRegistryName("x01_leggings").setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.FEET) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                if (bootsModel == null) {
                    bootsModel = new ModelPowerArmor(3, "x01", false);
                }
                return bootsModel;
            }
        }.setTranslationKey("x01_boots").setRegistryName("x01_boots").setCreativeTab(TabPowerArmor.tab));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(helmet, 0, new ModelResourceLocation("afp:x01/x01_helmet", "inventory"));
        ModelLoader.setCustomModelResourceLocation(legs, 0, new ModelResourceLocation("afp:x01/x01_leggings", "inventory"));
        ModelLoader.setCustomModelResourceLocation(boots, 0, new ModelResourceLocation("afp:x01/x01_boots", "inventory"));

        ModelLoader.setCustomMeshDefinition(body, stack -> {
            boolean jetpack = stack.getTagCompound() != null && stack.getTagCompound().getBoolean("jetpack");
            String modelPath = jetpack ? "afp:x01/x01_j_chestplate" : "afp:x01/x01_chestplate";
            return new ModelResourceLocation(modelPath, "inventory");
        });

        ModelLoader.registerItemVariants(
                body,
                new ModelResourceLocation("afp:x01/x01_chestplate", "inventory"),
                new ModelResourceLocation("afp:x01/x01_j_chestplate", "inventory")
        );
    }
}