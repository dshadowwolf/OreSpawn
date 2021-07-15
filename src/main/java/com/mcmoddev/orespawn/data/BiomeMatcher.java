package com.mcmoddev.orespawn.data;

import com.sun.jndi.rmi.registry.RegistryContextFactory;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mcmoddev.orespawn.utils.Helpers.makeBlockResourceLocation;

public class BiomeMatcher {
	private final List<RegistryKey<Biome>> allowedBiomes = new LinkedList<>();
	public static BiomeMatcher buildFromData(List<String> biomeBlacklist, List<String> biomeWhitelist, DynamicRegistries dynamicRegistries) {
		BiomeMatcher result = new BiomeMatcher();
		MutableRegistry<Biome> registry = dynamicRegistries.getRegistry(Registry.BIOME_KEY);
		if (biomeWhitelist.size() == 0 || biomeWhitelist.contains("orespawn4:any"))
			result.defaultLoad(registry);
		else
			result.loadWhitelist(biomeWhitelist);

		if (biomeBlacklist.size() > 0)
			result.clearBlacklistedBits(biomeBlacklist);

		return result;
	}

	public boolean biomeMatches(final ResourceLocation biomeRL) {
		return biomeMatches(RegistryKey.getOrCreateKey(Registry.BIOME_KEY, biomeRL));
	}

	public boolean biomeMatches(RegistryKey<Biome> biomeKey) {
		return allowedBiomes.contains(biomeKey);
	}

	public boolean biomeMatches(final String biomeName) {
		return biomeMatches(makeBlockResourceLocation(biomeName));
	}

	private void clearBlacklistedBits(List<String> biomeBlacklist) {
		biomeBlacklist.stream().map( name -> RegistryKey.getOrCreateKey(Registry.BIOME_KEY, makeBlockResourceLocation(name)))
			.filter( biomeKey -> allowedBiomes.contains(biomeKey))
			.forEach( biomeKey -> allowedBiomes.remove(biomeKey));
	}

	private void loadWhitelist(List<String> biomeWhitelist) {
		allowedBiomes.addAll( biomeWhitelist.stream().map( name -> RegistryKey.getOrCreateKey(Registry.BIOME_KEY, makeBlockResourceLocation(name))).collect(Collectors.toList()));
	}

	private void defaultLoad(MutableRegistry<Biome> registry) {
		allowedBiomes.addAll( registry.getEntries().stream().map( entry -> entry.getKey()).collect(Collectors.toList()) );
	}
}
