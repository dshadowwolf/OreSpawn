package com.mcmoddev.orespawn.loaders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.mcmoddev.orespawn.OreSpawn;
import com.mcmoddev.orespawn.api.OS4API;
import com.mcmoddev.orespawn.data.Constants;
import com.mcmoddev.orespawn.data.Constants.FileTypes;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;

public class ResourceLoader {
	public void runLoaders() {
		List<ResourceLocation> foundFiles = new LinkedList<>();
		ModList.get().getModFiles().stream().map(mfi -> iterateFiles(mfi.getFile()))
				.forEach(lrl -> lrl.stream().forEach(rl -> foundFiles.add(rl)));

		iterateDiskFiles().stream().forEach(rl -> foundFiles.add(rl));

		List<ResourceLocation> featuresFiles = foundFiles.stream().filter(rl -> rl.getPath().startsWith("features/"))
				.collect(Collectors.toList());
		List<ResourceLocation> replacementsFiles = foundFiles.stream()
				.filter(rl -> rl.getPath().startsWith("replacements/")).collect(Collectors.toList());
		List<ResourceLocation> presetsFiles = foundFiles.stream().filter(rl -> rl.getPath().startsWith("presets/"))
				.collect(Collectors.toList());
		List<ResourceLocation> spawnConfigs = foundFiles.stream().filter(rl -> !featuresFiles.contains(rl))
				.filter(rl -> !replacementsFiles.contains(rl)).filter(rl -> !presetsFiles.contains(rl))
				.collect(Collectors.toList());

		OreSpawn.LOGGER.info("Found {} features files, {} replacements files, {} presets files and {} spawn configuration files",
				featuresFiles.size(), replacementsFiles.size(), presetsFiles.size(), spawnConfigs.size());

		OreSpawn.LOGGER.info("> Features Files:");
		featuresFiles.forEach(rl -> OreSpawn.LOGGER.info(">> {}", rl.toString()));
		OreSpawn.LOGGER.info("> Replacements Files:");
		replacementsFiles.forEach(rl -> OreSpawn.LOGGER.info(">> {}", rl.toString()));
		OreSpawn.LOGGER.info("> Presets Files:");
		presetsFiles.forEach(rl -> OreSpawn.LOGGER.info(">> {}", rl.toString()));
		OreSpawn.LOGGER.info("> Spawn Configs:");
		spawnConfigs.forEach(rl -> OreSpawn.LOGGER.info(">> {}", rl.toString()));

		
		if (Paths.get(Constants.FileBits.CONFIG_DIR, Constants.FileBits.OS4, Constants.FileBits.ALLOWED_MODS).toFile().exists()) {
			OS4API.loadIntegrationWhitelist();
		}
		
		// TODO: Add Whitelist/Blacklist filtering
		featuresFiles.stream().forEach(rl -> {			
			if (rl.getNamespace().matches("orespawn-disk")) {
				loadFromDisk(rl, Constants.FileTypes.FEATURES);
			} else {
				loadFromResource(rl, Constants.FileTypes.FEATURES);
			}
		});
		
		replacementsFiles.stream().forEach(rl -> {			
			if (rl.getNamespace().matches("orespawn-disk")) {
				loadFromDisk(rl, Constants.FileTypes.REPLACEMENTS);
			} else {
				loadFromResource(rl, Constants.FileTypes.REPLACEMENTS);
			}
		});
		
		presetsFiles.stream().forEach(rl -> {			
			if (rl.getNamespace().matches("orespawn-disk")) {
				loadFromDisk(rl, Constants.FileTypes.PRESETS);
			} else {
				loadFromResource(rl, Constants.FileTypes.PRESETS);
			}
		});
		
		spawnConfigs.stream().forEach(rl -> {			
			if (rl.getNamespace().matches("orespawn-disk")) {
				loadFromDisk(rl, Constants.FileTypes.SPAWN);
			} else {
				loadFromResource(rl, Constants.FileTypes.SPAWN);
			}
		});
	}
	
