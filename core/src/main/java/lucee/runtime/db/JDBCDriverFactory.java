package lucee.runtime.db;

import org.osgi.framework.Bundle;

import lucee.commons.io.log.Log;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.library.ClassDefinitionImpl;

public class JDBCDriverFactory implements PropFactory<JDBCDriver> {

	private static JDBCDriverFactory instance;

	public static JDBCDriverFactory getInstance() {
		if (instance == null) {
			instance = new JDBCDriverFactory();
		}
		return instance;
	}

	@Override
	public JDBCDriver evaluate(Config config, String name, Object val, short source) throws PageException {
		try {
			ClassDefinition cd;
			String label, id, connStr;
			{
				try {
					Struct driver = Caster.toStruct(val);

					// class definition
					driver.setEL(KeyConstants._class, name);
					cd = ConfigFactoryImpl.getClassDefinition(config, driver, "", config.getIdentification());
					if (StringUtil.isEmpty(cd.getClassName()) && !StringUtil.isEmpty(cd.getName())) {
						try {
							Bundle bundle = OSGiUtil.loadBundle(cd.getName(), cd.getVersion(), config.getIdentification(), null, false);
							String cn = JDBCDriver.extractClassName(bundle);
							cd = new ClassDefinitionImpl(cn, cd.getName(), cd.getVersionAsString(), config.getIdentification());
						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
						}
					}

					label = ConfigFactoryImpl.getAttr(config, driver, "label");
					id = ConfigFactoryImpl.getAttr(config, driver, "id");
					connStr = ConfigFactoryImpl.getAttr(config, driver, "connectionString");
					// check if label exists
					if (StringUtil.isEmpty(label)) {
						ConfigFactoryImpl.log(config, Log.LEVEL_INFO, "missing label for jdbc driver [" + cd.getClassName() + "]");
						throw new ApplicationException("missing label for jdbc driver [" + cd.getClassName() + "]");

					}

					// check if it is a bundle
					if (!cd.isBundle() && !((ClassDefinitionImpl) cd).isMaven()) {
						ConfigFactoryImpl.log(config, Log.LEVEL_INFO, "jdbc driver [" + label + "] does not describe a bundle or a maven endpoint");
						throw new ApplicationException("jdbc driver [" + label + "] does not describe a bundle or a maven endpoint");
					}
					return new JDBCDriver(label, id, connStr, cd);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					ConfigFactoryImpl.log(config, t);
					throw Caster.toPageException(t);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	@Override
	public Struct schema(Prop<JDBCDriver> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");
		sct.setEL(KeyConstants._description, "Defines a JDBC driver used for database connectivity.");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// JDBC Specific properties
		Struct label = new StructImpl(Struct.TYPE_LINKED);
		label.setEL(KeyConstants._type, "string");
		label.setEL(KeyConstants._description, "The display name for the driver in the Administrator.");
		properties.setEL("label", label);

		Struct id = new StructImpl(Struct.TYPE_LINKED);
		id.setEL(KeyConstants._type, "string");
		id.setEL(KeyConstants._description, "A unique identifier for the driver.");
		properties.setEL("id", id);

		Struct connStr = new StructImpl(Struct.TYPE_LINKED);
		connStr.setEL(KeyConstants._type, "string");
		connStr.setEL(KeyConstants._description, "The template for the JDBC connection string.");
		properties.setEL("connectionString", connStr);

		// Class Definition properties (bundleName, bundleVersion, etc.)
		PropFactory.appendClassDefinitionProps(properties, "");

		// Required fields for a valid driver
		Array required = new ArrayImpl();
		required.appendEL("label");
		required.appendEL("class"); // In this context, the key name acts as the class
		sct.setEL(KeyImpl.init("required"), required);

		return sct;
	}

	@Override
	public Object resolvedValue(JDBCDriver value) {
		if (value == null) return null;
		return value;
	}
}
