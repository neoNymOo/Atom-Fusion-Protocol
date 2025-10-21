package com.nymoo.afp;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;
import java.util.function.Supplier;

public class ModElementRegistry implements IFuelHandler, IWorldGenerator {
    private static final Map<ResourceLocation, SoundEvent> SOUNDS = new HashMap<>();

    static {
        Arrays.asList("exo_click", "servo_step1", "servo_step2", "servo_step3", "fusion_core", "wrench", "switch_click", "poweron", "fusion_core_in_out", "power_armor_in_out")
                .forEach(name -> SOUNDS.put(
                        new ResourceLocation(Tags.MOD_ID, name),
                        new SoundEvent(new ResourceLocation(Tags.MOD_ID, name))
                ));
    }

    public final List<ModElement> elements = new ArrayList<>();
    public final List<Supplier<Block>> blocks = new ArrayList<>();
    public final List<Supplier<Item>> items = new ArrayList<>();
    public final List<Supplier<Biome>> biomes = new ArrayList<>();
    public final List<Supplier<EntityEntry>> entities = new ArrayList<>();
    public final List<Supplier<Potion>> potions = new ArrayList<>();
    private int messageID = 0;

    public static SoundEvent getSound(ResourceLocation location) {
        return SOUNDS.get(location);
    }

    public void preInit(FMLPreInitializationEvent event) {
        loadModElements(event);
        Collections.sort(elements);
        elements.forEach(ModElement::initElements);
        registerNetworkMessages();
    }

    private void loadModElements(FMLPreInitializationEvent event) {
        for (ASMDataTable.ASMData asmData : event.getAsmData().getAll(ModElement.Tag.class.getName())) {
            try {
                Class<?> clazz = Class.forName(asmData.getClassName());
                if (ModElement.class.isAssignableFrom(clazz)) {
                    elements.add((ModElement) clazz.getConstructor(this.getClass()).newInstance(this));
                }
            } catch (ReflectiveOperationException | ClassCastException e) {
                System.err.println("Failed to load mod element: " + asmData.getClassName());
                e.printStackTrace();
            }
        }
    }

    private void registerNetworkMessages() {
        addNetworkMessage(
                ModDataSyncManager.WorldSavedDataSyncMessageHandler.class,
                ModDataSyncManager.WorldSavedDataSyncMessage.class,
                Side.SERVER, Side.CLIENT
        );
    }

    public void registerSounds(RegistryEvent.Register<net.minecraft.util.SoundEvent> event) {
        for (Map.Entry<ResourceLocation, SoundEvent> entry : SOUNDS.entrySet()) {
            entry.getValue().setRegistryName(entry.getKey());
            event.getRegistry().register(entry.getValue());
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator cg, IChunkProvider cp) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int dimension = world.provider.getDimension();

        for (ModElement element : elements) {
            element.generateWorld(random, baseX, baseZ, world, dimension, cg, cp);
        }
    }

    @Override
    public int getBurnTime(ItemStack fuel) {
        for (ModElement element : elements) {
            int burnTime = element.addFuel(fuel);
            if (burnTime != 0) {
                return burnTime;
            }
        }
        return 0;
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        World world = player.world;

        sendWorldData(player, ModDataSyncManager.MapVariables.get(world), 0);
        sendWorldData(player, ModDataSyncManager.WorldVariables.get(world), 1);
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player.world.isRemote) return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        World world = player.world;

        sendWorldData(player, ModDataSyncManager.WorldVariables.get(world), 1);
    }

    private void sendWorldData(EntityPlayerMP player, WorldSavedData data, int type) {
        if (data != null) {
            AtomFusionProtocol.PACKET_HANDLER.sendTo(
                    new ModDataSyncManager.WorldSavedDataSyncMessage(type, data),
                    player
            );
        }
    }

    public <T extends IMessage, V extends IMessage> void addNetworkMessage(
            Class<? extends IMessageHandler<T, V>> handler,
            Class<T> messageClass,
            Side... sides) {
        for (Side side : sides) {
            AtomFusionProtocol.PACKET_HANDLER.registerMessage(handler, messageClass, messageID++, side);
        }
    }

    public List<ModElement> getElements() {
        return elements;
    }

    public List<Supplier<Block>> getBlocks() {
        return blocks;
    }

    public List<Supplier<Item>> getItems() {
        return items;
    }

    public List<Supplier<Biome>> getBiomes() {
        return biomes;
    }

    public List<Supplier<EntityEntry>> getEntities() {
        return entities;
    }

    public List<Supplier<Potion>> getPotions() {
        return potions;
    }

    public static class GuiHandler implements IGuiHandler {
        @Override
        public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            return null;
        }

        @Override
        public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            return null;
        }
    }

    public static class ModElement implements Comparable<ModElement> {
        protected final ModElementRegistry elements;
        protected final int sortid;

        public ModElement(ModElementRegistry elements, int sortid) {
            this.elements = elements;
            this.sortid = sortid;
        }

        public void initElements() {
        }

        public void init(FMLInitializationEvent event) {
        }

        public void preInit(FMLPreInitializationEvent event) {
        }

        public void generateWorld(Random random, int posX, int posZ, World world, int dimID, IChunkGenerator cg, IChunkProvider cp) {
        }

        public void serverLoad(FMLServerStartingEvent event) {
        }

        public void registerModels(ModelRegistryEvent event) {
        }

        public int addFuel(ItemStack fuel) {
            return 0;
        }

        @Override
        public int compareTo(ModElement other) {
            return Integer.compare(this.sortid, other.sortid);
        }

        @Retention(RetentionPolicy.RUNTIME)
        public @interface Tag {
        }
    }
}