package com.nymoo.afp.common.items;

import com.nymoo.afp.ElementsAFP;
import com.nymoo.afp.common.entities.EntityExoskeleton;
import com.nymoo.afp.common.tabs.TabEquipment;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ElementsAFP.ModElement.Tag
public class ItemExoskeleton extends ElementsAFP.ModElement {
    @GameRegistry.ObjectHolder("afp:exoskeleton")
    public static final Item itemExoskeleton = null;

    public ItemExoskeleton(ElementsAFP instance) {
        super(instance, 1);
    }

    @Override
    public void initElements() {
        elements.items.add(() -> new ItemCustom());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemExoskeleton, 0, new ModelResourceLocation("afp:exoskeleton", "inventory"));
    }

    public static class ItemCustom extends Item {
        public ItemCustom() {
            setMaxDamage(0);
            maxStackSize = 1;
            setTranslationKey("exoskeleton");
            setRegistryName("exoskeleton");
            setCreativeTab(TabEquipment.tab);
        }

        @Override
        public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                          EnumHand hand, EnumFacing facing,
                                          float hitX, float hitY, float hitZ) {
            BlockPos placementPos = pos.offset(facing);

            if (!world.isRemote && player.canPlayerEdit(placementPos, facing, player.getHeldItem(hand))) {
                EntityExoskeleton.Exoskeleton entity = new EntityExoskeleton.Exoskeleton(world);

                float rotationYaw = MathHelper.wrapDegrees(player.rotationYaw + 180.0F);

                // Установка позиции и углов поворота
                entity.setLocationAndAngles(
                        placementPos.getX() + 0.5,
                        placementPos.getY(),
                        placementPos.getZ() + 0.5,
                        rotationYaw,
                        0
                );

                // Проверка возможности размещения
                if (world.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty() &&
                        world.getEntitiesWithinAABBExcludingEntity(entity, entity.getEntityBoundingBox()).isEmpty()) {
                    world.spawnEntity(entity);
                    if (!player.capabilities.isCreativeMode) {
                        player.getHeldItem(hand).shrink(1);
                    }
                    return EnumActionResult.SUCCESS;
                }
            }
            return EnumActionResult.PASS;
        }
    }
}