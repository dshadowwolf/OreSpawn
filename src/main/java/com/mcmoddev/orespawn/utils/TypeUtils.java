package com.mcmoddev.orespawn.utils;

import org.objectweb.asm.Type;

import com.mcmoddev.orespawn.OreSpawn;

public class TypeUtils {
	public static Class<?> getClassFromType(Type type) {
		try {
			return Class.forName(type.getClassName(), false, OreSpawn.class.getClassLoader());
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}
