package lucee.runtime.net.rpc.server;

import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;

public class WSUtil {

	// used by genertaed bytecode
	public static Object invoke(String name, Object[] args) throws PageException {
		return invoke(null, name, args);
	}

	public static Object invoke(Config config, String name, Object[] args) throws PageException {
		return ((ConfigWebImpl) ThreadLocalPageContext.getConfig(config)).getWSHandler().getWSServer(ThreadLocalPageContext.get()).invoke(name, args);
	}
}
