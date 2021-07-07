package com.mcmoddev.orespawn.utils;

import com.mcmoddev.orespawn.OreSpawn;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.util.ResourceLocation;

public class Helpers {
	public static boolean objectNotNull(Object obj) {
		return obj != null;
	}

	public static ResourceLocation makeResourceLocation(String blockName) {
		if(blockName.contains(":")) return new ResourceLocation(blockName);
		else return new ResourceLocation("minecraft", blockName);
	}
	
	public static BlockState deserializeState(final String fullState) {
		try {
			return new BlockStateParser(new StringReader(fullState), false).parse(false).getState();
		} catch (CommandSyntaxException e) {
			OreSpawn.LOGGER.error("Error parsing serialized BlockState {} - {}", fullState, e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

}
