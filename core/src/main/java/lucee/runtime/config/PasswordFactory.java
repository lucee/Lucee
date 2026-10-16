package lucee.runtime.config;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class PasswordFactory implements PropFactory<Password> {

	private static PasswordFactory instance;

	private PasswordFactory() {}

	public static PasswordFactory getInstance() {
		if (instance == null) {
			instance = new PasswordFactory();
		}
		return instance;
	}

	@Override
	public Password evaluate(Config config, String name, Object val, short source) throws PageException {
		return PasswordImpl.read(config, name, Caster.toString(val), ((ConfigPro) config).getSalt());
	}

	@Override
	public Struct schema(Prop<Password> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "string");
		sct.setEL(KeyConstants._description, "The administrator password. Supports plain text (auto-encrypted), hashed, or hashed+salted.");

		return sct;
	}

	@Override
	public Object resolvedValue(Password value) {
		return value;
	}
}
