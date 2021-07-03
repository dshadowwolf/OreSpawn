package com.mcmoddev.orespawn.builders;

import com.mcmoddev.orespawn.registries.IReplacementEntry;
import com.mcmoddev.orespawn.registries.PresetsRegistry;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcmoddev.orespawn.OreSpawn;
import com.mcmoddev.orespawn.data.OS4BlockData;
import com.mcmoddev.orespawn.data.Constants;

public class SpawnBuilder {
	private String myName;
	private boolean doRetro;
	private boolean active;
	private DimensionMatcher dimensions;
	private IReplacementEntry replacements;
	private BiomeMatcher biomes;
	private List<OS4BlockData> blocks;
	private JsonObject parameters;
	
	public SpawnBuilder(final String spawnName) {
		myName = spawnName;
	}

	public SpawnBuilder addItem(final String itemName, final JsonElement itemData) {
		JsonElement actualData = itemData;
		if (itemData.isJsonPrimitive()) {
			if (itemData.getAsString().startsWith("$")) {
				// its a preset, lets mangle it in...
				actualData = PresetsRegistry.INSTANCE.getPreset(itemData.getAsString().substring(1));
			}
		}
		
		switch(itemName) {
		case Constants.ConfigNames.REPLACEMENT:
			this.setReplacement(actualData);
			break;
		case Constants.ConfigNames.FEATURE:
			this.setFeature(actualData);
			break;
		case Constants.ConfigNames.DIMENSIONS:
			this.setDimensions(new DimensionMatcher(actualData.getAsJsonObject()));
			break;
		case Constants.ConfigNames.BIOMES:
			this.setBiomes(new BiomeMatcher(actualData.getAsJsonObject()));
			break;
		case Constants.ConfigNames.BLOCKS:
			this.setBlocks(OS4BlockData.parseJsonData(actualData));
			break;
		case Constants.ConfigNames.PARAMETERS:
			this.parameters = actualData.getAsJsonObject();
			break;
		default:
			OreSpawn.LOGGER.error("Unknown config item {} - skipping", itemName);
		}
		
		return this;
	}
	
	private void setBlocks(Object parseJsonData) {
		// TODO Auto-generated method stub
		
	}

	private void setBiomes(BiomeMatcher biomeMatcher) {
		// TODO Auto-generated method stub
		
	}

	private void setDimensions(DimensionMatcher dimensionMatcher) {
		// TODO Auto-generated method stub
		
	}

	private void setReplacement(final JsonElement data) {
		
	}

	private void setFeature(final JsonElement data) {
		
	}

	public JsonObject getParameters() {
		return parameters;
	}

	public List<OS4BlockData> getBlocks() {
		return blocks;
	}

	public String getMyName() {
		return myName;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isRetrogen() {
		return doRetro;
	}
	
	public BiomeMatcher getBiomes() {
		return biomes;
	}
	
	public IReplacementEntry getReplacements() {
		return replacements;
	}
	
	public DimensionMatcher getDimensions() {
		return dimensions;
	}

	public Object build() {
		// TODO Auto-generated method stub
		return null;
	}
}
