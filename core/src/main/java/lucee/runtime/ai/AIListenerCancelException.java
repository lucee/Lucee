package lucee.runtime.ai;

import lucee.runtime.exp.PageException;

/**
 * thrown when the listener of an AI session cancels a request before it is sent (LDEV-6540), the
 * request never reached the provider, so it is not recorded as a failed call
 */
public final class AIListenerCancelException extends RuntimeException {

	private static final long serialVersionUID = 4283377152331738192L;

	private final PageException pe;

	public AIListenerCancelException(PageException pe) {
		super(pe.getMessage(), pe);
		this.pe = pe;
	}

	public PageException getPageException() {
		return pe;
	}
}
