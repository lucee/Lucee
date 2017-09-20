package lucee.runtime.net.rpc.server;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.rpc.WSHandler;

public class WSUtil {
	public static Object invoke(String name, Object[] args)
			throws PageException {
		return WSHandler.getInstance()
				.getWSServer(ThreadLocalPageContext.get()).invoke(name, args);
	}
}
