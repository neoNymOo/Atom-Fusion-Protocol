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

/**
 * Центральный реестр элементов мода для управления контентом.
 * Обрабатывает регистрацию блоков, предметов, биомов, сущностей, зелий и звуков.
 * Реализует генерацию мира и систему топлива.
 */
public class ModElementRegistry implements IFuelHandler, IWorldGenerator {
    /**
     * Карта зарегистрированных звуков мода
     */
    private static final Map<ResourceLocation, SoundEvent> SOUNDS = new HashMap<>();

    static {
        Arrays.asList("exo_click", "servo_step1", "servo_step2", "servo_step3", "fusion_core", "wrench", "switch_click", "poweron", "fusion_core_in_out", "power_armor_in_out")
                .forEach(name -> SOUNDS.put(
                        new ResourceLocation(Tags.MOD_ID, name),
                        new SoundEvent(new ResourceLocation(Tags.MOD_ID, name))
                ));
    }

    /**
     * Список элементов мода для управления жизненным циклом
     */
    public final List<ModElement> elements = new ArrayList<>();
    /**
     * Список поставщиков блоков для регистрации
     */
    public final List<Supplier<Block>> blocks = new ArrayList<>();
    /**
     * Список поставщиков предметов для регистрации
     */
    public final List<Supplier<Item>> items = new ArrayList<>();
    /**
     * Список поставщиков биомов для регистрации
     */
    public final List<Supplier<Biome>> biomes = new ArrayList<>();
    /**
     * Список поставщиков сущностей для регистрации
     */
    public final List<Supplier<EntityEntry>> entities = new ArrayList<>();
    /**
     * Список поставщиков зелий для регистрации
     */
    public final List<Supplier<Potion>> potions = new ArrayList<>();
    /**
     * Счетчик ID для сетевых сообщений
     */
    private int messageID = 0;

    /**
     * Получает звуковое событие по его локации.
     *
     * @param location Локация звука
     * @return Звуковое событие или null если не найдено
     */
    public static SoundEvent getSound(ResourceLocation location) {
        return SOUNDS.get(location);
    }

    /**
     * Выполняет предварительную инициализацию реестра.
     * Загружает элементы мода, сортирует их и регистрирует сетевые сообщения.
     *
     * @param event Событие предварительной инициализации
     */
    public void preInit(FMLPreInitializationEvent event) {
        loadModElements(event);
        Collections.sort(elements);
        elements.forEach(ModElement::initElements);
        registerNetworkMessages();
    }

    /**
     * Загружает элементы мода через рефлексию на основе аннотаций.
     *
     * @param event Событие предварительной инициализации для доступа к данным ASM
     */
    private void loadModElements(FMLPreInitializationEvent event) {
        for (ASMDataTable.ASMData asmData : event.getAsmData().getAll(ModElement.Tag.class.getName())) {
            try {
                Class<?> clazz = Class.forName(asmData.getClassName());
                if (ModElement.class.isAssignableFrom(clazz)) {
                    elements.add((ModElement) clazz.getConstructor(this.getClass()).newInstance(this));
                }
            } catch (ReflectiveOperationException | ClassCastException exception) {
                System.err.println("Failed to load mod element: " + asmData.getClassName());
                exception.printStackTrace();
            }
        }
    }

    /**
     * Регистрирует сетевые сообщения для коммуникации между клиентом и сервером.
     */
    private void registerNetworkMessages() {
        addNetworkMessage(
                ModDataSyncManager.WorldSavedDataSyncMessageHandler.class,
                ModDataSyncManager.WorldSavedDataSyncMessage.class,
                Side.SERVER, Side.CLIENT
        );
    }

    /**
     * Регистрирует звуки мода в реестре игры.
     *
     * @param event Событие регистрации звуков
     */
    public void registerSounds(RegistryEvent.Register<net.minecraft.util.SoundEvent> event) {
        for (Map.Entry<ResourceLocation, SoundEvent> entry : SOUNDS.entrySet()) {
            entry.getValue().setRegistryName(entry.getKey());
            event.getRegistry().register(entry.getValue());
        }
    }

    /**
     * Генерирует контент мода в мире.
     * Вызывается для каждого чанка при генерации мира.
     *
     * @param random         Генератор случайных чисел
     * @param chunkX         Координата X чанка
     * @param chunkZ         Координата Z чанка
     * @param world          Мир для генерации
     * @param chunkGenerator Генератор чанков
     * @param chunkProvider  Провайдер чанков
     */
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int dimension = world.provider.getDimension();

