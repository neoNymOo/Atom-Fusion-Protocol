package com.nymoo.afp;

import com.nymoo.afp.common.entity.EntityExoskeleton;
import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.item.ItemFusionCore;
import com.nymoo.afp.common.util.UtilEntityExoskeleton;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

/**
 * Менеджер синхронизации данных мода между клиентом и сервером.
 * Управляет сетевыми сообщениями для взаимодействий с экзоскелетом и ядрами синтеза.
 */
public class ModDataSyncManager {
    /**
     * Сетевой канал для взаимодействий с экзоскелетом
     */
    public static final SimpleNetworkWrapper INTERACT_NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("afp_interact");

    static {
        INTERACT_NETWORK.registerMessage(InteractMessage.Handler.class, InteractMessage.class, 0, Side.SERVER);
        INTERACT_NETWORK.registerMessage(StartSoundMessage.Handler.class, StartSoundMessage.class, 1, Side.SERVER);
        INTERACT_NETWORK.registerMessage(StopSoundMessage.Handler.class, StopSoundMessage.class, 2, Side.SERVER);
        INTERACT_NETWORK.registerMessage(StopSoundBroadcast.Handler.class, StopSoundBroadcast.class, 3, Side.CLIENT);
    }

    /**
     * Абстрактный класс для синхронизированных данных мира с поддержкой сетевой синхронизации.
     */
    public static abstract class SyncedWorldData extends WorldSavedData {
        /**
         * Тип данных: 0 - уровень карты, 1 - уровень измерения
         */
        private final int type;

        public SyncedWorldData(String name, int type) {
            super(name);
            this.type = type;
        }

        /**
         * Синхронизирует данные по сети между клиентом и сервером.
         *
         * @param world Мир для синхронизации данных
         */
        public void syncData(World world) {
            markDirty();
            if (world.isRemote) {
                AtomFusionProtocol.PACKET_HANDLER.sendToServer(new WorldSavedDataSyncMessage(type, this));
            } else {
                if (type == 0) {
                    AtomFusionProtocol.PACKET_HANDLER.sendToAll(new WorldSavedDataSyncMessage(type, this));
                } else {
                    AtomFusionProtocol.PACKET_HANDLER.sendToDimension(new WorldSavedDataSyncMessage(type, this), world.provider.getDimension());
                }
            }
        }
    }

    /**
     * Данные уровня карты, синхронизируемые между всеми измерениями.
     */
    public static class MapVariables extends SyncedWorldData {
        /**
         * Имя данных для хранения в мире
         */
        public static final String DATA_NAME = Tags.MOD_ID + "_map";

        public MapVariables(String mapName) {
            super(mapName, 0);
        }

        public MapVariables() {
            super(DATA_NAME, 0);
        }

        /**
         * Получает или создает экземпляр данных уровня карты для мира.
         *
         * @param world Мир для получения данных
         * @return Экземпляр MapVariables
         */
        public static MapVariables get(World world) {
            MapVariables instance = (MapVariables) world.getMapStorage().getOrLoadData(MapVariables.class, DATA_NAME);
            if (instance == null) {
                instance = new MapVariables();
                world.getMapStorage().setData(DATA_NAME, instance);
            }
            return instance;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            // Реализация чтения данных из NBT
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            // Реализация записи данных в NBT
            return nbt;
        }
    }

    /**
     * Данные уровня измерения, синхронизируемые в пределах одного измерения.
     */
    public static class WorldVariables extends SyncedWorldData {
        /**
         * Имя данных для хранения в мире
         */
        public static final String DATA_NAME = Tags.MOD_ID + "_world";

        public WorldVariables(String mapName) {
            super(mapName, 1);
        }

        public WorldVariables() {
            super(DATA_NAME, 1);
        }

        /**
         * Получает или создает экземпляр данных уровня измерения для мира.
         *
         * @param world Мир для получения данных
         * @return Экземпляр WorldVariables
         */
        public static WorldVariables get(World world) {
            WorldVariables instance = (WorldVariables) world.getMapStorage().getOrLoadData(WorldVariables.class, DATA_NAME);
            if (instance == null) {
                instance = new WorldVariables();
                world.getMapStorage().setData(DATA_NAME, instance);
            }
            return instance;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            // Реализация чтения данных из NBT
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            // Реализация записи данных в NBT
            return nbt;
        }
    }

