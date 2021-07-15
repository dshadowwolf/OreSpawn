package com.mcmoddev.orespawn.data;

import com.mcmoddev.orespawn.OreSpawn;
import com.mcmoddev.orespawn.api.OS4Feature;
import com.mcmoddev.orespawn.utils.ReflectionHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FeaturesStore {
	private static final Map<String, String> featuresList = new HashMap<>(1024);
	private static final Map<String, Class<? extends OS4Feature>> features = new HashMap<>(1024);

	public static void addNewFeature(String key, String value) {
		if (!featuresList.containsKey(key)) featuresList.put(key, value);
		else OreSpawn.LOGGER.error("Duplicate feature name {} with value {} found during load, ignoring.", key, value);
	}

	public static void resolveFeatures() {
		featuresList.entrySet().stream()
			.map( entry -> {
				String name = entry.getKey();
				String FQCN = entry.getValue();
				Class<? extends OS4Feature> resolved = ReflectionHelper.getFeatureNamed(name, FQCN);
				if (Objects.nonNull(resolved)) return Pair.of(name, resolved);
				else return null;
			})
			.filter(Objects::nonNull)
			.forEach(p -> features.put(p.getKey(), p.getValue()));
		if (features.size() == 0) {
			OreSpawn.LOGGER.fatal("No Features Available! Hard Exit!");
			throw new NullPointerException();
		}
	}
}
