package lucee.runtime.type;

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

// FUTURE move to Array
public interface ArrayPro extends Array {
	public abstract Iterator<Entry<Integer, Object>> entryArrayIterator();

	public Object pop() throws PageException;

	public Object pop(Object defaultValue);

	public Object shift() throws PageException;

	public Object shift(Object defaultValue);

	public abstract Object getE(PageContext pc, int key) throws PageException;

	public Object setE(PageContext pc, int key, Object value) throws PageException;

	public Object get(PageContext pc, int key, Object defaultValue);

}
