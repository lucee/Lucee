package lucee.runtime.interpreter;

public class SecurityInterpreterException extends InterpreterException {
	private static final long serialVersionUID = -31253141390505300L;

	public SecurityInterpreterException(String message) {
		super(message);
	}

	public SecurityInterpreterException(String message, String detail) {
		super(message, detail);
	}

}
