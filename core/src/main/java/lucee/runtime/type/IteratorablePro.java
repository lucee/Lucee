package lucee.runtime.type;

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.type.Collection.Key;

// FUTURE add to loader
public interface IteratorablePro extends Iteratorable {

	/**
	 * @return return an Iterator for Keys as Collection.Keys
	 */
	public Iterator<Collection.Key> keyIterator(PageContext pc);

	/**
	 * @return return an Iterator for Keys as String
	 */
	public Iterator<String> keysAsStringIterator(PageContext pc);

	public Collection.Key[] keys(PageContext pc);

	/**
	 * 
	 * @return return an Iterator for Values
	 */
	public Iterator<Object> valueIterator(PageContext pc);

	public Iterator<Entry<Key, Object>> entryIterator(PageContext pc);
}