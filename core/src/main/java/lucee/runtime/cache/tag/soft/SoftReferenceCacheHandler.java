package lucee.runtime.cache.tag.soft;

import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.HARD;
import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.SOFT;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.map.ReferenceMap;

import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.MapCacheHandler;
import lucee.runtime.op.Caster;

public class SoftReferenceCacheHandler extends MapCacheHandler {

	private static Map<String, CacheItem> map = Collections.synchronizedMap(new ReferenceMap<String, CacheItem>(HARD, SOFT, 32, 0.75f));

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
