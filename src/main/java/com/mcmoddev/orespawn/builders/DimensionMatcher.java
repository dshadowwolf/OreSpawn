package com.mcmoddev.orespawn.builders;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;

public class DimensionMatcher {
	private final List<RegistryKey<Dimension>> allowedDimensions;
	private final List<String> rawDimensions;
	private boolean loaded = false;
	
	public DimensionMatcher(JsonArray dimensionData) {
		allowedDimensions = new LinkedList<>();
		rawDimensions = new LinkedList<>();
		dimensionData.forEach(dim -> rawDimensions.add(dim.getAsString()));	
	}

	private RegistryKey<Dimension> getKeyFromString(final String dimensionName) {
		String mod = dimensionName.contains(":")?dimensionName.split(":")[0]:"minecraft";
		String name = dimensionName.contains(":")?dimensionName.split(":")[1]:dimensionName;
		ResourceLocation loc = new ResourceLocation(mod, name);
		return RegistryKey.getOrCreateKey(Registry.DIMENSION_KEY, loc);
	}
	public boolean isDimensionAllowed(final String dimension) {
		if (allowedDimensions.isEmpty() || !loaded) return true;
		return allowedDimensions.contains(getKeyFromString(dimension));
	}
	
	public void resolveData() {
		allowedDimensions.addAll(
				rawDimensions.stream()
				.map( rawDim -> getKeyFromString(rawDim) )
				.collect(Collectors.toList()));
		loaded = true;
	}
}
