package com.mcmoddev.orespawn.data;

import com.mcmoddev.orespawn.utils.Helpers;
import net.minecraft.block.BlockState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReplacementsStore {
	private static final Map<String, List<BlockData>> baseBlocks = new HashMap<>(1024);
	private static final Map<String, List<BlockState>> resolved = new HashMap<>(1024);

	public static void add(final String entryName, final List<BlockData> rawBlocks) {
		baseBlocks.put(entryName, rawBlocks);
	}

	public static void resolveBlocks() {
		baseBlocks.entrySet().stream()
			.forEach( entry -> {
				String name = entry.getKey();
				List<BlockState> resolvedBlocks = new LinkedList<>();
				entry.getValue().forEach( blockData -> resolvedBlocks.add(Helpers.deserializeState(blockData.getBlockWithState())));
				resolved.put(name, resolvedBlocks);
			});
	}

	public static OS4Replacer get(String replacementName) {
		return new OS4Replacer(resolved.get(replacementName));
	}
}
