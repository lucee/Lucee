package lucee.runtime.functions.gateway;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.gateway.GatewayEngineImpl;

public final class GatewayAction implements Function {

	private static final long serialVersionUID = -4801573283953497373L;

	public static String call(PageContext pc, String gatewayID, String action) throws PageException {
		GatewayEngineImpl g = ((GatewayEngineImpl) ((ConfigWebPro) pc.getConfig()).getGatewayEngine());

		action = action.trim().toLowerCase();

		if ("start".equals(action)) g.start(gatewayID);
		else if ("stop".equals(action)) g.stop(gatewayID);
		else if ("restart".equals(action)) g.restart(gatewayID);
		else new FunctionException(pc, "GatewayAction", 2, "action", "invalid action [" + action + "], valid values are [start,stop,restart]");
		return null;
	}
}