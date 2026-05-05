package lucee.runtime.net.mail;

import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class ServerFactory implements PropFactory<Server> {

	private static ServerFactory instance;

	public static ServerFactory getInstance() {
		if (instance == null) {
			instance = new ServerFactory();
		}
		return instance;
	}

	private ServerFactory() {}

	@Override
	public Server evaluate(Config config, String name, Object val, short source) throws PageException {

		int index = Caster.toIntValue(name, 0); // in case of an array, the name is the index
		Struct el = Caster.toStruct(val);
		return new ServerImpl(Caster.toIntValue(ConfigFactoryImpl.getAttr(config, el, "id"), index), ConfigFactoryImpl.getAttr(config, el, "smtp"),
				Caster.toIntValue(ConfigFactoryImpl.getAttr(config, el, "port"), 25), ConfigFactoryImpl.getAttr(config, el, "username"),
				ConfigUtil.decrypt(ConfigFactoryImpl.getAttr(config, el, "password")), ConfigFactoryImpl.toLong(ConfigFactoryImpl.getAttr(config, el, "life"), 1000 * 60 * 5),
				ConfigFactoryImpl.toLong(ConfigFactoryImpl.getAttr(config, el, "idle"), 1000 * 60 * 1),
				ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, el, "tls"), false), ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, el, "ssl"), false),
				ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, el, "reuseConnection"), true), ServerImpl.TYPE_GLOBAL);
	}

	@Override
	public Struct schema(Prop<Server> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// 1. Connection Details
		properties.setEL(KeyImpl.init("smtp"), PropFactory.createSimple("string", "The SMTP server address (hostname or IP)."));
		properties.setEL(KeyConstants._port, PropFactory.createSimple("integer", "The SMTP server port (default 25)."));

		// 2. Authentication
		properties.setEL(KeyConstants._username, PropFactory.createSimple("string", "The SMTP username."));
		properties.setEL(KeyConstants._password, PropFactory.createSimple("string", "The SMTP password (can be encrypted)."));

		// 3. Security Flags
		properties.setEL(KeyImpl.init("tls"), PropFactory.createSimple("boolean", "Use TLS for the connection."));
		properties.setEL(KeyImpl.init("ssl"), PropFactory.createSimple("boolean", "Use SSL for the connection."));

		// 4. Connection Management (Pooling)
		properties.setEL(KeyImpl.init("life"), PropFactory.createSimple("integer", "The maximum life of a connection in milliseconds."));
		properties.setEL(KeyImpl.init("idle"), PropFactory.createSimple("integer", "The maximum idle time for a connection in milliseconds."));
		properties.setEL(KeyImpl.init("reuseConnection"), PropFactory.createSimple("boolean", "Whether to reuse connections. Default is true."));

		// 5. Identification
		properties.setEL(KeyConstants._id, PropFactory.createSimple("integer", "The unique ID for this mail server."));

		return sct;
	}

	@Override
	public Object resolvedValue(Server value) {
		return value;
	}

}
