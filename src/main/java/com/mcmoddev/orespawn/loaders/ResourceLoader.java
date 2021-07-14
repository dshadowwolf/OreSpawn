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

import static com.mcmoddev.orespawn.data.Config.COMMON;
import static com.mcmoddev.orespawn.utils.Helpers.makePath;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import org.apache.commons.io.FileUtils;


public class ResourceLoader {
	public void runLoaders() {
		List<ResourceLocation> foundFiles = new LinkedList<>();
		if (!COMMON.ignoreResources.get())
			ModList.get().getModFiles().stream().map(mfi -> iterateFiles(mfi.getFile()))
			.forEach(foundFiles::addAll);

		if (COMMON.extractToDisk.get()) {
			for( ResourceLocation f : foundFiles) {
				if (!OS4API.isKnownConfig(f)) {
					extractFileToDisk(f);
					OS4API.addKnownConfig(f);
				}
			}
		}

		foundFiles.addAll(iterateDiskFiles());

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

		featuresFiles.stream()
		.filter(rl -> (OS4API.isAllowed(rl.getNamespace()) && OS4API.isAllowed(rl.getPath()+".json")))
		.forEach(rl -> loadResourceLocation(rl, Constants.FileTypes.FEATURES));

		replacementsFiles.stream()
		.filter(rl -> (OS4API.isAllowed(rl.getNamespace()) && OS4API.isAllowed(rl.getPath()+".json")))
		.forEach(rl -> loadResourceLocation(rl, Constants.FileTypes.REPLACEMENTS));

		presetsFiles.stream()
		.filter(rl -> (OS4API.isAllowed(rl.getNamespace()) && OS4API.isAllowed(rl.getPath()+".json")))
		.forEach(rl -> loadResourceLocation(rl, Constants.FileTypes.PRESETS));

		spawnConfigs.stream()
		.filter(rl -> (OS4API.isAllowed(rl.getNamespace()) && OS4API.isAllowed(rl.getPath()+".json")))
		.forEach(rl -> loadResourceLocation(rl, Constants.FileTypes.SPAWN));
	}

	private void extractFileToDisk(ResourceLocation f) {
		Path root = makePath(f, Constants.FileBits.RESOURCE);
		Path targetRoot = Constants.JSONPATH.toAbsolutePath();

		String resourceType = f.getPath().contains("/")?getType(f.getPath()):"config";
		String extractPath = resourceType.equals("config")?f.getNamespace()+"_"+f.getPath().replaceAll("/", "-"):
			"config_data/"+resourceType+"/"+f.toString().replaceAll(":", "-").replaceAll("/", "_");
		try {
			FileUtils.copyInputStreamToFile(Files.newInputStream(root), Paths.get(targetRoot.toString(), extractPath).toFile());
		} catch (IOException e) {
			OreSpawn.LOGGER.error("Exception trying to copy the config {} out to the filesystem as required by the configuration settings: {}", f.toString(), e.getMessage());
			e.printStackTrace();
		}
	}

	private String getType(String path) {
		String p = path.split("/")[0];
		switch(p) {
			case "features":
			case "replacements":
			case "presets":
				return p.substring(0, p.length()-1); // strip the `s` off
			default:
				return "config";
		}
	}

	private void loadResourceLocation(final ResourceLocation rl, final FileTypes type) {
		if (rl.getNamespace().matches("orespawn-disk")) {
			loadFromDisk(rl, type);
		} else {
			loadFromResource(rl, type);
		}
	}

	private List<ResourceLocation> iterateFiles(ModFile modFile) {
		try {
			Path root = modFile.getLocator().findPath(modFile, Constants.FileBits.RESOURCE_PATH).toAbsolutePath();

			return Files.walk(root).map(path -> root.relativize(path.toAbsolutePath()))
					.filter(path -> path.getNameCount() <= 64). // Make sure the depth is within bounds
					filter(path -> path.toString().endsWith(".json")).map(path -> Joiner.on('/').join(path))
					.map(path -> path.substring(0, path.length() - 5))
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
					.map(path -> Joiner.on('/').join(path))
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
}
