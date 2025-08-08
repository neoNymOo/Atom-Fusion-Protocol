package com.nymoo.afp;

import com.nymoo.afp.common.handler.HandlerLivingJumpEvent;
import com.nymoo.afp.proxy.IProxy;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

// Главный класс мода - точка входа
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.MOD_VERSION + "-" + Tags.MC_VERSION)
public class AtomFusionProtocol {
    // Сетевой обработчик для синхронизации данных
    public static final SimpleNetworkWrapper PACKET_HANDLER = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);

    // Прокси для разделения клиент/сервер логики
    @SidedProxy(clientSide = "com.nymoo.afp.proxy.ClientProxy", serverSide = "com.nymoo.afp.proxy.ServerProxy")
    public static IProxy proxy;

    // Экземпляр мода
    @Mod.Instance(Tags.MOD_ID)
    public static AtomFusionProtocol instance;

    // Реестр всех элементов мода (блоки, предметы и т.д.)
    public final ModElementRegistry elements = new ModElementRegistry();

    // Инициализация: регистрация обработчиков, мировых генераторов
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(elements);

        GameRegistry.registerWorldGenerator(elements, 5); // Генерация в мире
        GameRegistry.registerFuelHandler(elements); // Топливо для печей
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModElementRegistry.GuiHandler()); // GUI

        elements.preInit(event); // Предварительная инициализация элементов
        proxy.preInit(event); // Прокси инициализация
    }

    // Основная инициализация: события, рецепты
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new HandlerLivingJumpEvent()); // Обработчик прыжков
        elements.getElements().forEach(element -> element.init(event)); // Инициализация элементов
        proxy.init(event); // Прокси инициализация
    }

    // Пост-инициализация: взаимодействие с другими модами
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    // Обработка запуска сервера: команды
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        elements.getElements().forEach(element -> element.serverLoad(event)); // Загрузка серверных элементов
        proxy.serverStarting(event); // Прокси запуск сервера
    }

    // Регистрация блоков
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(elements.getBlocks().stream().map(Supplier::get).toArray(Block[]::new));
    }

    // Регистрация предметов
    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(elements.getItems().stream().map(Supplier::get).toArray(Item[]::new));
    }

    // Регистрация биомов
    @SubscribeEvent
    public void registerBiomes(RegistryEvent.Register<Biome> event) {
        event.getRegistry().registerAll(elements.getBiomes().stream().map(Supplier::get).toArray(Biome[]::new));
    }

    // Регистрация существ
    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().registerAll(elements.getEntities().stream().map(Supplier::get).toArray(EntityEntry[]::new));
    }

    // Регистрация зелий
    @SubscribeEvent
    public void registerPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().registerAll(elements.getPotions().stream().map(Supplier::get).toArray(Potion[]::new));
    }

    // Регистрация звуков
    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<net.minecraft.util.SoundEvent> event) {
        elements.registerSounds(event);
    }

    // Регистрация моделей (только клиент)
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        elements.getElements().forEach(element -> element.registerModels(event));
    }

    // Инициализация жидкостей
    static {
        FluidRegistry.enableUniversalBucket();
    }
}