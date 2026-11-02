package lucee.runtime.ai;

import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;

public final class AIEnginePool {

	private Map<String, AIEngine> instances = new ConcurrentHashMap<>();

	// engines created from a connection struct passed instead of a name, the key is a hash of the struct
	private final Map<String, SoftReference<AIEngine>> dynamicInstances = new ConcurrentHashMap<>();

	public AIEngine getEngine(Config config, String name) throws PageException {
		// get existing instance
		AIEngine aie = instances.get(name);
		if (aie != null) return aie;

		// loading new instance
		aie = ((ConfigPro) config).getAIEngine(name.toLowerCase());
		if (aie == null) {
			throw new ApplicationException(ExceptionUtil.similarKeyMessage(((ConfigPro) config).getAIEngineNames(), name, "source", "sources", "ai pool", true));
		}
		instances.put(name, aie);
		return aie;
	}

	/**
	 * returns the engine for a connection defined by a struct (the same as the value of a connection in
	 * this.ai in the Application.cfc), an engine is only created once for the same definition, no matter
	 * how often or from where the struct is passed.
	 * 
	 * @param config config used to create the engine
	 * @param connection the connection definition
	 * @return the engine
	 * @throws PageException if the definition is invalid or the engine cannot be created
	 */
	public AIEngine getEngine(Config config, Struct connection) throws PageException {
		if (connection == null || connection.isEmpty()) throw new ApplicationException("the AI connection struct is empty");

		Struct data = (Struct) ConfigUtil.replacePlaceHolders(config, AIEngineFactory.toConnection(connection));
		String id = toHash(data);

		AIEngine aie = get(id);
		if (aie != null) return aie;

		final PageException[] failure = new PageException[1];
		final AIEngine[] result = new AIEngine[1];
		dynamicInstances.compute(id, (key, ref) -> {
			AIEngine existing = ref == null ? null : ref.get();
			if (existing != null) {
				result[0] = existing;
				return ref;
			}
			try {
				result[0] = AIEngineFactory.getInstance(config, "dynamic-" + id, data);
				return new SoftReference<AIEngine>(result[0]);
			}
			catch (Exception e) {
				failure[0] = Caster.toPageException(e);
				return null;
			}
		});
		if (failure[0] != null) throw failure[0];
		removeCleared();
		return result[0];
	}

	private AIEngine get(String id) {
		SoftReference<AIEngine> ref = dynamicInstances.get(id);
		return ref == null ? null : ref.get();
	}

	private void removeCleared() {
		Iterator<Map.Entry<String, SoftReference<AIEngine>>> it = dynamicInstances.entrySet().iterator();
		while (it.hasNext()) {
			if (it.next().getValue().get() == null) it.remove();
		}
	}

	public void flushEngine(String name) {
		instances.remove(name);
	}

	private static String toHash(Struct data) throws PageException {
		try {
			StringBuilder sb = new StringBuilder();
			append(sb, data);
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(sb.toString().getBytes("UTF-8"));
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte b: digest) {
				hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
			}
			return hex.toString();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	// serializes a definition in a way that does not depend on the order or the case of the keys
	private static void append(StringBuilder sb, Object value) {
		if (value instanceof Struct) {
			Struct sct = (Struct) value;
			Key[] keys = sct.keys();
			Arrays.sort(keys, new Comparator<Key>() {
				@Override
				public int compare(Key k1, Key k2) {
					return k1.getUpperString().compareTo(k2.getUpperString());
				}
			});
			sb.append('{');
			for (Key k: keys) {
				appendString(sb, k.getUpperString());
				append(sb, sct.get(k, null));
			}
			sb.append('}');
		}
		else if (value instanceof Array) {
			Iterator<?> it = ((Array) value).valueIterator();
			sb.append('[');
			while (it.hasNext()) {
				append(sb, it.next());
			}
			sb.append(']');
		}
		else if (value == null) sb.append('N');
		else if (Decision.isSimpleValue(value)) appendString(sb, Caster.toString(value, ""));
		else appendString(sb, value.getClass().getName() + "@" + System.identityHashCode(value));
	}

	private static void appendString(StringBuilder sb, String str) {
		sb.append(str.length()).append(':').append(str).append(';');
	}
}
