package lucee.commons.io.log;

import java.io.OutputStream;
import java.io.PrintStream;

import lucee.runtime.op.Caster;

/**
 * A PrintStream that redirects all output to a Lucee Log instance.
 */
public class LoggingPrintStream extends PrintStream {

	private final Log log;
	private final String logName;
	private final int logLevel;

	public LoggingPrintStream(Log log, String logName, int logLevel) {
		super(new OutputStream() {
			@Override
			public void write(int b) {
				// Required but not used - we override the print methods
			}
		});
		this.log = log;
		this.logName = logName;
		this.logLevel = logLevel;
	}

	@Override
	public void println(String x) {
		if (x != null && !x.trim().isEmpty()) {
			log.log(logLevel, logName, x);
		}
	}

	@Override
	public void print(String s) {
		if (s != null && !s.trim().isEmpty()) {
			log.log(logLevel, logName, s);
		}
	}

	@Override
	public void println(Object x) {
		if (x != null) {
			log.log(logLevel, logName, Caster.toString(x, null));
		}
	}

	@Override
	public void print(Object obj) {
		if (obj != null) {
			log.log(logLevel, logName, Caster.toString(obj, null));
		}
	}

	@Override
	public PrintStream format(String format, Object... args) {
		log.log(logLevel, logName, String.format(format, args));
		return this;
	}
}