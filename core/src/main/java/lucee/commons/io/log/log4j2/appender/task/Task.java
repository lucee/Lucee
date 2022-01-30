package lucee.commons.io.log.log4j2.appender.task;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;

import lucee.commons.lang.ExceptionUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.spooler.ExecutionPlan;
import lucee.runtime.spooler.SpoolerTaskListener;
import lucee.runtime.spooler.SpoolerTaskPro;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;
import lucee.runtime.util.Creation;

public class Task implements SpoolerTaskPro {

	private static final long serialVersionUID = 5649820047520607442L;

	private String id;
	private long lastExecution;
	private long nextExecution;
	private int tries = 0;
	private final Array exceptions;
	private final long creation = System.currentTimeMillis();
	private boolean closed;
	private final Struct detail;

	private final Appender appender;
	private final LogEvent le;

	public Task(Appender appender, LogEvent le) {
		this.appender = appender;
		this.le = le;
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		exceptions = engine.getCreationUtil().createArray();
		detail = engine.getCreationUtil().createStruct();
	}

	@Override
	public final Object execute(Config config) throws PageException {
		lastExecution = System.currentTimeMillis();
		tries++;
		try {
			appender.append(le);
			return null;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			CFMLEngine engine = CFMLEngineFactory.getInstance();
			Cast caster = engine.getCastUtil();
			Creation creator = engine.getCreationUtil();

			PageException pe = caster.toPageException(t);

			Struct exception = creator.createStruct();
			exception.put("message", pe.getMessage());
			exception.put("detail", pe.getDetail());
			exception.put("type", pe.getTypeAsString());
			exception.put("stacktrace", pe.getStackTraceAsString());
			exception.put("class", pe.getClass().getName());
			exception.put("time", caster.toLong(System.currentTimeMillis()));
			exceptions.appendEL(exception);

			throw pe;
		}
		finally {
			lastExecution = System.currentTimeMillis();
		}
	}

	@Override
	public Struct detail() {
		return detail;
	}

	@Override
	public final String subject() {
		return appender.getName();
	}

	@Override
	public final String getType() {
		return "log";
	}

	@Override
	public final Array getExceptions() {
		return exceptions;
	}

	@Override
	public final void setClosed(boolean closed) {
		this.closed = closed;
	}

	@Override
	public final boolean closed() {
		return closed;
	}

	@Override
	public final ExecutionPlan[] getPlans() {
		return null;
	}

	@Override
	public final long getCreation() {
		return creation;
	}

	@Override
	public final int tries() {
		return tries;
	}

	@Override
	public final void setLastExecution(long lastExecution) {
		this.lastExecution = lastExecution;
	}

	@Override
	public final long lastExecution() {
		return lastExecution;
	}

	@Override
	public final void setNextExecution(long nextExecution) {

		this.nextExecution = nextExecution;
	}

	@Override
	public final long nextExecution() {
		return nextExecution;
	}

	@Override
	public final String getId() {
		return id;
	}

	@Override
	public final void setId(String id) {
		this.id = id;
	}

	@Override
	public SpoolerTaskListener getListener() {
		return null; // not supported
	}
}