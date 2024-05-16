package lucee.runtime.type.wrap;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ArraySupport;

public class StructAsArray extends ArraySupport implements Array, List {

	private Struct sct;

	private StructAsArray(Struct sct) {
		this.sct = sct;
	}

	public static Array toArray(Struct sct) throws ExpressionException {
		if (sct instanceof Array) return (Array) sct;

		Iterator<Key> it = sct.keyIterator();
		Key k;
		while (it.hasNext()) {
			k = it.next();
			if (!Decision.isInteger(k.getString())) throw new ExpressionException("can't cast struct to an array, key [" + k.getString() + "] is not a number");
		}
		return new StructAsArray(sct);
	}

	public static Array toArray(Struct sct, Array defaultValue) {
		if (sct instanceof Array) return (Array) sct;

		Iterator<Key> it = sct.keyIterator();
		Key k;
		while (it.hasNext()) {
			k = it.next();
			if (!Decision.isInteger(k.getString())) return defaultValue;
		}
		return new StructAsArray(sct);
	}

	@Override
	public Collection duplicate(boolean dc) {
		return new StructAsArray((Struct) sct.duplicate(dc));
	}

	@Override
	public Object get(String k) throws PageException {
		return sct.get(k);
	}

	@Override
	public final Object get(Key k) throws PageException {
		return sct.get(k);
	}

	@Override
	public final Object get(PageContext pc, Key k) throws PageException {
		return sct.get(pc, k);
	}

	@Override
	public Object get(String k, Object defaultValue) {
		return sct.get(k, defaultValue);
	}

	@Override
	public final Object get(Key k, Object defaultValue) {
		return sct.get(k, defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Key k, Object defaultValue) {
		return sct.get(pc, k, defaultValue);
	}

	@Override
	public Key[] keys() {
		return sct.keys();
	}

	@Override
	public Object remove(Key k) throws PageException {
		return sct.remove(k);
	}

	@Override
	public Object removeEL(Key k) {
		return sct.removeEL(k);
	}

	@Override
	public Object set(String k, Object value) throws PageException {
		if (!Decision.isInteger(k)) throw new ExpressionException("can't cast struct to an array, key [" + k + "] is not a number");
		return sct.set(k, value);
	}

	@Override
	public Object set(Key k, Object value) throws PageException {
		if (!Decision.isInteger(k.getString())) throw new ExpressionException("can't cast struct to an array, key [" + k + "] is not a number");
		return sct.set(k, value);
	}

	@Override
	public Object setEL(String k, Object value) {
		if (Decision.isInteger(k)) return sct.setEL(k, value);
		return value;
	}

	@Override
	public Object setEL(Key k, Object value) {
		if (Decision.isInteger(k)) return sct.setEL(k, value);
		return value;
	}

	@Override
	public DumpData toDumpData(PageContext pc, int arg1, DumpProperties arg2) {
		DumpData dd = sct.toDumpData(pc, arg1, arg2);
		if (dd instanceof DumpTable) {
			DumpTable dt = (DumpTable) dd;
			dt.setTitle(dt.getTitle() + " as Array");
		}
		return dd;
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return sct.entryIterator();
	}

	@Override
	public Iterator<Key> keyIterator() {
		return sct.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return sct.keysAsStringIterator();
	}

	@Override
	public Object append(Object v) throws PageException {
		int newKey = 1;
		int[] keys = intKeys();
		for (int k: keys) {
			if (k >= newKey) newKey = k + 1;
		}
		return setE(newKey, v);
	}

	@Override
	public Object appendEL(Object v) {
		int newKey = 1;
		int[] keys = intKeys();
		for (int k: keys) {
			if (k >= newKey) newKey = k + 1;
		}
		return set(newKey, v);
	}

	@Override
	public Object get(int key, Object defaultValue) {
		return sct.get(KeyImpl.toKey(key), defaultValue);
	}

	@Override
	public int getDimension() {
		return 1;
	}

	@Override
	public Object getE(int key) throws PageException {
		return sct.get(KeyImpl.toKey(key));
	}

	@Override
	public boolean insert(int index, Object value) throws PageException {
		// TODO make a better impl
		Array arr = asTempArray();
		boolean res = arr.insert(index, value);
		storeBack(arr);
		return res;
	}

	@Override
	public int[] intKeys() {
		Iterator<Key> it = sct.keyIterator();
		int[] indexes = new int[sct.size()];
		int index = 0;
		try {
			while (it.hasNext()) {
				indexes[index++] = Caster.toIntValue(it.next().getString());
			}
		}
		catch (ExpressionException ee) {
			throw new PageRuntimeException(ee);
		}
		return indexes;
	}

	@Override
	public Object prepend(Object value) throws PageException {
		// TODO make a better impl
		Array arr = asTempArray();
		Object res = arr.prepend(value);
		storeBack(arr);
		return res;
	}

	@Override
	public Object removeE(int k) throws PageException {
		return remove(KeyImpl.toKey(k));
	}

	@Override
	public Object removeEL(int k) {
		return removeEL(KeyImpl.toKey(k));
	}

	@Override
	public Object pop() throws PageException {
		return removeE(size());
	}

	@Override
	public Object pop(Object defaultValue) {
		try {
			return removeE(size());
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public Object shift() throws PageException {
		return removeE(1);
	}

	@Override
	public Object shift(Object defaultValue) {
		try {
			return removeE(1);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public void resize(int newSize) throws PageException {
		// with structs not necessary
	}

	@Override
	public Object setE(int k, Object value) throws PageException {
		return set(KeyImpl.toKey(k), value);
	}

	@Override
	public Object setEL(int k, Object value) {
		return setEL(KeyImpl.toKey(k), value);
	}

	@Override
	public void sortIt(Comparator c) {

	}

	@Override
	public int size() {
		return sct.size();
	}

	private Array asTempArray() throws PageException {
		Array arr = new ArrayImpl();
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e = null;
		try {
			while (it.hasNext()) {
				e = it.next();
				arr.setE(Caster.toIntValue(e.getKey().getString()), e.getValue());
			}
		}
		catch (ExpressionException ee) {
			throw new ExpressionException("can't cast struct to an array, key [" + e.getKey().getString() + "] is not a number");
		}
		return arr;
	}

	private void storeBack(Array arr) {
		sct.clear();

		Iterator<Entry<Key, Object>> it = arr.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			sct.setEL(e.getKey(), e.getValue());
		}
	}

}
