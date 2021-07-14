package com.mcmoddev.orespawn.data;

import com.mcmoddev.orespawn.OreSpawn;

import java.util.HashMap;
import java.util.Map;

public class FeaturesStore {
	private static Map<String, String> featuresList = new HashMap<>(1024);

	public static void addNewFeature(String key, String value) {
		if (!featuresList.containsKey(key)) featuresList.put(key, value);
		else OreSpawn.LOGGER.error("Duplicate feature name {} with value {} found during load, ignoring.", key, value);
	}
}
