package com.nymoo.afp.common.item;

import com.hbm.items.tool.ItemGeigerCounter;
import com.nymoo.afp.AtomFusionProtocol;
import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.render.model.armor.PowerArmorModel;
import com.nymoo.afp.common.tab.TabPowerArmor;
import com.nymoo.afp.common.util.UtilPowerArmor;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ModElementRegistry.ModElement.Tag
public class ArmorX02 extends ModElementRegistry.ModElement {

    @GameRegistry.ObjectHolder("afp:x02_helmet")
    public static final Item helmet = null;
    @GameRegistry.ObjectHolder("afp:x02_chestplate")
    public static final Item chestplate = null;
    @GameRegistry.ObjectHolder("afp:x02_leggings")
    public static final Item leggings = null;
    @GameRegistry.ObjectHolder("afp:x02_boots")
    public static final Item boots = null;

    @SideOnly(Side.CLIENT)
    private PowerArmorModel helmetModel;
    @SideOnly(Side.CLIENT)
    private PowerArmorModel chestplateModel;
    @SideOnly(Side.CLIENT)
    private PowerArmorModel chestplateModelJet;
    @SideOnly(Side.CLIENT)
    private PowerArmorModel leggingsModel;
    @SideOnly(Side.CLIENT)
    private PowerArmorModel bootsModel;

    public ArmorX02(ModElementRegistry instance) {
        super(instance, 3);
    }

    @Override
    public void initElements() {
        AFPConfig.ArmorSet config = AFPConfig.getArmorSet("X02");

        ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial(
                "x02",
                "minecraft:diamond",
                config.durability,
                new int[]{config.bootsProtection, config.leggingsProtection, config.chestplateProtection, config.helmetProtection},
                config.enchantability,
                net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                config.toughness
        );

        elements.items.add(() -> new ItemX02Helmet(enuma, 0, EntityEquipmentSlot.HEAD)
                .setTranslationKey("x02_helmet")
                .setRegistryName("x02_helmet")
                .setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemX02Chestplate(enuma, 0, EntityEquipmentSlot.CHEST)
                .setTranslationKey("x02_chestplate")
                .setRegistryName("x02_chestplate")
                .setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemX02Leggings(enuma, 0, EntityEquipmentSlot.LEGS)
                .setTranslationKey("x02_leggings")
                .setRegistryName("x02_leggings")
                .setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemX02Boots(enuma, 0, EntityEquipmentSlot.FEET)
                .setTranslationKey("x02_boots")
                .setRegistryName("x02_boots")
                .setCreativeTab(TabPowerArmor.tab));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(helmet, 0, new ModelResourceLocation("afp:x02/x02_helmet", "inventory"));
        ModelLoader.setCustomModelResourceLocation(leggings, 0, new ModelResourceLocation("afp:x02/x02_leggings", "inventory"));
        ModelLoader.setCustomModelResourceLocation(boots, 0, new ModelResourceLocation("afp:x02/x02_boots", "inventory"));

        ModelLoader.setCustomMeshDefinition(chestplate, stack -> {
            boolean jetpack = stack.getTagCompound() != null && stack.getTagCompound().getBoolean("jetpack");
            String modelPath = jetpack ? "afp:x02/x02_chestplate_jetpack" : "afp:x02/x02_chestplate";
            return new ModelResourceLocation(modelPath, "inventory");
        });

        ModelLoader.registerItemVariants(
                chestplate,
                new ModelResourceLocation("afp:x02/x02_chestplate", "inventory"),
                new ModelResourceLocation("afp:x02/x02_chestplate_jetpack", "inventory")
        );
    }

    public class ItemX02Helmet extends ItemArmor implements IPowerArmor {
        public ItemX02Helmet(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
            super(materialIn, renderIndexIn, equipmentSlotIn);
        }

        @Override
        public String getPowerArmorType() {
            return "x02";
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            if (helmetModel == null) {
                helmetModel = new PowerArmorModel(0, "x02", false, false);
            }
            return helmetModel;
        }

        @Override
        public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
            return AFPConfig.canPlayerEquipPowerArmor;
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
            return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
        }
    }

    public class ItemX02Chestplate extends ItemArmor implements IPowerArmor {
        public ItemX02Chestplate(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
            super(materialIn, renderIndexIn, equipmentSlotIn);
        }

        @Override
        public String getPowerArmorType() {
            return "x02";
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            boolean jetpack = stack.hasTagCompound() && stack.getTagCompound().getBoolean("jetpack");

            if (jetpack) {
                if (chestplateModelJet == null) {
                    chestplateModelJet = new PowerArmorModel(1, "x02", true, false);
                }
                return chestplateModelJet;
            } else {
                if (chestplateModel == null) {
                    chestplateModel = new PowerArmorModel(1, "x02", false, false);
                }
                return chestplateModel;
            }
        }

        @Override
        public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
            return AFPConfig.canPlayerEquipPowerArmor;
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
            return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
        }

        @Override
        public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
            UtilPowerArmor.handleStepSound(world, player);
            if (AtomFusionProtocol.IS_HBM_LOADED) {
                ItemGeigerCounter.playGeiger(world, player);
            }
            UtilPowerArmor.handleEnergyDepletion(world, player, itemStack);
        }
    }

    public class ItemX02Leggings extends ItemArmor implements IPowerArmor {
        public ItemX02Leggings(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
            super(materialIn, renderIndexIn, equipmentSlotIn);
        }

        @Override
        public String getPowerArmorType() {
            return "x02";
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            if (leggingsModel == null) {
                leggingsModel = new PowerArmorModel(2, "x02", false, false);
            }
            return leggingsModel;
        }

        @Override
        public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
            return AFPConfig.canPlayerEquipPowerArmor;
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
            return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
        }
    }

    public class ItemX02Boots extends ItemArmor implements IPowerArmor {
        public ItemX02Boots(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
            super(materialIn, renderIndexIn, equipmentSlotIn);
        }

        @Override
        public String getPowerArmorType() {
            return "x02";
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            if (bootsModel == null) {
                bootsModel = new PowerArmorModel(3, "x02", false, false);
            }
            return bootsModel;
        }

        @Override
        public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
            return AFPConfig.canPlayerEquipPowerArmor;
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
            return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
        }
    }
}