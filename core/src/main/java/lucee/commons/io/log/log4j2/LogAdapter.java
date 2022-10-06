package lucee.commons.io.log.log4j2;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import lucee.commons.io.log.Log;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;

public class LogAdapter implements Log {

	private Logger logger;
	private Level level;

	public LogAdapter(Logger logger, Level level) {
		this.logger = logger;
		this.level = level;
	}

	public void validate() {
		if (logger instanceof org.apache.logging.log4j.core.Logger && !logger.getLevel().equals(level)) {
			org.apache.logging.log4j.core.Logger cl = (org.apache.logging.log4j.core.Logger) logger;
			cl.setLevel(level);
		}
	}

	@Override
	public void log(int level, String application, String message) {
		logger.log(toLevel(level), merge(application, message));
	}

	@Override
	public void log(int level, String application, String message, Throwable t) {
		if (StringUtil.isEmpty(message)) logger.log(toLevel(level), application, t);
		else logger.log(toLevel(level), merge(application, message), t);
	}

	@Override
	public void log(int level, String application, Throwable t) {
		t = toThrowable(t);
		String msg = t.getMessage();
		if (StringUtil.isEmpty(msg)) msg = t.getClass().getName();
		log(level, application, msg, t);
	}

	@Override
	public void trace(String application, String message) {
		log(Log.LEVEL_TRACE, application, message);
	}

	@Override
	public void info(String application, String message) {
		log(Log.LEVEL_INFO, application, message);
	}

	@Override
	public void debug(String application, String message) {
		log(Log.LEVEL_DEBUG, application, message);
	}

	@Override
	public void warn(String application, String message) {
		log(Log.LEVEL_WARN, application, message);
	}

	@Override
	public void error(String application, String message) {
		log(Log.LEVEL_ERROR, application, message);
	}

	@Override
	public void fatal(String application, String message) {
		log(Log.LEVEL_FATAL, application, message);
	}

	@Override
	public void error(String application, Throwable t) {
		log(LEVEL_ERROR, application, t);
	}

	@Override
	public void error(String application, String message, Throwable t) {
		log(LEVEL_ERROR, application, message, t);
	}

	@Override
	public int getLogLevel() {

		return toLevel(logger.getLevel());
	}

	@Override
	public void setLogLevel(int level) {
		if (logger instanceof org.apache.logging.log4j.core.Logger) {
			org.apache.logging.log4j.core.Logger cl = (org.apache.logging.log4j.core.Logger) logger;
			cl.setLevel(LogAdapter.toLevel(level));
		}
		else {
			logger.atLevel(LogAdapter.toLevel(level));
		}
	}

	public Logger getLogger() {
		return logger;
	}

	private Throwable toThrowable(Throwable t) {
		ExceptionUtil.rethrowIfNecessary(t);
		if (t instanceof InvocationTargetException) return ((InvocationTargetException) t).getTargetException();
		return t;
	}

	private String merge(String application, String message) {
		if (StringUtil.isEmpty(application)) return message;
		return application + "->" + message;
	}

	static Level toLevel(int level) {
		switch (level) {
		case Log.LEVEL_FATAL:
			return Level.FATAL;
		case Log.LEVEL_ERROR:
			return Level.ERROR;
		case Log.LEVEL_WARN:
			return Level.WARN;
		case Log.LEVEL_DEBUG:
			return Level.DEBUG;
		case Log.LEVEL_INFO:
			return Level.INFO;
		case Log.LEVEL_TRACE:
			return Level.TRACE;
		}
		return Level.INFO;
	}

	private static int toLevel(Level level) {
		if (Level.FATAL.equals(level)) return Log.LEVEL_FATAL;
		if (Level.ERROR.equals(level)) return Log.LEVEL_ERROR;
		if (Level.WARN.equals(level)) return Log.LEVEL_WARN;
		if (Level.DEBUG.equals(level)) return Log.LEVEL_DEBUG;
		if (Level.INFO.equals(level)) return Log.LEVEL_INFO;
		if (Level.TRACE.equals(level)) return Log.LEVEL_TRACE;
		return Log.LEVEL_INFO;
	}
}