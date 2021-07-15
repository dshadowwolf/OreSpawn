package com.mcmoddev.orespawn;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.item.Item;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcmoddev.orespawn.api.OS4API;
import com.mcmoddev.orespawn.loaders.ResourceLoader;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

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
		// Register the doClientStuff method for modloading

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.addListener(this::itemRegistryEvent);
		MinecraftForge.EVENT_BUS.addListener(this::doServerStartTasks);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {
		OS4API.loadKnownConfigs();
		ResourceLoader loader = new ResourceLoader();
		loader.runLoaders();
		OS4API.saveKnownConfigs();
	}


	private void doClientStuff(final FMLClientSetupEvent event) {
	}

	private void enqueueIMC(final InterModEnqueueEvent event) {
	}

	private void processIMC(final InterModProcessEvent event) {
	}

	private void itemRegistryEvent(final RegistryEvent.Register<Item> ev) {
		OS4API.resolveBlockData();
	}

	private void doServerStartTasks(final FMLServerStartingEvent ev) {
		OS4API.resolveWorldData(ev.getServer().getDynamicRegistries());
	}
}
