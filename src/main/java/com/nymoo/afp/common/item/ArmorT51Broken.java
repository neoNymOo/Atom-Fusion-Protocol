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
public class ArmorT51Broken extends ModElementRegistry.ModElement {

    @GameRegistry.ObjectHolder("afp:t51_helmet_broken")
    public static final Item helmet = null;
    @GameRegistry.ObjectHolder("afp:t51_chestplate_broken")
    public static final Item chestplate = null;
    @GameRegistry.ObjectHolder("afp:t51_leggings_broken")
    public static final Item leggings = null;
    @GameRegistry.ObjectHolder("afp:t51_boots_broken")
    public static final Item boots = null;

    @SideOnly(Side.CLIENT)
    private PowerArmorModel helmetModel;
    @SideOnly(Side.CLIENT)
    private PowerArmorModel chestplateModel;
    @SideOnly(Side.CLIENT)
    private PowerArmorModel leggingsModel;
    @SideOnly(Side.CLIENT)
    private PowerArmorModel bootsModel;

    public ArmorT51Broken(ModElementRegistry instance) {
        super(instance, 12);
    }

    @Override
    public void initElements() {
        AFPConfig.ArmorSet config = AFPConfig.getArmorSet("T51");

        ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial(
                "t51_broken",
                "minecraft:diamond",
                config.durability,
                new int[]{config.bootsProtection, config.leggingsProtection, config.chestplateProtection, config.helmetProtection},
                config.enchantability,
                net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                config.toughness
        );

        elements.items.add(() -> new ItemT51Helmet(enuma, 0, EntityEquipmentSlot.HEAD)
                .setTranslationKey("t51_helmet_broken")
                .setRegistryName("t51_helmet_broken")
                .setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemT51Chestplate(enuma, 0, EntityEquipmentSlot.CHEST)
                .setTranslationKey("t51_chestplate_broken")
                .setRegistryName("t51_chestplate_broken")
                .setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemT51Leggings(enuma, 0, EntityEquipmentSlot.LEGS)
                .setTranslationKey("t51_leggings_broken")
                .setRegistryName("t51_leggings_broken")
                .setCreativeTab(TabPowerArmor.tab));

        elements.items.add(() -> new ItemT51Boots(enuma, 0, EntityEquipmentSlot.FEET)
                .setTranslationKey("t51_boots_broken")
                .setRegistryName("t51_boots_broken")
                .setCreativeTab(TabPowerArmor.tab));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(helmet, 0, new ModelResourceLocation("afp:t51/t51_helmet_broken", "inventory"));
        ModelLoader.setCustomModelResourceLocation(chestplate, 0, new ModelResourceLocation("afp:t51/t51_chestplate_broken", "inventory"));
        ModelLoader.setCustomModelResourceLocation(leggings, 0, new ModelResourceLocation("afp:t51/t51_leggings_broken", "inventory"));
        ModelLoader.setCustomModelResourceLocation(boots, 0, new ModelResourceLocation("afp:t51/t51_boots_broken", "inventory"));
    }

    public class ItemT51Helmet extends ItemArmor implements IPowerArmor {
        public ItemT51Helmet(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
            super(materialIn, renderIndexIn, equipmentSlotIn);
        }

        @Override
        public String getPowerArmorType() {
            return "t51_broken";
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            if (helmetModel == null) {
                helmetModel = new PowerArmorModel(0, "t51", false, true);
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

    public class ItemT51Chestplate extends ItemArmor implements IPowerArmor {
        public ItemT51Chestplate(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
            super(materialIn, renderIndexIn, equipmentSlotIn);
        }

        @Override
        public String getPowerArmorType() {
            return "t51_broken";
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            if (chestplateModel == null) {
                chestplateModel = new PowerArmorModel(1, "t51", false, true);
            }
            return chestplateModel;
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

    public class ItemT51Leggings extends ItemArmor implements IPowerArmor {
        public ItemT51Leggings(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
            super(materialIn, renderIndexIn, equipmentSlotIn);
        }

        @Override
        public String getPowerArmorType() {
            return "t51_broken";
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            if (leggingsModel == null) {
                leggingsModel = new PowerArmorModel(2, "t51", false, true);
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

    public class ItemT51Boots extends ItemArmor implements IPowerArmor {
        public ItemT51Boots(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
            super(materialIn, renderIndexIn, equipmentSlotIn);
        }

        @Override
        public String getPowerArmorType() {
            return "t51_broken";
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            if (bootsModel == null) {
                bootsModel = new PowerArmorModel(3, "t51", false, true);
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