package com.nymoo.afp.common.item;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.entity.EntityExoskeleton;
import com.nymoo.afp.common.tab.TabEquipment;
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

/**
 * Элемент мода для регистрации предмета экзоскелета.
 * Предмет используется для размещения экзоскелета в мире как сущности.
 */
@ModElementRegistry.ModElement.Tag
public class ItemExoskeleton extends ModElementRegistry.ModElement {
    /**
     * Зарегистрированный предмет экзоскелета
     */
    @GameRegistry.ObjectHolder("afp:exoskeleton")
    public static final Item itemExoskeleton = null;

    public ItemExoskeleton(ModElementRegistry instance) {
        super(instance, 1);
    }

    @Override
    public void initElements() {
        elements.items.add(() -> new ItemCustom());
    }

    /**
     * Регистрирует модель предмета экзоскелета для клиентской части.
     *
     * @param event Событие регистрации моделей
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemExoskeleton, 0, new ModelResourceLocation("afp:exoskeleton", "inventory"));
    }

    /**
     * Пользовательская реализация предмета экзоскелета.
     * Позволяет размещать экзоскелет в мире при использовании на блоке.
     */
    public static class ItemCustom extends Item {
        public ItemCustom() {
            setMaxDamage(0);
            maxStackSize = 1;
            setTranslationKey("exoskeleton");
            setRegistryName("exoskeleton");
            setCreativeTab(TabEquipment.tab);
        }

        /**
         * Обрабатывает использование предмета на блоке.
         * Размещает сущность экзоскелета в мире при соблюдении условий.
         *
         * @param player Игрок, использующий предмет
         * @param world  Мир, в котором используется предмет
         * @param pos    Позиция блока, на котором используется предмет
         * @param hand   Рука, в которой находится предмет
         * @param facing Направление использования
         * @param hitX   Координата X попадания в блок
         * @param hitY   Координата Y попадания в блок
         * @param hitZ   Координата Z попадания в блок
         * @return Результат использования предмета
         */
        @Override
        public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
            BlockPos placementPos = pos.offset(facing);

            if (!world.isRemote && player.canPlayerEdit(placementPos, facing, player.getHeldItem(hand))) {
                EntityExoskeleton.Exoskeleton entity = new EntityExoskeleton.Exoskeleton(world);

                float rotationYaw = MathHelper.wrapDegrees(player.rotationYaw + 180.0F);

                entity.setLocationAndAngles(
                        placementPos.getX() + 0.5,
                        placementPos.getY(),
                        placementPos.getZ() + 0.5,
                        rotationYaw,
                        0
                );

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