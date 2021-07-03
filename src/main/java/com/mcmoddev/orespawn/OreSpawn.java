package com.mcmoddev.orespawn;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcmoddev.orespawn.data.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Joiner;

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

    private List<ResourceLocation> iterateFiles(ModFile modFile) {
        try {
            Path root = modFile.getLocator().findPath(modFile, "assets", "orespawn4-data").toAbsolutePath();
            
            return Files.walk(root).
                    map(path -> root.relativize(path.toAbsolutePath())).
                    filter(path -> path.getNameCount() <= 64). // Make sure the depth is within bounds
                    filter(path -> path.toString().endsWith(".json")).
                    map(path -> Joiner.on('/').join(path)).
                    map(path -> path.toString()).
                    map(path -> path.substring(0, path.length() - 5)).
                    map(path -> new ResourceLocation(modFile.getModInfos().get(0).getModId(), path)).
                    collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }    	    	
    }
    
    List<ResourceLocation> iterateDiskFiles() {
        try {
    	Path diskPath = Paths.get(Constants.FileBits.CONFIG_DIR, Constants.FileBits.OS4).toAbsolutePath();
    	return Files.walk(diskPath)
    	    	.map(path -> diskPath.relativize(path.toAbsolutePath()))
                .filter(path -> path.getNameCount() <= 64)
                .filter(path -> path.toString().endsWith(".json"))
                .map(path -> Joiner.on('/').join(path))
                .map(path -> path.toString())
                .map(path -> path.toLowerCase(Locale.US))
                .map(path -> path.substring(0, path.length() - 5))
    	    	.map(path -> new ResourceLocation("orespawn-disk", path))
    	    	.collect(Collectors.toList());
    } catch (IOException e) {
        return Collections.emptyList();
    }    	    	
    }
    private void setup(final FMLCommonSetupEvent event) {
    	List<ResourceLocation> foundFiles = new LinkedList<>();
    	ModList.get().getModFiles().stream()
    	.map(mfi -> iterateFiles(mfi.getFile()))
    	.forEach(lrl -> lrl.stream().forEach(rl -> foundFiles.add(rl)));

    	iterateDiskFiles().stream().forEach(rl -> foundFiles.add(rl));
    	
    	List<ResourceLocation> featuresFiles = foundFiles.stream()
    			.filter(rl -> rl.getPath().startsWith("features/"))
    			.collect(Collectors.toList());
    	List<ResourceLocation> replacementsFiles = foundFiles.stream()
    			.filter(rl -> rl.getPath().startsWith("replacements/"))
    			.collect(Collectors.toList());
    	List<ResourceLocation> presetsFiles = foundFiles.stream()
    			.filter(rl -> rl.getPath().startsWith("presets/"))
    			.collect(Collectors.toList());
    	List<ResourceLocation> spawnConfigs = foundFiles.stream()
    			.filter(rl -> !featuresFiles.contains(rl))
    			.filter(rl -> !replacementsFiles.contains(rl))
    			.filter(rl -> !presetsFiles.contains(rl))
    			.collect(Collectors.toList());

    	LOGGER.info("Found {} features files, {} replacements files, {} presets files and {} spawn configuration files",
    			featuresFiles.size(), replacementsFiles.size(), presetsFiles.size(), spawnConfigs.size());
    	
    	LOGGER.info("> Features Files:");
    	featuresFiles.forEach(rl -> LOGGER.info(">> {}", rl.toString()));
    	LOGGER.info("> Replacements Files:");
    	replacementsFiles.forEach(rl -> LOGGER.info(">> {}", rl.toString()));
    	LOGGER.info("> Presets Files:");
    	presetsFiles.forEach(rl -> LOGGER.info(">> {}", rl.toString()));
    	LOGGER.info("> Spawn Configs:");
    	spawnConfigs.forEach(rl -> LOGGER.info(">> {}", rl.toString()));
    	
    	// load configs
    	/*
    	OS4API.loadPresets();
    	OS4API.loadReplacements();
    	OS4API.loadFeatures();
    	OS4API.loadIntegrationWhitelist();
    	OS4API.loadSpawns();
    	*/
    	// register world gen
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    private void processIMC(final InterModProcessEvent event) {
    }
}
