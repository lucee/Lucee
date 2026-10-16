package lucee.runtime.config;

import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.dynamic.meta.Constructor;

public class StartupFactory implements PropFactory<Startup> {

	private static StartupFactory instance;

	public static StartupFactory getInstance() {
		if (instance == null) {
			instance = new StartupFactory();
		}
		return instance;
	}

	@Override
	public Startup evaluate(Config config, String name, Object val, short source) throws PageException {

		try {
			Struct child = Caster.toStruct(val);
			// class
			ClassDefinition cd = ConfigFactoryImpl.getClassDefinition(config, child, "", config.getIdentification());

			Class clazz = cd.getClazz();

			Constructor constr = Reflector.getConstructor(clazz, new Class[] { Config.class }, null);
			if (constr != null) return new Startup(cd, constr.newInstance(new Object[] { config }));
			return new Startup(cd, ClassUtil.loadInstance(clazz));

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	@Override
	public Struct schema(Prop<Startup> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");
		sct.setEL(KeyConstants._description, "Defines a startup hook class that is instantiated and executed when the Lucee engine starts.");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// 1. Lucee ClassDefinition (Adds class, bundleName, bundleVersion)
		// We pass "" as the prefix because the keys are top-level in the startup object
		PropFactory.appendClassDefinitionProps(properties, "");

		// 2. Required fields
		Array required = new ArrayImpl();
		required.appendEL("class");
		sct.setEL(KeyImpl.init("required"), required);

		return sct;
	}

	@Override
	public Object resolvedValue(Startup value) {
		return value;
	}

}
