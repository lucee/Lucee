package lucee.commons.io.res;

import java.util.Map;

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
import lucee.transformer.library.ClassDefinitionImpl;

public class ResourceProviderDefFactory implements PropFactory<ResourceProviderDef> {

	private static ResourceProviderDefFactory instanceFalse;
	private static ResourceProviderDefFactory instanceTrue;
	private final boolean schemeRequired;

	public ResourceProviderDefFactory(boolean schemeRequired) {
		this.schemeRequired = schemeRequired;
	}

	public static ResourceProviderDefFactory getInstance(boolean schemeRequired) {

		if (schemeRequired) {
			if (instanceTrue == null) {
				instanceTrue = new ResourceProviderDefFactory(schemeRequired);
			}
			return instanceTrue;
		}

		if (instanceFalse == null) {
			instanceFalse = new ResourceProviderDefFactory(false);
		}
		return instanceFalse;
	}

	@Override
	public ResourceProviderDef evaluate(Config config, String name, Object val, short source) throws PageException {
		try {
			// can be an array with a single entry
			Array arr = Caster.toArray(val, null);
			if (arr != null) {
				if (arr.size() != 1) throw new ApplicationException("only an array with a single item is allowed");
				val = arr.getE(1);
			}

			Struct defaultProvider = Caster.toStruct(val);

			ClassDefinition cd = ConfigFactoryImpl.getClassDefinition(config, defaultProvider, "", config.getIdentification());

			String scheme = ConfigFactoryImpl.getAttr(config, defaultProvider, "scheme");
			if (schemeRequired && StringUtil.isEmpty(scheme)) {
				throw new ApplicationException("scheme is required");
			}
			// no class defined: let the engine inject the built-in default provider for this scheme
			// (e.g. a declared "s3" entry without a class falls back to DummyS3ResourceProvider)
			if (!cd.hasClass()) {
				return null;
			}
			if ("lucee.commons.io.res.type.ftp.FTPResourceProvider".equals(cd.getClassName())) {
				cd = new ClassDefinitionImpl("org.lucee.extension.ftp.vfs.FTPResourceProvider", "org.lucee:ftp");
			}

			Map<String, String> args = ConfigFactoryImpl.toArguments(defaultProvider, "arguments", true, false);

			return new ResourceProviderDef(scheme, cd, args);

		}
		catch (Exception ex) {
			throw Caster.toPageException(ex);
		}
	}

	@Override
	public Struct schema(Prop<ResourceProviderDef> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");
		sct.setEL(KeyConstants._description, "Registers a Virtual File System (VFS) provider to handle specific resource schemes.");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// Scheme (e.g., s3, ftp, dropbox)
		Struct scheme = new StructImpl(Struct.TYPE_LINKED);
		scheme.setEL(KeyConstants._type, "string");
		scheme.setEL(KeyConstants._description, "The URI scheme this provider handles (e.g., 's3' for s3:// paths).");
		properties.setEL("scheme", scheme);

		// Arguments (can be a Struct or a semicolon-separated String)
		Struct arguments = new StructImpl(Struct.TYPE_LINKED);
		arguments.setEL(KeyConstants._type, "object");
		arguments.setEL(KeyConstants._description,
				"Configuration parameters for the provider (e.g., access keys, hostnames, or timeouts). Supports a Struct or a 'key:value;key:value' string.");
		properties.setEL("arguments", arguments);

		// Class Definition properties (bundleName, className, etc.)
		PropFactory.appendClassDefinitionProps(properties, "");

		// Required fields
		Array required = new ArrayImpl();
		if (schemeRequired) {
			required.appendEL("scheme");
		}
		required.appendEL("class");
		sct.setEL(KeyImpl.init("required"), required);

		return sct;
	}

	@Override
	public Object resolvedValue(ResourceProviderDef value) {
		return value;
	}

}
