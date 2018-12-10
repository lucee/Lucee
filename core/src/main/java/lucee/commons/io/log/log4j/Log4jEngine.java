package lucee.commons.io.log.log4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogEngine;
import lucee.commons.io.log.log4j.appender.ConsoleAppender;
import lucee.commons.io.log.log4j.appender.RollingResourceAppender;
import lucee.commons.io.log.log4j.appender.TaskAppender;
import lucee.commons.io.log.log4j.layout.ClassicLayout;
import lucee.commons.io.res.Resource;
import lucee.commons.io.retirement.RetireListener;
import lucee.runtime.config.Config;

public class Log4jEngine implements LogEngine {

    private static final String DEFAULT_PATTERN = "%d{dd.MM.yyyy HH:mm:ss,SSS} %-5p [%c] %m%n";

    private Config config;

    public Log4jEngine(Config config) {
	this.config = config;
    }

    @Override
    public Log getConsoleLog(boolean errorStream, String name, int level) {

	PrintWriter pw = errorStream ? config.getErrWriter() : config.getOutWriter();
	if (pw == null) pw = new PrintWriter(errorStream ? System.err : System.out);

	return new LogAdapter(Log4jUtil._getLogger(config, new ConsoleAppender(pw, new PatternLayout(DEFAULT_PATTERN)), name, level));
    }

    @Override
    public Log getResourceLog(Resource res, Charset charset, String name, int level, int timeout, RetireListener listener, boolean async) throws IOException {
	Appender a = new RollingResourceAppender(new ClassicLayout(), res, charset, true, RollingResourceAppender.DEFAULT_MAX_FILE_SIZE,
		RollingResourceAppender.DEFAULT_MAX_BACKUP_INDEX, timeout, listener); // no open stream at all

	if (async) {
	    a = new TaskAppender(config, a);
	}
	return new LogAdapter(Log4jUtil._getLogger(config, a, name, level));
    }

}
