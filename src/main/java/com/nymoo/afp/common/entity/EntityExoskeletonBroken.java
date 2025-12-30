package com.nymoo.afp.common.entity;

import com.nymoo.afp.ModElementRegistry;
import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.render.model.armor.PowerArmorBrokenModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

@ModElementRegistry.ModElement.Tag
public class EntityExoskeletonBroken extends ModElementRegistry.ModElement {

    public static final int ENTITY_ID = 2;

    public EntityExoskeletonBroken(ModElementRegistry instance) {
        super(instance, 2);
    }

    @Override
    public void initElements() {
        elements.entities.add(() -> EntityEntryBuilder.create()
                .entity(ExoskeletonBroken.class)
                .id(new ResourceLocation("afp", "entity_exoskeleton_broken"), ENTITY_ID)
                .name("entity_exoskeleton_broken")
                .tracker(64, 3, true)
                .build());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(ExoskeletonBroken.class, PowerArmorBrokenModel.RenderExoskeletonBroken::new);
    }

    public static class ExoskeletonBroken extends EntityCreature {

        private static final String[] ARMOR_MODELS = {"x03", "x02", "x01", "t60", "t51", "t45"};
        private static final String[] ARMOR_PARTS = {"helmet", "chestplate", "leggings", "boots"};

        private final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        private boolean followerFragment = false;
        private String selectedArmorModel = "t45"; // Default fallback

        public ExoskeletonBroken(World world) {
            super(world);
            setSize(AFPConfig.exoskeletonBrokenHitboxWidth, AFPConfig.exoskeletonBrokenHitboxHeight);
            setNoAI(true);
            enablePersistence();
            this.isImmuneToFire = true;
        }

        @Nullable
        @Override
        public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
            livingdata = super.onInitialSpawn(difficulty, livingdata);

            // Логика запускается только для серверной стороны и только если это ПЕРВЫЙ фрагмент (лидер)
            if (!this.world.isRemote && !this.followerFragment) {
                // Выравнивание лидера по сетке поворотов (90 градусов)
                EnumFacing facing = EnumFacing.fromAngle(this.rotationYaw);
                this.rotationYaw = facing.getHorizontalAngle();
                this.rotationYawHead = this.rotationYaw;
                this.renderYawOffset = this.rotationYaw;
                this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);