    /**
     * Обработчик сообщений синхронизации данных мира.
     */
    public static class WorldSavedDataSyncMessageHandler implements IMessageHandler<WorldSavedDataSyncMessage, IMessage> {
        @Override
        public IMessage onMessage(WorldSavedDataSyncMessage message, MessageContext context) {
            Runnable task = () -> syncData(message, context);
            if (context.side == Side.SERVER) {
                context.getServerHandler().player.getServerWorld().addScheduledTask(task);
            } else {
                Minecraft.getMinecraft().addScheduledTask(task);
            }
            return null;
        }

        /**
         * Синхронизирует полученные данные в соответствующем мире.
         *
         * @param message Сообщение с данными для синхронизации
         * @param context Контекст сетевого сообщения
         */
        private void syncData(WorldSavedDataSyncMessage message, MessageContext context) {
            World world = context.side == Side.SERVER
                    ? context.getServerHandler().player.world
                    : Minecraft.getMinecraft().player.world;

            if (context.side == Side.SERVER) {
                message.data.markDirty();
                if (message.type == 0) {
                    AtomFusionProtocol.PACKET_HANDLER.sendToAll(message);
                } else {
                    AtomFusionProtocol.PACKET_HANDLER.sendToDimension(message, world.provider.getDimension());
                }
            }

            String dataName = message.type == 0 ? MapVariables.DATA_NAME : WorldVariables.DATA_NAME;
            world.getMapStorage().setData(dataName, message.data);
        }
    }

    /**
     * Сетевое сообщение для синхронизации данных мира между клиентом и сервером.
     */
    public static class WorldSavedDataSyncMessage implements IMessage {
        /**
         * Тип данных: 0 - уровень карты, 1 - уровень измерения
         */
        public int type;
        /**
         * Синхронизируемые данные
         */
        public WorldSavedData data;

        public WorldSavedDataSyncMessage() {
        }

        public WorldSavedDataSyncMessage(int type, WorldSavedData data) {
            this.type = type;
            this.data = data;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(type);
            ByteBufUtils.writeTag(buf, data.writeToNBT(new NBTTagCompound()));
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            type = buf.readInt();
            NBTTagCompound nbt = ByteBufUtils.readTag(buf);
            if (type == 0) {
                data = new MapVariables();
            } else {
                data = new WorldVariables();
            }
            data.readFromNBT(nbt);
        }
    }

    /**
     * Сообщение взаимодействия с экзоскелетом или другим игроком.
     * Используется для установки/извлечения ядер синтеза и входа в экзоскелет.
     */
    public static class InteractMessage implements IMessage {
        /**
         * Тип взаимодействия: 0 - установка ядра, 1 - извлечение ядра, 2 - вход в экзоскелет
         */
        private int type;
        /**
         * ID целевой сущности
         */
        private int entityId;
        /**
         * Целевой слот экипировки
         */
        private EntityEquipmentSlot slot;

        public InteractMessage() {
        }

        public InteractMessage(int type, int entityId, EntityEquipmentSlot slot) {
            this.type = type;
            this.entityId = entityId;
            this.slot = slot;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            type = buf.readInt();
            entityId = buf.readInt();
            int slotOrdinal = buf.readInt();
            slot = EntityEquipmentSlot.values()[MathHelper.clamp(slotOrdinal, 0, EntityEquipmentSlot.values().length - 1)];
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(type);
            buf.writeInt(entityId);
            buf.writeInt(slot.ordinal());
        }

        /**
         * Обработчик сообщений взаимодействия на стороне сервера.
         * Выполняет валидацию и вызывает соответствующие методы утилит.
         */
        public static class Handler implements IMessageHandler<InteractMessage, IMessage> {
            @Override
            public IMessage onMessage(InteractMessage message, MessageContext ctx) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    World world = player.world;
                    Entity entity = world.getEntityByID(message.entityId);
                    if (entity == null) {
                        return;
                    }

                    double distanceSquared = player.getDistanceSq(entity);
                    if (distanceSquared > 1.0D) {
                        return;
                    }

