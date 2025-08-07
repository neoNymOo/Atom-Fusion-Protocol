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
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ElementsAFP.ModElement.Tag
public class ArmorExo extends ElementsAFP.ModElement {
    @GameRegistry.ObjectHolder("afp:exo_helmet")
    public static final Item helmet = null;
    @GameRegistry.ObjectHolder("afp:exo_chestplate")
    public static final Item body = null;
    @GameRegistry.ObjectHolder("afp:exo_leggings")
    public static final Item legs = null;
    @GameRegistry.ObjectHolder("afp:exo_boots")
    public static final Item boots = null;

    @SideOnly(Side.CLIENT)
    private ModelPowerArmor helmetModel;
    @SideOnly(Side.CLIENT)
    private ModelPowerArmor chestplateModel;
    @SideOnly(Side.CLIENT)
    private ModelPowerArmor leggingsModel;
    @SideOnly(Side.CLIENT)
    private ModelPowerArmor bootsModel;

    public ArmorExo(ElementsAFP instance) {
        super(instance, 7);
    }

    @Override
    public void initElements() {
        ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial("exo", "minecraft:diamond", 34, new int[]{2, 5, 6, 2}, 0,
                (SoundEvent) SoundEvent.REGISTRY.getObject(new ResourceLocation("")), 0f);

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.HEAD) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                if (helmetModel == null) {
                    helmetModel = new ModelPowerArmor(0, "exo", false);
                }
                return helmetModel;
            }

            @Override
            public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
                return false;
            }
        }.setTranslationKey("exo_helmet").setRegistryName("exo_helmet").setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.CHEST) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                boolean jetpack = stack.hasTagCompound() && stack.getTagCompound().getBoolean("jetpack");
                    if (chestplateModel == null) {
                        chestplateModel = new ModelPowerArmor(1, "exo", false);
                    }
                    return chestplateModel;
                }

            @Override
            public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
                return false;
            }

            @Override
            public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
                PowerArmorUtil.handleStepSound(world, player);
            }
        }.setTranslationKey("exo_chestplate").setRegistryName("exo_chestplate").setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.LEGS) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                if (leggingsModel == null) {
                    leggingsModel = new ModelPowerArmor(2, "exo", false);
                }
                return leggingsModel;
            }

            @Override
            public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
                return false;
            }
        }.setTranslationKey("exo_leggings").setRegistryName("exo_leggings").setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.FEET) {
            @Override
            @SideOnly(Side.CLIENT)
            public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
                if (bootsModel == null) {
                    bootsModel = new ModelPowerArmor(3, "exo", false);
                }
                return bootsModel;
            }

            @Override
            public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
                return false;
            }
        }.setTranslationKey("exo_boots").setRegistryName("exo_boots").setCreativeTab(TabPowerArmor.tab));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(helmet, 0, new ModelResourceLocation("afp:exo/exo_helmet", "inventory"));
        ModelLoader.setCustomModelResourceLocation(body, 0, new ModelResourceLocation("afp:exo/exo_chestplate", "inventory"));
        ModelLoader.setCustomModelResourceLocation(legs, 0, new ModelResourceLocation("afp:exo/exo_leggings", "inventory"));
        ModelLoader.setCustomModelResourceLocation(boots, 0, new ModelResourceLocation("afp:exo/exo_boots", "inventory"));
    }
}