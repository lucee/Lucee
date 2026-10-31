package lucee.runtime.ai;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.UDF;

/**
 * Listener calling a UDF for every chunk. When the UDF returns boolean false the inquiry is
 * cancelled, any other return value (including no return value) keeps the stream going.
 */
public final class UDFAIResponseListener implements CancelableAIResponseListener {

	private UDF listener;
	private PageContext pc;
	private boolean cancelled;

	public UDFAIResponseListener(PageContext pc, UDF listener) {
		this.pc = pc;
		this.listener = listener;
	}

	@Override
	public void listen(String part, int chunkIndex, boolean isComplete) throws PageException {
		if (cancelled) return;
		check(listener.call(pc, new Object[] { part, chunkIndex + 1, isComplete, "text", "text/plain", 1 }, true));
	}

	@Override
	public void listen(Object part, String contentType, int chunkIndex, int partIndex, boolean isComplete) throws PageException {
		if (cancelled) return;
		check(listener.call(pc, new Object[] { part, chunkIndex + 1, isComplete, (part instanceof byte[] ? "binary" : "text"), contentType, partIndex + 1 }, true));
	}

	private void check(Object rsp) {
		if (Boolean.FALSE.equals(rsp)) cancelled = true;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
}
