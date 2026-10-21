package lucee.commons.io.log.slf4j;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.MessageFormatter;

import lucee.commons.io.log.Log;

/**
 * LDEV-6664 SLF4J logger writing to Lucee logging, the SLF4J logger name is used as log type.
 */
public final class LuceeSLF4JLogger extends AbstractLogger {

	private static final long serialVersionUID = 6205617447069548573L;

	public LuceeSLF4JLogger(String name) {
		this.name = name;
	}

	@Override
	public boolean isTraceEnabled() {
		return SLF4JLog.isEnabled(Log.LEVEL_TRACE);
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return isTraceEnabled();
	}

	@Override
	public boolean isDebugEnabled() {
		return SLF4JLog.isEnabled(Log.LEVEL_DEBUG);
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return isDebugEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return SLF4JLog.isEnabled(Log.LEVEL_INFO);
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return isInfoEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return SLF4JLog.isEnabled(Log.LEVEL_WARN);
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return isWarnEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return SLF4JLog.isEnabled(Log.LEVEL_ERROR);
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return isErrorEnabled();
	}

	@Override
	protected String getFullyQualifiedCallerName() {
		return null;
	}

	@Override
	protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
		String msg = MessageFormatter.basicArrayFormat(messagePattern, arguments);
		SLF4JLog.log(toLuceeLevel(level), name, msg, throwable);
	}

	private static int toLuceeLevel(Level level) {
		switch (level) {
		case TRACE:
			return Log.LEVEL_TRACE;
		case DEBUG:
			return Log.LEVEL_DEBUG;
		case INFO:
			return Log.LEVEL_INFO;
		case WARN:
			return Log.LEVEL_WARN;
		default:
			return Log.LEVEL_ERROR;
		}
	}
}
