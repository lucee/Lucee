package lucee.runtime.config;

import lucee.runtime.type.Struct;

public class ComponentMetaData {

	public final Struct meta;
	public final long lastMod;

	public ComponentMetaData(Struct meta, long lastMod) {
		this.meta = meta;
		this.lastMod = lastMod;
	}
}