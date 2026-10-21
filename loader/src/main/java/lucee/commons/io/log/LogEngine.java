package lucee.commons.io.log;

import java.nio.charset.Charset;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.io.retirement.RetireListener;
import lucee.runtime.config.Config;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageException;

/**
 * Engine that creates the loggers used by Lucee. Lucee ships with a simple built-in implementation,
 * an extension can replace it with a more powerful one (for example based on log4j) by declaring
 * the implementation class in its manifest with the attribute "log-engine". An implementation needs
 * a public constructor without arguments, {@link #init(Config)} is called once before any other
 * method.
 */
public interface LogEngine {

	public void init(Config config) throws PageException;

	public Log getConsoleLog(boolean errorStream, String name, int level) throws PageException;

	public Log getResourceLog(Resource res, Charset charset, String name, int level, int timeout, RetireListener listener, boolean async) throws PageException;

	public ClassDefinition appenderClassDefintion(String string) throws PageException;

	public ClassDefinition layoutClassDefintion(String string) throws PageException;

	public Log getLogger(Config config, Object appender, String name, int level) throws PageException;

	public Object getLayout(ClassDefinition cd, Map<String, String> layoutArgs, ClassDefinition cdAppender, String name) throws PageException;

	public Object getAppender(Config config, Object layout, String name, ClassDefinition cd, Map<String, String> appenderArgs) throws PageException;

	public void closeAppender(Object appender) throws PageException;

	public Object getDefaultLayout() throws PageException;

	public Object getClassicLayout() throws PageException;

	public String getVersion();
}
