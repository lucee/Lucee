package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.storage.IKStorageScopeSupport;
import lucee.runtime.type.scope.storage.StorageScopeImpl;

public class SessionTouch implements Function {
	private static final long serialVersionUID = 4287563982175634891L;

	public static String call( PageContext pc ) throws PageException {
		Session session = ((PageContextImpl) pc).sessionScope();
		if ( session instanceof IKStorageScopeSupport ) {
			((IKStorageScopeSupport) session).setDirty();
		}
		else if ( session instanceof StorageScopeImpl ) {
			((StorageScopeImpl) session).setDirty();
		}
		return null;
	}
}
