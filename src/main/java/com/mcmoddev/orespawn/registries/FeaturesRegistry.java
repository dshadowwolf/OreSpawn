package com.mcmoddev.orespawn.registries;

import java.lang.reflect.InvocationTargetException;

import com.mcmoddev.orespawn.OreSpawn;
import com.mcmoddev.orespawn.api.OS4Feature;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import net.minecraftforge.registries.RegistryBuilder;

public class FeaturesRegistry {
	private static final IForgeRegistryModifiable<IFeatureEntry> featuresRegistry = (IForgeRegistryModifiable<IFeatureEntry>) new RegistryBuilder<IFeatureEntry>()
			.setName(new ResourceLocation("orespawn", "featuress_registry"))
			.allowModification()
			.setType(IFeatureEntry.class)
			.setMaxID(Integer.MAX_VALUE)
			.create();
	
	public static final FeaturesRegistry INSTANCE = new FeaturesRegistry();
	
	FeaturesRegistry() {
		
	}
	
	public void addFeature(final DefaultFeatureEntry feature) {
		if ( feature.getClass() == null) return;
		featuresRegistry.register(feature);
	}
	
	public void addFeature(final ResourceLocation loc, final String featureClass) {
		this.addFeature(new DefaultFeatureEntry(loc, featureClass));
	}
	
	public void addFeature(final String entryName, final String featureClass) {
		String resDom = entryName.contains(":")?entryName.split(":")[0]:"orespawn";
		String resPath = entryName.contains(":")?entryName.split(":")[1]:entryName;
		this.addFeature(new ResourceLocation(resDom, resPath), featureClass);
	}
	
	public OS4Feature getFeature(final String featureName) {
		IFeatureEntry feature = featuresRegistry.getValue(featureName.contains(":")?new ResourceLocation(featureName):new ResourceLocation("orespawn",featureName));
		try {
			return (OS4Feature) feature.getClass().getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			OreSpawn.LOGGER.fatal(e.getMessage());
			return null;
		}
	}
	
	private class DefaultFeatureEntry implements IFeatureEntry {
		private ResourceLocation resLoc;
		private String featureClassPath;
		private Class<? extends OS4Feature> featureClass;
		
		public DefaultFeatureEntry(final ResourceLocation location, final String classPath) {
			this.resLoc = location;
			this.featureClassPath = classPath;
		}
		
		public Class<? extends OS4Feature> getFeature() {
			return this.featureClass;
		}
		
		public String getClassName() {
			return this.featureClassPath;
		}
		
		public void setFeatureName(String name) {
			String nameBits[] = name.split(":");
			String resDom = nameBits.length > 0?nameBits[0]:"orespawn";
			String resPath = nameBits.length > 0?nameBits[1]:name;
			this.resLoc = new ResourceLocation(resDom, resPath);
		}

		@Override
		public ResourceLocation getRegistryName() {
			return this.resLoc;
		}

		@Override
		public Class<IFeatureEntry> getRegistryType() {
			return IFeatureEntry.class;
		}

		@Override
		public IFeatureEntry setRegistryName(ResourceLocation arg0) {
			this.resLoc = arg0;
			return this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void resolve() {
			try {
				this.featureClass = (Class<? extends OS4Feature>)Class.forName(this.featureClassPath);
			} catch(ClassNotFoundException e) {
				OreSpawn.LOGGER.error(e.getMessage());
				this.featureClass = null;
			}
		}
	}

	public void doDataResolution() {
		featuresRegistry.getValues().stream().forEach(ent -> ent.resolve());
	}
}
