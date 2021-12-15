package lucee.runtime.type.scope.util;

import java.util.Iterator;
import java.util.Set;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;

public class EnvStruct extends AbsSystemStruct {

	private static EnvStruct instance = new EnvStruct();

	@Override
	public int size() {
		return System.getenv().size();
	}

	@Override
	public void clear() {
		System.getenv().clear();
	}

	@Override
	public Object removeEL(Key key) {
		String k = getKey(key);
		if (k != null) return System.getenv().remove(key);
		return null;
	}

	@Override
	public final Object get(Key key) throws PageException {
		return get((PageContext) null, key);
	}

	@Override
	public final Object get(PageContext pc, Key key) throws PageException {
		Iterator<Entry<String, String>> it = System.getenv().entrySet().iterator();
		Entry<String, String> e;
		if (key == null) throw StructSupport.invalidKey(null, this, key, null);
		while (it.hasNext()) {
			e = it.next();
			if (key.equals(e.getKey())) return e.getValue();
		}
		throw StructSupport.invalidKey(null, this, key, null);
	}

	@Override
	public final Object get(Key key, Object defaultValue) {
		return get((PageContext) null, key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		Iterator<Entry<String, String>> it = System.getenv().entrySet().iterator();
		Entry<String, String> e;
		if (key == null) return defaultValue;
		while (it.hasNext()) {
			e = it.next();
			if (key.equals(e.getKey())) return e.getValue();
		}
		return defaultValue;
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		String k = getKey(key);
		if (k == null) return System.getenv().put(key.getString(), Caster.toString(value));
		return System.getenv().put(k, Caster.toString(value));
	}

	@Override
	public Object setEL(Key key, Object value) {
		String k = getKey(key);
		if (k == null) return System.getenv().put(key.getString(), Caster.toString(value, value.toString()));
		return System.getenv().put(k, Caster.toString(value, value.toString()));
	}

	@Override
	public final boolean containsKey(Key key) {
		return getKey(key) != null;
	}

	@Override
	public final boolean containsKey(PageContext pc, Key key) {
		return getKey(key) != null;
	}

	private String getKey(Key key) {
		Iterator<String> it = System.getenv().keySet().iterator();
		String k;
		while (it.hasNext()) {
			if (key.equals(k = it.next())) return k;
		}
		return null;
	}

	@Override
	public Key[] keys() {
		Set<String> set = System.getenv().keySet();
		Iterator<String> it = set.iterator();
		Key[] keys = new Key[set.size()];
		int index = 0;
		String k;
		while (it.hasNext()) {
			k = it.next();
			keys[index++] = KeyImpl.toKey(k, KeyImpl.init(k));
		}
		return keys;
	}

	public static EnvStruct getInstance() {
		return instance;
	}

	@Override
	public int getType() {
		return StructUtil.getType(System.getenv());
	}
}