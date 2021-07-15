package com.mcmoddev.orespawn.data;

import com.mcmoddev.orespawn.utils.Helpers;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DimensionMatcher {
	private final List<RegistryKey<Dimension>> allowedDimensions = new LinkedList<>();
 	public static DimensionMatcher buildFromData(List<String> dimensionList, DynamicRegistries dynamicRegistries) {
 		DimensionMatcher result = new DimensionMatcher();
		MutableRegistry<Dimension> registry = dynamicRegistries.getRegistry(Registry.DIMENSION_KEY);
		DimensionType overWorldType = dynamicRegistries.getRegistry(Registry.WORLD_KEY).getValueForKey(World.OVERWORLD).getDimensionType();
		DimensionType netherType = dynamicRegistries.getRegistry(Registry.WORLD_KEY).getValueForKey(World.THE_NETHER).getDimensionType();
		DimensionType endType = dynamicRegistries.getRegistry(Registry.WORLD_KEY).getValueForKey(World.THE_END).getDimensionType();

		if (dimensionList.contains("orespawn4:overworlds"))
			result.loadMatching(overWorldType, registry);
		if (dimensionList.contains("orespawn4:nethers"))
			result.loadMatching(netherType, registry);
		if (dimensionList.contains("orespawn4:ends"))
			result.loadMatching(endType, registry);

		if (dimensionList.size() > 0)
			result.loadList(dimensionList);

		return result;
	}

	public boolean matches(final String name) {
 		return matches(Helpers.makeBlockResourceLocation(name));
	}

	public boolean matches(final ResourceLocation resourceLocation) {
 		return matches(RegistryKey.getOrCreateKey(Registry.DIMENSION_KEY, resourceLocation));
	}

	public boolean matches(RegistryKey<Dimension> registryKey) {
 		return allowedDimensions.contains(registryKey);
	}

	private void loadList(List<String> dimensionList) {
		allowedDimensions.addAll(dimensionList.stream().filter(name -> !name.startsWith("orespawn4:"))
			.map( Helpers::makeBlockResourceLocation )
			.map( resourceLocation -> RegistryKey.getOrCreateKey(Registry.DIMENSION_KEY, resourceLocation) )
			.collect(Collectors.toList()));
	}

	private void loadMatching(DimensionType matchType, MutableRegistry<Dimension> registry) {
 		allowedDimensions.addAll(registry.getEntries().stream().filter(entry -> entry.getValue().getDimensionType().isSame(matchType)).map(Map.Entry::getKey).collect(Collectors.toList()));
	}
}
