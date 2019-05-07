package lucee.runtime.cache.tag;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public interface CacheHandlerPro extends CacheHandler {

	// FUTURE move methods to CacheHandler and delete this interface

	/**
	 * This method will be used by Time-based cache handers, e.g. TimespanCacheHander, to check that the
	 * cached item is still viable. If the cached item is too old then null will be returned. If 0 is
	 * passed, then the cached item will be deleted.
	 * <p>
	 * Non-Time-based cache handlers should delegate the call to get(pc, cacheId)
	 *
	 * @param pc the PageContext
	 * @param cacheId the key of the cached item
	 * @param cachePolicy a Time-based object that will indicate the maximum lifetime of the cache
	 * @return
	 * @throws PageException
	 */
	public CacheItem get(PageContext pc, String cacheId, Object cachePolicy) throws PageException;

}
