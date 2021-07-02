package com.mcmoddev.orespawn.data;

public class OS4BlockData {
	private final String blockIdentifier;
	private final String blockState;
	
	public OS4BlockData(final String blockName, final String state) {
		this.blockIdentifier = blockName;
		this.blockState = state==null?"":state;
	}
	
	public final String getBlock() {
		String fmt = this.blockState.length()==0?"%s%s":"%s[%s]";
		return String.format(fmt, this.blockIdentifier, this.blockState);
	}
	
	public final String getBlockName() {
		return this.blockIdentifier;
	}
	
	public final String getBlockState() {
		return this.blockState;
	}
}
