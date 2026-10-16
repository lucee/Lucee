package lucee.commons.io.log.log4j2;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import lucee.commons.io.SystemUtil;

public class ContextualMessage extends SimpleMessage {
	private static final long serialVersionUID = 3106308000640632352L;

	private static String label;

	private final String context;
	private final String application;
	private final String message;
	private final Throwable throwable;

	public ContextualMessage(String context, String application, String message, Throwable throwable) {
		super(message != null ? message : "");
		this.context = context != null ? context : "";
		this.application = application != null ? application : "";
		this.message = message != null ? message : "";
		this.throwable = throwable;

	}

	public static Message create(String application, String message, Throwable throwable) {
		return new ContextualMessage(getLabel(), application, message, throwable);
	}

	public String getContext() {
		return context;
	}

	public String getApplication() {
		return application;
	}

	@Override
	public Throwable getThrowable() {
		return throwable;
	}

	@Override
	public String toString() {
		return getFormattedMessage(); // Uses parent's implementation
	}

	public static String getLabel() {
		if (label == null) {
			synchronized (SystemUtil.createToken("ContextualMessage", "getLabel")) {
				if (label == null) {
					label = System.getenv(SystemUtil.isWindows() ? "COMPUTERNAME" : "HOSTNAME");
					if (label == null) label = "";
				}
			}
		}
		return label;
	}
}