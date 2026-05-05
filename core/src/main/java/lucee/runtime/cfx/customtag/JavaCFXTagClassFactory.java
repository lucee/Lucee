package lucee.runtime.cfx.customtag;

import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class JavaCFXTagClassFactory implements PropFactory<CFXTagClass> {

	private static JavaCFXTagClassFactory instance;

	public static JavaCFXTagClassFactory getInstance() {
		if (instance == null) {
			instance = new JavaCFXTagClassFactory();
		}
		return instance;
	}

	@Override
	public CFXTagClass evaluate(Config config, String name, Object val, short source) throws PageException {
		Struct cfxTag = Caster.toStruct(val);

		try {

			System.setProperty("cfx.bin.path", config.getConfigDir().getRealResource("bin").getAbsolutePath());

			String type = ConfigFactoryImpl.getAttr(config, cfxTag, "type");
			// Java CFX Tags
			if ("java".equalsIgnoreCase(type)) {
				ClassDefinition cd = ConfigFactoryImpl.getClassDefinition(config, cfxTag, "", config.getIdentification());
				if (!StringUtil.isEmpty(name) && cd.hasClass()) {
					return new JavaCFXTagClass(name, cd);
				}
				throw new ApplicationException("name is required");
			}
			throw new ApplicationException("type [" + type + "] is not supported, only type [java] is supported");
		}
		catch (Exception ex) {
			throw Caster.toPageException(ex);
		}
	}

	@Override
	public Struct schema(Prop<CFXTagClass> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// 1. The Type attribute
		// Based on your code: if ("java".equalsIgnoreCase(type))
		Struct typeAttr = PropFactory.createSimple("string", "The type of CFX tag. For this factory, it must be 'java'.");
		Array typeEnums = new ArrayImpl();
		typeEnums.appendEL("java");
		typeAttr.setEL("enum", typeEnums);
		properties.setEL(KeyImpl.init("type"), typeAttr);

		// 2. The Class/Bundle/Maven definition
		// This covers the 'class', 'bundleName', 'maven', etc., used by getClassDefinition
		PropFactory.appendClassDefinitionProps(properties);

		// 3. Optional description if CFX tags support it in the struct
		properties.setEL(KeyConstants._description, PropFactory.createSimple("string", "A description of the CFX tag's purpose."));

		return sct;
	}

	@Override
	public Object resolvedValue(CFXTagClass value) {
		return value;
	}

}
