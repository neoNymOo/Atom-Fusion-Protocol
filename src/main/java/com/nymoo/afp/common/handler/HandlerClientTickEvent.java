package com.nymoo.afp.common.handler;

import com.nymoo.afp.common.entity.EntityExoskeleton;
import com.nymoo.afp.common.item.ItemFusionCore;
import com.nymoo.afp.common.util.UtilEntityExoskeleton;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Mouse;
import io.netty.buffer.Unpooled;

import static com.nymoo.afp.common.util.UtilEntityExoskeleton.hasBooleanTag;

@Mod.EventBusSubscriber
public class HandlerClientTickEvent {
    // Максимальное время удержания для завершения операции (в секундах)
    public static final float MAX_HOLD_TIME = 1.5F;
    // Звук взаимодействия с ядерным блоком
    private static final SoundEvent FUSION_CORE_INTERACT_SOUND = new SoundEvent(new ResourceLocation("afp", "fusion_core_interact"));
    // Сетевой канал для синхронизации с сервером
    private static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("afp_fusion");
    // Время начала удержания кнопки
    public static long startHoldTime = 0L;
    // Текущий воспроизводимый звук процесса
    public static LoadingSound currentSound = null;

    static {
        // Регистрируем обработчик сетевых сообщений
        NETWORK.registerMessage(FusionCoreMessage.Handler.class, FusionCoreMessage.class, 0, Side.SERVER);
    }

    /**
     * Обработчик клиентского тика. Отслеживает удержание ПКМ для взаимодействия с экзоскелетом.
     * Основная логика:
     *  - Проверяем условия взаимодействия (удержание ПКМ + крадется)
     *  - Определяем целевой слот брони
     *  - Проверяем возможность установки/извлечения ядерного блока
     *  - Запускаем звуковой и визуальный индикаторы процесса
     *  - Отправляем серверу запрос по завершении времени удержания
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // Работаем только в конце фазы тика
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        EntityPlayer player = mc.player;
        if (player == null) return;

        // Проверяем удержание ПКМ и режим крадущегося движения
        boolean isHolding = Mouse.isButtonDown(1);
        if (!isHolding || !player.isSneaking()) {
            resetHoldAndSoundIfNeeded(mc);
            return;
        }

        World world = mc.world;
        if (world == null) {
            resetHoldAndSoundIfNeeded(mc);
            return;
        }

        // Проверяем наличие цели взгляда
        RayTraceResult mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != RayTraceResult.Type.ENTITY) {
            resetHoldAndSoundIfNeeded(mc);
            return;
        }

        // Цель должна быть экзоскелетом
        Entity entity = mop.entityHit;
        if (!(entity instanceof EntityExoskeleton.Exoskeleton)) {
            resetHoldAndSoundIfNeeded(mc);
            return;
        }

        EntityExoskeleton.Exoskeleton exo = (EntityExoskeleton.Exoskeleton) entity;
        // Проверяем наличие припаянного нагрудника
        ItemStack chestStack = exo.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!hasBooleanTag(chestStack, "soldered")) {
            resetHoldAndSoundIfNeeded(mc);
            return;
        }

        // Определяем целевой слот based на высоте попадания
        float hitY = (float) (mop.hitVec.y - exo.posY);
        EntityEquipmentSlot clickedSlot = EntityExoskeleton.Exoskeleton.SLOT_ORDER[3];
        for (int i = 0; i < EntityExoskeleton.Exoskeleton.SLOT_HEIGHT_THRESHOLDS.length; i++) {
            if (hitY > EntityExoskeleton.Exoskeleton.SLOT_HEIGHT_THRESHOLDS[i]) {
                clickedSlot = EntityExoskeleton.Exoskeleton.SLOT_ORDER[i];
                break;
            }
        }

        // Работаем только с нагрудником
        if (clickedSlot != EntityEquipmentSlot.CHEST) {
            resetHoldAndSoundIfNeeded(mc);
            return;
        }

        // Проверяем дистанцию и угол взаимодействия
        double distanceSq = player.getDistanceSq(exo);
        if (distanceSq > 1.0D) {
            resetHoldAndSoundIfNeeded(mc);
            return;
        }

        float yawDiff = MathHelper.wrapDegrees(player.rotationYaw - exo.rotationYaw);
        if (yawDiff < -55.0F || yawDiff > 55.0F) {
            resetHoldAndSoundIfNeeded(mc);
            return;
        }

        // Определяем тип операции (установка/извлечение)
        ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
        boolean isInstall = !held.isEmpty() && held.getItem() == ItemFusionCore.itemFusionCore;
        boolean isUninstall = held.isEmpty();

        // Проверяем соответствие состояния слота и операции
        boolean shouldProceed = false;
        if (isInstall || isUninstall) {
            NBTTagCompound tag = chestStack.getTagCompound();
            boolean hasEnergy = tag != null && tag.hasKey("fusion_energy");
            if ((isInstall && !hasEnergy) || (isUninstall && hasEnergy)) {
                shouldProceed = true;
            }
        }

        if (!shouldProceed) {
            resetHoldAndSoundIfNeeded(mc);
            return;
        }

        // Запускаем процесс удержания
        if (startHoldTime == 0L) {
            startHoldTime = System.currentTimeMillis();
            if (currentSound == null || !mc.getSoundHandler().isSoundPlaying(currentSound)) {
                currentSound = new LoadingSound(FUSION_CORE_INTERACT_SOUND, (float) exo.posX, (float) exo.posY, (float) exo.posZ);
                mc.getSoundHandler().playSound(currentSound);
            }
        }

        // Проверяем завершение процесса удержания
        long now = System.currentTimeMillis();
        float elapsed = (now - startHoldTime) / 1000.0F;
        if (elapsed >= MAX_HOLD_TIME) {
            int type = isInstall ? 0 : 1;
            NETWORK.sendToServer(new FusionCoreMessage(type, exo.getEntityId(), clickedSlot));
            startHoldTime = 0L;
            if (currentSound != null) {
                mc.getSoundHandler().stopSound(currentSound);
                currentSound = null;
            }
        }
    }

    /**
     * Сбрасывает состояние удержания и останавливает звук при прерывании операции
     */
    private static void resetHoldAndSoundIfNeeded(Minecraft mc) {
        if (startHoldTime != 0L) {
            long now = System.currentTimeMillis();
            float elapsed = (now - startHoldTime) / 1000.0F;
            if (elapsed < MAX_HOLD_TIME) {
                if (currentSound != null) {
                    mc.getSoundHandler().stopSound(currentSound);
                    currentSound = null;
                }
            }
            startHoldTime = 0L;
        }
    }

