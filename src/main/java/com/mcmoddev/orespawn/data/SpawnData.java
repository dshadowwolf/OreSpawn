package com.mcmoddev.orespawn.data;

import com.google.common.collect.ImmutableList;
import com.mcmoddev.orespawn.api.OS4Feature;
import com.mcmoddev.orespawn.builders.BiomeMatcher;
import com.mcmoddev.orespawn.builders.DimensionMatcher;
import com.mcmoddev.orespawn.registries.IReplacementEntry;
import com.mcmoddev.orespawn.utils.OS4BlockStateMatcher;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import java.util.List;

public class SpawnData {

	private final String spawnName;
	private final boolean spawnActive;
	private final boolean spawnRetrogen;
	private final BiomeMatcher spawnBiomes;
	private final DimensionMatcher spawnDimensions;
	private final OS4BlockStateMatcher replacementMatcher;
	private final OS4Feature spawnFeature;
	private final List<OS4BlockData> spawnBlocks;

	public SpawnData(@Nonnull final String name, final boolean isSpawnActivated,
					 final boolean spawnDoesRetrogen, @Nonnull final BiomeMatcher biomes,
					 @Nonnull final DimensionMatcher dimensions, @Nonnull final IReplacementEntry replacements,
					 @Nonnull final OS4Feature feature, @Nonnull final List<OS4BlockData> blocks) {
		spawnName = name;
		spawnActive = isSpawnActivated;
		spawnRetrogen = spawnDoesRetrogen;
		spawnBiomes = biomes;
		spawnDimensions = dimensions;
		replacementMatcher = replacements.getBlockMatcher();
		spawnFeature = feature;
		spawnBlocks = ImmutableList.copyOf(blocks);
	}

	public String getName() {
		return spawnName;
	}

	public boolean isActive() {
		return spawnActive;
	}

	public boolean willRetrogen() {
		return spawnRetrogen;
	}

	public boolean biomeMatch(@Nonnull final Biome biomeIn) {
		return spawnBiomes.isAllowed(biomeIn.getRegistryName().toString());
	}

	public boolean dimensionMatch(final String dimensionName) {
		return spawnDimensions.isDimensionAllowed(dimensionName);
	}

	public boolean canReplace(final BlockState blockStateIn) {
		return replacementMatcher.matches(blockStateIn);
	}

	public List<OS4BlockData> getBlocks() {
		return ImmutableList.copyOf(spawnBlocks);
	}

	public OS4Feature getFeature() {
		return spawnFeature;
	}
}
