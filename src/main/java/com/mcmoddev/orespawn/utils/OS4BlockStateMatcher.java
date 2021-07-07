package com.mcmoddev.orespawn.utils;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;

public class OS4BlockStateMatcher {
	private final List<BlockState> statesToMatch;

	public OS4BlockStateMatcher(List<BlockState> blockStates) {
		statesToMatch = ImmutableList.copyOf(blockStates);
	}

	public boolean matches(final BlockState state) {
		if (statesToMatch.isEmpty()) return false;
		
		return statesToMatch.stream().anyMatch(bs -> (bs == state) || 
				(bs.matchesBlock(state.getBlock()) &&
						bs.getProperties().containsAll(state.getProperties())));
	}
}
