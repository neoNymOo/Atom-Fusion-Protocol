package com.nymoo.afp;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import com.nymoo.afp.common.entity.EntityExoskeleton;
import com.nymoo.afp.common.item.IPowerArmor;
import com.nymoo.afp.common.item.ItemFusionCore;
import com.nymoo.afp.common.util.UtilEntityExoskeleton;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Manages synchronization of mod-specific data between client and server.
 * Handles world saved data for map-wide and dimension-specific variables.
 * Also manages network messages for interactions with exoskeleton entities.
 */
public class ModDataSyncManager {
    /**
     * Abstract base class for synchronized world data.
     * Provides methods to mark data dirty and sync across network.
     */
    public static abstract class SyncedWorldData extends WorldSavedData {
        private final int type;

        public SyncedWorldData(String name, int type) {
            super(name);
            this.type = type;
        }

        /**
         * Synchronizes the data across the network.
         * If on client, sends to server; if on server, broadcasts to all or dimension.
         * @param world The world context for synchronization.
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
     * Map-wide variables that are synced across all dimensions.
     */
    public static class MapVariables extends SyncedWorldData {
        public static final String DATA_NAME = Tags.MOD_ID + "_map";

        public MapVariables() {
            super(DATA_NAME, 0);
        }

        /**
         * Retrieves or creates the MapVariables instance for the world.
         * @param world The world to get variables for.
         * @return The MapVariables instance.
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
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            return nbt;
        }
    }

    /**
     * Dimension-specific variables that are synced within the dimension.
     */
    public static class WorldVariables extends SyncedWorldData {
        public static final String DATA_NAME = Tags.MOD_ID + "_world";

        public WorldVariables() {
            super(DATA_NAME, 1);
        }

        /**
         * Retrieves or creates the WorldVariables instance for the world.
         * @param world The world to get variables for.
         * @return The WorldVariables instance.
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
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            return nbt;
        }
    }

    /**
     * Handler for world saved data synchronization messages.
     * Processes messages on both client and server sides.
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
         * Synchronizes the received data.
         * On server, marks dirty and broadcasts; on client, sets data.
         * @param message The sync message.
         * @param context The message context.
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
     * Network message for synchronizing world saved data.
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
        public void toBytes(io.netty.buffer.ByteBuf buf) {
            buf.writeInt(type);
            ByteBufUtils.writeTag(buf, data.writeToNBT(new NBTTagCompound()));
        }

        @Override
        public void fromBytes(io.netty.buffer.ByteBuf buf) {
            type = buf.readInt();
            NBTTagCompound nbt = ByteBufUtils.readTag(buf);
            data = type == 0 ? new MapVariables() : new WorldVariables();
            data.readFromNBT(nbt);
        }
    }

    /**
     * Network wrapper for exoskeleton interaction messages.
     * Registered in static initializer.
     */
    public static final SimpleNetworkWrapper INTERACT_NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("afp_interact");

    static {
        INTERACT_NETWORK.registerMessage(InteractMessage.Handler.class, InteractMessage.class, 0, Side.SERVER);
    }

    /**
     * Network message for exoskeleton interactions (install/uninstall fusion core, enter).
     * Sent from client to server when hold action completes.
     */
    public static class InteractMessage implements IMessage {
        private int type;
        private int entityId;
        private EntityEquipmentSlot slot;

        public InteractMessage() {}

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
         * Handler for interaction messages on server.
         * Validates conditions and calls appropriate UtilEntityExoskeleton methods.
         */
        public static class Handler implements IMessageHandler<InteractMessage, IMessage> {
            @Override
            public IMessage onMessage(InteractMessage message, MessageContext ctx) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    World world = player.world;
                    if (message.type == 0 || message.type == 1 || message.type == 2) {
                        Entity entity = world.getEntityByID(message.entityId);
                        if (!(entity instanceof EntityExoskeleton.Exoskeleton)) return;

                        EntityExoskeleton.Exoskeleton exo = (EntityExoskeleton.Exoskeleton) entity;
                        double distanceSq = player.getDistanceSq(exo);
                        if (distanceSq > 1.0D) return;

                        float yawDiff = MathHelper.wrapDegrees(player.rotationYaw - exo.rotationYaw);
                        if (yawDiff < -55.0F || yawDiff > 55.0F) return;

                        if (message.type == 0 || message.type == 1) {
                            ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
                            ItemStack chest = exo.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                            NBTTagCompound tag = chest.getTagCompound();
                            boolean hasEnergy = tag != null && tag.hasKey("fusion_energy");

                            if (message.type == 0) {
                                if (held.isEmpty() || held.getItem() != ItemFusionCore.itemFusionCore || hasEnergy) return;
                                UtilEntityExoskeleton.tryInstallFusionCore(world, player, EnumHand.MAIN_HAND, exo, message.slot);
                            } else if (message.type == 1) {
                                if (!held.isEmpty() || !hasEnergy) return;
                                UtilEntityExoskeleton.tryUninstallFusionCore(world, player, EnumHand.MAIN_HAND, exo, message.slot);
                            }
                        } else if (message.type == 2) {
                            ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
                            if (!held.isEmpty()) return;
                            UtilEntityExoskeleton.tryEnterExoskeleton(world, player, exo);
                        }
                    }
                });
                return null;
            }
        }
    }
}