                    float yawDifference = MathHelper.wrapDegrees(player.rotationYaw - entity.rotationYaw);
                    if (yawDifference < -55.0F || yawDifference > 55.0F) {
                        return;
                    }

                    ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
                    if (entity instanceof EntityExoskeleton.Exoskeleton) {
                        handleExoskeletonInteraction(world, player, heldItem, (EntityExoskeleton.Exoskeleton) entity, message);
                    } else if (entity instanceof EntityPlayer && entity != player && (message.type == 0 || message.type == 1)) {
                        handlePlayerInteraction(world, player, heldItem, (EntityPlayer) entity, message);
                    }
                });
                return null;
            }

            /**
             * Обрабатывает взаимодействие с экзоскелетом.
             *
             * @param world       Мир, в котором происходит взаимодействие
             * @param player      Игрок, выполняющий действие
             * @param heldItem    Предмет в руке игрока
             * @param exoskeleton Целевой экзоскелет
             * @param message     Сообщение взаимодействия
             */
            private void handleExoskeletonInteraction(World world, EntityPlayerMP player, ItemStack heldItem,
                                                      EntityExoskeleton.Exoskeleton exoskeleton, InteractMessage message) {
                ItemStack chestStack = exoskeleton.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                if (message.type == 0 || message.type == 1) {
                    NBTTagCompound tagCompound = chestStack.getTagCompound();
                    boolean hasEnergy = tagCompound != null && tagCompound.hasKey("fusion_depletion");

                    if (message.type == 0) {
                        if (heldItem.isEmpty() || heldItem.getItem() != ItemFusionCore.itemFusionCore || hasEnergy) {
                            return;
                        }
                        UtilEntityExoskeleton.tryInstallFusionCore(world, player, EnumHand.MAIN_HAND, exoskeleton, message.slot);
                    } else if (message.type == 1) {
                        if (!heldItem.isEmpty() || !hasEnergy) {
                            return;
                        }
                        UtilEntityExoskeleton.tryUninstallFusionCore(world, player, EnumHand.MAIN_HAND, exoskeleton, message.slot);
                    }
                } else if (message.type == 2) {
                    if (!heldItem.isEmpty()) {
                        return;
                    }
                    UtilEntityExoskeleton.tryEnterExoskeleton(world, player, exoskeleton);
                }
            }

            /**
             * Обрабатывает взаимодействие с другим игроком.
             *
             * @param world        Мир, в котором происходит взаимодействие
             * @param player       Игрок, выполняющий действие
             * @param heldItem     Предмет в руке игрока
             * @param targetPlayer Целевой игрок
             * @param message      Сообщение взаимодействия
             */
            private void handlePlayerInteraction(World world, EntityPlayerMP player, ItemStack heldItem,
                                                 EntityPlayer targetPlayer, InteractMessage message) {
                ItemStack chestStack = targetPlayer.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                if (chestStack.isEmpty() || !(chestStack.getItem() instanceof IPowerArmor) || message.slot != EntityEquipmentSlot.CHEST) {
                    return;
                }

                NBTTagCompound tagCompound = chestStack.getTagCompound();
                boolean hasEnergy = tagCompound != null && tagCompound.hasKey("fusion_depletion");

                if (message.type == 0) {
                    if (heldItem.isEmpty() || heldItem.getItem() != ItemFusionCore.itemFusionCore || hasEnergy) {
                        return;
                    }
                    UtilEntityExoskeleton.tryInstallFusionCoreOnPlayer(world, player, EnumHand.MAIN_HAND, targetPlayer);
                } else if (message.type == 1) {
                    if (!heldItem.isEmpty() || !hasEnergy) {
                        return;
                    }
                    UtilEntityExoskeleton.tryUninstallFusionCoreFromPlayer(world, player, EnumHand.MAIN_HAND, targetPlayer);
                }
            }
        }
    }

    /**
     * Сообщение для начала воспроизведения звука на сервере.
     * Используется для синхронизации звуков взаимодействия между игроками.
     */
    public static class StartSoundMessage implements IMessage {
        /**
         * Режим действия для определения типа звука
         */
        private int mode;
        /**
         * Координата X для позиционирования звука
         */
        private double x;
        /**
         * Координата Y для позиционирования звука
         */
        private double y;
        /**
         * Координата Z для позиционирования звука
         */
        private double z;

        public StartSoundMessage() {
        }

        public StartSoundMessage(int mode, double x, double y, double z) {
            this.mode = mode;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            mode = buf.readInt();
            x = buf.readDouble();
            y = buf.readDouble();
            z = buf.readDouble();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(mode);
            buf.writeDouble(x);
            buf.writeDouble(y);
            buf.writeDouble(z);
        }

        /**
         * Обработчик сообщения начала звука на стороне сервера.
         * Воспроизводит звук для всех игроков в мире.
         */
        public static class Handler implements IMessageHandler<StartSoundMessage, IMessage> {
            @Override
            public IMessage onMessage(StartSoundMessage message, MessageContext ctx) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    String soundName = (message.mode == 0 || message.mode == 1) ? "fusion_core_in_out" : "power_armor_in_out";
                    SoundEvent soundEvent = ModElementRegistry.getSound(new ResourceLocation(Tags.MOD_ID, soundName));
                    float volume = 1.0F;
                    World world = player.world;
                    world.playSound(player, message.x, message.y, message.z, soundEvent, SoundCategory.PLAYERS, volume, 1.0F);
                });
                return null;
            }
        }
    }

    /**
     * Сообщение для остановки воспроизведения звука на сервере.
     * Используется при прерывании взаимодействий.
     */
    public static class StopSoundMessage implements IMessage {
        /**
         * Режим действия для определения типа звука
         */
        private int mode;
        /**
         * Координата X позиции звука
         */
        private double x;
        /**
         * Координата Y позиции звука
         */
        private double y;
        /**
         * Координата Z позиции звука
         */
        private double z;

        public StopSoundMessage() {
        }

        public StopSoundMessage(int mode, double x, double y, double z) {
            this.mode = mode;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            mode = buf.readInt();
            x = buf.readDouble();
            y = buf.readDouble();
            z = buf.readDouble();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(mode);
            buf.writeDouble(x);
            buf.writeDouble(y);
            buf.writeDouble(z);
        }

        /**
         * Обработчик сообщения остановки звука на стороне сервера.
         * Рассылает команду остановки ближайшим игрокам.
         */
        public static class Handler implements IMessageHandler<StopSoundMessage, IMessage> {
            @Override
            public IMessage onMessage(StopSoundMessage message, MessageContext ctx) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    String soundName = Tags.MOD_ID + ":" + ((message.mode == 0 || message.mode == 1)
                            ? "fusion_core_in_out"
                            : "power_armor_in_out");
                    String categoryName = SoundCategory.PLAYERS.getName();
                    double range = 32.0D;
                    AxisAlignedBB boundingBox = new AxisAlignedBB(message.x - range, message.y - range, message.z - range,
                            message.x + range, message.y + range, message.z + range);
                    List<EntityPlayerMP> players = player.world.getEntitiesWithinAABB(EntityPlayerMP.class, boundingBox);
                    StopSoundBroadcast broadcast = new StopSoundBroadcast(soundName, categoryName);
                    for (EntityPlayerMP targetPlayer : players) {
                        if (targetPlayer != player) {
                            INTERACT_NETWORK.sendTo(broadcast, targetPlayer);
                        }
                    }
                });
                return null;
            }
        }
    }

    /**
     * Широковещательное сообщение для остановки звука на клиентах.
     * Отправляется сервером для синхронизации остановки звуков.
     */
    public static class StopSoundBroadcast implements IMessage {
        /**
         * Имя останавливаемого звука
         */
        private String soundName;
        /**
         * Категория звука для остановки
         */
        private String categoryName;

        public StopSoundBroadcast() {
        }

        public StopSoundBroadcast(String soundName, String categoryName) {
            this.soundName = soundName;
            this.categoryName = categoryName;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            soundName = ByteBufUtils.readUTF8String(buf);
            categoryName = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, soundName);
            ByteBufUtils.writeUTF8String(buf, categoryName);
        }

        /**
         * Обработчик широковещательного сообщения остановки звука на стороне клиента.
         * Останавливает указанный звук в клиенте.
         */
        public static class Handler implements IMessageHandler<StopSoundBroadcast, IMessage> {
            @Override
            public IMessage onMessage(StopSoundBroadcast message, MessageContext ctx) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    SoundCategory category = SoundCategory.getByName(message.categoryName);
                    Minecraft.getMinecraft().getSoundHandler().stop(message.soundName, category);
                });
                return null;
            }
        }
    }
}