package lucee.runtime.rest;

import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
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

public class MappingFactory implements PropFactory<Mapping> {

	private static MappingFactory instance;

	public static MappingFactory getInstance() {
		if (instance == null) {
			instance = new MappingFactory();
		}
		return instance;
	}

	@Override
	public Mapping evaluate(Config config, String name, Object val, short source) throws PageException {
		try {
			Struct el = Caster.toStruct(val);

			String physical = ConfigFactoryImpl.getAttr(config, el, "physical");
			String virtual = ConfigFactoryImpl.getAttr(config, el, "virtual");
			boolean readonly = ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, el, "readonly"), false);
			boolean hidden = ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, el, "hidden"), false);
			boolean _default = ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, el, "default"), false);
			if (physical != null) {
				return new lucee.runtime.rest.Mapping(config, virtual, physical, hidden, readonly, _default);
			}
			throw new ApplicationException("attribute [physical] is required");

		}
		catch (Exception ex) {
			throw Caster.toPageException(ex);
		}
	}

	@Override
	public Struct schema(Prop<Mapping> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");
		sct.setEL(KeyConstants._description, "Defines a REST mapping which links a virtual URI path to a physical directory containing REST-enabled CFCs.");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// 1. Virtual Path
		properties.setEL(KeyImpl.init("virtual"),
				PropFactory.createSimple("string", "The virtual path used after the '/rest/' prefix (e.g., if virtual is 'app/v1', the URL is '/rest/app/v1')."));

		// 2. Physical Path
		properties.setEL(KeyImpl.init("physical"),
				PropFactory.createSimple("string", "The absolute or relative physical path to the directory containing the REST component files."));

		// 3. Readonly
		properties.setEL(KeyConstants._readOnly, PropFactory.createSimple("boolean", "If true, this mapping cannot be modified via the Lucee Administrator."));

		// 4. Hidden
		properties.setEL(KeyImpl.init("hidden"), PropFactory.createSimple("boolean", "If true, this mapping will not be displayed in the Lucee Administrator mapping list."));

		// 5. Default
		properties.setEL(KeyConstants._default, PropFactory.createSimple("boolean", "If true, this mapping becomes the primary handler for the '/rest/' root. "
				+ "Requests to '/rest/' will automatically route to this mapping without needing the virtual path in the URL."));

		// Required fields
		Array required = new ArrayImpl();
		required.appendEL("physical");
		sct.setEL(KeyImpl.init("required"), required);

		return sct;
	}

	@Override
	public Object resolvedValue(Mapping defaultValue) {
		return defaultValue;
	}

}
