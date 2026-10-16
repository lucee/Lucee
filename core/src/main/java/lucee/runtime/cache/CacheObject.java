package lucee.runtime.cache;

import lucee.runtime.type.Collection;

public interface CacheObject {

	public String getCacheId(Collection arguments, String defaultValue);

	public int getCachetype();

}
