package com.nymoo.afp.common.item;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.util.UtilPowerArmor;
import net.minecraft.client.model.ModelBiped;
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
public class ArmorExo extends ModElementRegistry.ModElement {
    @GameRegistry.ObjectHolder("afp:exo_helmet")
    public static final Item helmet = null;
    @GameRegistry.ObjectHolder("afp:exo_chestplate")
    public static final Item chestplate = null;
    @GameRegistry.ObjectHolder("afp:exo_leggings")
    public static final Item leggings = null;
    @GameRegistry.ObjectHolder("afp:exo_boots")
    public static final Item boots = null;

    public ArmorExo(ModElementRegistry instance) {
        super(instance, 8);
    }

    @Override
    public void initElements() {
        AFPConfig.ArmorSet config = AFPConfig.getArmorSet("Exo");
        ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial(
                "exo",
                "minecraft:diamond",
                config.durability,
                new int[]{config.bootsProtection, config.leggingsProtection, config.chestplateProtection, config.helmetProtection},
                config.enchantability,
                net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("")),
                config.toughness
        );
        elements.items.add(() -> new ItemExoHelmet(enuma, 0, EntityEquipmentSlot.HEAD)
                .setTranslationKey("exo_helmet")
                .setRegistryName("exo_helmet")
                .setCreativeTab(null));
        elements.items.add(() -> new ItemExoChestplate(enuma, 0, EntityEquipmentSlot.CHEST)
                .setTranslationKey("exo_chestplate")
                .setRegistryName("exo_chestplate")
                .setCreativeTab(null));
        elements.items.add(() -> new ItemExoLeggings(enuma, 0, EntityEquipmentSlot.LEGS)
                .setTranslationKey("exo_leggings")
                .setRegistryName("exo_leggings")
                .setCreativeTab(null));
        elements.items.add(() -> new ItemExoBoots(enuma, 0, EntityEquipmentSlot.FEET)
                .setTranslationKey("exo_boots")
                .setRegistryName("exo_boots")
                .setCreativeTab(null));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(helmet, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation("afp:exo/exo_helmet", "inventory"));
        ModelLoader.setCustomModelResourceLocation(chestplate, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation("afp:exo/exo_chestplate", "inventory"));
        ModelLoader.setCustomModelResourceLocation(leggings, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation("afp:exo/exo_leggings", "inventory"));
        ModelLoader.setCustomModelResourceLocation(boots, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation("afp:exo/exo_boots", "inventory"));
    }

    @SideOnly(Side.CLIENT)
    public static ModelBiped getBipedModel(EntityLivingBase entity, EntityEquipmentSlot slot) {
        ModelBiped model = new ModelBiped(0.0F);
        model.bipedHead.showModel = slot == EntityEquipmentSlot.HEAD;
        model.bipedBody.showModel = (slot == EntityEquipmentSlot.CHEST) || (slot == EntityEquipmentSlot.LEGS);
        model.bipedRightArm.showModel = slot == EntityEquipmentSlot.CHEST;
        model.bipedLeftArm.showModel = slot == EntityEquipmentSlot.CHEST;
        model.bipedRightLeg.showModel = (slot == EntityEquipmentSlot.LEGS) || (slot == EntityEquipmentSlot.FEET);
        model.bipedLeftLeg.showModel = (slot == EntityEquipmentSlot.LEGS) || (slot == EntityEquipmentSlot.FEET);
        model.isSneak = entity.isSneaking();
        model.isRiding = entity.isRiding();
        model.isChild = entity.isChild();
        return model;
    }

    public class ItemExoHelmet extends ItemArmor implements IPowerArmor {
        public ItemExoHelmet(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
            super(materialIn, renderIndexIn, equipmentSlotIn);
        }

        @Override
        public String getPowerArmorType() {
            return "exo";
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
            return "afp:textures/models/armor/exo/exo_full.png";
        }

        @Override
        public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
            return AFPConfig.canPlayerEquipPowerArmor;
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
            return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
        }

        @SideOnly(Side.CLIENT)
        @Override
        public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            ModelBiped model = new ModelBiped(0.0F);
            model.bipedHead.showModel = slot == EntityEquipmentSlot.HEAD;
            model.bipedBody.showModel = (slot == EntityEquipmentSlot.CHEST) || (slot == EntityEquipmentSlot.LEGS);
            model.bipedRightArm.showModel = slot == EntityEquipmentSlot.CHEST;
            model.bipedLeftArm.showModel = slot == EntityEquipmentSlot.CHEST;
            model.bipedRightLeg.showModel = (slot == EntityEquipmentSlot.LEGS) || (slot == EntityEquipmentSlot.FEET);
            model.bipedLeftLeg.showModel = (slot == EntityEquipmentSlot.LEGS) || (slot == EntityEquipmentSlot.FEET);
            model.isSneak = entity.isSneaking();
            model.isRiding = entity.isRiding();
            model.isChild = entity.isChild();
            return model;
        }
    }

    public class ItemExoChestplate extends ItemArmor implements IPowerArmor {
        public ItemExoChestplate(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
            super(materialIn, renderIndexIn, equipmentSlotIn);
        }

        @Override
        public String getPowerArmorType() {
            return "exo";
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
            return "afp:textures/models/armor/exo/exo_full.png";
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
            UtilPowerArmor.handleEnergyDepletion(world, player, itemStack);
        }

        @SideOnly(Side.CLIENT)
        @Override
        public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            ModelBiped model = new ModelBiped(0.0F);
            model.bipedHead.showModel = slot == EntityEquipmentSlot.HEAD;
            model.bipedBody.showModel = (slot == EntityEquipmentSlot.CHEST) || (slot == EntityEquipmentSlot.LEGS);
            model.bipedRightArm.showModel = slot == EntityEquipmentSlot.CHEST;
            model.bipedLeftArm.showModel = slot == EntityEquipmentSlot.CHEST;
            model.bipedRightLeg.showModel = (slot == EntityEquipmentSlot.LEGS) || (slot == EntityEquipmentSlot.FEET);
            model.bipedLeftLeg.showModel = (slot == EntityEquipmentSlot.LEGS) || (slot == EntityEquipmentSlot.FEET);
            model.isSneak = entity.isSneaking();
            model.isRiding = entity.isRiding();
            model.isChild = entity.isChild();
            return model;
        }
    }

    public class ItemExoLeggings extends ItemArmor implements IPowerArmor {
        public ItemExoLeggings(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
            super(materialIn, renderIndexIn, equipmentSlotIn);
        }

        @Override
        public String getPowerArmorType() {
            return "exo";
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
            return "afp:textures/models/armor/exo/exo_full.png";
        }

        @Override
        public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
            return AFPConfig.canPlayerEquipPowerArmor;
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
            return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
        }

        @SideOnly(Side.CLIENT)
        @Override
        public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            ModelBiped model = new ModelBiped(0.0F);
            model.bipedHead.showModel = slot == EntityEquipmentSlot.HEAD;
            model.bipedBody.showModel = (slot == EntityEquipmentSlot.CHEST) || (slot == EntityEquipmentSlot.LEGS);
            model.bipedRightArm.showModel = slot == EntityEquipmentSlot.CHEST;
            model.bipedLeftArm.showModel = slot == EntityEquipmentSlot.CHEST;
            model.bipedRightLeg.showModel = (slot == EntityEquipmentSlot.LEGS) || (slot == EntityEquipmentSlot.FEET);
            model.bipedLeftLeg.showModel = (slot == EntityEquipmentSlot.LEGS) || (slot == EntityEquipmentSlot.FEET);
            model.isSneak = entity.isSneaking();
            model.isRiding = entity.isRiding();
            model.isChild = entity.isChild();
            return model;
        }
    }

    public class ItemExoBoots extends ItemArmor implements IPowerArmor {
        public ItemExoBoots(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
            super(materialIn, renderIndexIn, equipmentSlotIn);
        }

        @Override
        public String getPowerArmorType() {
            return "exo";
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
            return "afp:textures/models/armor/exo/exo_full.png";
        }

        @Override
        public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
            return AFPConfig.canPlayerEquipPowerArmor;
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
            return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
        }

        @SideOnly(Side.CLIENT)
        @Override
        public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            ModelBiped model = new ModelBiped(0.0F);
            model.bipedHead.showModel = slot == EntityEquipmentSlot.HEAD;
            model.bipedBody.showModel = (slot == EntityEquipmentSlot.CHEST) || (slot == EntityEquipmentSlot.LEGS);
            model.bipedRightArm.showModel = slot == EntityEquipmentSlot.CHEST;
            model.bipedLeftArm.showModel = slot == EntityEquipmentSlot.CHEST;
            model.bipedRightLeg.showModel = (slot == EntityEquipmentSlot.LEGS) || (slot == EntityEquipmentSlot.FEET);
            model.bipedLeftLeg.showModel = (slot == EntityEquipmentSlot.LEGS) || (slot == EntityEquipmentSlot.FEET);
            model.isSneak = entity.isSneaking();
            model.isRiding = entity.isRiding();
            model.isChild = entity.isChild();
            return model;
        }
    }
}