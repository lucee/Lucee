package lucee.runtime.type;

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.runtime.exp.PageException;

// FUTURE move to Array
public interface ArrayPro extends Array {
	public abstract Iterator<Entry<Integer, Object>> entryArrayIterator();

	public Object pop() throws PageException;

	public Object pop(Object defaultValue);

	public Object shift() throws PageException;

	public Object shift(Object defaultValue);

}
