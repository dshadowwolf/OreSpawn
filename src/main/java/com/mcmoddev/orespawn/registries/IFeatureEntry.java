package com.mcmoddev.orespawn.registries;

import net.minecraftforge.registries.IForgeRegistryEntry;

import com.mcmoddev.orespawn.api.OS4Feature;

public interface IFeatureEntry extends IForgeRegistryEntry<IFeatureEntry> {
	Class<? extends OS4Feature> getFeature();
	String getClassName();
	void setFeatureName(String name);
	void resolve();
}
