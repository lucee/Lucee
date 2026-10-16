package lucee.runtime.ai;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleException;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public final class AIEngineFactory {

	private static final Map<String, SoftReference<AIEngine>> instances = new ConcurrentHashMap<>();

	public static String createId(ClassDefinition<? extends AIEngine> cd, Struct properties, String name, String _default) {
		Key[] keys = properties.keys();
		Arrays.sort(keys, new Comparator<Key>() {
			@Override
			public int compare(Key k1, Key k2) {
				return k1.getUpperString().compareTo(k2.getUpperString());
			}
		});

		StringBuilder sb = new StringBuilder()

				.append(name).append(';')

				.append(_default).append(';')

				.append(cd.toString()).append(';');

		for (Key k: keys) {
			sb.append(k).append(':').append(properties.get(k, "")).append(';');
		}
		return HashUtil.create64BitHashAsString(sb.toString());
	}

	public static AIEngine getInstance(Config config, String name, Struct data) throws PageException, ClassException, BundleException {

		ClassDefinition<AIEngine> cd;

		cd = ConfigFactoryImpl.getClassDefinition(config, data, "", config.getIdentification());
		if (cd.hasClass()) {

			Struct custom = Caster.toStruct(data.get(KeyConstants._custom, null), null);
			if (custom == null) custom = Caster.toStruct(data.get(KeyConstants._properties, null), null);
			if (custom == null) custom = Caster.toStruct(data.get(KeyConstants._arguments, null), null);
			String _default = Caster.toString(data.get(KeyConstants._default, null), null);
			return getInstance(config, cd, custom, name, _default);
		}
		throw new ApplicationException("class defintion is invalid");
	}

	public static AIEngine getInstance(Config config, ClassDefinition<? extends AIEngine> cd, Struct properties, String name, String _default)
			throws PageException, ClassException, BundleException {
		String id = createId(cd, properties, name, _default);
		AIEngine aie = getExistingInstance(id, null);
		if (aie != null) return aie;

		aie = (AIEngine) ClassUtil.loadInstance(cd.getClazz());
		LogUtil.logx(config, Log.LEVEL_TRACE, "ai-factory", "create AI instance [" + cd.toString() + "]", "ai", "application");
		aie.init(cd, properties, name, _default, id);
		instances.put(id, new SoftReference<AIEngine>(aie));
		return aie;
	}

	public static AIEngine getExistingInstance(String id, AIEngine defaultValue) {
		AIEngine aie;
		SoftReference<AIEngine> ref = instances.get(id);
		if (ref != null) {
			aie = ref.get();
			if (aie != null) return aie;
		}

		return defaultValue;
	}
}
