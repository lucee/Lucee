package lucee.runtime.type.scope.storage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.cache.Cache;
import lucee.commons.io.log.Log;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cache.ram.RamCache;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.scope.ScopeContext;

public class IKHandlerCache implements IKHandler {
	private static final ConcurrentHashMap<String, Object> tokens = new ConcurrentHashMap<String, Object>();

	protected boolean storeEmpty = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.store.empty", null), false);

	@Override
	public IKStorageValue loadData(PageContext pc, String appName, String name, String strType, int type, Log log) throws PageException {
		Cache cache = getCache(pc, name);
		String key = getKey(pc.getCFID(), appName, strType);
		synchronized (getToken(key)) { // sync necessary?
			Object val = cache.getValue(key, null);
			if (val instanceof byte[][]) {
				ScopeContext.info(log,
						"load existing data from cache [" + name + "] to create " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID());
				return new IKStorageValue((byte[][]) val);
			}
			else if (val instanceof IKStorageValue) {
				ScopeContext.info(log,
						"load existing data from cache [" + name + "] to create " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID());
				return (IKStorageValue) val;
			}
			else {
				ScopeContext.info(log, "create new " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID() + " in cache [" + name + "]");
			}
			return null;
		}
	}

	@Override
	public void store(IKStorageScopeSupport storageScope, PageContext pc, String appName, String name, String cfid, Map<Collection.Key, IKStorageScopeItem> data, Log log) {
		try {
			Cache cache = getCache(ThreadLocalPageContext.get(pc), name);
			String key = getKey(cfid, appName, storageScope.getTypeAsString());

			synchronized (getToken(key)) {
				Object existingVal = cache.getValue(key, null);

				if (storeEmpty || storageScope.hasContent()) {
					cache.put(key,
							deserializeIKStorageValueSupported(cache) ? new IKStorageValue(IKStorageScopeSupport.prepareToStore(data, existingVal, storageScope.lastModified()))
									: IKStorageValue.toByteRepresentation(IKStorageScopeSupport.prepareToStore(data, existingVal, storageScope.lastModified())),
							new Long(storageScope.getTimeSpan()), null);
				}
				else if (existingVal != null) {
					cache.remove(key);
				}
			}
		}
		catch (Exception e) {
			ScopeContext.error(log, e);
		}
	}

	private boolean deserializeIKStorageValueSupported(Cache cache) {
		// FUTURE extend Cache interface to make sure it can handle serilasation
		if (cache == null) return false;
		if (cache instanceof RamCache) return true;
		if (cache.getClass().getName().equals("org.lucee.extension.cache.eh.EHCache")) return true;
		return false;
	}

	@Override
	public void unstore(IKStorageScopeSupport storageScope, PageContext pc, String appName, String name, String cfid, Log log) {
		try {
			Cache cache = getCache(pc, name);
			String key = getKey(cfid, appName, storageScope.getTypeAsString());

			synchronized (getToken(key)) {
				cache.remove(key);
			}
		}
		catch (Exception pe) {
		}
	}

	private static Cache getCache(PageContext pc, String cacheName) throws PageException {
		try {
			CacheConnection cc = CacheUtil.getCacheConnection(pc, cacheName);
			if (!cc.isStorage()) throw new ApplicationException("storage usage for this cache is disabled, you can enable this in the Lucee administrator.");
			return CacheUtil.getInstance(cc, ThreadLocalPageContext.getConfig(pc)); // cc.getInstance(config);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public String getType() {
		return "Cache";
	}

	public static String getKey(String cfid, String appName, String type) {
		return new StringBuilder("lucee-storage:").append(type).append(":").append(cfid).append(":").append(appName).toString().toUpperCase();
	}

	public static Object getToken(String key) {
		Object newLock = new Object();
		Object lock = tokens.putIfAbsent(key, newLock);
		if (lock == null) {
			lock = newLock;
		}
		return lock;
	}
}
