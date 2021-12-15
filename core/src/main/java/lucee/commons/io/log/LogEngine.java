package lucee.commons.io.log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import lucee.commons.io.log.log4j.Log4jEngine;
import lucee.commons.io.res.Resource;
import lucee.commons.io.retirement.RetireListener;
import lucee.runtime.config.Config;
import lucee.runtime.db.ClassDefinition;

public abstract class LogEngine {

	public static LogEngine getInstance(Config config) {
		return new Log4jEngine(config);
	}

	public abstract Log getConsoleLog(boolean errorStream, String name, int level);

	public abstract Log getResourceLog(Resource res, Charset charset, String name, int level, int timeout, RetireListener listener, boolean async) throws IOException;

	public abstract ClassDefinition appenderClassDefintion(String string);

	public abstract ClassDefinition layoutClassDefintion(String string);

	public abstract Log getLogger(Config config, Object appender, String name, int level);

	public abstract Object getLayout(ClassDefinition cd, Map<String, String> layoutArgs, ClassDefinition cdAppender, String name);

	public abstract Object getAppender(Config config, Object layout, String name, ClassDefinition cd, Map<String, String> appenderArgs);

	public abstract void closeAppender(Object appender);

	public abstract Object getDefaultLayout();
}
