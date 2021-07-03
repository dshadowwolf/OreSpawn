package com.mcmoddev.orespawn.registries;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import net.minecraftforge.registries.RegistryBuilder;

public class PresetsRegistry {
	private static final IForgeRegistryModifiable<IPresetEntry> presetsRegistry = (IForgeRegistryModifiable<IPresetEntry>) new RegistryBuilder<IPresetEntry>()
			.setName(new ResourceLocation("orespawn", "presets_registry"))
			.allowModification()
			.setType(IPresetEntry.class)
			.setMaxID(Integer.MAX_VALUE)
			.create();
	
	public static final PresetsRegistry INSTANCE = new PresetsRegistry();
	
	PresetsRegistry() {
	}
	
	public JsonElement getPreset(String name) {
		return (name.contains(":")?presetsRegistry.getValue(new ResourceLocation(name)):presetsRegistry.getValue(new ResourceLocation("orespawn", name))).getData();
	}
	
	public void addPreset(String name, JsonElement data) {
		this.addPreset(new ResourceLocation("orespawn", name), data);
	}
	
	public void addPreset(ResourceLocation rl, JsonElement data) {
		this.addPreset(rl, new DefaultEntry(data));
	}
	
	public void addPreset(ResourceLocation rl, IPresetEntry entry) {
		presetsRegistry.register(entry.setRegistryName(rl));
	}
	
	private class DefaultEntry implements IPresetEntry {
		private ResourceLocation name;
		private JsonElement presetData;
		
		DefaultEntry(final JsonElement data) {
			this.presetData = data;
		}
		
		@Override
		public ResourceLocation getRegistryName() {
			return name;
		}

		@Override
		public Class<IPresetEntry> getRegistryType() {
			return IPresetEntry.class;
		}

		@Override
		public IPresetEntry setRegistryName(ResourceLocation arg0) {
			this.name = arg0;
			return this;
		}

		@Override
		public JsonElement getData() {
			return presetData==null?new JsonArray():this.presetData;
		}

		@Override
		public IPresetEntry setData(JsonElement data) {
			this.presetData = data;
			return this;
		}
		
	}
}
