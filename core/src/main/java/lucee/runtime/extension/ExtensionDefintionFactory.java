package lucee.runtime.extension;

import java.util.Map;

import lucee.runtime.config.Config;
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

public class ExtensionDefintionFactory implements PropFactory<ExtensionDefintion> {

	private static ExtensionDefintionFactory instance;

	public static ExtensionDefintionFactory getInstance() {
		if (instance == null) {
			instance = new ExtensionDefintionFactory();
		}
		return instance;
	}

	@Override
	public ExtensionDefintion evaluate(Config config, String name, Object val, short source) throws PageException {

		try {
			Struct childSct = Caster.toStruct(val);

			Map<String, String> child = Caster.toStringMap(childSct);
			// MUST toExtensionDefinition nned to throw a proper exception
			ExtensionDefintion ed = RHExtension.toExtensionDefinition(config, child);
			if (ed != null) return ed;
			throw new ApplicationException("unable to load extension defintion");

		}
		catch (Exception ex) {
			throw Caster.toPageException(ex);
		}
	}

	@Override
	public Struct schema(Prop<ExtensionDefintion> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");
		sct.setEL(KeyConstants._description, "Defines a Lucee Extension (LEX). You must provide either a unique 'id' or a valid 'resource/path/url' for auto-discovery.");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// Identification (Optional if path is provided)
		addProp(properties, "id", "string", "The unique identifier (UUID) of the extension. Optional if a resource path is provided.");
		addProp(properties, "version", "string", "The specific version of the extension. If omitted with a path, the version is extracted from the .lex header.");

		// Unified Resource Provider Keys
		String resourceDesc = "The location of the .lex file via Lucee's VFS (local, http, s3, etc.).";
		addProp(properties, "resource", "string", resourceDesc);
		addProp(properties, "path", "string", "Alias for 'resource'.");
		addProp(properties, "url", "string", "Alias for 'resource'.");

		// Validation Logic: Require ID OR a Resource path
		// We represent this in the schema so the UI/Validator knows at least one is needed
		Array anyOf = new ArrayImpl();

		Struct optionId = new StructImpl();
		Array reqId = new ArrayImpl();
		reqId.appendEL("id");
		optionId.setEL("required", reqId);

		Struct optionRes = new StructImpl();
		Array reqRes = new ArrayImpl(); // In Lucee 7.2, we accept any of the 3 aliases
		reqRes.appendEL("path"); // Or resource/url depending on how strict you want the schema
		optionRes.setEL("required", reqRes);

		anyOf.appendEL(optionId);
		anyOf.appendEL(optionRes);

		// In a standard Lucee schema struct, we'd use the "anyOf" key
		sct.setEL(KeyImpl.init("anyOf"), anyOf);

		return sct;
	}

	@Override
	public Object resolvedValue(ExtensionDefintion value) {
		return value;
	}

	private void addProp(Struct properties, String name, String type, String desc) {
		Struct p = new StructImpl(Struct.TYPE_LINKED);
		p.setEL(KeyConstants._type, type);
		p.setEL(KeyConstants._description, desc);
		properties.setEL(name, p);
	}

}
