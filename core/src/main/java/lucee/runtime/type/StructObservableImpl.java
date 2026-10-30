package lucee.runtime.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;

/**
 * Struct that calls a listener function (onChange) after a key was set, updated or removed. Only the
 * mutators are overridden, so all other struct types are not affected.
 */
public final class StructObservableImpl extends StructImpl {

	private static final long serialVersionUID = 3417209385571946052L;

	private final UDF udf;
	private final Object lock = new Object();

	public StructObservableImpl(UDF udf) {
		super(TYPE_SYNC);
		this.udf = udf;
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		Object old;
		synchronized (lock) {
			old = super.g(key, NULL);
			super.set(key, value);
		}
		onChange(key, old == NULL ? null : old, value);
		return value;
	}

	@Override
	public Object setEL(Key key, Object value) {
		Object old;
		synchronized (lock) {
			old = super.g(key, NULL);
			super.setEL(key, value);
		}
		try {
			onChange(key, old == NULL ? null : old, value);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
		return value;
	}

	@Override
	public Object remove(Key key) throws PageException {
		Object old;
		synchronized (lock) {
			old = super.g(key, NULL);
			if (old == NULL) return super.remove(key); // throws the regular "key does not exist" exception
			super.removeEL(key);
		}
		onChange(key, old, null);
		if (old != null || NullSupportHelper.full()) return old;
		throw new ExpressionException("can't remove key [" + key + "] from struct, key value is [null] what is equal do not existing in case full null support is not enabled");
	}

	@Override
	public Object removeEL(Key key) {
		Object old;
		synchronized (lock) {
			old = super.g(key, NULL);
			if (old == NULL) return null;
			super.removeEL(key);
		}
		try {
			onChange(key, old, null);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
		return old;
	}

	@Override
	public Object remove(Key key, Object defaultValue) {
		Object old;
		synchronized (lock) {
			old = super.g(key, NULL);
			if (old == NULL) return defaultValue;
			super.removeEL(key);
		}
		try {
			onChange(key, old, null);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
		if (old != null || NullSupportHelper.full()) return old;
		return defaultValue;
	}

	@Override
	public void clear() {
		List<Entry<Key, Object>> removed;
		synchronized (lock) {
			removed = new ArrayList<Entry<Key, Object>>(size());
			Iterator<Entry<Key, Object>> it = super.entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				removed.add(new java.util.AbstractMap.SimpleEntry<Key, Object>(e.getKey(), e.getValue()));
			}
			super.clear();
		}
		try {
			for (Entry<Key, Object> e: removed) {
				onChange(e.getKey(), e.getValue(), null);
			}
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		DumpTable dt = (DumpTable) super.toDumpData(pageContext, maxlevel, properties);
		dt.setComment("this struct has a onChange listener defined");
		return dt;
	}

	private void onChange(Key key, Object oldValue, Object newValue) throws PageException {
		udf.call(ThreadLocalPageContext.get(), new Object[] { key, oldValue, newValue, this }, true);
	}
}
