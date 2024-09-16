package lucee.runtime.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class AIEnginePool {

	private Map<String, AIEngine> instances = new ConcurrentHashMap<>();

	public AIEngine getEngine(PageContext pc, String name, AIEngine defaultValue) {
		// get existing instance
		AIEngine aie = instances.get(name);
		if (aie != null) return aie;

		// loading new instance
		AIEngineFactory factory = ((ConfigPro) pc.getConfig()).getAIEngineFactory(name.toLowerCase());
		try {
			return factory.createInstance(pc.getConfig());
		}
		catch (Exception e) {

		}

		return defaultValue;
	}

	public AIEngine getEngine(Config config, String name) throws PageException {
		// get existing instance
		AIEngine aie = instances.get(name);
		if (aie != null) return aie;

		// loading new instance
		AIEngineFactory factory = ((ConfigPro) config).getAIEngineFactory(name.toLowerCase());
		if (factory == null) {
			throw new ApplicationException(ExceptionUtil.similarKeyMessage(((ConfigPro) config).getAIEngineFactoryNames(), name, "source", "sources", "ai pool", true));
		}

		try {
			aie = factory.createInstance(config);
			if (aie != null) return aie;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		throw new ApplicationException("there is no matching engine for the name [" + name + "] found");
	}

	/*
	 * private Map<String, AIEngine> getCollection(String nameAI) { Map<String, AIEngine> coll =
	 * instances.get(nameAI); if (coll == null) { synchronized (SystemUtil.createToken("ai-coll",
	 * nameAI)) { coll = instances.get(nameAI); if (coll == null) { coll = new ConcurrentHashMap<>();
	 * instances.put(nameAI, coll); } } } return coll; }
	 */
}