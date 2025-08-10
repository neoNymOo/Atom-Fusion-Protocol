package com.nymoo.afp;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ModDataSyncManager {
    public static abstract class SyncedWorldData extends WorldSavedData {
        private final int type;

        public SyncedWorldData(String name, int type) {
            super(name);
            this.type = type;
        }

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

    public static class MapVariables extends SyncedWorldData {
        public static final String DATA_NAME = Tags.MOD_ID + "_map";

        public MapVariables() {
            super(DATA_NAME, 0);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {}

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            return nbt;
        }

        public static MapVariables get(World world) {
            MapVariables instance = (MapVariables) world.getMapStorage().getOrLoadData(MapVariables.class, DATA_NAME);
            if (instance == null) {
                instance = new MapVariables();
                world.getMapStorage().setData(DATA_NAME, instance);
            }
            return instance;
        }
    }

    public static class WorldVariables extends SyncedWorldData {
        public static final String DATA_NAME = Tags.MOD_ID + "_world";

        public WorldVariables() {
            super(DATA_NAME, 1);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {}

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            return nbt;
        }

        public static WorldVariables get(World world) {
            WorldVariables instance = (WorldVariables) world.getMapStorage().getOrLoadData(WorldVariables.class, DATA_NAME);
            if (instance == null) {
                instance = new WorldVariables();
                world.getMapStorage().setData(DATA_NAME, instance);
            }
            return instance;
        }
    }

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

    public static class WorldSavedDataSyncMessage implements IMessage {
        public int type;
        public WorldSavedData data;

        public WorldSavedDataSyncMessage() {}

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
}