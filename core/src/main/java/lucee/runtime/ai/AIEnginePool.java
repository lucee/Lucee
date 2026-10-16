package lucee.runtime.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;

public final class AIEnginePool {

	private Map<String, AIEngine> instances = new ConcurrentHashMap<>();

	public AIEngine getEngine(Config config, String name) throws PageException {
		// get existing instance
		AIEngine aie = instances.get(name);
		if (aie != null) return aie;

		// loading new instance
		aie = ((ConfigPro) config).getAIEngine(name.toLowerCase());
		if (aie == null) {
			throw new ApplicationException(ExceptionUtil.similarKeyMessage(((ConfigPro) config).getAIEngineNames(), name, "source", "sources", "ai pool", true));
		}
		instances.put(name, aie);
		return aie;
	}

	public void flushEngine(String name) {
		instances.remove(name);
	}

}