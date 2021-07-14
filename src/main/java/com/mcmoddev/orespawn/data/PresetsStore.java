package com.mcmoddev.orespawn.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class PresetsStore {
	private static final Map<String, JsonElement> presets = new HashMap<>(1024);

	/*
		Presets File Format:
			{
		     "section name": {
		        "item name": value_as_needed_for_section
		     },
		     ...
		   }

		Sections: feature, replacement, parameter, dimension, biome
	*/

	public static void load(final JsonObject presetsIn) {
		presetsIn.entrySet().forEach(entry -> {
			entry.getValue().getAsJsonObject().entrySet().forEach(
				presetEntry -> {
					final String fullName = String.format("$.%s.%s", entry.getKey(), presetEntry.getKey());
					presets.put(fullName, loadPresetDispatch(entry.getKey(), presetEntry.getValue()));
			});
		});
	}

	private static JsonElement loadPresetDispatch(final String sectionName, final JsonElement value) {
		// TODO: sectionName should be used to properly handle the section loading
		return value;
	}

	public static JsonElement getPreset(final String presetName) {
		String lookupName = presetName.startsWith("$.")?presetName:String.format("$.%s", presetName);
		if (presets.containsKey(lookupName)) return presets.get(lookupName);
		return new JsonArray();
	}

	public static String interpolateString(JsonElement value) {
		if (value.isJsonPrimitive() && value.getAsString().startsWith("$.")) {
			String getMe = value.getAsString();
			return presets.containsKey(getMe)?getPreset(getMe).getAsString():getMe;
		}

		return value.getAsString();
	}

	public static JsonArray interpolateArray(JsonElement value) {
		if (value.isJsonPrimitive() && value.getAsString().startsWith("$.")) {
			String getMe = value.getAsString();
			return getPreset(getMe).getAsJsonArray();
		}

		return value.getAsJsonArray();
	}

	public static JsonObject interpolateObject(JsonElement value) {
		if (value.isJsonPrimitive() && value.getAsString().startsWith("$.")) {
			String getMe = value.getAsString();
			JsonElement preset = getPreset(getMe);
			return preset.isJsonObject()?preset.getAsJsonObject():new JsonObject();
		}

		return value.getAsJsonObject();
	}
}