    /**
     * Сетевое сообщение для синхронизации операций с ядерным блоком
     * type: 0 - установка, 1 - извлечение
     * entityId: ID целевого экзоскелета
     * slot: целевой слот (всегда CHEST)
     */
    public static class FusionCoreMessage implements IMessage {
        private int type;
        private int entityId;
        private EntityEquipmentSlot slot;

        public FusionCoreMessage() {}

        public FusionCoreMessage(int type, int entityId, EntityEquipmentSlot slot) {
            this.type = type;
            this.entityId = entityId;
            this.slot = slot;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            type = buf.readInt();
            entityId = buf.readInt();
            int slotOrdinal = buf.readInt();
            EntityEquipmentSlot[] values = EntityEquipmentSlot.values();
            slot = (slotOrdinal >= 0 && slotOrdinal < values.length) ? values[slotOrdinal] : EntityEquipmentSlot.CHEST;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(type);
            buf.writeInt(entityId);
            buf.writeInt(slot != null ? slot.ordinal() : EntityEquipmentSlot.CHEST.ordinal());
        }

        /**
         * Обработчик сообщения на серверной стороне
         * Выполняет окончательные проверки и применяет изменения
         */
        public static class Handler implements IMessageHandler<FusionCoreMessage, IMessage> {
            @Override
            public IMessage onMessage(FusionCoreMessage message, MessageContext ctx) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    World world = player.world;
                    Entity entity = world.getEntityByID(message.entityId);
                    if (!(entity instanceof EntityExoskeleton.Exoskeleton)) return;

                    EntityExoskeleton.Exoskeleton exo = (EntityExoskeleton.Exoskeleton) entity;
                    // Повторная проверка условий на сервере
                    double distanceSq = player.getDistanceSq(exo);
                    if (distanceSq > 1.0D) return;

                    float yawDiff = MathHelper.wrapDegrees(player.rotationYaw - exo.rotationYaw);
                    if (yawDiff < -55.0F || yawDiff > 55.0F) return;

                    ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
                    ItemStack chest = exo.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                    NBTTagCompound tag = chest.getTagCompound();
                    boolean hasEnergy = tag != null && tag.hasKey("fusion_energy");

                    // Выполняем соответствующую операцию
                    if (message.type == 0) {
                        if (held.isEmpty() || held.getItem() != ItemFusionCore.itemFusionCore || hasEnergy) return;
                        UtilEntityExoskeleton.tryInstallFusionCore(world, player, EnumHand.MAIN_HAND, exo, message.slot);
                    } else if (message.type == 1) {
                        if (!held.isEmpty() || !hasEnergy) return;
                        UtilEntityExoskeleton.tryUninstallFusionCore(world, player, EnumHand.MAIN_HAND, exo, message.slot);
                    }
                });
                return null;
            }
        }
    }

    /**
     * Позиционированный звук для процесса взаимодействия
     */
    public static class LoadingSound extends PositionedSound {
        public LoadingSound(SoundEvent soundIn, float x, float y, float z) {
            super(soundIn, SoundCategory.PLAYERS);
            this.repeat = false;
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.xPosF = x;
            this.yPosF = y;
            this.zPosF = z;
        }
    }
}