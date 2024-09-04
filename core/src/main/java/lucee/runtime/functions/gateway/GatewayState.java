package lucee.runtime.functions.gateway;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.gateway.GatewayUtil;

public final class GatewayState implements Function {

	public static String call(PageContext pc, String gatewayID) throws PageException {
		GatewayEngineImpl g = ((GatewayEngineImpl) ((ConfigWebPro) pc.getConfig()).getGatewayEngine());
		return GatewayUtil.toState(g.getState(gatewayID), "UNDEFINED");
	}
}