package lucee.runtime.interpreter;

public class SecurityInterpreterException extends InterpreterException {
	private static final long serialVersionUID = -31253141390505300L;

	/**
	 * message used when a variable is accessed in an expression while the "limitEvaluation" security
	 * setting is enabled
	 */
	public static final String LIMIT_EVALUATION_MESSAGE = "Dynamic evaluation blocked: variable expansion is not permitted because the \"limitEvaluation\" security setting is enabled (sysprop lucee.security.limitEvaluation, env var LUCEE_SECURITY_LIMITEVALUATION).";

	public SecurityInterpreterException(String message) {
		super(message);
	}

	public SecurityInterpreterException(String message, String detail) {
		super(message, detail);
	}

}
