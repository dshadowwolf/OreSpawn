package com.mcmoddev.orespawn.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcmoddev.orespawn.OreSpawn;

public class OS4BlockData {
	private final String blockIdentifier;
	private final String blockState;
	private final int chance;
	
	public OS4BlockData(final String blockName, final String state, final int chance) {
		this.blockIdentifier = blockName;
		this.blockState = state;
		this.chance = chance;
	}

	public OS4BlockData(final String blockName, final String state) {
		this(blockName, state, 100);
	}
	
	public final int getChance() {
		return this.chance;
	}
	
	public final String getBlock() {
		String fmt = this.blockState.length()==0?"%s%s":"%s[%s]";
		return String.format(fmt, this.blockIdentifier, this.blockState);
	}
	
	public final String getBlockName() {
		return this.blockIdentifier;
	}
	
	public final String getBlockState() {
		return this.blockState;
	}

	public static OS4BlockData parseJsonData(JsonElement itemData) {
		JsonObject actual = itemData.getAsJsonObject();
		
		int chance = actual.has("chance")?actual.get("chance").getAsInt():100;
		String state = actual.has("state")?actual.get("state").getAsString():"";
		String name = actual.has("name")?actual.get("name").getAsString():"i-am:a-missing-value";
		OreSpawn.LOGGER.info("actual: {}\nchance: {} :: state: {} :: name: {}", actual.toString(), chance, state, name);
		
		return new OS4BlockData(name, state, chance);
	}
}
