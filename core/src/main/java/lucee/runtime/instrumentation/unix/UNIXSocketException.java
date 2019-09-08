package lucee.runtime.instrumentation.unix;

import java.net.SocketException;

/**
 * Something went wrong with the communication to a Unix socket.
 */
public class UNIXSocketException extends SocketException {
	private static final long serialVersionUID = 1L;
	private final String socketFile;

	public UNIXSocketException(String reason) {
		this(reason, (String) null);
	}

	public UNIXSocketException(String reason, final Throwable cause) {
		this(reason, (String) null);
		initCause(cause);
	}

	public UNIXSocketException(String reason, final String socketFile) {
		super(reason);
		this.socketFile = socketFile;
	}

	@Override
	public String toString() {
		if (socketFile == null) {
			return super.toString();
		}
		else {
			return super.toString() + " (socket: " + socketFile + ")";
		}
	}
}
