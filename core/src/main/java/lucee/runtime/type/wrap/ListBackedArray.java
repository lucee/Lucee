package lucee.runtime.type.wrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.util.ListIteratorImpl;

/**
 * a CFML array view over a user supplied java.util.List
 */
public final class ListBackedArray extends ListAsArray {

	public ListBackedArray(List list) {
		super(list);
	}

	@Override
	public Object append(Object o) throws PageException {
		list.add(o);
		return o;
	}

	@Override
	public Object appendEL(Object o) {
		list.add(o);
		return o;
	}

	@Override
	public Object get(PageContext pc, int key, Object defaultValue) {
		if (key <= 0) return defaultValue;
		if (key > list.size()) return defaultValue;

		try {
			Object rtn = list.get(key - 1);
			if (rtn == null) {
				if (NullSupportHelper.full(pc)) {
					return null;
				}
				return defaultValue;
			}
			return rtn;
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public Object getE(PageContext pc, int key) throws PageException {
		if (key <= 0) {
			Integer idx = list.size() + key < 0 ? -1 : list.size() + key;
			if (idx < 0 || key == 0) {
				throw new ExpressionException("Array index [" + key + "] out of range, array size is [" + list.size() + "]");
			}
			Object rtn = list.get(idx);
			return rtn;
		}
		if (key > list.size()) throw new ExpressionException("Array index [" + key + "] out of range, array size is [" + list.size() + "]");
		Object rtn = list.get(key - 1);
		if (rtn == null) {
			if (NullSupportHelper.full(pc)) {
				return null;
			}
			throw new ExpressionException("Element at position [" + key + "] does not exist in list");
		}
		return rtn;
	}

	@Override
	public boolean insert(int key, Object value) throws PageException {
		try {
			list.add(key - 1, value);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new ExpressionException("can't insert value to array at position " + key + ", array goes from 1 to " + size());
		}
		return true;
	}

	@Override
	public int[] intKeys() {
		ListIterator lit = list.listIterator();
		ArrayList keys = new ArrayList();
		int index = 0;
		Object v;
		while (lit.hasNext()) {
			index = lit.nextIndex() + 1;
			v = lit.next();
			if (v != null) keys.add(Integer.valueOf(index));
		}
		int[] intKeys = new int[keys.size()];
		Iterator it = keys.iterator();
		index = 0;
		while (it.hasNext()) {
			intKeys[index++] = ((Integer) it.next()).intValue();
		}

		return intKeys;
	}

	@Override
	public Object prepend(Object o) throws PageException {
		list.add(0, o);
		return o;
	}

	@Override
	public Object removeE(int key) throws PageException {
		try {
			return list.remove(key - 1);
		}
		catch (Exception e) {
			ExpressionException ee = new ExpressionException("can not remove Element at position [" + key + "]", e.getMessage());
			ee.setStackTrace(e.getStackTrace());
			throw ee;
		}
	}

	@Override
	public Object pop() throws PageException {
		if (size() == 0) throw new ExpressionException("can not pop Element from array, array is empty");
		try {
			return list.remove(size() - 1);
		}
		catch (Exception e) {
			ExpressionException ee = new ExpressionException("can not pop Element from array", e.getMessage());
			ee.setStackTrace(e.getStackTrace());
			throw ee;
		}
	}

	@Override
	public Object pop(Object defaultValue) {
		if (size() == 0) return defaultValue;
		try {
			return list.remove(size() - 1);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public Object shift() throws PageException {
		if (size() == 0) throw new ExpressionException("can not pop Element from array, array is empty");
		try {
			return list.remove(0);
		}
		catch (Exception e) {
			ExpressionException ee = new ExpressionException("can not pop Element from array", e.getMessage());
			ee.setStackTrace(e.getStackTrace());
			throw ee;
		}
	}

	@Override
	public Object shift(Object defaultValue) {
		if (size() == 0) return defaultValue;
		try {
			return list.remove(0);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public void resize(int to) throws PageException {
		while (size() < to)
			list.add(null);
	}

	@Override
	public Object setE(PageContext pc, int key, Object value) throws PageException {
		if (key <= size()) {
			try {
				list.set(key - 1, value);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				throw new ExpressionException("can not set Element at position [" + key + "]", t.getMessage());
			}
		}
		else {
			while (size() < key - 1)
				list.add(null);
			list.add(value);
		}
		return value;
	}

	@Override
	public Object setEL(int key, Object value) {
		if (key <= size()) {
			try {
				list.set(key - 1, value);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				return value;
			}

		}
		else {
			while (size() < key - 1)
				list.add(null);
			list.add(value);
		}
		return value;
	}

	@Override
	public void sortIt(Comparator comp) {
		if (getDimension() > 1) throw new PageRuntimeException("only 1 dimensional arrays can be sorted");
		Collections.sort(list, comp);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public ArrayList toArrayList() {
		return new ArrayList(list);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new ListBackedArray((List) Duplicator.duplicate(list, deepCopy));
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return DumpUtil.toDumpData(list, pageContext, maxlevel, dp);
	}

	@Override
	public Iterator iterator() {
		return list.iterator();
	}

	@Override
	public boolean add(Object o) {
		return list.add(o);
	}

	@Override
	public boolean addAll(int index, java.util.Collection c) {
		return list.addAll(index, c);
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public boolean containsAll(java.util.Collection c) {
		return list.containsAll(c);
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public Iterator<Object> valueIterator() {
		return new ListIteratorImpl(list, 0);
		// return list.iterator();
	}

	@Override
	public Iterator<Object> getIterator() {
		return new ListIteratorImpl(list, 0);
		// return list.iterator();
	}
}
