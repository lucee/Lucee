package lucee.runtime.cache;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.library.ClassDefinitionImpl;

public class CacheConnectionFactory implements PropFactory<CacheConnection> {
	private static CacheConnectionFactory instance;

	public static CacheConnectionFactory getInstance() {
		if (instance == null) {
			instance = new CacheConnectionFactory();
		}
		return instance;
	}

	@Override
	public CacheConnection evaluate(Config c, String name, Object val, short source) throws PageException {
		ConfigPro config = (ConfigPro) c;
		Struct data = Caster.toStruct(val);
		ClassDefinition cd;
		CacheConnection cc;

		try {

			cd = ConfigFactoryImpl.getClassDefinition(config, data, "", config.getIdentification());
			if (!cd.isBundle() && !((ClassDefinitionImpl) cd).isMaven()) {
				ClassDefinition _cd = config.getCacheDefinition(cd.getClassName());
				if (_cd != null) cd = _cd;
			}

			Struct custom = ConfigUtil.getAsStruct(config, data, true, "custom");
			// Workaround for old EHCache class definitions
			if (cd.getClassName() != null && cd.getClassName().endsWith(".EHCacheLite")) {
				cd = new ClassDefinitionImpl("org.lucee.extension.cache.eh.EHCache");
				if (!custom.containsKey("distributed")) custom.setEL("distributed", "off");
				if (!custom.containsKey("asynchronousReplicationIntervalMillis")) custom.setEL("asynchronousReplicationIntervalMillis", "1000");
				if (!custom.containsKey("maximumChunkSizeBytes")) custom.setEL("maximumChunkSizeBytes", "5000000");

			}
			else if (cd.getClassName() != null && (cd.getClassName().endsWith(".extension.io.cache.eh.EHCache") || cd.getClassName().endsWith("lucee.runtime.cache.eh.EHCache"))) {
				cd = new ClassDefinitionImpl("org.lucee.extension.cache.eh.EHCache");
			}
			return new CacheConnectionImpl(config, name, cd, custom, Caster.toBooleanValue(ConfigFactoryImpl.getAttr(config, data, "readOnly"), false),
					Caster.toBooleanValue(ConfigFactoryImpl.getAttr(config, data, "storage"), false));

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			ConfigFactoryImpl.log(config, t);
			throw Caster.toPageException(t);
		}
	}

	@Override
	public Struct schema(Prop<CacheConnection> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// Use our new helper to add all ClassDefinition fields
		PropFactory.appendClassDefinitionProps(properties);

		// Standard Cache-specific properties
		properties.setEL(KeyConstants._readOnly, PropFactory.createSimple("boolean", "Prevents modification of the cache configuration."));
		properties.setEL(KeyConstants._storage, PropFactory.createSimple("boolean", "Allows Lucee to use this cache for Session/Client storage."));

		// The custom block for driver-specific settings
		Struct custom = new StructImpl(Struct.TYPE_LINKED);
		custom.setEL(KeyConstants._type, "object");
		custom.setEL("additionalProperties", true); // Allows any key-value pairs
		properties.setEL("custom", custom);

		return sct;
	}

	@Override
	public Object resolvedValue(CacheConnection value) {
		return value;
	}

}
