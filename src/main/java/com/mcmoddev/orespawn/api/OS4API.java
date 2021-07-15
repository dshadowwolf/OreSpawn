package com.mcmoddev.orespawn.api;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

import com.mcmoddev.orespawn.data.*;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.io.FileUtils;

import com.google.gson.stream.JsonReader;
import com.mcmoddev.orespawn.OreSpawn;

import net.minecraft.util.registry.DynamicRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.mcmoddev.orespawn.utils.Helpers.makeInternalResourceLocation;

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
			for (JsonElement el : topLevel.get("mods").getAsJsonArray())
				configBlacklist.addMod(el.getAsString());
		if (topLevel.has("configs"))
			for (JsonElement el : topLevel.get("configs").getAsJsonArray())
				configBlacklist.addConfig(el.getAsString());
	}

	private static JsonElement getParsedJson(InputStream stream) {
		InputStreamReader readerForStream = new InputStreamReader(stream);
		JsonReader jsonReader = new JsonReader(readerForStream);
		JsonParser parserToReturnFrom = new JsonParser();
		return parserToReturnFrom.parse(jsonReader);
	}

	public static void loadPresetsFromStream(InputStream inFile) {
		JsonObject elements = getParsedJson(inFile).getAsJsonObject();

		// let the storage actually do the load - it can do the recursion necessary to get the name and all that
		PresetsStore.load(elements);
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

	private static void addModConfig(final String itemIn) {
		addKnownConfig(makeInternalResourceLocation(itemIn));
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

	public static void resolveBlockData() {
		ReplacementsStore.resolveBlocks();
		SpawnStore.resolveSpawnBlocks();
	}

	public static void resolveWorldData(DynamicRegistries dynamicRegistries) {
		SpawnStore.resolveBiomesAndDimensions(dynamicRegistries);
	}

	public static void loadFeaturesFromStream(InputStream newInputStream) {
		/*
		  {
		     "feature name" : "feature classpath",
		     ...
		  }
		 */
		JsonObject featuresIn = getParsedJson(newInputStream).getAsJsonObject();

		featuresIn.entrySet().stream()
			.map( baseEntry -> Pair.of(baseEntry.getKey(), baseEntry.getValue().getAsString()) )
			.forEach( manipulated -> FeaturesStore.addNewFeature( manipulated.getKey(), manipulated.getValue()));
	}

	public static void loadReplacementsFromStream(InputStream newInputStream) {
		/*
		  {
		     "replacement name" : [ { block data }, ... ],
		     ...
		  }
		 */
		JsonObject replacementsIn = getParsedJson(newInputStream).getAsJsonObject();

		for ( Map.Entry<String, JsonElement> x : replacementsIn.entrySet()) {
			String entryName = x.getKey();
			JsonArray entries = x.getValue().getAsJsonArray();
			List<BlockData> blocks = new LinkedList<>();
			entries.forEach( entry -> blocks.add(BlockData.makeFromJson(entry.getAsJsonObject())));
			ReplacementsStore.add(entryName, blocks);
		}
	}

	public static void loadSpawnsFromStream(InputStream newInputStream) {
		/*
		   {
		     "version": 2.0,
		     "spawns": {
		        "spawn name": {
		          "feature": "feature name or $.preset",
		          "replacements": "replacement name or $.preset or array",
		          "dimensions": array - whitelist, with special values or $.preset,
		          "biomes": object of whitelist/blacklist segments or $.preset,
		          "blocks": array of block data or $.preset,
		          "parameters": variable object or $.preset
		        },
		        ...
		      }
		    }
		 */
		JsonObject spawnFileData = getParsedJson(newInputStream).getAsJsonObject();

		if (!spawnFileData.has("version") ||
			(spawnFileData.has("version") && spawnFileData.get("version").getAsFloat() != 2.0)) {
			OreSpawn.LOGGER.error("Spawn File does not have a version or is of an unhandled or unknown version. Ignoring.");
			return;
		}
		if (!spawnFileData.has("spawns")) {
			OreSpawn.LOGGER.error("Spawn File of incorrect format or has no spawns, ignoring.");
			return;
		}

		OreSpawn.LOGGER.info("Loading Spawns, version {}", spawnFileData.get("version").getAsFloat());

		JsonObject rawSpawnData = spawnFileData.get("spawns").getAsJsonObject();

		rawSpawnData.entrySet().forEach( entry -> SpawnStore.loadFromJson(entry));
	}
}
