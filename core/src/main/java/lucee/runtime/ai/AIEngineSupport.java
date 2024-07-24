package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.List;

import lucee.runtime.functions.other.CreateUniqueId;

public abstract class AIEngineSupport implements AIEngine {

	private String id;

	List<Conversation> history = new ArrayList<>();

	private AIEngineFactory factory;

	public void init(AIEngineFactory factory) {
		this.factory = factory;
	}

	@Override
	public AIEngineFactory getFactory() {
		return factory;
	}

	@Override
	public Conversation[] getHistory() {
		return history.toArray(new Conversation[history.size()]);
	}

	protected List<Conversation> getHistoryAsList() {
		return history;
	}

	@Override
	public String getId() {
		if (id == null) {
			id = CreateUniqueId.invoke();
		}
		return id;
	}
}
