package lucee.commons.io.log;

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
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class LogFactory implements PropFactory<LoggerAndSourceData> {

	private static String forceLogAppender = SystemUtil.getSystemPropOrEnvVar("lucee.logging.force.appender", null);
	private static String forceLogLevel = SystemUtil.getSystemPropOrEnvVar("lucee.logging.force.level", null);

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

		// 4. Flags
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
			String tmp;
			Map<String, String> appenderArgs, layoutArgs;
			ClassDefinition cdAppender, cdLayout;
			int level = Log.LEVEL_ERROR;
			boolean readOnly = false;

			try {

				// appender
				if (forceLogAppender != null) cdAppender = config.getLogEngine().appenderClassDefintion(forceLogAppender);
				else cdAppender = ConfigFactoryImpl.getClassDefinition(config, data, "appender", config.getIdentification());
				if (!cdAppender.hasClass()) {
					tmp = StringUtil.trim(ConfigFactoryImpl.getAttr(config, data, "appender"), "");
					cdAppender = config.getLogEngine().appenderClassDefintion(tmp);
				}
				else if (!cdAppender.isBundle()) {
					cdAppender = config.getLogEngine().appenderClassDefintion(cdAppender.getClassName());
				}
				appenderArgs = ConfigFactoryImpl.toArguments(data, "appenderArguments", true, false);

				// layout
				cdLayout = ConfigFactoryImpl.getClassDefinition(config, data, "layout", config.getIdentification());
				if (!cdLayout.hasClass()) {
					tmp = StringUtil.trim(ConfigFactoryImpl.getAttr(config, data, "layout"), "");
					cdLayout = config.getLogEngine().layoutClassDefintion(tmp);
				}
				else if (!cdLayout.isBundle()) {
					cdLayout = config.getLogEngine().layoutClassDefintion(cdLayout.getClassName());
				}
				layoutArgs = ConfigFactoryImpl.toArguments(data, "layoutArguments", true, false);

				String strLevel = ConfigFactoryImpl.getAttr(config, data, "level");
				if (forceLogLevel != null) strLevel = forceLogLevel;
				if (StringUtil.isEmpty(strLevel, true)) strLevel = ConfigFactoryImpl.getAttr(config, data, "logLevel");
				level = LogUtil.toLevel(StringUtil.trim(strLevel, ""), Log.LEVEL_ERROR);
				readOnly = Caster.toBooleanValue(ConfigFactoryImpl.getAttr(config, data, "readOnly"), false);
				// ignore when no appender/name is defined
				if (cdAppender.hasClass() && !StringUtil.isEmpty(name)) {
					if (cdLayout.hasClass()) {
						return createLogger(config, name, level, cdAppender, appenderArgs, cdLayout, layoutArgs, readOnly, false, source).init();
					}
					return createLogger(config, name, level, cdAppender, appenderArgs, null, null, readOnly, false, source).init();
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

	public static LoggerAndSourceData createLogger(Config config, String name, int level, ClassDefinition appender, Map<String, String> appenderArgs, ClassDefinition layout,
			Map<String, String> layoutArgs, boolean readOnly, boolean dyn, short source) {
		String id = LoggerAndSourceData.id(name.toLowerCase(), appender, appenderArgs, layout, layoutArgs, level, readOnly);
		return new LoggerAndSourceData(config, id, name.toLowerCase(), appender, appenderArgs, layout, layoutArgs, level, readOnly, dyn).setSource(source);
	}
}
