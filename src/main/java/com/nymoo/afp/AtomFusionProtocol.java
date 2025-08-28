package com.nymoo.afp;

import com.nymoo.afp.common.config.AFPConfig;
import com.nymoo.afp.common.handler.HandlerLivingJumpEvent;
import com.nymoo.afp.proxy.IProxy;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
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

import java.io.File;
import java.util.function.Supplier;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.MOD_VERSION + "-" + Tags.MC_VERSION)
public class AtomFusionProtocol {
    public static final boolean IS_HBM_LOADED = Loader.isModLoaded("hbm");
    public static final SimpleNetworkWrapper PACKET_HANDLER = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);
    @SidedProxy(clientSide = "com.nymoo.afp.proxy.ClientProxy", serverSide = "com.nymoo.afp.proxy.ServerProxy")
    public static IProxy proxy;
    @Mod.Instance(Tags.MOD_ID)
    public static AtomFusionProtocol instance;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    public ModElementRegistry elements = new ModElementRegistry();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configFile = event.getSuggestedConfigurationFile();
        Configuration config = new Configuration(configFile);
        try {
            config.load();
            AFPConfig.loadFromConfig(config);
        } catch (Exception e) {
            System.err.println("Error loading the config " + Tags.MOD_ID + ": " + e.getMessage());
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
        MinecraftForge.EVENT_BUS.register(this);
        GameRegistry.registerWorldGenerator(elements, 5);
        GameRegistry.registerFuelHandler(elements);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModElementRegistry.GuiHandler());
        elements.preInit(event);
        MinecraftForge.EVENT_BUS.register(elements);
        elements.getElements().forEach(element -> element.preInit(event));
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new HandlerLivingJumpEvent());
        elements.getElements().forEach(element -> element.init(event));
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        elements.getElements().forEach(element -> element.serverLoad(event));
        proxy.serverStarting(event);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(elements.getBlocks().stream().map(Supplier::get).toArray(Block[]::new));
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(elements.getItems().stream().map(Supplier::get).toArray(Item[]::new));
    }

    @SubscribeEvent
    public void registerBiomes(RegistryEvent.Register<Biome> event) {
        event.getRegistry().registerAll(elements.getBiomes().stream().map(Supplier::get).toArray(Biome[]::new));
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().registerAll(elements.getEntities().stream().map(Supplier::get).toArray(EntityEntry[]::new));
    }

    @SubscribeEvent
    public void registerPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().registerAll(elements.getPotions().stream().map(Supplier::get).toArray(Potion[]::new));
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<net.minecraft.util.SoundEvent> event) {
        elements.registerSounds(event);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        elements.getElements().forEach(element -> element.registerModels(event));
    }
}