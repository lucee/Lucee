package lucee.runtime.gateway;

import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class GatewayEntryFactory implements PropFactory<GatewayEntry> {

	private static GatewayEntryFactory instance;

	public static GatewayEntryFactory getInstance() {
		if (instance == null) {
			instance = new GatewayEntryFactory();
		}
		return instance;
	}

	@Override
	public GatewayEntry evaluate(Config config, String name, Object val, short source) throws PageException {
		// validate input
		if (StringUtil.isEmpty(name)) {
			throw new ApplicationException("missing id");
		}
		Struct eConnection = Caster.toStruct(val);

		return new GatewayEntryImpl(name, ConfigFactoryImpl.getClassDefinition(config, eConnection, "", config.getIdentification()),
				ConfigFactoryImpl.getAttr(config, eConnection, "cfcPath"), ConfigFactoryImpl.getAttr(config, eConnection, "listenerCFCPath"),
				ConfigFactoryImpl.getAttr(config, eConnection, "startupMode"), ConfigUtil.getAsStruct(config, eConnection, true, "custom"),
				Caster.toBooleanValue(ConfigFactoryImpl.getAttr(config, eConnection, "readOnly"), false));

	}

	@Override
	public Struct schema(Prop<GatewayEntry> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// 1. Driver/Class Identification
		PropFactory.appendClassDefinitionProps(properties);

		// 2. Gateway Specific Paths
		properties.setEL(KeyImpl.init("cfcPath"), PropFactory.createSimple("string", "The path to the CFC that handles the gateway events."));

		properties.setEL(KeyImpl.init("listenerCFCPath"), PropFactory.createSimple("string", "The path to the listener CFC."));

		// 3. Startup Mode (automatic, manual, disabled)
		Struct startupMode = PropFactory.createSimple("string", "Defines the startup mode for this gateway.");
		Array modes = new ArrayImpl();
		modes.appendEL("automatic");
		modes.appendEL("manual");
		modes.appendEL("disabled");
		startupMode.setEL("enum", modes);
		properties.setEL(KeyImpl.init("startupMode"), startupMode);

		// 4. Standard Flags
		properties.setEL(KeyConstants._readOnly, PropFactory.createSimple("boolean", "If true, the gateway entry cannot be modified via the administrator."));

		// 5. Custom Driver Settings
		Struct custom = new StructImpl(Struct.TYPE_LINKED);
		custom.setEL(KeyConstants._type, "object");
		custom.setEL(KeyImpl.init("additionalProperties"), true);
		properties.setEL(KeyConstants._custom, custom);

		return sct;
	}

	@Override
	public Object resolvedValue(GatewayEntry value) {
		return value;
	}

}