        for (ModElement element : elements) {
            element.generateWorld(random, baseX, baseZ, world, dimension, chunkGenerator, chunkProvider);
        }
    }

    /**
     * Определяет время горения предмета в качестве топлива.
     *
     * @param fuel Предмет для проверки как топливо
     * @return Время горения в тиках, 0 если не является топливом
     */
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

    /**
     * Обрабатывает вход игрока на сервер.
     * Синхронизирует данные мира с подключившимся игроком.
     *
     * @param event Событие входа игрока
     */
    @SubscribeEvent
    public void onPlayerLoggedIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        World world = player.world;

        sendWorldData(player, ModDataSyncManager.MapVariables.get(world), 0);
        sendWorldData(player, ModDataSyncManager.WorldVariables.get(world), 1);
    }

    /**
     * Обрабатывает смену измерения игроком.
     * Синхронизирует данные нового измерения с игроком.
     *
     * @param event Событие смены измерения
     */
    @SubscribeEvent
    public void onPlayerChangedDimension(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player.world.isRemote) return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        World world = player.world;

        sendWorldData(player, ModDataSyncManager.WorldVariables.get(world), 1);
    }

    /**
     * Отправляет данные мира игроку по сети.
     *
     * @param player Игрок для отправки данных
     * @param data   Данные мира для отправки
     * @param type   Тип данных: 0 - уровень карты, 1 - уровень измерения
     */
    private void sendWorldData(EntityPlayerMP player, WorldSavedData data, int type) {
        if (data != null) {
            AtomFusionProtocol.PACKET_HANDLER.sendTo(
                    new ModDataSyncManager.WorldSavedDataSyncMessage(type, data),
                    player
            );
        }
    }

    /**
     * Регистрирует сетевое сообщение для обработки на указанных сторонах.
     *
     * @param handler      Класс-обработчик сообщения
     * @param messageClass Класс сообщения
     * @param sides        Стороны для регистрации (клиент, сервер или обе)
     * @param <T>          Тип сообщения
     * @param <V>          Тип ответного сообщения
     */
    public <T extends IMessage, V extends IMessage> void addNetworkMessage(
            Class<? extends IMessageHandler<T, V>> handler,
            Class<T> messageClass,
            Side... sides) {
        for (Side side : sides) {
            AtomFusionProtocol.PACKET_HANDLER.registerMessage(handler, messageClass, messageID++, side);
        }
    }

    /**
     * Получает список всех элементов мода.
     *
     * @return Неизменяемый список элементов мода
     */
    public List<ModElement> getElements() {
        return elements;
    }

    /**
     * Получает список поставщиков блоков для регистрации.
     *
     * @return Список поставщиков блоков
     */
    public List<Supplier<Block>> getBlocks() {
        return blocks;
    }

    /**
     * Получает список поставщиков предметов для регистрации.
     *
     * @return Список поставщиков предметов
     */
    public List<Supplier<Item>> getItems() {
        return items;
    }

    /**
     * Получает список поставщиков биомов для регистрации.
     *
     * @return Список поставщиков биомов
     */
    public List<Supplier<Biome>> getBiomes() {
        return biomes;
    }

    /**
     * Получает список поставщиков сущностей для регистрации.
     *
     * @return Список поставщиков сущностей
     */
    public List<Supplier<EntityEntry>> getEntities() {
        return entities;
    }

    /**
     * Получает список поставщиков зелий для регистрации.
     *
     * @return Список поставщиков зелий
     */
    public List<Supplier<Potion>> getPotions() {
        return potions;
    }

    /**
     * Обработчик графических интерфейсов для регистрации GUI мода.
     */
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

    /**
     * Базовый класс для элементов мода с поддержкой жизненного цикла.
     * Автоматически загружается через аннотации и сортируется по приоритету.
     */
    public static class ModElement implements Comparable<ModElement> {
        /**
         * Родительский реестр элементов
         */
        protected final ModElementRegistry elements;
        /**
         * Идентификатор сортировки для определения порядка загрузки
         */
        protected final int sortid;

        public ModElement(ModElementRegistry elements, int sortid) {
            this.elements = elements;
            this.sortid = sortid;
        }

        /**
         * Инициализирует элементы мода (блоки, предметы и т.д.).
         * Вызывается во время предварительной инициализации.
         */
        public void initElements() {
        }

        /**
         * Выполняет инициализацию элемента.
         * Вызывается во время основной инициализации мода.
         *
         * @param event Событие инициализации
         */
        public void init(FMLInitializationEvent event) {
        }

        /**
         * Выполняет предварительную инициализацию элемента.
         * Вызывается во время предварительной инициализации мода.
         *
         * @param event Событие предварительной инициализации
         */
        public void preInit(FMLPreInitializationEvent event) {
        }

        /**
         * Генерирует контент элемента в мире.
         * Вызывается для каждого чанка во время генерации мира.
         *
         * @param random         Генератор случайных чисел
         * @param posX           Базовая координата X чанка
         * @param posZ           Базовая координата Z чанка
         * @param world          Мир для генерации
         * @param dimensionID    ID измерения
         * @param chunkGenerator Генератор чанков
         * @param chunkProvider  Провайдер чанков
         */
        public void generateWorld(Random random, int posX, int posZ, World world, int dimensionID, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        }

        /**
         * Выполняет загрузку элемента на сервере.
         * Вызывается при запуске сервера.
         *
         * @param event Событие запуска сервера
         */
        public void serverLoad(FMLServerStartingEvent event) {
        }

        /**
         * Регистрирует модели элемента для клиентской части.
         * Вызывается только на клиенте.
         *
         * @param event Событие регистрации моделей
         */
        public void registerModels(ModelRegistryEvent event) {
        }

        /**
         * Определяет время горения предмета как топлива для этого элемента.
         *
         * @param fuel Предмет для проверки
         * @return Время горения в тиках, 0 если не является топливом
         */
        public int addFuel(ItemStack fuel) {
            return 0;
        }

        /**
         * Сравнивает элементы по идентификатору сортировки.
         * Используется для определения порядка загрузки элементов.
         *
         * @param other Другой элемент для сравнения
         * @return Результат сравнения идентификаторов сортировки
         */
        @Override
        public int compareTo(ModElement other) {
            return Integer.compare(this.sortid, other.sortid);
        }

        /**
         * Аннотация для пометки классов как элементов мода.
         * Автоматически обнаруживается и загружается во время предварительной инициализации.
         */
        @Retention(RetentionPolicy.RUNTIME)
        public @interface Tag {
        }
    }
}