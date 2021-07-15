package com.mcmoddev.orespawn.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcmoddev.orespawn.OreSpawn;
import com.mcmoddev.orespawn.utils.Helpers;
import net.minecraft.block.BlockState;
import net.minecraft.util.WeightedList;
import net.minecraft.util.registry.DynamicRegistries;

import java.util.*;

public class SpawnStore {
	private static final Map<String, SpawnData> spawns = new TreeMap<>();

	public static void loadFromJson(final Map.Entry<String, JsonElement> dataIn) {
		SpawnData newSpawn = new SpawnData();
		dataIn.getValue().getAsJsonObject().entrySet().forEach(baseEntry -> {
			newSpawn.setName(baseEntry.getKey());
			baseEntry.getValue().getAsJsonObject().entrySet().forEach(entry -> {
				switch (entry.getKey()) {
					case "feature":
						newSpawn.setFeatureName(PresetsStore.interpolateString(entry.getValue()));
						break;
					case "replaces":
						newSpawn.setReplacementName(PresetsStore.interpolateString(entry.getValue()));
						break;
					case "dimensions":
						JsonArray midpoint = PresetsStore.interpolateArray(entry.getValue());
						for (JsonElement jsonElement : midpoint) {
							String dimName = jsonElement.getAsString();
							newSpawn.addDimension(dimName);
						}
						break;
					case "biomes":
						JsonObject baseData = PresetsStore.interpolateObject(entry.getValue());
						for (Map.Entry<String, JsonElement> ent : baseData.entrySet()) {
							if (ent.getKey().matches("whitelist")) {
								ent.getValue().getAsJsonArray().forEach(item -> newSpawn.addBiomeToWhitelist(item.getAsString()));
							} else if (ent.getKey().matches("blacklist")) {
								ent.getValue().getAsJsonArray().forEach(item -> newSpawn.addBiomeToBlacklist(item.getAsString()));
							}
						}
						break;
					case "parameters":
						newSpawn.setParameters(entry.getValue().getAsJsonObject());
						break;
					case "blocks":
						JsonArray blockData = PresetsStore.interpolateArray(entry.getValue());
						blockData.forEach( block -> newSpawn.addBlock(BlockData.makeFromJson(block.getAsJsonObject())));
						break;
					default:
						OreSpawn.LOGGER.error("Unkown entry {} in spawn {}", entry.getKey(), baseEntry.getKey());
				}
			});
		});
	}

	public static void resolveSpawnBlocks() {
		spawns.entrySet().forEach(entry -> {
			entry.getValue().resolveBlocks();
			entry.getValue().resolveReplacer();
		});
	}

	public static void resolveBiomesAndDimensions(DynamicRegistries dynamicRegistries) {
		spawns.entrySet().forEach(entry -> entry.getValue().resolveOtherData(dynamicRegistries));
	}

	public static class SpawnData {
		private String featureName;
		private String replacementName;
		private List<String> dimensionList;
		private List<String> biomeWhitelist;
		private List<String> biomeBlacklist;
		private JsonObject parameters;
		private String spawnName;
		private List<BlockData> blocks;

		private WeightedList<BlockState> blockStates;
		private OS4Replacer replacer;
		private BiomeMatcher biomes;
		private DimensionMatcher dimensions;

		private boolean resolvedBlocks = false;
		private boolean resolvedReplacements = false;
		private boolean resolvedOther = false;


		public SpawnData() {
			dimensionList = new LinkedList<>();
			biomeWhitelist = new LinkedList<>();
			biomeBlacklist = new LinkedList<>();
			blocks = new LinkedList<>();
			blockStates = new WeightedList<>();
		}

		public SpawnData setFeatureName(final String name) {
			featureName = name;
			return this;
		}

		public SpawnData setReplacementName(final String name) {
			replacementName = name;
			return this;
		}

		public SpawnData addDimension(final String dimensionName) {
			dimensionList.add(dimensionName);
			return this;
		}

		public SpawnData setBiomesAll() {
			biomeWhitelist = Collections.emptyList();
			biomeBlacklist = Arrays.asList("orespawn4:any");
			return this;
		}

		public SpawnData addBiomeToWhitelist(final String biomeName) {
			biomeWhitelist.add(biomeName);
			return this;
		}

		public SpawnData addBiomeToBlacklist(final String biomeName) {
			biomeBlacklist.add(biomeName);
			return this;
		}

		public SpawnData setAllOverworld() {
			dimensionList = Arrays.asList("orespawn4:overworlds");
			return this;
		}

		public SpawnData setAllNether() {
			dimensionList = Arrays.asList("orespawn4:nethers");

			return this;
		}

		public SpawnData setAllVoidOrEnd() {
			dimensionList = Arrays.asList("orespawn4:ends");

			return this;
		}

		public SpawnData setParameters(final JsonObject parametersElement) {
			parameters = parametersElement;

			return this;
		}

		public SpawnData setName(final String name) {
			spawnName = name;
			return this;
		}

		public SpawnData addBlock(final BlockData block) {
			blocks.add(block);
			return this;
		}

		public void resolveReplacer() {
			if (resolvedReplacements) return;
			replacer = ReplacementsStore.get(replacementName);
			resolvedReplacements = true;
		}

		public void resolveBlocks() {
			blocks.stream().forEach( blockData -> {
				BlockState baseState = Helpers.deserializeState(blockData.getBlockWithState());
				blockStates.addWeighted(baseState, blockData.getChance());
			});
			resolvedBlocks = true;
		}

		public void resolveOtherData(DynamicRegistries dynamicRegistries) {
			biomes = BiomeMatcher.buildFromData(biomeBlacklist, biomeWhitelist, dynamicRegistries);
			dimensions = DimensionMatcher.buildFromData(dimensionList, dynamicRegistries);
		}
	}
}
