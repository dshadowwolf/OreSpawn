package com.mcmoddev.orespawn.data;

import com.google.gson.JsonObject;

public class BlockData {
	private final String blockName;
	private final String blockState;
	private final boolean hasState;
	private final int chanceToAppear;

	public BlockData(final String name, final String state, final int chance) {
		blockName = name;
		blockState = state;
		hasState = true;
		chanceToAppear = chance;
	}

	public BlockData(final String name, final String state) {
		blockName = name;
		blockState = state;
		hasState = true;
		chanceToAppear = -1;
	}

	public BlockData(final String name, final int chance) {
		blockName = name;
		chanceToAppear = chance;
		hasState = false;
		blockState = "";
	}

	public BlockData(final String name) {
		blockName = name;
		chanceToAppear = -1;
		hasState = false;
		blockState = "";
	}

	public String getBlockWithState() {
		return hasState?String.format("%s[%s]", blockName, blockState):blockName;
	}

	public int getChance() {
		return chanceToAppear;
	}

	public static BlockData makeFromJson(JsonObject dataIn) {
		if (dataIn.has("state")) {
			if (dataIn.has("chance")) {
				return new BlockData(dataIn.get("name").getAsString(),
					dataIn.get("state").getAsString(), dataIn.get("chance").getAsInt());
			} else {
				return new BlockData(dataIn.get("name").getAsString(), dataIn.get("state").getAsString());
			}
		} else {
			if (dataIn.has("chance")) {
				return new BlockData(dataIn.get("name").getAsString(), dataIn.get("chance").getAsInt());
			} else {
				return new BlockData(dataIn.get("name").getAsString());
			}
		}
	}
}
