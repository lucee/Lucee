package lucee.commons.io.log.log4j2.appender;

import java.io.Serializable;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;

import lucee.commons.io.log.log4j2.appender.task.Task;
import lucee.runtime.config.Config;
import lucee.runtime.spooler.SpoolerEngine;

public class TaskAppender implements Appender {

	private Appender appender;
	private SpoolerEngine spoolerEngine;

	public TaskAppender(Config config, Appender appender) {
		this.appender = appender;
		spoolerEngine = config.getSpoolerEngine();
	}

	@Override
	public void append(LogEvent event) {
		spoolerEngine.add(new Task(appender, event));
	}

	@Override
	public ErrorHandler getHandler() {
		return appender.getHandler();
	}

	@Override
	public String getName() {
		return appender.getName();
	}

	@Override
	public State getState() {
		return appender.getState();
	}

	@Override
	public void initialize() {
		appender.initialize();
	}

	@Override
	public void start() {
		appender.start();
	}

	@Override
	public void stop() {
		appender.stop();
	}

	@Override
	public boolean isStarted() {
		return appender.isStarted();
	}

	@Override
	public boolean isStopped() {
		return appender.isStopped();
	}

	@Override
	public Layout<? extends Serializable> getLayout() {
		return appender.getLayout();
	}

	@Override
	public boolean ignoreExceptions() {
		return appender.ignoreExceptions();
	}

	@Override
	public void setHandler(ErrorHandler handler) {
		appender.setHandler(handler);
	}
}