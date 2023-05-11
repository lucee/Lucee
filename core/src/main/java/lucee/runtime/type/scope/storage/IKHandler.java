package lucee.runtime.type.scope.storage;

import java.util.Map;

import lucee.commons.io.log.Log;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;

public interface IKHandler {
	public IKStorageValue loadData(PageContext pc, String appName, String name, String strType, int type, Log log) throws PageException;

	public void store(IKStorageScopeSupport storageScope, PageContext pc, String appName, String name, Map<Collection.Key, IKStorageScopeItem> data, Log log);

	public void unstore(IKStorageScopeSupport storageScope, PageContext pc, String appName, String name, Log log);

	public String getType();
}
