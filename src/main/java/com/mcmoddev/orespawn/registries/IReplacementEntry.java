package com.mcmoddev.orespawn.registries;

import java.util.List;

import com.mcmoddev.orespawn.data.OS4BlockData;
import com.mcmoddev.orespawn.utils.OS4BlockStateMatcher;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IReplacementEntry extends IForgeRegistryEntry<IReplacementEntry> {
	List<OS4BlockData> getReplacementData();
	List<BlockState> getReplacementState();
	List<Block> getReplacementBlock();
	void setData(List<OS4BlockData> data);
	void resolveBlocks();
	OS4BlockStateMatcher getBlockMatcher();
}
