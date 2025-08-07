package com.nymoo.afp.common.items;

import com.nymoo.afp.ElementsAFP;
import com.nymoo.afp.common.render.model.armor.ModelPowerArmor;
import com.nymoo.afp.common.tabs.TabPowerArmor;
import com.nymoo.afp.common.utils.PowerArmorUtil;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ElementsAFP.ModElement.Tag
public class ArmorT45 extends ElementsAFP.ModElement {
    @GameRegistry.ObjectHolder("afp:t45_helmet")
    public static final Item helmet = null;
    @GameRegistry.ObjectHolder("afp:t45_chestplate")
    public static final Item body = null;
    @GameRegistry.ObjectHolder("afp:t45_leggings")
    public static final Item legs = null;
    @GameRegistry.ObjectHolder("afp:t45_boots")
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

    public ArmorT45(ElementsAFP instance) {
        super(instance, 6);
    }

    @Override
    public void initElements() {
        ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial("t45", "minecraft:diamond", 69, new int[]{12, 17, 19, 14}, 0,
                (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("")), 4f);

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.HEAD) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                if (helmetModel == null) {
                    helmetModel = new ModelPowerArmor(0, "t45", false);
                }
                return helmetModel;
            }

            @Override
            public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
                return false;
            }
        }.setTranslationKey("t45_helmet").setRegistryName("t45_helmet").setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.CHEST) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                boolean jetpack = stack.hasTagCompound() && stack.getTagCompound().getBoolean("jetpack");

                if (jetpack) {
                    if (chestplateModelJet == null) {
                        chestplateModelJet = new ModelPowerArmor(1, "t45", true);
                    }
                    return chestplateModelJet;
                } else {
                    if (chestplateModel == null) {
                        chestplateModel = new ModelPowerArmor(1, "t45", false);
                    }
                    return chestplateModel;
                }
            }

            @Override
            public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
                return false;
            }

            @Override
            public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
                PowerArmorUtil.handleStepSound(world, player);
            }
        }.setTranslationKey("t45_chestplate").setRegistryName("t45_chestplate").setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.LEGS) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                if (leggingsModel == null) {
                    leggingsModel = new ModelPowerArmor(2, "t45", false);
                }
                return leggingsModel;
            }

            @Override
            public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
                return false;
            }
        }.setTranslationKey("t45_leggings").setRegistryName("t45_leggings").setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.FEET) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                if (bootsModel == null) {
                    bootsModel = new ModelPowerArmor(3, "t45", false);
                }
                return bootsModel;
            }

            @Override
            public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
                return false;
            }
        }.setTranslationKey("t45_boots").setRegistryName("t45_boots").setCreativeTab(TabPowerArmor.tab));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(helmet, 0, new ModelResourceLocation("afp:t45/t45_helmet", "inventory"));
        ModelLoader.setCustomModelResourceLocation(legs, 0, new ModelResourceLocation("afp:t45/t45_leggings", "inventory"));
        ModelLoader.setCustomModelResourceLocation(boots, 0, new ModelResourceLocation("afp:t45/t45_boots", "inventory"));

        ModelLoader.setCustomMeshDefinition(body, stack -> {
            boolean jetpack = stack.getTagCompound() != null && stack.getTagCompound().getBoolean("jetpack");
            String modelPath = jetpack ? "afp:t45/t45_j_chestplate" : "afp:t45/t45_chestplate";
            return new ModelResourceLocation(modelPath, "inventory");
        });

        ModelLoader.registerItemVariants(
                body,
                new ModelResourceLocation("afp:t45/t45_chestplate", "inventory"),
                new ModelResourceLocation("afp:t45/t45_j_chestplate", "inventory")
        );
    }
}