package com.mcmoddev.orespawn.data;

import java.util.List;
import java.util.LinkedList;

public class ConfigBlacklist {
	private static final List<String> blockedMods = new LinkedList<>();
	private static final List<String> blockedConfigs = new LinkedList<>();
	
	public ConfigBlacklist() {
		
	}
	
	public void addMod(final String modid) {
		blockedMods.add(modid);
	}
	
	public void addConfig(final String configName) {
		blockedConfigs.add(configName);
	}
	
	public boolean isAllowed(final String checkName) {
		return !(blockedMods.contains(checkName) || blockedConfigs.contains(checkName));
	}
	
	public boolean isBlocked(final String checkName) {
		return !isAllowed(checkName);
	}
}
