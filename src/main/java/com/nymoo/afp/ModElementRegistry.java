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

// Реестр всех элементов мода
public class ModElementRegistry implements IFuelHandler, IWorldGenerator {
    // Коллекции для хранения элементов мода
    public final List<ModElement> elements = new ArrayList<>();
    public final List<Supplier<Block>> blocks = new ArrayList<>();
    public final List<Supplier<Item>> items = new ArrayList<>();
    public final List<Supplier<Biome>> biomes = new ArrayList<>();
    public final List<Supplier<EntityEntry>> entities = new ArrayList<>();
    public final List<Supplier<Potion>> potions = new ArrayList<>();

    // Реестр звуков мода
    private static final Map<ResourceLocation, SoundEvent> SOUNDS = new HashMap<>();
    private int messageID = 0; // Счетчик ID сетевых сообщений

    // Статическая инициализация звуков
    static {
        Arrays.asList("exo_click", "servo_step1", "servo_step2", "servo_step3", "fusion_core", "wrench", "switch_click", "poweron")
                .forEach(name -> SOUNDS.put(
                        new ResourceLocation(Tags.MOD_ID, name),
                        new SoundEvent(new ResourceLocation(Tags.MOD_ID, name))
                ));
    }

    // Предварительная инициализация: загрузка элементов, сортировка
    public void preInit(FMLPreInitializationEvent event) {
        loadModElements(event); // Загрузка классов через reflection
        Collections.sort(elements); // Сортировка элементов
        elements.forEach(ModElement::initElements); // Инициализация элементов
        registerNetworkMessages(); // Регистрация сетевых сообщений
    }

    // Загрузка классов, помеченных аннотацией @ModElement.Tag
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

    // Регистрация сетевых сообщений
    private void registerNetworkMessages() {
        addNetworkMessage(
                ModDataSyncManager.WorldSavedDataSyncMessageHandler.class,
                ModDataSyncManager.WorldSavedDataSyncMessage.class,
                Side.SERVER, Side.CLIENT
        );
    }

    // Регистрация звуков в игре
    public void registerSounds(RegistryEvent.Register<net.minecraft.util.SoundEvent> event) {
        for (Map.Entry<ResourceLocation, SoundEvent> entry : SOUNDS.entrySet()) {
            entry.getValue().setRegistryName(entry.getKey());
            event.getRegistry().register(entry.getValue());
        }
    }

    // Генерация в мире: вызов для каждого элемента
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator cg, IChunkProvider cp) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int dimension = world.provider.getDimension();

        for (ModElement element : elements) {
            element.generateWorld(random, baseX, baseZ, world, dimension, cg, cp);
        }
    }

    // Обработка топлива: поиск подходящего элемента
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

    // Событие входа игрока: синхронизация данных
    @SubscribeEvent
    public void onPlayerLoggedIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        World world = player.world;

        sendWorldData(player, ModDataSyncManager.MapVariables.get(world), 0); // Данные карты
        sendWorldData(player, ModDataSyncManager.WorldVariables.get(world), 1); // Данные мира
    }

    // Событие смены измерения: синхронизация данных мира
    @SubscribeEvent
    public void onPlayerChangedDimension(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player.world.isRemote) return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        World world = player.world;

        sendWorldData(player, ModDataSyncManager.WorldVariables.get(world), 1);
    }

    // Отправка данных игроку
    private void sendWorldData(EntityPlayerMP player, WorldSavedData data, int type) {
        if (data != null) {
            AtomFusionProtocol.PACKET_HANDLER.sendTo(
                    new ModDataSyncManager.WorldSavedDataSyncMessage(type, data),
                    player
            );
        }
    }

    // Регистрация сетевого сообщения
    public <T extends IMessage, V extends IMessage> void addNetworkMessage(
            Class<? extends IMessageHandler<T, V>> handler,
            Class<T> messageClass,
            Side... sides) {
        for (Side side : sides) {
            AtomFusionProtocol.PACKET_HANDLER.registerMessage(handler, messageClass, messageID++, side);
        }
    }

    // Обработчик GUI (пустой)
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

    // Геттеры для коллекций элементов
    public List<ModElement> getElements() { return elements; }
    public List<Supplier<Block>> getBlocks() { return blocks; }
    public List<Supplier<Item>> getItems() { return items; }
    public List<Supplier<Biome>> getBiomes() { return biomes; }
    public List<Supplier<EntityEntry>> getEntities() { return entities; }
    public List<Supplier<Potion>> getPotions() { return potions; }

    // Базовый класс для элементов мода
    public static class ModElement implements Comparable<ModElement> {
        @Retention(RetentionPolicy.RUNTIME)
        public @interface Tag {} // Маркер для автоматической загрузки

        protected final ModElementRegistry elements;
        protected final int sortid; // Порядок загрузки

        public ModElement(ModElementRegistry elements, int sortid) {
            this.elements = elements;
            this.sortid = sortid;
        }

        // Методы для переопределения в элементах
        public void initElements() {} // Инициализация компонентов
        public void init(FMLInitializationEvent event) {} // Основная инициализация
        public void preInit(FMLPreInitializationEvent event) {} // Предварительная инициализация
        public void generateWorld(Random random, int posX, int posZ, World world, int dimID, IChunkGenerator cg, IChunkProvider cp) {} // Генерация в мире
        public void serverLoad(FMLServerStartingEvent event) {} // Загрузка на сервере
        public void registerModels(ModelRegistryEvent event) {} // Регистрация моделей (клиент)
        public int addFuel(ItemStack fuel) { return 0; } // Время горения топлива

        // Сравнение для сортировки
        @Override
        public int compareTo(ModElement other) {
            return Integer.compare(this.sortid, other.sortid);
        }
    }
}