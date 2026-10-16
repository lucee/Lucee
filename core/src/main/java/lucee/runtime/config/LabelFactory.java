package lucee.runtime.config;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class LabelFactory implements PropFactory<LabelFactory.Label> {

	public static final class Label {

		public final String id;
		public final String name;

		public Label(String id, String name) {
			this.id = id;
			this.name = name;
		}

	}

	private static LabelFactory instance;

	public static LabelFactory getInstance() {
		if (instance == null) {
			instance = new LabelFactory();
		}
		return instance;
	}

	@Override
	public Label evaluate(Config config, String name, Object val, short source) throws PageException {

		Struct data = Caster.toStruct(val);

		String id = ConfigUtil.getAsString("id", data, null);
		String _name = ConfigUtil.getAsString("name", data, null);
		if (id != null && _name != null) {
			return new Label(id, name);
		}
		throw new ApplicationException("attribute [id] and [name] are required");
	}

	@Override
	public Struct schema(Prop<Label> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");
		sct.setEL(KeyConstants._description, "Defines a human-readable label for a specific Lucee Web Context.");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// ID property (The MD5 Hash)
		Struct id = new StructImpl(Struct.TYPE_LINKED);
		id.setEL(KeyConstants._type, "string");
		id.setEL(KeyConstants._description,
				"The MD5 hash of the web context root path. " + "Generate using: lucee.commons.io.SystemUtil::hash(getPageContext().getConfig().getServletContext())");
		properties.setEL(KeyConstants._id, id);

		// Name property (The Label)
		Struct labelName = new StructImpl(Struct.TYPE_LINKED);
		labelName.setEL(KeyConstants._type, "string");
		labelName.setEL(KeyConstants._description, "The human-readable name for the context, used in Admin and as {web-context-label} placeholder.");
		properties.setEL(KeyConstants._name, labelName);

		// Required fields
		Array required = new ArrayImpl();
		required.appendEL("id");
		required.appendEL("name");
		sct.setEL(KeyImpl.init("required"), required);

		return sct;
	}

	@Override
	public Object resolvedValue(Label value) {
		return value;
	}
}
