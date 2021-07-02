package com.mcmoddev.orespawn.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
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

import com.mcmoddev.orespawn.OreSpawn;
import com.mcmoddev.orespawn.data.Constants;
import com.mcmoddev.orespawn.data.OS4BlockData;
import com.mcmoddev.orespawn.registries.FeaturesRegistry;
import com.mcmoddev.orespawn.registries.PresetsRegistry;
import com.mcmoddev.orespawn.registries.ReplacementsRegistry;

public class OS3API {
	private static Map<String,String> modRefs = new HashMap<>(1024);
	private static List<String> allowedConfigs = new LinkedList<>();
	
	public static void addMod(final String modId, final String modPath) {
		modRefs.put(modId, modPath);
	}
	
	public static Map<String, String> getMods() {
		return Collections.unmodifiableMap(modRefs);
	}
	
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
								if (repl.getValue().isJsonPrimitive()) {
									// TODO: Preset Shit
								} else {
									JsonArray replValues = repl.getValue().getAsJsonArray();
									List<OS4BlockData> blocks = new LinkedList<OS4BlockData>();
									for( JsonElement el : replValues) {
										JsonObject zz = el.getAsJsonObject();
										String blName = zz.get("name").getAsString();
										String state = zz.has("state")?zz.get("state").getAsString():"";
										blocks.add(new OS4BlockData(blName, state));
									}
									ReplacementsRegistry.INSTANCE.addReplacement(replName, blocks);
								}
							});
					});
		} catch (final IOException e) {
			OreSpawn.LOGGER.error(e.getMessage());
		}
	}
	
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
		// find all JSON files that are not in `sysconf`
		// filter for only active-flagged files
		// load them, interpolating any presets present
		// stuff them into a registry
		// return
	}
}
