package com.mcmoddev.orespawn.registries;

import java.util.Map;
import java.util.TreeMap;

import com.mcmoddev.orespawn.data.SpawnData;

import net.minecraft.util.registry.DynamicRegistries;

public class SpawnStore {
	private static final Map<String, SpawnData> spawns = new TreeMap<>();
	
	public static void add(SpawnData spawnData) {
		spawns.put(spawnData.getName(), spawnData);
	}

	public static void doResolveData(DynamicRegistries dynamicRegistries) {
		// TODO Auto-generated method stub
		
	}

}
