package lucee.commons.io.log;

import java.util.ArrayList;
import java.util.List;

import lucee.runtime.config.Config;

public class LogBuffer implements Log {

	private List<LogData> datas = new ArrayList<>();
	private int level = Log.LEVEL_TRACE;

	@Override
	public void debug(String application, String message) {
		datas.add(new LogData(Log.LEVEL_DEBUG, application, message, null));
	}

	@Override
	public void error(String application, String message) {
		datas.add(new LogData(Log.LEVEL_ERROR, application, message, null));
	}

	@Override
	public void error(String application, Throwable exeption) {
		datas.add(new LogData(Log.LEVEL_ERROR, application, null, exeption));
	}

	@Override
	public void error(String application, String message, Throwable exception) {
		datas.add(new LogData(Log.LEVEL_ERROR, application, message, exception));
	}

	@Override
	public void fatal(String application, String message) {
		datas.add(new LogData(Log.LEVEL_FATAL, application, message, null));
	}

	@Override
	public int getLogLevel() {
		return level;
	}

	@Override
	public void info(String application, String message) {
		datas.add(new LogData(Log.LEVEL_INFO, application, message, null));
	}

	@Override
	public void log(int level, String application, String message) {
		datas.add(new LogData(level, application, message, null));
	}

	@Override
	public void log(int level, String application, Throwable exception) {
		datas.add(new LogData(level, application, null, exception));
	}

	@Override
	public void log(int level, String application, String message, Throwable exception) {
		datas.add(new LogData(level, application, message, exception));
	}

	@Override
	public void setLogLevel(int level) {
		this.level = level;
	}

	@Override
	public void trace(String application, String message) {
		datas.add(new LogData(Log.LEVEL_TRACE, application, message, null));
	}

	@Override
	public void warn(String application, String message) {
		datas.add(new LogData(Log.LEVEL_WARN, application, message, null));
	}

	public void flush(Config config, String logName) {
		Log log;
		try {
			log = config.getLog(logName);
		}
		catch (Exception e) {
			return;
		}

		for (LogData data: datas) {
			if (data.exception != null) log.log(data.level, data.application, data.message, data.exception);
			else log.log(data.level, data.application, data.message);
		}
		datas.clear();
	}

	public void flush(Log log) {
		for (LogData data: datas) {
			if (data.exception != null) log.log(data.level, data.application, data.message, data.exception);
			else log.log(data.level, data.application, data.message);
		}
		datas.clear();
	}

	private static class LogData {

		private int level;
		private String application;
		private String message;
		private Throwable exception;

		public LogData(int level, String application, String message, Throwable exception) {
			this.level = level;
			this.application = application;
			this.message = message;
			this.exception = exception;
		}

	}

}
