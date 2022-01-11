package lucee.commons.io.log;

import java.nio.charset.Charset;
import java.util.Map;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.log4j.Log4jEngine;
import lucee.commons.io.log.log4j2.Log4j2Engine;
import lucee.commons.io.res.Resource;
import lucee.commons.io.retirement.RetireListener;
import lucee.runtime.config.Config;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public abstract class LogEngine {

	public static LogEngine newInstance(Config config) {
		int version = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.log4j.version", null), 2);
		if (version == 1) return new Log4jEngine(config);
		return new Log4j2Engine(config);
	}

	public abstract Log getConsoleLog(boolean errorStream, String name, int level) throws PageException;

	public abstract Log getResourceLog(Resource res, Charset charset, String name, int level, int timeout, RetireListener listener, boolean async) throws PageException;

	public abstract ClassDefinition appenderClassDefintion(String string) throws PageException;

	public abstract ClassDefinition layoutClassDefintion(String string) throws PageException;

	public abstract Log getLogger(Config config, Object appender, String name, int level) throws PageException;

	public abstract Object getLayout(ClassDefinition cd, Map<String, String> layoutArgs, ClassDefinition cdAppender, String name) throws PageException;

	public abstract Object getAppender(Config config, Object layout, String name, ClassDefinition cd, Map<String, String> appenderArgs) throws PageException;

	public abstract void closeAppender(Object appender) throws PageException;

	public abstract Object getDefaultLayout() throws PageException;

	public abstract Object getClassicLayout() throws PageException;

	public abstract String getVersion();
}