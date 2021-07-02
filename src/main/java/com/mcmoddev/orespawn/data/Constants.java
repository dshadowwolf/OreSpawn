package com.mcmoddev.orespawn.data;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {
	public static final String VERSION = "4.0.0";
	public static final String CRASH_SECTION = "OreSpawn Version";
	public static final Path SYSCONF = Paths.get(FileBits.CONFIG_DIR, FileBits.OS4, FileBits.SYSCONF);
	public static class FileBits {
		public static final String CONFIG_DIR = "config";
		public static final String OS4 = "mmd-orespawn-4";
		public static final String SYSCONF = "sysconf";
		public static final String PRESETS = "presets.json";
		public static final String ALLOWED_MODS = "active_mods.json";
	}
}
