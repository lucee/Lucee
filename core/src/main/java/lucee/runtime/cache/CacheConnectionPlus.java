package lucee.runtime.cache;

import lucee.commons.io.cache.Cache;

public interface CacheConnectionPlus extends CacheConnection {
	public Cache getLoadedInstance();
}
