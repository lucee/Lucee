package lucee.runtime.ai;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.SystemUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class AISessionPool {

	private Map<String, Map<String, AIEngine>> instances = new ConcurrentHashMap<>();

	public AIEngine createSession(PageContext pc, String name, String initalMessage) throws PageException {
		// TODO add support to load AIEngine from application.cfc

		AIEngineFactory factory = ((ConfigPro) pc.getConfig()).getAIEngineFactory(name.toLowerCase());
		try {
			return factory.createInstance(pc.getConfig(), initalMessage);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public AIEngine getSession(PageContext pc, String nameSession, AIEngine defaultValue) {
		Iterator<Map<String, AIEngine>> it = instances.values().iterator();
		Map<String, AIEngine> coll;
		AIEngine aie;
		while (it.hasNext()) {
			coll = it.next();
			aie = coll.get(nameSession);
			if (aie != null) return aie;
		}
		return defaultValue;
	}

	public AIEngine getSession(PageContext pc, String nameSession) throws ApplicationException {
		AIEngine aie = getSession(pc, nameSession, null);
		if (aie == null) throw new ApplicationException("there is no matching session found");
		return aie;
	}

	public void returnSession(PageContext pc, AIEngine aie) {
		getCollection(aie.getFactory().getName()).put(aie.getId(), aie);
	}

	private Map<String, AIEngine> getCollection(String nameAI) {
		Map<String, AIEngine> coll = instances.get(nameAI);
		if (coll == null) {
			synchronized (SystemUtil.createToken("ai-coll", nameAI)) {
				coll = instances.get(nameAI);
				if (coll == null) {
					coll = new ConcurrentHashMap<>();
					instances.put(nameAI, coll);
				}
			}
		}
		return coll;
	}
}