package com.mcmoddev.orespawn;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import com.mcmoddev.orespawn.api.OS3API;
import com.mcmoddev.orespawn.api.os3plugin;
import com.mcmoddev.orespawn.api.plugin.PluginLoader;
import com.mcmoddev.orespawn.utils.Helpers;
import com.mcmoddev.orespawn.utils.TypeUtils;

import java.util.stream.Collectors;

@Mod("orespawn")
public class OreSpawn {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public OreSpawn() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
    	final Type annotationType = Type.getType(os3plugin.class);
    	ModList.get().getAllScanData().stream()
    		.flatMap( scanData -> scanData.getAnnotations()
    					.stream()
    					.filter(a -> annotationType.equals(a.getAnnotationType())))
    		.map(ModFileScanData.AnnotationData::getClassType)
    		.map(TypeUtils::getClassFromType)
    		.filter(Helpers::objectNotNull)
    		.forEach(PluginLoader::Load);
    	// load configs
    	OS3API.loadPresets();
    	OS3API.loadReplacements();
    	OS3API.loadFeatures();
    	OS3API.loadIntegrationWhitelist();
    	OS3API.loadSpawns();
    	
    	// register world gen
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("orespawn", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK"); return "Hello world";}
        );
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(final FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing
    // to the MOD Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
