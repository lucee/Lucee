package lucee.commons.io.log;

import org.apache.logging.log4j.Level;

import lucee.commons.io.log.log4j2.LogAdapter;

public class LogReference implements Log {

	private LogAdapter log;

	public LogReference(LogAdapter log) {
		this.log = log;
	}

	@Override
	public void log(int level, String application, String message) {
		log.log(LogAdapter.toLevel(level), application, message);
	}

	@Override
	public void log(int level, String application, String message, Throwable t) {
		log.log(LogAdapter.toLevel(level), application, message, t);
	}

	@Override
	public void log(int level, String application, Throwable t) {
		log.log(LogAdapter.toLevel(level), application, null, t);
	}

	@Override
	public void trace(String application, String message) {
		log.log(Level.TRACE, application, message);
	}

	@Override
	public void info(String application, String message) {
		log.log(Level.INFO, application, message);
	}

	@Override
	public void debug(String application, String message) {
		log.log(Level.DEBUG, application, message);
	}

	@Override
	public void warn(String application, String message) {
		log.log(Level.WARN, application, message);
	}

	@Override
	public void error(String application, String message) {
		log.log(Level.ERROR, application, message);
	}

	@Override
	public void error(String application, Throwable t) {
		log.log(Level.ERROR, application, null, t);
	}

	@Override
	public void error(String application, String message, Throwable t) {
		log.log(Level.ERROR, application, message, t);
	}

	@Override
	public void fatal(String application, String message) {
		log.log(Level.FATAL, application, message);
	}

	@Override
	public int getLogLevel() {
		return log.getLogLevel();
	}

	@Override
	public void setLogLevel(int level) {
		log.setLogLevel(level);
	}

}
