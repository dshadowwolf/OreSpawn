package com.mcmoddev.orespawn.utils;

import com.mcmoddev.orespawn.OreSpawn;
import com.mcmoddev.orespawn.data.Constants;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Helpers {
	public static boolean objectNotNull(Object obj) {
		return obj != null;
	}

	public static ResourceLocation makeResourceLocation(String blockName) {
		if(blockName.contains(":")) return new ResourceLocation(blockName);
		else return new ResourceLocation("minecraft", blockName);
	}

	public static @Nullable BlockState deserializeState(final String fullState) {
		@Nullable BlockState result;
		try {
			result = new BlockStateParser(new StringReader(fullState), false).parse(false).getState();
		} catch (CommandSyntaxException e) {
			OreSpawn.LOGGER.error("Error parsing serialized BlockState {} - {}", fullState, e.getMessage());
			e.printStackTrace();
			result = null;
		}
		return result;
	}
	public static @Nullable Path makePath(ResourceLocation rl, String type) {
		@Nullable Path result;
		switch (type) {
			case Constants.FileBits.DISK:
				result = Paths.get(Constants.FileBits.CONFIG_DIR, Constants.FileBits.OS4, rl.getPath() + ".json");
				break;
			case Constants.FileBits.RESOURCE:
				ModFile mf = ModList.get().getModFileById(rl.getNamespace()).getFile();
				result = mf.getLocator().findPath(mf, "assets", "orespawn4-data", rl.getPath() + ".json");
				break;
			default:
				OreSpawn.LOGGER.error("Asked to resolve a path for {} of type {} -- I do not know how to do this", rl.toString(), type);
				result = null;
				break;
		}
		return result;
	}

}
