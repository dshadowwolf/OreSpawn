package com.mcmoddev.orespawn.registries;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mcmoddev.orespawn.OreSpawn;
import com.mcmoddev.orespawn.data.OS4BlockData;
import com.mcmoddev.orespawn.utils.Helpers;
import com.mcmoddev.orespawn.utils.OS4BlockStateMatcher;

public class ReplacementsRegistry {
	private static final IForgeRegistryModifiable<IReplacementEntry> replacementsRegistry = (IForgeRegistryModifiable<IReplacementEntry>) new RegistryBuilder<IReplacementEntry>()
			.setName(new ResourceLocation("orespawn", "replacements_registry"))
			.allowModification()
			.setType(IReplacementEntry.class)
			.setMaxID(Integer.MAX_VALUE)
			.create();

	public static final ReplacementsRegistry INSTANCE = new ReplacementsRegistry();

	ReplacementsRegistry() {

	}

	public void addReplacement(final String repName, final List<OS4BlockData> data) {
		if(repName.contains(":")) addReplacement(new ResourceLocation(repName), data);
		else addReplacement(new ResourceLocation("orespawn", repName), data);
	}

	public void addReplacement(final ResourceLocation loc, final List<OS4BlockData> data) {
		DefaultReplacementEntry ent = new DefaultReplacementEntry(loc, data);
		addReplacement(ent);
	}

	public void addReplacement(final IReplacementEntry entry) {
		replacementsRegistry.register(entry);
	}

	public IReplacementEntry get(final String name) {
		ResourceLocation loc = name.contains(":")?new ResourceLocation(name):new ResourceLocation("orespawn", name);
		if (replacementsRegistry.containsKey(loc)) return replacementsRegistry.getValue(loc);
		else return replacementsRegistry.getValue(new ResourceLocation("orespawn", "default"));
	}

	public void dump() {
		replacementsRegistry.getValues()
		.forEach(entry -> {
			OreSpawn.LOGGER.info("Replacement Entry {} -- contains:", entry.getRegistryName());
			entry.getReplacementState()
			.forEach(bs -> OreSpawn.LOGGER.info(">> {}", bs.toString()));
		});
	}

	private class DefaultReplacementEntry implements IReplacementEntry {
		private ResourceLocation name;
		private final List<OS4BlockData> data;
		private final List<BlockState> blockStates;

		DefaultReplacementEntry(ResourceLocation loc, List<OS4BlockData> blocks) {
			this.name = loc;
			this.data = blocks;
			this.blockStates = new LinkedList<>();
		}

		@Override
		public ResourceLocation getRegistryName() {
			return this.name;
		}

		@Override
		public Class<IReplacementEntry> getRegistryType() {
			return IReplacementEntry.class;
		}

		@Override
		public IReplacementEntry setRegistryName(ResourceLocation arg0) {
			this.name = arg0;
			return this;
		}

		@Override
		public List<OS4BlockData> getReplacementData() {
			return ImmutableList.copyOf(this.data);
		}

		@Override
		public List<BlockState> getReplacementState() {
			return ImmutableList.copyOf(this.blockStates);
		}

		@Override
		public List<Block> getReplacementBlock() {
			return ImmutableList.copyOf(this.blockStates.stream()
					.map(bls -> bls.getBlockState().getBlock()).collect(Collectors.toList()));
		}

		@Override
		public void setData(List<OS4BlockData> data) {
			Collections.copy(this.data, data);
		}

		@Override
		public void resolveBlocks() {
			data.stream()
			.filter(Objects::nonNull)
			.map(blockData -> Helpers.deserializeState(String.format("%s[%s]",
					blockData.getBlockName(), blockData.getBlockState())))
			.filter(Objects::nonNull)
			.forEach(blockStates::add);
		}

		@Override
		public OS4BlockStateMatcher getBlockMatcher() {
			if (this.blockStates.isEmpty()) return null;

			return new OS4BlockStateMatcher(this.blockStates);
		}
	}

	public void doDataResolution(@SuppressWarnings("unused") DynamicRegistries dynamicRegistries) {
		replacementsRegistry.getValues()
		.forEach(IReplacementEntry::resolveBlocks);
	}
}
