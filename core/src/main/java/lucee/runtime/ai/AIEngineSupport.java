package lucee.runtime.ai;

import lucee.commons.io.log.LogUtil;
import lucee.runtime.functions.other.CreateUniqueId;

public abstract class AIEngineSupport implements AIEngine {

	public static final String DEFAULT_USERAGENT = "Lucee (AI Request)";
	private String id;

	private AIEngineFactory factory;

	public AIEngine init(AIEngineFactory factory) {
		this.factory = factory;
		return this;
	}

	@Override
	public AIEngineFactory getFactory() {
		return factory;
	}

	@Override
	public String getId() {
		if (id == null) {
			id = CreateUniqueId.invoke();
		}
		return id;
	}

	public static void log(Exception e) {
		LogUtil.log("ai", "ai", e);
	}
}
