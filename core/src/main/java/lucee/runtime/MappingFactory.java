package lucee.runtime;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.listener.ModernAppListener;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class MappingFactory implements PropFactory<Mapping> {

	private static MappingFactory[] instances = new MappingFactory[3];

	public static final short TYPE_REGULAR = 0;
	public static final short TYPE_COMPONENT = 1;
	public static final short TYPE_CUSTOM_TAG = 2;

	private short type;

	private MappingFactory(short type) {
		this.type = type;
	}

	public static MappingFactory getInstance(short type) {
		if (type > 3 || type < 0) throw new RuntimeException("invalid type [" + type + "]");

		if (instances[type] == null) {
			instances[type] = new MappingFactory(type);
		}
		return instances[type];
	}

	@Override
	public Mapping evaluate(Config config, String name, Object val, short source) throws PageException {

		Struct el = Caster.toStruct(val);

		String virtual = null;
		if (TYPE_REGULAR != type) {
			virtual = ConfigFactoryImpl.getAttr(config, el, "virtual");
		}
		if (StringUtil.isEmpty(virtual)) virtual = name;

		String physical = ConfigFactoryImpl.getAttr(config, el, "physical");
		String archive = ConfigFactoryImpl.getAttr(config, el, "archive");
		String strListType = ConfigFactoryImpl.getAttr(config, el, "listenerType");
		if (StringUtil.isEmpty(strListType)) strListType = ConfigFactoryImpl.getAttr(config, el, "listener-type");
		if (StringUtil.isEmpty(strListType)) strListType = ConfigFactoryImpl.getAttr(config, el, "listenertype");

		String strListMode = ConfigFactoryImpl.getAttr(config, el, "listenerMode");
		if (StringUtil.isEmpty(strListMode)) strListMode = ConfigFactoryImpl.getAttr(config, el, "listener-mode");
		if (StringUtil.isEmpty(strListMode)) strListMode = ConfigFactoryImpl.getAttr(config, el, "listenermode");

		boolean readonly = ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, el, "readonly"), false);
		boolean hidden = ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, el, "hidden"), false);
		boolean toplevel = ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, el, "toplevel"), true);

		boolean appMapping = false;
		boolean ignoreVirtual = false;

		// regular type check
		if (TYPE_REGULAR == type) {
			// lucee
			if ("/lucee/".equalsIgnoreCase(virtual)) {
				if (StringUtil.isEmpty(strListType, true)) strListType = "modern";
				if (StringUtil.isEmpty(strListMode, true)) strListMode = "curr2root";
				toplevel = true;
			}
		}
		else if (TYPE_COMPONENT == type) {
			if ("{lucee-web}/components/".equals(physical) || "{lucee-server}/components/".equals(physical)) return null;
			toplevel = true;
			ignoreVirtual = true;
		}
		else if (TYPE_CUSTOM_TAG == type) {
			if ("{lucee-web}/customtags/".equals(physical) || "{lucee-server}/customtags/".equals(physical)) return null;
			toplevel = true;
			ignoreVirtual = true;
		}

		int listenerMode = ConfigUtil.toListenerMode(strListMode, -1);
		int listenerType = ConfigUtil.toListenerType(strListType, -1);

		ApplicationListener listener = null;
		if (TYPE_REGULAR == type) {
			listener = ConfigUtil.loadListener(listenerType, null);
			if (listener != null || listenerMode != -1) {
				// type
				if (listener == null) listener = ConfigUtil.loadListener(ConfigUtil.toListenerType(config.getApplicationListener().getType(), -1), null);
				if (listener == null) listener = new ModernAppListener();

				// mode
				if (listenerMode == -1) {
					listenerMode = config.getApplicationListener().getMode();
				}
				listener.setMode(listenerMode);

			}
		}
		else if (TYPE_CUSTOM_TAG == type) {
			listenerMode = -1;
			listenerType = -1;
			// TODO i guess this applies for component as well?
		}

		// physical!=null &&
		if ((physical != null || archive != null)) {

			short insTemp = inspectTemplate(config, el);

			int insTempSlow = Caster.toIntValue(ConfigFactoryImpl.getAttr(config, el, "inspectTemplateIntervalSlow"), ConfigPro.INSPECT_INTERVAL_UNDEFINED);
			int insTempFast = Caster.toIntValue(ConfigFactoryImpl.getAttr(config, el, "inspectTemplateIntervalFast"), ConfigPro.INSPECT_INTERVAL_UNDEFINED);
			if (TYPE_REGULAR == type) {
				if ("/lucee/".equalsIgnoreCase(virtual) || "/lucee".equalsIgnoreCase(virtual) || "/lucee-server/".equalsIgnoreCase(virtual)
						|| "/lucee-server-context".equalsIgnoreCase(virtual))
					insTemp = ConfigPro.INSPECT_AUTO;
			}
			String primary = ConfigFactoryImpl.getAttr(config, el, "primary");
			boolean physicalFirst = primary == null || !"archive".equalsIgnoreCase(primary);

			return new MappingImpl(config, virtual, physical, archive, insTemp, insTempSlow, insTempFast, physicalFirst, hidden, readonly, toplevel, appMapping, ignoreVirtual,
					listener, listenerMode, listenerType).setSource(source);
		}
		throw new ApplicationException("you need to define [physical] or [archive]");
	}

	private static short inspectTemplate(Config config, Struct data) {
		String strInsTemp = SystemUtil.getSystemPropOrEnvVar("lucee.inspect.template", null); // TODO
		if (StringUtil.isEmpty(strInsTemp, true)) strInsTemp = ConfigFactoryImpl.getAttr(config, data, "inspectTemplate");
		if (StringUtil.isEmpty(strInsTemp, true)) strInsTemp = ConfigFactoryImpl.getAttr(config, data, "inspect");
		if (StringUtil.isEmpty(strInsTemp, true)) {
			Boolean trusted = Caster.toBoolean(ConfigFactoryImpl.getAttr(config, data, "trusted"), null);
			if (trusted != null) {
				if (trusted.booleanValue()) return ConfigPro.INSPECT_AUTO;
				return ConfigPro.INSPECT_ALWAYS;
			}
			return ConfigPro.INSPECT_UNDEFINED;
		}

		return ConfigUtil.inspectTemplate(strInsTemp, ConfigPro.INSPECT_UNDEFINED);
	}

	@Override
	public Struct schema(Prop<Mapping> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// 1. Path Definition
		properties.setEL(KeyImpl.init("virtual"), PropFactory.createSimple("string", "The virtual path (e.g., /inc). Not required if key name is used."));
		properties.setEL(KeyImpl.init("physical"), PropFactory.createSimple("string", "The physical path to the directory."));
		properties.setEL(KeyImpl.init("archive"), PropFactory.createSimple("string", "The path to a zip/lar archive containing the resources."));
		properties.setEL(KeyImpl.init("primary"), PropFactory.createSimple("string", "Which resource to check first. 'physical' or 'archive'."));

		// 2. Inspection & Trust (Handles 'inspectTemplate', 'inspect', and 'trusted')
		Struct inspect = PropFactory.createSimple("string", "How Lucee checks for changes in the source files.");
		Array inspectEnums = new ArrayImpl();
		inspectEnums.appendEL("always");
		inspectEnums.appendEL("never");
		inspectEnums.appendEL("once");
		inspectEnums.appendEL("auto");
		inspect.setEL("enum", inspectEnums);

		properties.setEL(KeyImpl.init("inspectTemplate"), inspect);
		properties.setEL(KeyImpl.init("inspect"), inspect);
		properties.setEL(KeyImpl.init("trusted"), PropFactory.createSimple("boolean", "Alias for inspectTemplate. true = never, false = always."));

		// 3. Listener Settings (Only relevant for TYPE_REGULAR)
		if (this.type == TYPE_REGULAR) {
			Struct listenerType = PropFactory.createSimple("string", "The application listener type (e.g. 'modern', 'none').");
			Array lTypes = new ArrayImpl();
			lTypes.appendEL("none");
			lTypes.appendEL("classic");
			lTypes.appendEL("modern");
			lTypes.appendEL("mixed");
			listenerType.setEL("enum", lTypes);

			properties.setEL(KeyImpl.init("listenerType"), listenerType);
			properties.setEL(KeyImpl.init("listener-type"), listenerType); // alias

			Struct listenerMode = PropFactory.createSimple("string", "The listener mode (e.g. 'curr2root').");
			properties.setEL(KeyImpl.init("listenerMode"), listenerMode);
			properties.setEL(KeyImpl.init("listener-mode"), listenerMode); // alias
		}

		// 4. Flags
		properties.setEL(KeyConstants._readOnly, PropFactory.createSimple("boolean", "If true, mapping cannot be changed in Admin."));

		properties.setEL(KeyConstants._hidden, PropFactory.createSimple("boolean", "If true, mapping is not visible in Admin."));

		properties.setEL(KeyImpl.init("toplevel"), PropFactory.createSimple("boolean", "If true, search for files in this mapping if not found in current directory."));

		return sct;
	}

	@Override
	public Object resolvedValue(Mapping value) {
		return value;
	}

}