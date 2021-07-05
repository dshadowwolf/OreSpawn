package com.mcmoddev.orespawn.builders;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BiomeMatcher {
	private final List<RegistryKey<Biome>> allowedBiomes = new LinkedList<>();
	private final List<RegistryKey<Biome>> blockedBiomes = new LinkedList<>();
	private final List<String> rawBiomeWhitelist = new LinkedList<>();
	private final List<String> rawBiomeBlacklist = new LinkedList<>();
	private boolean resolved = false;

	public BiomeMatcher(JsonObject rawData) {
		if (rawData.has("whitelist"))
			loadList(rawData.get("whitelist").getAsJsonArray(), rawBiomeWhitelist);
		if (rawData.has("blacklist"))
			loadList(rawData.get("blacklist").getAsJsonArray(), rawBiomeBlacklist);
	}

	private void loadList(JsonArray inputData, List<String> theList) {
		inputData.forEach(elem -> theList.add(elem.getAsString()));
	}

	public void resolve() {
		if (!rawBiomeWhitelist.isEmpty())
			allowedBiomes.addAll( rawBiomeWhitelist.stream()
					.map( name -> getRegistryKeyFor(name) )
					.collect(Collectors.toList()));
		if (!rawBiomeBlacklist.isEmpty())
			blockedBiomes.addAll( rawBiomeBlacklist.stream()
					.map( name -> getRegistryKeyFor(name) )
					.collect(Collectors.toList()));
		
		resolved = true;
	}

	private RegistryKey<Biome> getRegistryKeyFor(String name) {
		return RegistryKey.getOrCreateKey(Registry.BIOME_KEY, getResourceLocationFor(name));
	}

	private ResourceLocation getResourceLocationFor(String name) {
		String mod = name.contains(":")?name.split(":")[0]:"minecraft";
		String lname = name.contains(":")?name.split(":")[1]:name;
		return new ResourceLocation(mod, lname);
	}
	
	public boolean isAllowed(final String name) {
		if (!resolved) return true;
		if (allowedBiomes.isEmpty() && blockedBiomes.isEmpty()) return true;
		
		
		RegistryKey<Biome> key = getRegistryKeyFor(name);
		return (allowedBiomes.contains(key) && !blockedBiomes.contains(key));
	}
}
