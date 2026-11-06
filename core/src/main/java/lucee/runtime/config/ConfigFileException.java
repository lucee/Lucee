package lucee.runtime.config;

import lucee.commons.io.res.Resource;
import lucee.runtime.exp.ApplicationException;

/**
 * Thrown when a configuration file (.CFConfig.json) exists but cannot be read or parsed. The file is
 * never changed, renamed or replaced in that case.
 */
public final class ConfigFileException extends ApplicationException {

	private static final long serialVersionUID = 5750417836619104312L;

	private final String path;

	public ConfigFileException(Resource configFile, Throwable cause) {
		super("The configuration file [" + configFile + "] exists but could not be read: " + toReason(cause), "The file has not been changed, renamed or replaced, and no default configuration has been created. "
				+ "Fix the file (the position of the problem is part of the message) or remove it to start with a default configuration, then try again.");
		this.path = String.valueOf(configFile);
		initCause(cause);
	}

	public String getPath() {
		return path;
	}

	private static String toReason(Throwable cause) {
		if (cause == null) return "unknown reason";
		String msg = cause.getMessage();
		if (msg == null || msg.trim().isEmpty()) return cause.getClass().getName();
		return msg.trim();
	}
}
