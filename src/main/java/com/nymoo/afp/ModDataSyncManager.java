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
 * Управляет синхронизацией мод-специфических данных между клиентом и сервером.
 * Обрабатывает сохраненные данные мира для переменных уровня карты и измерения.
 * Также управляет сетевыми сообщениями для взаимодействий с сущностями экзоскелета.
 * Расширен для поддержки взаимодействий с другими игроками для операций с ядрами fusion.
 */
public class ModDataSyncManager {
    /**
     * Обертка сети для сообщений взаимодействия с экзоскелетом.
     * Регистрируется в статическом инициализаторе.
     */
    public static final SimpleNetworkWrapper INTERACT_NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("afp_interact");

    static {
        INTERACT_NETWORK.registerMessage(InteractMessage.Handler.class, InteractMessage.class, 0, Side.SERVER);
        INTERACT_NETWORK.registerMessage(StartSoundMessage.Handler.class, StartSoundMessage.class, 1, Side.SERVER);
        INTERACT_NETWORK.registerMessage(StopSoundMessage.Handler.class, StopSoundMessage.class, 2, Side.SERVER);
        INTERACT_NETWORK.registerMessage(StopSoundBroadcast.Handler.class, StopSoundBroadcast.class, 3, Side.CLIENT);
    }

    /**
     * Абстрактный базовый класс для синхронизированных данных мира.
     * Предоставляет методы для пометки данных как грязных и синхронизации по сети.
     */
    public static abstract class SyncedWorldData extends WorldSavedData {
        private final int type;

        public SyncedWorldData(String name, int type) {
            super(name);
            this.type = type;
        }

        /**
         * Синхронизирует данные по сети.
         * Если на клиенте, отправляет на сервер; если на сервере, рассылает всем или в измерение.
         *
         * @param world Контекст мира для синхронизации.
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
     * Переменные уровня карты, синхронизируемые по всем измерениям.
     */
    public static class MapVariables extends SyncedWorldData {
        public static final String DATA_NAME = Tags.MOD_ID + "_map";

        // Конструктор, ожидаемый Minecraft при загрузке данных
        public MapVariables(String mapName) {
            super(mapName, 0);
        }

        public MapVariables() {
            super(DATA_NAME, 0);
        }

        /**
         * Получает или создает экземпляр MapVariables для мира.
         *
         * @param world Мир, для которого получить переменные.
         * @return Экземпляр MapVariables.
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
            // Здесь можно добавить чтение специфических данных, если нужно
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            // Здесь можно добавить запись специфических данных, если нужно
            return nbt;
        }
    }

    /**
     * Переменные специфичные для измерения, синхронизируемые в пределах измерения.
     */
    public static class WorldVariables extends SyncedWorldData {
        public static final String DATA_NAME = Tags.MOD_ID + "_world";

        // Конструктор, ожидаемый Minecraft при загрузке данных
        public WorldVariables(String mapName) {
            super(mapName, 1);
        }

        public WorldVariables() {
            super(DATA_NAME, 1);
        }

        /**
         * Получает или создает экземпляр WorldVariables для мира.
         *
         * @param world Мир, для которого получить переменные.
         * @return Экземпляр WorldVariables.
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
            // Здесь можно добавить чтение специфических данных, если нужно
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            // Здесь можно добавить запись специфических данных, если нужно
            return nbt;
        }
    }

    /**
     * Обработчик сообщений синхронизации сохраненных данных мира.
     * Обрабатывает сообщения на сторонах клиента и сервера.
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
         * Синхронизирует полученные данные.
         * На сервере помечает грязными и рассылает; на клиенте устанавливает данные.
         *
         * @param message Сообщение синхронизации.
         * @param context Контекст сообщения.
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
     * Сетевое сообщение для синхронизации сохраненных данных мира.
     */
    public static class WorldSavedDataSyncMessage implements IMessage {
        public int type;
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
     * Сетевое сообщение для взаимодействий с экзоскелетом (установка/удаление ядер fusion, вход).
     * Отправляется с клиента на сервер при завершении удержания.
     */
    public static class InteractMessage implements IMessage {
        private int type;
        private int entityId;
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
         * Обработчик сообщений взаимодействий на сервере.
         * Валидирует условия и вызывает соответствующие методы UtilEntityExoskeleton.
         * Поддерживает как сущности экзоскелета, так и других игроков для действий с ядрами fusion.
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

                    // Валидация расстояния и поворота
                    double distanceSq = player.getDistanceSq(entity);
                    if (distanceSq > 1.0D) {
                        return;
                    }

