package com.mcmoddev.orespawn.api;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;

import com.google.gson.stream.JsonReader;
import com.mcmoddev.orespawn.OreSpawn;
import com.mcmoddev.orespawn.builders.SpawnBuilder;
import com.mcmoddev.orespawn.data.ConfigBlacklist;
import com.mcmoddev.orespawn.data.Constants;
import com.mcmoddev.orespawn.data.OS4BlockData;
import com.mcmoddev.orespawn.registries.FeaturesRegistry;
import com.mcmoddev.orespawn.registries.PresetsRegistry;
import com.mcmoddev.orespawn.registries.ReplacementsRegistry;
import com.mcmoddev.orespawn.registries.SpawnStore;

import net.minecraft.util.registry.DynamicRegistries;

import static com.mcmoddev.orespawn.utils.Helpers.makeResourceLocation;

public class OS4API {
	private static final ConfigBlacklist configBlacklist = new ConfigBlacklist();
	private static final List<ResourceLocation> knownConfigs = new LinkedList<>();

	/*
	 * Load the `Active Configs` file
	 * This one needs a bit more thought, right now its just a list of filenames...
	 * It might be better to have some structure so mods themselves can be listed
	 */
	public static void loadIntegrationWhitelist() {
		Path p = Constants.SYSCONF.resolve(Constants.FileBits.ALLOWED_MODS);
		final JsonParser parser = new JsonParser();
		String rawJson;

		try {
			rawJson = FileUtils.readFileToString(p.toFile(), Charset.defaultCharset());
		} catch (final IOException e) {
			OreSpawn.LOGGER.error(e.getMessage());
			return;
		}

		JsonObject topLevel = parser.parse(rawJson).getAsJsonObject();
		if (topLevel.has("mods"))
			topLevel.get("mods").getAsJsonArray().forEach(modid -> configBlacklist.addMod(modid.getAsString()));
		if (topLevel.has("configs"))
			topLevel.get("configs").getAsJsonArray().forEach(configFile -> configBlacklist.addConfig(configFile.getAsString()));
	}

	private static JsonElement getParsedJson(InputStream stream) {
		InputStreamReader readerForStream = new InputStreamReader(stream);
		JsonReader jsonReader = new JsonReader(readerForStream);
		JsonParser parserToReturnFrom = new JsonParser();
		return parserToReturnFrom.parse(jsonReader);
	}

	public static void loadSpawnsFromStream(InputStream inFile) {
		JsonObject rawJson = getParsedJson(inFile).getAsJsonObject();

		String fileVersion = rawJson.get(Constants.ConfigNames.VERSION).getAsString();
		JsonObject spawnWrapper = rawJson.get(Constants.ConfigNames.SPAWNS).getAsJsonObject();

		OreSpawn.LOGGER.info("Loading JSON version {}", fileVersion);

		// load the spawns
		spawnWrapper.entrySet()
		.forEach( entry -> {
			String spawnName = entry.getKey();
			OreSpawn.LOGGER.info("Loading spawn {} -- {}", spawnName, entry.getValue());
			JsonObject spawnData = entry.getValue().getAsJsonObject();
			SpawnBuilder theBuilder = new SpawnBuilder(spawnName);
			spawnData.entrySet().forEach(spawn -> theBuilder.addItem(spawn.getKey(), spawn.getValue()));
			SpawnStore.add(theBuilder.build());
		});

	}

	public static void loadPresetsFromStream(InputStream inFile) {
		JsonObject elements = getParsedJson(inFile).getAsJsonObject();

		elements.entrySet()
		.forEach( entry -> {
			String entryZone = entry.getKey();
			entry.getValue().getAsJsonObject().entrySet()
			.forEach( subEnt -> {
				String entryName = String.format("%s.%s", entryZone, subEnt.getKey());
				PresetsRegistry.INSTANCE.addPreset(entryName, subEnt.getValue());
			});

		});
	}

	public static void loadReplacementsFromStream(InputStream inFile) {
		JsonObject elements = getParsedJson(inFile).getAsJsonObject();

		elements.entrySet()
		.forEach( repl -> {
			String replName = repl.getKey();
			JsonArray replValues = repl.getValue().getAsJsonArray();
			List<OS4BlockData> blocks = new LinkedList<>();
			for( JsonElement el : replValues) {
				JsonObject zz = el.getAsJsonObject();
				String blName = zz.get("name").getAsString();
				String state = zz.has("state")?zz.get("state").getAsString():"";
				blocks.add(new OS4BlockData(blName, state));
			}
			ReplacementsRegistry.INSTANCE.addReplacement(replName, blocks);
		});
	}

	public static void loadFeaturesFromStream(InputStream inFile) {
		JsonArray elements = getParsedJson(inFile).getAsJsonArray();

		elements.forEach( ent -> {
			JsonObject entry = ent.getAsJsonObject();
			String featureName = entry.get("name").getAsString();
			String className = entry.get("class").getAsString();
			FeaturesRegistry.INSTANCE.addFeature(featureName, className);
		});
	}

	public static boolean isAllowed(final String checkName) {
		return configBlacklist.isAllowed(checkName);
	}

	public static void loadKnownConfigs() {
		Path knownConfigs = Paths.get(Constants.SYSCONF.toAbsolutePath().toString(), "known-configs.json");
		if (knownConfigs.toFile().exists() && knownConfigs.toFile().canRead()) {
			JsonParser parser = new JsonParser();
			JsonElement root;
			try (
				FileInputStream baseInput = new FileInputStream(knownConfigs.toFile());
				BufferedInputStream dataInput = new BufferedInputStream(baseInput);
				InputStreamReader theReader = new InputStreamReader(dataInput)) {
				root = parser.parse(new JsonReader(theReader));
			} catch(IOException ex) {
				OreSpawn.LOGGER.error("Unable to load known configs file: {}", ex.getMessage());
				ex.printStackTrace();
				return;
			}

			root.getAsJsonArray().forEach( el -> addModConfig(el.toString()));
		}
	}

	private static void addModConfig(String itemIn) {
		addKnownConfig(makeResourceLocation(itemIn));
	}

	public static void saveKnownConfigs() {
		Path knownConfigs = Paths.get(Constants.SYSCONF.toAbsolutePath().toString(), "known-configs.json");
		if (knownConfigs.toFile().canWrite()) {
			try (FileOutputStream outStream = new FileOutputStream(knownConfigs.toString());
				 OutputStreamWriter writer = new OutputStreamWriter(outStream)) {
				Gson gson = new Gson();
				writer.write(gson.toJson(knownConfigs));
			} catch (IOException ex) {
				OreSpawn.LOGGER.error("Error saving known configs file: {}", ex.getMessage());
				ex.printStackTrace();
			}
		}
	}

	public static boolean isKnownConfig(ResourceLocation config) {
		return knownConfigs.contains(config);
	}

	public static void addKnownConfig(ResourceLocation config) {
		knownConfigs.add(config);
	}

	public static void resolveData(DynamicRegistries dynamicRegistries) {
		FeaturesRegistry.INSTANCE.doDataResolution();
		ReplacementsRegistry.INSTANCE.doDataResolution(dynamicRegistries);
		SpawnStore.doResolveData(dynamicRegistries);
		ReplacementsRegistry.INSTANCE.dump();
	}
}
