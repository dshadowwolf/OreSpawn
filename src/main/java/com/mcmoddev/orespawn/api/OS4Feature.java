package com.mcmoddev.orespawn.api;

import com.google.gson.JsonObject;
import com.mcmoddev.orespawn.data.SpawnStore;

public interface OS4Feature {
	String setName(String spawnName);
	String getDefaultParameters();
	String setParameters(JsonObject parametersBlock);
	String setData(SpawnStore.SpawnData spawnData);
}
