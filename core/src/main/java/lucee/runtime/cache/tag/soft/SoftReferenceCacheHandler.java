package lucee.runtime.cache.tag.soft;

import java.util.Collections;
import java.util.Map;

import lucee.commons.collection.RefMap;
import lucee.commons.collection.RefMap.ReferenceType;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.MapCacheHandler;
import lucee.runtime.op.Caster;

public final class SoftReferenceCacheHandler extends MapCacheHandler {

	private static Map<String, CacheItem> map = Collections.synchronizedMap(new RefMap<>(ReferenceType.SOFT, 32, 0.75f));

	@Override
	protected Map<String, CacheItem> map() {
		return map;
	}

	@Override
	public boolean acceptCachedWithin(Object cachedWithin) {
		String str = Caster.toString(cachedWithin, "").trim();
		return str.equalsIgnoreCase("soft");
	}

	@Override
	public String pattern() {
		return "soft";
	}

}
