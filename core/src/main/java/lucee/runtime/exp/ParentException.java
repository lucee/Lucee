package lucee.runtime.exp;

import lucee.runtime.PageSource;

public final class ParentException extends Exception {
	private static final long serialVersionUID = 3949948965230342458L;
	private static final StackTraceElement[] EMPTY = new StackTraceElement[0];

	private final StackTraceElement[] capturedStack;

	public ParentException() {
		super("parent thread stacktrace");
		this.capturedStack = null;
	}

	public ParentException(PageSource ps, String tagName) {
		super("parent thread stacktrace", null, false, false);
		this.capturedStack = ps == null ? EMPTY : new StackTraceElement[] {
				new StackTraceElement("cfml", tagName == null ? "cfthread" : tagName, ps.getDisplayPath(), 1)
		};
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		return capturedStack != null ? capturedStack.clone() : super.getStackTrace();
	}
}
