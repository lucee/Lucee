package lucee.runtime.functions.gateway;

import lucee.commons.io.SystemUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.gateway.Gateway;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.type.dt.TimeSpan;

public final class GatewayAction implements Function {

	private static final long serialVersionUID = -4801573283953497373L;

	public static String call(PageContext pc, String gatewayID, String action) throws PageException {
		return call(pc, gatewayID, action, true, null);
	}

	public static String call(PageContext pc, String gatewayID, String action, boolean waitForIt) throws PageException {
		return call(pc, gatewayID, action, waitForIt, null);
	}

	public static String call(PageContext pc, String gatewayID, String action, boolean waitForIt, TimeSpan tsTimeout) throws PageException {
		GatewayEngineImpl g = ((GatewayEngineImpl) ((ConfigWebPro) pc.getConfig()).getGatewayEngine());
		long timeout = 1000L;
		if (tsTimeout != null) timeout = tsTimeout.getMillis();

		action = action.trim().toLowerCase();
		int expectedTargetStatus;
		if ("start".equals(action)) {
			expectedTargetStatus = Gateway.RUNNING;
			g.start(gatewayID);
		}
		else if ("stop".equals(action)) {
			expectedTargetStatus = Gateway.STOPPED;
			g.stop(gatewayID);
		}
		else if ("restart".equals(action)) {
			expectedTargetStatus = Gateway.RUNNING;
			g.restart(gatewayID);
		}
		else throw new FunctionException(pc, "GatewayAction", 2, "action", "invalid action [" + action + "], valid values are [start,stop,restart]");

		if (waitForIt) {
			long start = System.currentTimeMillis();
			while (g.getState(gatewayID) != expectedTargetStatus) {
				SystemUtil.sleep(100L);
				if (start + timeout < System.currentTimeMillis()) break;
			}
		}

		return null;
	}
}