package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.List;

import lucee.runtime.functions.other.CreateUniqueId;

public abstract class AISessionSupport implements AISession {

	private String id;
	private AIEngine engine;
	List<Conversation> history = new ArrayList<>();
	private long timeout;

	public AISessionSupport(AIEngine engine, long timeout) {
		this.engine = engine;
		if (timeout < 0) this.timeout = engine.getTimeout();
		else this.timeout = timeout;
	}

	@Override
	public final long getTimeout() {
		return timeout;
	}

	@Override
	public final AIEngine getEngine() {
		return engine;
	}

	@Override
	public final Conversation[] getHistory() {
		return history.toArray(new Conversation[history.size()]);
	}

	protected final List<Conversation> getHistoryAsList() {
		return history;
	}

	@Override
	public final String getId() {
		if (id == null) {
			id = CreateUniqueId.invoke();
		}
		return id;
	}

}
