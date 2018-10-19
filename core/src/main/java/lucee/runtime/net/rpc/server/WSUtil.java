package lucee.runtime.net.rpc.server;

import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.rpc.WSHandler;

public class WSUtil {

    // used by genertaed bytecode
    public static Object invoke(String name, Object[] args) throws PageException {
	return invoke(null, name, args);
    }

    public static Object invoke(Config config, String name, Object[] args) throws PageException {
	return ((ConfigImpl) ThreadLocalPageContext.getConfig(config)).getWSHandler().getWSServer(ThreadLocalPageContext.get()).invoke(name, args);
    }
}
