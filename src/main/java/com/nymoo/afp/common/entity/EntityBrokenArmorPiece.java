package com.nymoo.afp.common.entity;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.config.AFPConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Элемент мода для регистрации сущности экзоскелета.
 * Управляет созданием и рендерингом экзоскелета как неигровой сущности.
 */
@ModElementRegistry.ModElement.Tag
public class EntityBrokenArmorPiece extends ModElementRegistry.ModElement {
    /**
     * ID сущности для регистрации в реестре
     */
    public static final int ENTITYID = 2;

    public EntityBrokenArmorPiece(ModElementRegistry instance) {
        super(instance, 1);
    }

    @Override
    public void initElements() {
        elements.entities.add(() -> EntityEntryBuilder.create()
                .entity(BrokenArmorPiece.class)
                .id(new ResourceLocation("afp", "entity_broken_armor_piece"), ENTITYID)
                .name("entity_broken_armor_piece")
                .tracker(64, 3, true)
                .build());
    }

    /**
     * Регистрирует рендерер для сущности экзоскелета на клиенте.
     * Использует стандартную модель бипеда с пустой текстурой и слоем брони.
     *
     * @param event Событие предварительной инициализации
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(BrokenArmorPiece.class, renderManager -> {
            RenderBiped<BrokenArmorPiece> renderer = new RenderBiped<BrokenArmorPiece>(renderManager, new ModelBiped(), 0.5f) {
                private final ResourceLocation TEXTURE = new ResourceLocation("afp:textures/misc/blank.png");

                @Override
                protected ResourceLocation getEntityTexture(BrokenArmorPiece entity) {
                    return TEXTURE;
                }
            };
            renderer.addLayer(new LayerBipedArmor(renderer) {
                @Override
                protected void initArmor() {
                    modelLeggings = new ModelBiped(0.5F);
                    modelArmor = new ModelBiped(1.0F);
                }
            });
            return renderer;
        });
    }

    /**
     * Сущность экзоскелета - неигровая сущность для хранения и отображения силовой брони.
     * Используется как контейнер для брони при выходе игрока из экзоскелета.
     */
    public static class BrokenArmorPiece extends EntityCreature {
        /**
         * Изменяемая позиция блока для проверки столкновений
         */
        private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        public BrokenArmorPiece(World world) {
            super(world);
            setSize(AFPConfig.brokenArmorPieceHitboxWidth, AFPConfig.brokenArmorPieceHitboxHeight);
            setNoAI(true);
            enablePersistence();
        }

        /**
         * Проверяет неуязвимость сущности к различным типам урона.
         * Экзоскелет неуязвим ко всем типам урона по умолчанию.
         *
         * @param source Источник урона
         * @return Всегда true - экзоскелет неуязвим
         */
        @Override
        public boolean isEntityInvulnerable(DamageSource source) {
            return true;
        }

        /**
         * Обрабатывает атаку по экзоскелету.
         * Позволяет игроку в режиме приседания собрать экзоскелет в предмет, если весь комплект типа 'exo'.
         *
         * @param source Источник урона
         * @param amount Количество урона
         * @return true если атака обработана, false в противном случае
         */
        @Override
        public boolean attackEntityFrom(DamageSource source, float amount) {
            if (!world.isRemote && source.getTrueSource() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) source.getTrueSource();
                if (player.isSneaking()) {
                    Item exoskeletonItem = Item.getByNameOrId("afp:broken_armor_piece");
                    if (exoskeletonItem != null) {
                        ItemStack drop = new ItemStack(exoskeletonItem);
                        entityDropItem(drop, 0.0F);
                        setDead();
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Проверяет возможность толкания сущности.
         * Экзоскелет нельзя толкать.
         *
         * @return Всегда false - экзоскелет нельзя толкать
         */
        @Override
        public boolean canBePushed() {
            return false;
        }

        /**
         * Проверяет возможность столкновения с сущностью.
         * С экзоскелетом можно сталкиваться.
         *
         * @return Всегда true - с экзоскелетом можно сталкиваться
         */
        @Override
        public boolean canBeCollidedWith() {
            return true;
        }

        /**
         * Проверяет возможность деспауна сущности.
         * Экзоскелет никогда не деспаунится.
         *
         * @return Всегда false - экзоскелет не деспаунится
         */
        @Override
        protected boolean canDespawn() {
            return false;
        }

        /**
         * Определяет способность сущности дышать под водой. Требуется для отключения пузырьков вокруг сущности под водой.
         * Возврат true отключает механику утопления и появление пузырьков.
         *
         * @return true - сущность дышит под водой, false - подчиняется стандартной механике
         */
        @Override
        public boolean canBreatheUnderwater() {
            return true;
        }

        /**
         * Обновляет состояние экзоскелета каждый тик.
         * Обрабатывает гравитацию и проверяет столкновение с землей.
         */
        @Override
        public void onUpdate() {
            super.onUpdate();
            if (!world.isRemote) {
                mutablePos.setPos(posX, posY - 0.1, posZ);
                IBlockState state = world.getBlockState(mutablePos);
                if (state.isFullBlock()) {
                    motionY = 0;
                    onGround = true;
                } else {
                    motionY = Math.max(motionY - 0.08, -3.0);
                    move(MoverType.SELF, 0, motionY, 0);
                }
            }
        }

        /**
         * Устанавливает позицию и углы поворота экзоскелета.
         * Синхронизирует повороты головы и тела для единообразия отображения.
         *
         * @param x     Координата X
         * @param y     Координата Y
         * @param z     Координата Z
         * @param yaw   Угол поворота по горизонтали
         * @param pitch Угол поворота по вертикали
         */
        @Override
        public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
            super.setLocationAndAngles(x, y, z, yaw, pitch);
            rotationYawHead = yaw;
            renderYawOffset = yaw;
        }
    }
}