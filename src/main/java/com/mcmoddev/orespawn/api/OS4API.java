package com.mcmoddev.orespawn.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

public class OS4API {
	private static Map<String,String> modRefs = new HashMap<>(1024);
	private static ConfigBlacklist configBlacklist = new ConfigBlacklist();

	public static void addMod(final String modId, final String modPath) {
		modRefs.put(modId, modPath);
	}

	public static Map<String, String> getMods() {
		return Collections.unmodifiableMap(modRefs);
	}

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
		spawnWrapper.entrySet().stream()
		.forEach( entry -> {
			String spawnName = entry.getKey();
			OreSpawn.LOGGER.info("Loading spawn {} -- {}", spawnName, entry.getValue());
			JsonObject spawnData = entry.getValue().getAsJsonObject();
			SpawnBuilder theBuilder = new SpawnBuilder(spawnName);
			spawnData.entrySet().stream().forEach(spawn -> theBuilder.addItem(spawn.getKey(), spawn.getValue()));
			SpawnStore.add(spawnName, theBuilder.build());
		});

	}

	public static void loadPresetsFromStream(InputStream inFile) {
		JsonObject elements = getParsedJson(inFile).getAsJsonObject();

		elements.entrySet().stream()
		.forEach( entry -> {
			String entryZone = entry.getKey();
			entry.getValue().getAsJsonObject().entrySet().stream()
			.forEach( subEnt -> {
				String entryName = String.format("%s.%s", entryZone, subEnt.getKey());
				PresetsRegistry.INSTANCE.addPreset(entryName, subEnt.getValue());
			});

		});
	}

	public static void loadReplacementsFromStream(InputStream inFile) {
		JsonObject elements = getParsedJson(inFile).getAsJsonObject();

		elements.entrySet().stream()
		.forEach( repl -> {
			String replName = repl.getKey();
			JsonArray replValues = repl.getValue().getAsJsonArray();
			List<OS4BlockData> blocks = new LinkedList<OS4BlockData>();
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
}
