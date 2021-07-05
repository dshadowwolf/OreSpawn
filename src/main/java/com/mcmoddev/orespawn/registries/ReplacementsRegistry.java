package com.mcmoddev.orespawn.registries;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mcmoddev.orespawn.data.OS4BlockData;

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
	
	private class DefaultReplacementEntry implements IReplacementEntry {
		private ResourceLocation name;
		private final List<OS4BlockData> data;
		private List<IForgeBlockState> blockStates;

		DefaultReplacementEntry(ResourceLocation loc, List<OS4BlockData> blocks) {
			this.name = loc;
			this.data = blocks;
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
		public List<IForgeBlockState> getReplacementState() {
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
			// TODO: Complete - should iterate this.data and use the state deserialize routines
			// dereferencing from the blocks list
		}
		
	}
}
