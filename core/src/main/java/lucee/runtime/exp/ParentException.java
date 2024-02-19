package lucee.runtime.exp;

public class ParentException extends Exception {
	private static final long serialVersionUID = 8698505541398848801L;

	public ParentException(StackTraceElement[] elements) {
		setStackTrace(elements);
	}
}
