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
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AbstractPowerArmor extends ModElementRegistry.ModElement {
    protected final String armorName;
    protected final boolean hasJetpack;

    protected Item helmetItem;
    protected Item bodyItem;
    protected Item legsItem;
    protected Item bootsItem;

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

    public AbstractPowerArmor(ModElementRegistry instance, int sortid, String armorName, boolean hasJetpack) {
        super(instance, sortid);
        this.armorName = armorName;
        this.hasJetpack = hasJetpack;
    }

    public String getArmorName() {
        return armorName;
    }

    public boolean hasJetpack() {
        return hasJetpack;
    }

    protected abstract ItemArmor.ArmorMaterial getArmorMaterial();

    @Override
    public void initElements() {
        ItemArmor.ArmorMaterial material = getArmorMaterial();

        helmetItem = new PowerArmorItem(material, EntityEquipmentSlot.HEAD, 0);
        bodyItem = new PowerArmorItem(material, EntityEquipmentSlot.CHEST, 1);
        legsItem = new PowerArmorItem(material, EntityEquipmentSlot.LEGS, 2);
        bootsItem = new PowerArmorItem(material, EntityEquipmentSlot.FEET, 3);

        helmetItem.setRegistryName(armorName + "_helmet")
                .setTranslationKey(armorName + "_helmet")
                .setCreativeTab(TabPowerArmor.tab);

        bodyItem.setRegistryName(armorName + "_chestplate")
                .setTranslationKey(armorName + "_chestplate")
                .setCreativeTab(TabPowerArmor.tab);

        legsItem.setRegistryName(armorName + "_leggings")
                .setTranslationKey(armorName + "_leggings")
                .setCreativeTab(TabPowerArmor.tab);

        bootsItem.setRegistryName(armorName + "_boots")
                .setTranslationKey(armorName + "_boots")
                .setCreativeTab(TabPowerArmor.tab);

        elements.items.add(() -> helmetItem);
        elements.items.add(() -> bodyItem);
        elements.items.add(() -> legsItem);
        elements.items.add(() -> bootsItem);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(helmetItem, 0,
                new ModelResourceLocation("afp:" + armorName + "/" + armorName + "_helmet", "inventory"));
        ModelLoader.setCustomModelResourceLocation(legsItem, 0,
                new ModelResourceLocation("afp:" + armorName + "/" + armorName + "_leggings", "inventory"));
        ModelLoader.setCustomModelResourceLocation(bootsItem, 0,
                new ModelResourceLocation("afp:" + armorName + "/" + armorName + "_boots", "inventory"));

        if (hasJetpack) {
            ModelLoader.setCustomMeshDefinition(bodyItem, stack -> {
                boolean jetpack = stack.getTagCompound() != null && stack.getTagCompound().getBoolean("jetpack");
                String modelPath = jetpack ? armorName + "_j_chestplate" : armorName + "_chestplate";
                return new ModelResourceLocation("afp:" + armorName + "/" + modelPath, "inventory");
            });

            ModelLoader.registerItemVariants(bodyItem,
                    new ModelResourceLocation("afp:" + armorName + "/" + armorName + "_chestplate", "inventory"),
                    new ModelResourceLocation("afp:" + armorName + "/" + armorName + "_j_chestplate", "inventory")
            );
        } else {
            ModelLoader.setCustomModelResourceLocation(bodyItem, 0,
                    new ModelResourceLocation("afp:" + armorName + "/" + armorName + "_chestplate", "inventory"));
        }
    }

    private class PowerArmorItem extends ItemArmor implements IPowerArmor {
        public final int partType;

        public PowerArmorItem(ArmorMaterial material, EntityEquipmentSlot slot, int partType) {
            super(material, 0, slot);
            this.partType = partType;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
            switch (partType) {
                case 0:
                    if (helmetModel == null)
                        helmetModel = new PowerArmorModel(0, armorName, false);
                    return helmetModel;
                case 1:
                    boolean jetActive = hasJetpack && stack.hasTagCompound() && stack.getTagCompound().getBoolean("jetpack");
                    if (jetActive) {
                        if (chestplateModelJet == null)
                            chestplateModelJet = new PowerArmorModel(1, armorName, true);
                        return chestplateModelJet;
                    } else {
                        if (chestplateModel == null)
                            chestplateModel = new PowerArmorModel(1, armorName, false);
                        return chestplateModel;
                    }
                case 2:
                    if (leggingsModel == null)
                        leggingsModel = new PowerArmorModel(2, armorName, false);
                    return leggingsModel;
                case 3:
                    if (bootsModel == null)
                        bootsModel = new PowerArmorModel(3, armorName, false);
                    return bootsModel;
                default:
                    return null;
            }
        }

        @Override
        public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity entity) {
            if (AFPConfig.canPlayerEquipPowerArmor) return true;
            return false;
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
            return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(hand));
        }

        @Override
        public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
            UtilPowerArmor.handleStepSound(world, player);
            if (AtomFusionProtocol.IS_HBM_LOADED) {
                ItemGeigerCounter.playGeiger(world, player);
            }
        }

        @Override
        public String getArmorType() {
            return armorName;
        }
    }
}