                // 1. ШАНС НА ДЖЕКПОТ (Полный предмет)
                if (this.rand.nextFloat() < AFPConfig.brokenExoskeletonSpawnWeight) {
                    this.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Item.getByNameOrId("afp:exo_full_broken")));
                    // Если джекпот, соседей не создаем, цепочка не нужна.
                }
                else {
                    // 2. ВЫБОР МОДЕЛИ БРОНИ
                    float[] probs = new float[ARMOR_MODELS.length];
                    for (int i = 0; i < ARMOR_MODELS.length; i++) {
                        probs[i] = getSpawnChanceForModel(ARMOR_MODELS[i]);
                    }
                    float total = 0;
                    for (float p : probs) total += p;

                    if (total > 0) {
                        float r = this.rand.nextFloat() * total;
                        float cum = 0;
                        for (int i = 0; i < ARMOR_MODELS.length; i++) {
                            cum += probs[i];
                            if (r < cum) {
                                this.selectedArmorModel = ARMOR_MODELS[i];
                                break;
                            }
                        }
                    } else {
                        this.selectedArmorModel = ARMOR_MODELS[rand.nextInt(ARMOR_MODELS.length)];
                    }

                    // 3. ГЕНЕРАЦИЯ СОСЕДЕЙ (ИСПРАВЛЕНИЕ БАГА)
                    // Мы делаем это ДО обработки самого лидера или НЕЗАВИСИМО от его смерти.
                    // Координаты лидера this.posX существуют, даже если this.isDead == true.

                    EnumFacing spawnOffsetDir = facing; // Направление, куда строится линия
                    int offsetX = spawnOffsetDir.getXOffset();
                    int offsetZ = spawnOffsetDir.getZOffset();

                    for (int i = 1; i < 4; i++) {
                        ExoskeletonBroken neighbor = new ExoskeletonBroken(this.world);

                        // Сосед стоит на 1 блок дальше
                        double newX = this.posX + (offsetX * i);
                        double newZ = this.posZ + (offsetZ * i);
                        double newY = this.posY;

                        neighbor.setLocationAndAngles(newX, newY, newZ, this.rotationYaw, 0.0F);
                        neighbor.rotationYawHead = this.rotationYaw;
                        neighbor.renderYawOffset = this.rotationYaw;

                        neighbor.followerFragment = true; // Соседи не спавнят своих соседей
                        neighbor.selectedArmorModel = this.selectedArmorModel; // Наследуют модель лидера

                        // Попытка выдать предмет соседу
                        assignHeldItem(neighbor, this.selectedArmorModel, i);

                        if (!neighbor.isDead) {
                            if (neighbor.getCanSpawnHere()) {
                                this.world.spawnEntity(neighbor);
                            } else {
                                neighbor.setDead();
                            }
                        }
                    }

                    // 4. ОБРАБОТКА САМОГО ЛИДЕРА (Шлем)
                    // Если удача отвернется, метод вызовет setDead(), но соседи уже обработаны/запущены в цикл
                    assignHeldItem(this, this.selectedArmorModel, 0);
                }
            }
            return livingdata;
        }

        private void assignHeldItem(ExoskeletonBroken fragment, String model, int partIdx) {
            String pieceName = ARMOR_PARTS[partIdx];
            // Безопасное получение предмета (защита от null)
            Item item = Item.getByNameOrId("afp:" + model + "_" + pieceName + "_broken");

            if (item != null) {
                fragment.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(item));
            } else {
                // Если предмета не существует в коде мода, убиваем сущность чтобы не было фантомов
                fragment.setDead();
                return;
            }

            // ПРОВЕРКА ВЕРОЯТНОСТИ
            float chance = getSpawnChanceForPart(pieceName);
            // Используем дробную проверку для корректной математики
            if (fragment.rand.nextFloat() > chance) {
                fragment.setDead();
            }
        }

        private float getSpawnChanceForModel(String model) {
            switch (model) {
                case "x03": return (float)AFPConfig.probabilityX03;
                case "x02": return (float)AFPConfig.probabilityX02;
                case "x01": return (float)AFPConfig.probabilityX01;
                case "t60": return (float)AFPConfig.probabilityT60;
                case "t51": return (float)AFPConfig.probabilityT51;
                case "t45": return (float)AFPConfig.probabilityT45;
                default: return 0.33F;
            }
        }

        private float getSpawnChanceForPart(String pieceName) {
            switch (pieceName) {
                case "helmet": return (float)AFPConfig.probabilityHelmet;
                case "chestplate": return (float)AFPConfig.probabilityChestplate;
                case "leggings": return (float)AFPConfig.probabilityLeggings;
                case "boots": return (float)AFPConfig.probabilityBoots;
                default: return 1.0f;
            }
        }

        @Override
        public boolean attackEntityFrom(DamageSource source, float amount) {
            if (!world.isRemote && source.getTrueSource() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) source.getTrueSource();
                if (player.isSneaking()) {
                    ItemStack held = getHeldItemMainhand();
                    if (!held.isEmpty()) {
                        entityDropItem(held, 0.0F);
                        setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                        setDead();
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canBePushed() { return false; }

        @Override
        public boolean canBeCollidedWith() { return true; }

        @Override
        protected boolean canDespawn() { return false; }

        @Override
        public boolean canBreatheUnderwater() { return true; }

        @Override
        public boolean isEntityInvulnerable(DamageSource source) { return true; }

        @Override
        public boolean getCanSpawnHere() {
            return this.world.checkNoEntityCollision(this.getEntityBoundingBox())
                    && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty();
        }

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

        @Override
        public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
            super.setLocationAndAngles(x, y, z, yaw, pitch);
            rotationYawHead = yaw;
            renderYawOffset = yaw;
        }
    }
}