package com.mcmoddev.orespawn.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mcmoddev.orespawn.OreSpawn;
import com.mcmoddev.orespawn.builders.SpawnBuilder;
import com.mcmoddev.orespawn.data.Constants;
import com.mcmoddev.orespawn.data.OS4BlockData;
import com.mcmoddev.orespawn.registries.FeaturesRegistry;
import com.mcmoddev.orespawn.registries.PresetsRegistry;
import com.mcmoddev.orespawn.registries.ReplacementsRegistry;
import com.mcmoddev.orespawn.registries.SpawnStore;

public class OS4API {
	private static Map<String,String> modRefs = new HashMap<>(1024);
	private static List<String> allowedConfigs = new LinkedList<>();

	public static void addMod(final String modId, final String modPath) {
		modRefs.put(modId, modPath);
	}

	public static Map<String, String> getMods() {
		return Collections.unmodifiableMap(modRefs);
	}

	/*
	 * Walk the config directory, load all the various `presets-XXX.json` files
	 */
	public static void loadPresets() {
		PathMatcher matcher = FileSystems.getDefault()
				.getPathMatcher("glob:**/presets-*.json");

		// find all `presets_XXX.json` files
		try (final Stream<Path> stream = Files.walk(Constants.SYSCONF, 1)) {
			stream.filter(matcher::matches).map(Path::toFile)
			// load them into a registry
			.forEach( file -> {
				final JsonParser parser = new JsonParser();
				String rawJson;
				JsonObject elements;

				try {
					rawJson = FileUtils.readFileToString(file, Charset.defaultCharset());
				} catch (final IOException e) {
					OreSpawn.LOGGER.error(e.getMessage());
					return;
				}

				elements = parser.parse(rawJson).getAsJsonObject();

				elements.entrySet().stream()
				.forEach( entry -> {
					String entryZone = entry.getKey();
					entry.getValue().getAsJsonObject().entrySet().stream()
					.forEach( subEnt -> {
						String entryName = String.format("%s.%s", entryZone, subEnt.getKey());
						PresetsRegistry.INSTANCE.addPreset(entryName, subEnt.getValue());
					});

				});
			});
		} catch (final IOException e) {
			OreSpawn.LOGGER.error(e.getMessage());
		}
	}

	/*
	 * Walk the config directory, load all the various `replacements-XXX.json` files
	 */
	public static void loadReplacements() {
		PathMatcher matcher = FileSystems.getDefault()
				.getPathMatcher("glob:**/replacements-*.json");

		try (final Stream<Path> stream = Files.walk(Constants.SYSCONF, 1)) {
			stream.filter(matcher::matches).map(Path::toFile)
			.forEach( file -> {
				final JsonParser parser = new JsonParser();
				String rawJson;
				JsonObject elements;

				try {
					rawJson = FileUtils.readFileToString(file, Charset.defaultCharset());
				} catch (final IOException e) {
					OreSpawn.LOGGER.error(e.getMessage());
					return;
				}

				elements = parser.parse(rawJson).getAsJsonObject();

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
			});
		} catch (final IOException e) {
			OreSpawn.LOGGER.error(e.getMessage());
		}
	}

	/*
	 * Walk the config directory, load all the various `features-XXX.json` files
	 */
	public static void loadFeatures() {
		PathMatcher matcher = FileSystems.getDefault()
				.getPathMatcher("glob:**/features-*.json");

		try (final Stream<Path> stream = Files.walk(Constants.SYSCONF, 1)) {
			stream.filter(matcher::matches).map(Path::toFile)
			.forEach( file -> {
				final JsonParser parser = new JsonParser();
				String rawJson;
				JsonArray elements;

				try {
					rawJson = FileUtils.readFileToString(file, Charset.defaultCharset());
				} catch (final IOException e) {
					OreSpawn.LOGGER.error(e.getMessage());
					return;
				}

				elements = parser.parse(rawJson).getAsJsonArray();

				elements.forEach( ent -> {
					JsonObject entry = ent.getAsJsonObject();
					String featureName = entry.get("name").getAsString();
					String className = entry.get("class").getAsString();
					FeaturesRegistry.INSTANCE.addFeature(featureName, className);
				});
			});
		} catch (final IOException e) {
			OreSpawn.LOGGER.error(e.getMessage());
		}
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

		parser.parse(rawJson).getAsJsonArray().forEach( item -> allowedConfigs.add(item.getAsString()));
	}

	public static void loadSpawns() {
		final JsonParser parser = new JsonParser();

		Arrays.asList(Constants.JSONPATH.toFile().listFiles())
		.stream()
		// find all JSON files
		.filter( f -> f.toPath().endsWith(".json"))
		// that are not in SYSCONF
		.filter( f -> !f.getAbsolutePath().contains(Constants.FileBits.SYSCONF))
		// and are listed in allowedConfigs
		.filter( f -> allowedConfigs.contains(f.getName().substring(0, f.getName().length() - 5)))
		.forEach( inFile -> {
			// try to read the file
			String rawData;
			try {
				rawData = FileUtils.readFileToString(inFile, Charset.defaultCharset());
			} catch (final IOException e) {
				OreSpawn.LOGGER.error(String.format("Cannot load %s:\n%s", inFile.getName(), e.getMessage()));
				return;
			}

			// parse the file
			JsonObject rawJson = parser.parse(rawData).getAsJsonObject();

			String fileVersion = rawJson.get(Constants.ConfigNames.VERSION).getAsString();
			JsonObject spawnWrapper = rawJson.get(Constants.ConfigNames.SPAWNS).getAsJsonObject();

			OreSpawn.LOGGER.info("Loading JSON version {} from {}", fileVersion, inFile.getName());

			// load the spawns
			spawnWrapper.entrySet().stream()
			.forEach( entry -> {
				String spawnName = entry.getKey();
				JsonObject spawnData = entry.getValue().getAsJsonObject();
				SpawnBuilder theBuilder = new SpawnBuilder(spawnName);
				spawnData.entrySet().stream().forEach(spawn -> theBuilder.addItem(spawn.getKey(), spawn.getValue()));
				SpawnStore.add(spawnName, theBuilder.build());
			});
		});
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
}
