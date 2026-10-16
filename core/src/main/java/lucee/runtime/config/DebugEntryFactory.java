package lucee.runtime.config;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class DebugEntryFactory implements PropFactory<DebugEntry> {

	private static DebugEntryFactory instance;

	public static DebugEntryFactory getInstance() {
		if (instance == null) {
			instance = new DebugEntryFactory();
		}
		return instance;
	}

	@Override
	public DebugEntry evaluate(Config config, String name, Object val, short source) throws PageException {
		try {
			Struct data = Caster.toStruct(val);
			return new DebugEntry(ConfigFactoryImpl.getAttr(config, data, "id"), ConfigFactoryImpl.getAttr(config, data, "type"),
					ConfigFactoryImpl.getAttr(config, data, "iprange"), ConfigFactoryImpl.getAttr(config, data, "label"), ConfigFactoryImpl.getAttr(config, data, "path"),
					ConfigFactoryImpl.getAttr(config, data, "fullname"), ConfigUtil.getAsStruct(config, data, true, "custom"));
		}
		catch (Exception ex) {
			throw Caster.toPageException(ex);
		}

	}

	@Override
	public Struct schema(Prop<DebugEntry> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");
		sct.setEL(KeyConstants._description, "Defines a debug template or logger used for request profiling and debugging.");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// Core Debugging Properties
		addProp(properties, "id", "string", "Unique identifier for this debug entry.");
		addProp(properties, "label", "string", "Display name shown in the Lucee Administrator.");
		addProp(properties, "type", "string", "The category of the debug entry (e.g. 'classic', 'modern').");
		addProp(properties, "iprange", "string", "Comma-separated list of IP addresses or ranges allowed to see this debug info.");
		addProp(properties, "path", "string", "Physical path to the debugging template (.cfm file).");
		addProp(properties, "fullname", "string", "Full class name if the debug entry is handled by a Java class.");

		Struct custom = new StructImpl(Struct.TYPE_LINKED);
		custom.setEL(KeyConstants._type, "object");
		custom.setEL(KeyConstants._description, "Custom configuration parameters specific to this debug type.");
		properties.setEL("custom", custom);

		// Required fields
		Array required = new ArrayImpl();
		required.appendEL("label");
		required.appendEL("path");
		sct.setEL(KeyImpl.init("required"), required);

		return sct;
	}

	@Override
	public Object resolvedValue(DebugEntry value) {
		if (value == null) return null;
		return value;
	}

	private void addProp(Struct properties, String name, String type, String desc) {
		Struct p = new StructImpl(Struct.TYPE_LINKED);
		p.setEL(KeyConstants._type, type);
		p.setEL(KeyConstants._description, desc);
		properties.setEL(name, p);
	}

}
