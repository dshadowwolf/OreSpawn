package com.mcmoddev.orespawn.registries;

import java.util.List;

import com.mcmoddev.orespawn.data.OS4BlockData;

import net.minecraft.block.Block;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IReplacementEntry extends IForgeRegistryEntry<IReplacementEntry> {
	List<OS4BlockData> getReplacementData();
	List<IForgeBlockState> getReplacementState();
	List<Block> getReplacementBlock();
	void setData(List<OS4BlockData> data);
	void resolveBlocks();
}
