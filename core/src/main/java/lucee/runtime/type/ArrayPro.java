package lucee.runtime.type;

import java.util.Iterator;
import java.util.Map.Entry;
// FUTURE move to Array
public interface ArrayPro extends Array {
	public abstract Iterator<Entry<Integer, Object>> entryArrayIterator();
}