                    float yawDiff = MathHelper.wrapDegrees(player.rotationYaw - entity.rotationYaw);
                    if (yawDiff < -55.0F || yawDiff > 55.0F) {
                        return;
                    }

                    ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
                    if (entity instanceof EntityExoskeleton.Exoskeleton) {
                        EntityExoskeleton.Exoskeleton exo = (EntityExoskeleton.Exoskeleton) entity;
                        ItemStack chest = exo.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                        if (message.type == 0 || message.type == 1) {
                            NBTTagCompound tag = chest.getTagCompound();
                            boolean hasEnergy = tag != null && tag.hasKey("fusion_depletion");

                            if (message.type == 0) {
                                if (heldItem.isEmpty() || heldItem.getItem() != ItemFusionCore.itemFusionCore || hasEnergy) {
                                    return;
                                }
                                UtilEntityExoskeleton.tryInstallFusionCore(world, player, EnumHand.MAIN_HAND, exo, message.slot);
                            } else if (message.type == 1) {
                                if (!heldItem.isEmpty() || !hasEnergy) {
                                    return;
                                }
                                UtilEntityExoskeleton.tryUninstallFusionCore(world, player, EnumHand.MAIN_HAND, exo, message.slot);
                            }
                        } else if (message.type == 2) {
                            if (!heldItem.isEmpty()) {
                                return;
                            }
                            UtilEntityExoskeleton.tryEnterExoskeleton(world, player, exo);
                        }
                    } else if (entity instanceof EntityPlayer && entity != player && (message.type == 0 || message.type == 1)) {
                        EntityPlayer target = (EntityPlayer) entity;
                        ItemStack chest = target.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                        if (chest.isEmpty() || !(chest.getItem() instanceof IPowerArmor) || message.slot != EntityEquipmentSlot.CHEST) {
                            return;
                        }
                        NBTTagCompound tag = chest.getTagCompound();
                        boolean hasEnergy = tag != null && tag.hasKey("fusion_depletion");

                        if (message.type == 0) {
                            if (heldItem.isEmpty() || heldItem.getItem() != ItemFusionCore.itemFusionCore || hasEnergy) {
                                return;
                            }
                            UtilEntityExoskeleton.tryInstallFusionCoreOnPlayer(world, player, EnumHand.MAIN_HAND, target);
                        } else if (message.type == 1) {
                            if (!heldItem.isEmpty() || !hasEnergy) {
                                return;
                            }
                            UtilEntityExoskeleton.tryUninstallFusionCoreFromPlayer(world, player, EnumHand.MAIN_HAND, target);
                        }
                    }
                });
                return null;
            }
        }
    }

    /**
     * Сетевое сообщение для начала воспроизведения звука на сервере (для других игроков).
     * Отправляется с клиента на сервер при начале удержания.
     */
    public static class StartSoundMessage implements IMessage {
        private int mode;
        private double x;
        private double y;
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
         * Обработчик сообщения на сервере.
         * Воспроизводит звук для всех игроков кроме отправителя.
         */
        public static class Handler implements IMessageHandler<StartSoundMessage, IMessage> {
            @Override
            public IMessage onMessage(StartSoundMessage message, MessageContext ctx) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    String soundName = (message.mode == 0 || message.mode == 1) ? "fusion_core_in_out" : "power_armor_in_out";
                    SoundEvent sound = ModElementRegistry.getSound(new ResourceLocation(Tags.MOD_ID, soundName));
                    float volume = 1.0F; // Громкость по умолчанию, совпадает с клиентскими значениями
                    World world = player.world;
                    world.playSound(player, message.x, message.y, message.z, sound, SoundCategory.PLAYERS, volume, 1.0F);
                });
                return null;
            }
        }
    }

    /**
     * Сетевое сообщение для остановки звука на сервере (для других игроков).
     * Отправляется с клиента на сервер при прерывании удержания.
     */
    public static class StopSoundMessage implements IMessage {
        private int mode;
        private double x;
        private double y;
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
         * Обработчик сообщения на сервере.
         * Рассылает сообщение об остановке звука ближайшим игрокам, исключая отправителя.
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
                    AxisAlignedBB aabb = new AxisAlignedBB(message.x - range, message.y - range, message.z - range,
                            message.x + range, message.y + range, message.z + range);
                    List<EntityPlayerMP> players = player.world.getEntitiesWithinAABB(EntityPlayerMP.class, aabb);
                    StopSoundBroadcast broadcast = new StopSoundBroadcast(soundName, categoryName);
                    for (EntityPlayerMP p : players) {
                        if (p != player) {
                            INTERACT_NETWORK.sendTo(broadcast, p);
                        }
                    }
                });
                return null;
            }
        }
    }

    /**
     * Сетевое сообщение для остановки звука на клиенте.
     * Отправляется с сервера на клиентов для остановки звука.
     */
    public static class StopSoundBroadcast implements IMessage {
        private String soundName;
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
         * Обработчик сообщения на клиенте.
         * Останавливает указанный звук.
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