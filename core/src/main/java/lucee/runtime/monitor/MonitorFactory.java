package lucee.runtime.monitor;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.config.ConfigFactoryImpl.MonitorTemp;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.ConstructorInstance;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class MonitorFactory implements PropFactory<Monitor> {

	private static MonitorFactory instance;

	public static MonitorFactory getInstance() {
		if (instance == null) {
			instance = new MonitorFactory();
		}
		return instance;
	}

	@Override
	public Monitor evaluate(Config config, String nameX, Object val) throws PageException {

		ConfigServer configServer = ConfigUtil.getConfigServerImpl(config);

		short type;
		try {
			Struct el = Caster.toStruct(val);

			ClassDefinition cd = ConfigFactoryImpl.getClassDefinition(config, el, "", config.getIdentification());
			String strType = ConfigFactoryImpl.getAttr(config, el, "type");
			String name = ConfigFactoryImpl.getAttr(config, el, "name");
			boolean async = Caster.toBooleanValue(ConfigFactoryImpl.getAttr(config, el, "async"), false);
			boolean _log = Caster.toBooleanValue(ConfigFactoryImpl.getAttr(config, el, "log"), true);

			if ("request".equalsIgnoreCase(strType)) type = IntervallMonitor.TYPE_REQUEST;
			else if ("action".equalsIgnoreCase(strType)) type = Monitor.TYPE_ACTION;
			else type = IntervallMonitor.TYPE_INTERVAL;

			if (cd.hasClass() && !StringUtil.isEmpty(name)) {
				name = name.trim();
				Class clazz = cd.getClazz();
				Object obj;
				ConstructorInstance constr = Reflector.getConstructorInstance(clazz, new Object[] { configServer }, false);
				if (constr.getConstructor(null) != null) obj = constr.invoke();
				else obj = ClassUtil.newInstance(clazz);
				LogUtil.logGlobal(ThreadLocalPageContext.getConfigServer(configServer == null ? config : configServer), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(),
						"loaded " + (strType) + " monitor [" + clazz.getName() + "]");
				if (type == IntervallMonitor.TYPE_INTERVAL) {
					IntervallMonitor m = obj instanceof IntervallMonitor ? (IntervallMonitor) obj : new IntervallMonitorWrap(obj);
					m.init(configServer, name, _log);
					return m;
				}
				else if (type == Monitor.TYPE_ACTION) {
					ActionMonitor am = obj instanceof ActionMonitor ? (ActionMonitor) obj : new ActionMonitorWrap(obj);
					return new MonitorTemp(am, name, _log);
				}
				else {
					RequestMonitorPro m = new RequestMonitorProImpl(obj instanceof RequestMonitor ? (RequestMonitor) obj : new RequestMonitorWrap(obj));
					if (async) m = new AsyncRequestMonitor(m);
					m.init(configServer, name, _log);
					LogUtil.logGlobal(ThreadLocalPageContext.getConfigServer(configServer == null ? config : configServer), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(),
							"initialize " + (strType) + " monitor [" + clazz.getName() + "]");
					return m;
				}
			} // TODO better message
			throw new ApplicationException("invalid class defintion or missing name");
		}
		catch (Exception ex) {
			throw Caster.toPageException(ex);
		}
	}

	@Override
	public Struct schema(Prop<Monitor> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");
		sct.setEL(KeyConstants._description, "Defines a Lucee Monitor implementation (Request, Action, or Interval).");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// Name of the monitor instance
		Struct name = new StructImpl(Struct.TYPE_LINKED);
		name.setEL(KeyConstants._type, "string");
		name.setEL(KeyConstants._description, "The unique name for this monitor instance.");
		properties.setEL("name", name);

		// Monitor Type
		Struct type = new StructImpl(Struct.TYPE_LINKED);
		type.setEL(KeyConstants._type, "string");
		Array choices = new ArrayImpl();
		choices.appendEL("request");
		choices.appendEL("action");
		choices.appendEL("interval");
		type.setEL("enum", choices);
		type.setEL(KeyConstants._description, "The monitoring strategy: 'request' (every request), 'action' (specific events), or 'interval' (background snapshots).");
		properties.setEL("type", type);

		// Async execution (mostly for Request monitors)
		Struct async = new StructImpl(Struct.TYPE_LINKED);
		async.setEL(KeyConstants._type, "boolean");
		async.setEL(KeyConstants._description, "If true, the monitor processes data asynchronously to minimize impact on request execution time.");
		properties.setEL("async", async);

		// Logging
		Struct log = new StructImpl(Struct.TYPE_LINKED);
		log.setEL(KeyConstants._type, "boolean");
		log.setEL(KeyConstants._description, "Whether this monitor should log its activities.");
		properties.setEL("log", log);

		// Class Definition properties (bundleName, className, etc.)
		PropFactory.appendClassDefinitionProps(properties, "");

		// Required fields
		Array required = new ArrayImpl();
		required.appendEL("name");
		required.appendEL("type");
		required.appendEL("class");
		sct.setEL(KeyImpl.init("required"), required);

		return sct;
	}

	@Override
	public Object resolvedValue(Monitor value) {
		return value;
	}

}