	private List<ResourceLocation> iterateFiles(ModFile modFile) {
		try {
			Path root = modFile.getLocator().findPath(modFile, "assets", "orespawn4-data").toAbsolutePath();

			return Files.walk(root).map(path -> root.relativize(path.toAbsolutePath()))
					.filter(path -> path.getNameCount() <= 64). // Make sure the depth is within bounds
					filter(path -> path.toString().endsWith(".json")).map(path -> Joiner.on('/').join(path))
					.map(path -> path.toString()).map(path -> path.substring(0, path.length() - 5))
					.map(path -> new ResourceLocation(modFile.getModInfos().get(0).getModId(), path))
					.collect(Collectors.toList());
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	List<ResourceLocation> iterateDiskFiles() {
		try {
			Path diskPath = Paths.get(Constants.FileBits.CONFIG_DIR, Constants.FileBits.OS4).toAbsolutePath();
			return Files.walk(diskPath).map(path -> diskPath.relativize(path.toAbsolutePath()))
					.filter(path -> path.getNameCount() <= 64).filter(path -> path.toString().endsWith(".json"))
					.map(path -> Joiner.on('/').join(path)).map(path -> path.toString())
					.map(path -> path.toLowerCase(Locale.US)).map(path -> path.substring(0, path.length() - 5))
					.map(path -> new ResourceLocation("orespawn-disk", path)).collect(Collectors.toList());
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	private void loadFromDisk(ResourceLocation rl, FileTypes type) {
		Path fp = makePath(rl, Constants.FileBits.DISK);
		try {
			switch(type) {
			case FEATURES:
				OS4API.loadFeaturesFromStream(Files.newInputStream(fp));
				break;
			case PRESETS:
				OS4API.loadPresetsFromStream(Files.newInputStream(fp));
				break;
			case REPLACEMENTS:
				OS4API.loadReplacementsFromStream(Files.newInputStream(fp));
				break;
			case SPAWN:
				OS4API.loadSpawnsFromStream(Files.newInputStream(fp));
				break;
			default:
				OreSpawn.LOGGER.error("Asked to load {} of type {} - I don't know this type of data", rl.toString(), type);
				break;

			}
		} catch (IOException e) {
			OreSpawn.LOGGER.error("Exception loading resource {} -- {}", rl.toString(), e.getMessage());
			e.printStackTrace();
		}
	}

	private void loadFromResource(ResourceLocation rl, FileTypes type) {
		Path fp = makePath(rl, Constants.FileBits.RESOURCE);
		try {
			switch(type) {
			case FEATURES:
				OS4API.loadFeaturesFromStream(Files.newInputStream(fp));
				break;
			case PRESETS:
				OS4API.loadPresetsFromStream(Files.newInputStream(fp));
				break;
			case REPLACEMENTS:
				OS4API.loadReplacementsFromStream(Files.newInputStream(fp));
				break;
			case SPAWN:
				OS4API.loadSpawnsFromStream(Files.newInputStream(fp));
				break;
			default:
				OreSpawn.LOGGER.error("Asked to load {} of type {} - I don't know this type of data", rl.toString(), type);
				break;

			}
		} catch (IOException e) {
			OreSpawn.LOGGER.error("Exception loading resource {} -- {}", rl.toString(), e.getMessage());
			e.printStackTrace();
		}
	}
	
	private Path makePath(ResourceLocation rl, String type) {
		switch(type) {
		case Constants.FileBits.DISK:
			return Paths.get(Constants.FileBits.CONFIG_DIR, Constants.FileBits.OS4, rl.getPath()+".json");
		case Constants.FileBits.RESOURCE:
			ModFile mf = ModList.get().getModFileById(rl.getNamespace()).getFile(); 
			return mf.getLocator().findPath(mf, "assets", "orespawn4-data", rl.getPath()+".json");
		default:
			OreSpawn.LOGGER.error("Asked to resolve a path for {} of type {} -- I do not know how to do this", rl.toString(), type);
			return null;
		}
	}

}
