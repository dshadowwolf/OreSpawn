package com.mcmoddev.orespawn.registries;

import com.google.gson.JsonElement;

import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IPresetEntry extends IForgeRegistryEntry<IPresetEntry> {
	JsonElement getData();
	IPresetEntry setData(JsonElement data);
}
