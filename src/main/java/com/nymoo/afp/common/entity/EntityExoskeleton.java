package com.nymoo.afp.common.entity;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.item.ArmorExo;
import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.util.UtilEntityExoskeleton;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Элемент мода: регистрация и клиентский рендер сущности экзоскелета.
 * Сущность хранит полный комплект силовой брони и служит интерактивным контейнером.
 */
@ModElementRegistry.ModElement.Tag
public class EntityExoskeleton extends ModElementRegistry.ModElement {

    // Идентификатор сущности в реестре
    public static final int ENTITY_ID = 1;

    public EntityExoskeleton(ModElementRegistry instance) {
        super(instance, 1);
    }

    /**
     * Регистрирует сущность в наборе элементов мода.
     */
    @Override
    public void initElements() {
        elements.entities.add(() -> EntityEntryBuilder.create()
                .entity(Exoskeleton.class)
                .id(new ResourceLocation("afp", "entity_exoskeleton"), ENTITY_ID)
                .name("entity_exoskeleton")
                .tracker(64, 3, true)
                .build());
    }

    /**
     * Регистрирует рендер на клиенте: базовая модель + слой брони.
     *
     * @param event событие препинициализации клиента
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Exoskeleton.class, renderManager -> {
            RenderBiped<Exoskeleton> renderer = new RenderBiped<Exoskeleton>(renderManager, new ModelBiped(), 0.5f) {
                private final ResourceLocation TEXTURE = new ResourceLocation("afp:textures/models/armor/exo/exo_full.png");

                @Override
                protected ResourceLocation getEntityTexture(Exoskeleton entity) {
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
     * Сущность экзоскелета: статичный контейнер для полного набора брони.
     */
    public static class Exoskeleton extends EntityCreature {

        // Пороговые высоты для определения, какой слот кликнули (голова/грудь/ноги/ботинки).
        public static final float[] SLOT_HEIGHT_THRESHOLDS = {1.5F, 0.7F, 0.35F};

        // Порядок слотов по вертикали: голова, грудь, поножи, ботинки.
        public static final EntityEquipmentSlot[] SLOT_ORDER = {
                EntityEquipmentSlot.HEAD,
                EntityEquipmentSlot.CHEST,
                EntityEquipmentSlot.LEGS,
                EntityEquipmentSlot.FEET
        };

        // Вспомогательная позиция для проверки блока под сущностью.
        private final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        /**
         * Конструктор: настраивает хитбокс, устойчивость к огню и начальные предметы в слотах.
         *
         * @param world мир
         */
        public Exoskeleton(World world) {
            super(world);
            setSize(AFPConfig.exoskeletonHitboxWidth, AFPConfig.exoskeletonHitboxHeight);
            setNoAI(true);
            enablePersistence();
            this.isImmuneToFire = true;

            // Устанавливаем отображаемый комплект exo в слоты
            setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ArmorExo.helmet));
            setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ArmorExo.chestplate));
            setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ArmorExo.leggings));
            setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ArmorExo.boots));
        }

        /**
         * Делает сущность неуязвимой ко всем источникам урона.
         *
         * @param source источник урона
         * @return всегда true (урон игнорируется)
         */
        @Override
        public boolean isEntityInvulnerable(DamageSource source) {
            return true;
        }

        /**
         * Обработка "атаки" игроком: если игрок присел и внутри слотов полный комплект exo,
         * то создаётся предмет exo_full и сущность удаляется.
         *
         * @param source источник урона
         * @param amount количество урона (игнорируется)
         * @return true если действие обработано и сущность удалена
         */
        @Override
        public boolean attackEntityFrom(DamageSource source, float amount) {
            if (!world.isRemote && source.getTrueSource() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) source.getTrueSource();
                if (player.isSneaking()) {
                    boolean allExo = true;
                    for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                        if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                            ItemStack stack = getItemStackFromSlot(slot);
                            if (stack.isEmpty() || !(stack.getItem() instanceof IPowerArmor)
                                    || !((IPowerArmor) stack.getItem()).getPowerArmorType().equals("exo")) {
                                allExo = false;
                                break;
                            }
                        }
                    }
                    if (allExo) {
                        Item exoItem = Item.getByNameOrId("afp:exo_full");
                        if (exoItem != null) {
                            ItemStack drop = new ItemStack(exoItem);
                            entityDropItem(drop, 0.0F);
                            setDead();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Запрещает физическое толкание сущности.
         *
         * @return false
         */
        @Override
        public boolean canBePushed() {
            return false;
        }

        /**
         * Разрешает коллизии с сущностью (чтобы можно было её кликать/подбирать).
         *
         * @return true
         */
        @Override
        public boolean canBeCollidedWith() {
            return true;
        }

        /**
         * Отключает автоматический деспаун.
         *
         * @return false
         */
        @Override
        protected boolean canDespawn() {
            return false;
        }

        /**
         * Позволяет "дышать" под водой (нет утопления).
         *
         * @return true
         */
        @Override
        public boolean canBreatheUnderwater() {
            return true;
        }

        /**
         * Обновление каждый тик: проверяет блок под сущностью и имитирует падение.
         */
        @Override
        public void onUpdate() {
            super.onUpdate();
            if (!world.isRemote) {
                mutableBlockPos.setPos(posX, posY - 0.1, posZ);
                IBlockState state = world.getBlockState(mutableBlockPos);
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
         * Синхронизация поворотов при установке позиции.
         *
         * @param x     X
         * @param y     Y
         * @param z     Z
         * @param yaw   поворот по Y
         * @param pitch наклон (не используется)
         */
        @Override
        public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
            super.setLocationAndAngles(x, y, z, yaw, pitch);
            rotationYawHead = yaw;
            renderYawOffset = yaw;
        }

        /**
         * Обрабатывает взаимодействие игрока: определяет кликнутый слот по высоте попадания
         * и делегирует логику UtilEntityExoskeleton.
         *
         * @param player игрок
         * @param hand   рука игрока
         * @return true (взаимодействие обработано)
         */
        @Override
        public boolean processInteract(EntityPlayer player, EnumHand hand) {
            if (world.isRemote) return true;

            Vec3d eyesPos = player.getPositionEyes(1.0F);
            Vec3d lookVec = player.getLook(1.0F);
            Vec3d endPos = eyesPos.add(lookVec.x * 5, lookVec.y * 5, lookVec.z * 5);
            RayTraceResult rayTrace = getEntityBoundingBox().calculateIntercept(eyesPos, endPos);
            if (rayTrace == null) return true;

            float hitY = (float) (rayTrace.hitVec.y - posY);
            EntityEquipmentSlot clickedSlot = SLOT_ORDER[3]; // по умолчанию FEET

            for (int i = 0; i < SLOT_HEIGHT_THRESHOLDS.length; i++) {
                if (hitY > SLOT_HEIGHT_THRESHOLDS[i]) {
                    clickedSlot = SLOT_ORDER[i];
                    break;
                }
            }

            UtilEntityExoskeleton.handleInteraction(world, player, hand, this, clickedSlot);
            return true;
        }
    }
}
