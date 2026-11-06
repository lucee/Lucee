package lucee.runtime.ai;

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * What is known about one finished AI call (LDEV-6527)
 */
public final class AIUsageRecord {

	private final AIUsage usage;
	private final String model;
	private final long durationMs;
	private final boolean streamed;

	/**
	 * @param usage the usage, null when the provider did not report any
	 */
	public AIUsageRecord(AIUsage usage, String model, long durationMs, boolean streamed) {
		this.usage = usage;
		this.model = model;
		this.durationMs = durationMs;
		this.streamed = streamed;
	}

	public AIUsage getUsage() {
		return usage;
	}

	public String getModel() {
		return model;
	}

	public long getDurationMs() {
		return durationMs;
	}

	public boolean isStreamed() {
		return streamed;
	}

	public Struct toStruct() {
		Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
		if (usage != null) usage.toStruct(sct);
		if (model != null) sct.setEL("model", model);
		sct.setEL("durationMs", durationMs);
		sct.setEL("streamed", streamed);
		sct.setEL("estimated", false);
		return sct;
	}
}
