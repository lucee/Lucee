package lucee.runtime.regex;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.RequestTimeoutException;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.util.PageContextUtil;

/**
 * A CharSequence around a String that lets a running java.util.regex match be stopped. The regex
 * engine reads its input exclusively through charAt(), so a pattern that runs into catastrophic
 * backtracking keeps calling it. Every CHECK_INTERVAL calls we check if the thread was interrupted
 * (that is what the request timeout does to a request that is over time) or if the request timeout
 * is reached, and if so we throw instead of matching on forever.
 */
final class InterruptibleCharSequence implements CharSequence {

	// must be a power of 2 minus 1 (used as bit mask), so the check on the hot path is a single AND
	private static final int CHECK_MASK = (1 << 13) - 1;

	private final String str;
	private int counter;

	InterruptibleCharSequence(String str) {
		this.str = str;
	}

	@Override
	public char charAt(int index) {
		if ((++counter & CHECK_MASK) == 0) check();
		return str.charAt(index);
	}

	@Override
	public int length() {
		return str.length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return str.substring(start, end);
	}

	@Override
	public String toString() {
		return str;
	}

	private static void check() {
		PageContext pc = ThreadLocalPageContext.get();
		if (Thread.currentThread().isInterrupted()) {
			// interrupted by the request timeout, the interrupt flag is left untouched on purpose
			if (pc instanceof PageContextImpl) {
				StackTraceElement[] timeoutStackTrace = ((PageContextImpl) pc).getTimeoutStackTrace();
				if (timeoutStackTrace != null) throw new PageRuntimeException(new RequestTimeoutException(pc, timeoutStackTrace));
			}
			throw new PageRuntimeException(new ApplicationException("the thread was interrupted while processing a regular expression"));
		}
		// throws a RequestTimeoutException when the request timeout is reached
		if (pc != null) {
			try {
				PageContextUtil.checkRequestTimeout(pc);
			}
			catch (RequestTimeoutException rte) {
				// charAt can not throw a checked exception
				throw new PageRuntimeException(rte);
			}
		}
	}
}
