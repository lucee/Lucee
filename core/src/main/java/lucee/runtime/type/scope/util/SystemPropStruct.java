package lucee.runtime.type.scope.util;

import java.util.Iterator;
import java.util.Set;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.StructSupport;

public class SystemPropStruct extends AbsSystemStruct {

	private static SystemPropStruct instance = new SystemPropStruct();

	@Override
	public int size() {
		return System.getProperties().size();
	}

	@Override
	public void clear() {
		System.getProperties().clear();
	}

	@Override
	public Object removeEL(Key key) {
		Object k = getKey(key);
		if (k != null) return System.getProperties().remove(key);
		return null;
	}

	@Override
	public final Object get(Key key) throws PageException {
		return get((PageContext) null, key);
	}

	@Override
	public final Object get(PageContext pc, Key key) throws PageException {
		Iterator<Entry<Object, Object>> it = System.getProperties().entrySet().iterator();
		Entry<Object, Object> e;
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
	public final Object get(PageContext pc, Key key, Object defaultValue) {
		Iterator<Entry<Object, Object>> it = System.getProperties().entrySet().iterator();
		Entry<Object, Object> e;
		if (key == null) return defaultValue;
		while (it.hasNext()) {
			e = it.next();
			if (key.equals(e.getKey())) return e.getValue();
		}
		return defaultValue;
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		Object k = getKey(key);
		if (k == null) return System.setProperty(key.getString(), Caster.toString(value));
		return System.setProperty(Caster.toString(k), Caster.toString(value));
	}

	@Override
	public Object setEL(Key key, Object value) {
		Object k = getKey(key);
		if (k == null) return System.setProperty(key.getString(), Caster.toString(value, value.toString()));
		return System.setProperty(k.toString(), Caster.toString(value, value.toString()));
	}

	@Override
	public final boolean containsKey(Key key) {
		return getKey(key) != null;
	}

	@Override
	public final boolean containsKey(PageContext pc, Key key) {
		return getKey(key) != null;
	}

	private Object getKey(Key key) {
		Iterator<Object> it = System.getProperties().keySet().iterator();
		Object k;
		while (it.hasNext()) {
			if (key.equals(k = it.next())) return k;
		}
		return null;
	}

	@Override
	public Key[] keys() {
		Set<Object> set = System.getProperties().keySet();
		Iterator<Object> it = set.iterator();
		Key[] keys = new Key[set.size()];
		int index = 0;
		Object k;
		while (it.hasNext()) {
			k = it.next();
			keys[index++] = KeyImpl.toKey(k, KeyImpl.init(k.toString()));
		}
		return keys;
	}

	public static SystemPropStruct getInstance() {
		return instance;
	}

	@Override
	public int getType() {
		return Struct.TYPE_REGULAR;
	}
}