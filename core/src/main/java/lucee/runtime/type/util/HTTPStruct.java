package lucee.runtime.type.util;

import lucee.runtime.cache.CacheObject;
import lucee.runtime.config.Config;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class HTTPStruct extends StructImpl implements CacheObject {

	private String cacheId;

	public HTTPStruct(String cacheId) {
		this.cacheId = cacheId;
	}

	public HTTPStruct(String cacheId, Struct data) {
		this.cacheId = cacheId;
		copy(this, data, false);
	}

	@Override
	public String getCacheId(Collection arguments, String defaultValue) {
		return cacheId;
	}

	@Override
	public int getCachetype() {
		return Config.CACHE_TYPE_HTTP;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		HTTPStruct sct = new HTTPStruct(cacheId);
		copy(this, sct, deepCopy);
		return sct;
	}

}
