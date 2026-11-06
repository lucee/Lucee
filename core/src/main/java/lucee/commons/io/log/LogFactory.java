package lucee.commons.io.log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.library.ClassDefinitionImpl;

public class LogFactory implements PropFactory<LoggerAndSourceData> {

	private static String forceLogAppender = SystemUtil.getSystemPropOrEnvVar("lucee.logging.force.appender", null);
	private static String forceLogLevel = SystemUtil.getSystemPropOrEnvVar("lucee.logging.force.level", null);

	private static final Key APPENDERS = KeyImpl.init("appenders");

	private static LogFactory instance;

	public static LogFactory getInstance() {
		if (instance == null) {
			instance = new LogFactory();
		}
		return instance;
	}

	@Override
	public LoggerAndSourceData evaluate(Config config, String name, Object val, short source) throws PageException {
		try {
			return loadLogger((ConfigPro) config, name, Caster.toStruct(val), source);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Struct schema(Prop<LoggerAndSourceData> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// 1. Levels (Handling 'level' and 'logLevel' aliases)
		Struct level = PropFactory.createSimple("string", "The logging level (e.g., 'info', 'error').");
		Array levels = new ArrayImpl();
		levels.appendEL("fatal");
		levels.appendEL("error");
		levels.appendEL("warn");
		levels.appendEL("info");
		levels.appendEL("debug");
		levels.appendEL("trace");
		level.setEL("enum", levels);

		properties.setEL(KeyConstants._level, level);
		properties.setEL(KeyImpl.init("logLevel"), level);

		// 2. Appender Section
		// loadLogger looks for "appender" (simple name) or "appender-class", etc.
		properties.setEL(KeyImpl.init("appender"), PropFactory.createSimple("string", "The appender name or class."));

		// Calls the unified method with "appender" prefix
		PropFactory.appendClassDefinitionProps(properties, "appender");

		properties.setEL(KeyImpl.init("appenderArguments"), PropFactory.createSimple("object", "Arguments passed to the log appender."));

		// 3. Layout Section
		// loadLogger looks for "layout" (simple name) or "layout-class", etc.
		properties.setEL(KeyImpl.init("layout"), PropFactory.createSimple("string", "The layout name or class (e.g. 'classic', 'xml')."));

		// Calls the unified method with "layout" prefix
		PropFactory.appendClassDefinitionProps(properties, "layout");

		properties.setEL(KeyImpl.init("layoutArguments"), PropFactory.createSimple("object", "Arguments passed to the log layout."));

		// 4. Multiple appenders (LDEV-6792)
		Struct entry = new StructImpl(Struct.TYPE_LINKED);
		entry.setEL(KeyConstants._type, "object");
		Struct entryProps = new StructImpl(Struct.TYPE_LINKED);
		entry.setEL(KeyConstants._properties, entryProps);
		entryProps.setEL(KeyImpl.init("appender"), PropFactory.createSimple("string", "The appender name or class."));
		PropFactory.appendClassDefinitionProps(entryProps, "appender");
		entryProps.setEL(KeyImpl.init("appenderArguments"), PropFactory.createSimple("object", "Arguments passed to the log appender."));
		entryProps.setEL(KeyImpl.init("layout"), PropFactory.createSimple("string", "The layout name or class, if not defined the layout of the logger is used."));
		PropFactory.appendClassDefinitionProps(entryProps, "layout");
		entryProps.setEL(KeyImpl.init("layoutArguments"), PropFactory.createSimple("object", "Arguments passed to the log layout."));
		Struct appenders = PropFactory.createSimple("array", "More than one appender for the logger, replaces the appender defined with the keys above, the layout of the logger is used for the appenders without layout.");
		appenders.setEL(KeyImpl.init("items"), entry);
		properties.setEL(APPENDERS, appenders);

		// 5. Flags
		properties.setEL(KeyConstants._readOnly, PropFactory.createSimple("boolean", "If true, the logger cannot be modified via the administrator."));

		return sct;
	}

	@Override
	public Object resolvedValue(LoggerAndSourceData value) {
		return value;
	}

	private static LoggerAndSourceData loadLogger(ConfigPro config, final String name, final Struct data, short source) {

		try {
			// loggers
			int level = Log.LEVEL_ERROR;
			boolean readOnly = false;

			try {
				// more than one appender (LDEV-6792), not with a forced appender, that replaces everything
				AppenderDefinition main = null;
				List<AppenderDefinition> list = null;
				if (forceLogAppender == null) list = loadAppenders(config, data);
				if (list == null) main = loadAppender(config, data, forceLogAppender, null);

				String strLevel = ConfigFactoryImpl.getAttr(config, data, "level");
				if (forceLogLevel != null) strLevel = forceLogLevel;
				if (StringUtil.isEmpty(strLevel, true)) strLevel = ConfigFactoryImpl.getAttr(config, data, "logLevel");
				level = LogUtil.toLevel(StringUtil.trim(strLevel, ""), Log.LEVEL_ERROR);
				readOnly = Caster.toBooleanValue(ConfigFactoryImpl.getAttr(config, data, "readOnly"), false);

				// ignore when no appender/name is defined
				if (list != null && !StringUtil.isEmpty(name)) {
					return createLogger(config, name, level, list.toArray(new AppenderDefinition[list.size()]), readOnly, false, source).init();
				}
				if (main.appender.hasClass() && !StringUtil.isEmpty(name)) {
					return createLogger(config, name, level, main.appender, main.appenderArgs, main.layout, main.layoutArgs, readOnly, false, source).init();
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				LogUtil.logGlobal(config, ConfigFactoryImpl.class.getName(), t);
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			LogUtil.logGlobal(config, ConfigFactoryImpl.class.getName(), t);
		}
		return null;
	}

	/**
	 * the appenders defined with the "appenders" array of a logger, null if the logger does not have
	 * one (or it is empty) and has to be handled as a logger with one appender, defined by the
	 * "appender" keys of the logger itself
	 */
	private static List<AppenderDefinition> loadAppenders(ConfigPro config, Struct data) throws PageException {
		Object obj = data.get(APPENDERS, null);
		if (obj == null) return null;
		Array arr = Caster.toArray(obj);
		if (arr.size() == 0) return null;

		// the layout of the logger is the default for the layout of an appender
		AppenderDefinition main = loadLayout(config, data, null);
		List<AppenderDefinition> list = new ArrayList<AppenderDefinition>();
		Iterator<Object> it = arr.valueIterator();
		while (it.hasNext()) {
			list.add(loadAppender(config, Caster.toStruct(it.next()), null, main));
		}
		return list;
	}

	/**
	 * loads the appender with its layout, defined by the keys appender, appenderArguments, layout and
	 * layoutArguments (and the class definition variations of them) of the given struct, that is a
	 * logger or an entry of its appenders
	 * 
	 * @param forceAppender replaces the appender defined in the struct
	 * @param fallback the definition of the logger, if the appender does not define a layout, it gets
	 *            the layout of this definition, null for the logger itself
	 */
	private static AppenderDefinition loadAppender(ConfigPro config, Struct data, String forceAppender, AppenderDefinition fallback) throws PageException {
		String tmp;
		ClassDefinition cdAppender;
		Map<String, String> appenderArgs;

		// appender
		if (forceAppender != null) cdAppender = config.getLogEngine().appenderClassDefintion(forceAppender);
		else cdAppender = ConfigFactoryImpl.getClassDefinition(config, data, "appender", config.getIdentification());
		if (!cdAppender.hasClass()) {
			tmp = StringUtil.trim(ConfigFactoryImpl.getAttr(config, data, "appender"), "");
			cdAppender = config.getLogEngine().appenderClassDefintion(tmp);
		}
		else if (!cdAppender.isBundle() && !ClassDefinitionImpl.isMaven(cdAppender)) {
			cdAppender = config.getLogEngine().appenderClassDefintion(cdAppender.getClassName());
		}
		appenderArgs = ConfigFactoryImpl.toArguments(data, "appenderArguments", true, false);

		AppenderDefinition layout = loadLayout(config, data, fallback);
		return new AppenderDefinition(cdAppender, appenderArgs, layout.layout, layout.layoutArgs);
	}

	/**
	 * @return the layout (a definition without appender) defined by the keys layout and
	 *         layoutArguments of the given struct, if there is no layout and a fallback, the layout of
	 *         the fallback. The layout is null if there is none.
	 */
	private static AppenderDefinition loadLayout(ConfigPro config, Struct data, AppenderDefinition fallback) throws PageException {
		String tmp;
		ClassDefinition cdLayout = ConfigFactoryImpl.getClassDefinition(config, data, "layout", config.getIdentification());
		if (fallback != null && !cdLayout.hasClass() && StringUtil.isEmpty(ConfigFactoryImpl.getAttr(config, data, "layout"), true)) {
			// no layout defined for this appender
			return fallback;
		}
		if (!cdLayout.hasClass()) {
			tmp = StringUtil.trim(ConfigFactoryImpl.getAttr(config, data, "layout"), "");
			cdLayout = config.getLogEngine().layoutClassDefintion(tmp);
		}
		else if (!cdLayout.isBundle() && !ClassDefinitionImpl.isMaven(cdLayout)) {
			cdLayout = config.getLogEngine().layoutClassDefintion(cdLayout.getClassName());
		}
		Map<String, String> layoutArgs = ConfigFactoryImpl.toArguments(data, "layoutArguments", true, false);

		if (!cdLayout.hasClass()) return new AppenderDefinition(null, null, null, null);
		return new AppenderDefinition(null, null, cdLayout, layoutArgs);
	}

	public static LoggerAndSourceData createLogger(Config config, String name, int level, ClassDefinition appender, Map<String, String> appenderArgs, ClassDefinition layout,
			Map<String, String> layoutArgs, boolean readOnly, boolean dyn, short source) {
		String id = LoggerAndSourceData.id(name.toLowerCase(), appender, appenderArgs, layout, layoutArgs, level, readOnly);
		return new LoggerAndSourceData(config, id, name.toLowerCase(), appender, appenderArgs, layout, layoutArgs, level, readOnly, dyn).setSource(source);
	}

	/**
	 * a logger with one or more appenders (LDEV-6792)
	 */
	public static LoggerAndSourceData createLogger(Config config, String name, int level, AppenderDefinition[] appenders, boolean readOnly, boolean dyn, short source) {
		String id = LoggerAndSourceData.id(name.toLowerCase(), appenders, level, readOnly);
		return new LoggerAndSourceData(config, id, name.toLowerCase(), appenders, level, readOnly, dyn).setSource(source);
	}
}
