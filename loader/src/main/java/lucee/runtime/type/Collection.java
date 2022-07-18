/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.type;

import java.io.Serializable;

import lucee.runtime.dump.Dumpable;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Castable;

/**
 * interface collection, used for all collection types of Lucee (array, struct, query)
 */
public interface Collection extends Dumpable, Iteratorable, Cloneable, Serializable, Castable, ForEachIteratorable {

	/**
	 * @return the size of the collection
	 */
	public int size();

	/**
	 * @return returns a string array of all keys in the collection
	 * @deprecated use instead <code>keyIterator()</code>
	 */
	@Deprecated
	public Collection.Key[] keys();

	/**
	 * removes value from collection and return it when it exists, otherwise throws an exception
	 * 
	 * @param key key of the collection
	 * @return removed Object
	 * @throws PageException thrown when cannot remove value
	 */
	public Object remove(Collection.Key key) throws PageException;

	/**
	 * removes value from collection and return it when it exists, otherwise returns null
	 * 
	 * @param key key of the collection
	 * @return removed Object
	 */
	public Object removeEL(Collection.Key key);

	/**
	 * removes value from collection and return it when it exists, otherwise returns the given default
	 * value
	 * 
	 * @param key key of the collection
	 * @param defaultValue value to return if the entry does not exist
	 * @return removed Object
	 */
	public Object remove(Collection.Key key, Object defaultValue);

	/**
	 * clears the collection
	 */
	public void clear();

	/**
	 * return a value from the collection
	 * 
	 * @param key key of the value to get
	 * @return value on key position
	 * @throws PageException thrown when no value exist for given key
	 * @deprecated use instead <code>{@link #get(lucee.runtime.type.Collection.Key)}</code>
	 */
	@Deprecated
	public Object get(String key) throws PageException;

	/**
	 * return a value from the collection
	 * 
	 * @param key key of the value to get must be lower case
	 * @return value on key position
	 * @throws PageException thrown when no value exist for given key
	 */
	public Object get(Collection.Key key) throws PageException;

	/**
	 * return a value from the collection, if key doesn't exist, dont throw an exception, returns null
	 * 
	 * @param key key of the value to get
	 * @param defaultValue value returned when no value exists for given key
	 * @return value on key position or null
	 * @deprecated use instead <code>{@link #get(lucee.runtime.type.Collection.Key, Object)}</code>
	 */
	@Deprecated
	public Object get(String key, Object defaultValue);

	/**
	 * return a value from the collection, if key doesn't exist, dont throw an exception, returns null
	 * 
	 * @param key key of the value to get
	 * @param defaultValue value returned when no value exists for given key
	 * @return value on key position or null
	 */
	public Object get(Collection.Key key, Object defaultValue);

	/**
	 * sets a value to the collection
	 * 
	 * @param key key of the new value
	 * @param value value to set
	 * @return value setted
	 * @throws PageException exception thrown when fails to set the value
	 * @deprecated use instead <code>{@link #set(lucee.runtime.type.Collection.Key, Object)}</code>
	 */
	@Deprecated
	public Object set(String key, Object value) throws PageException;

	/**
	 * sets a value to the collection
	 * 
	 * @param key key of the new value
	 * @param value value to set
	 * @return value setted
	 * @throws PageException exception thrown when fails to set the value
	 */
	public Object set(Collection.Key key, Object value) throws PageException;

	/**
	 * sets a value to the collection, if key doesn't exist, dont throw an exception, returns null
	 * 
	 * @param key key of the value to get
	 * @param value value to set
	 * @return value on key position or null
	 * @deprecated use instead <code>{@link #setEL(lucee.runtime.type.Collection.Key, Object)}</code>
	 */
	@Deprecated
	public Object setEL(String key, Object value);

	/**
	 * sets a value to the collection, if key doesn't exist, dont throw an exception, returns null
	 * 
	 * @param key key of the value to get
	 * @param value value to set
	 * @return value on key position or null
	 */
	public Object setEL(Collection.Key key, Object value);

	/**
	 * @return this object cloned
	 */
	public Object clone();

	public Collection duplicate(boolean deepCopy);

	/**
	 * contains this key
	 * 
	 * @param key key to check for
	 * @return returns if collection has a key with given name
	 * @deprecated use instead <code>{@link #containsKey(lucee.runtime.type.Collection.Key)}</code>
	 */
	@Deprecated
	public boolean containsKey(String key);

	/**
	 * contains this key
	 * 
	 * @param key key to check for
	 * @return returns if collection has a key with given name
	 */
	public boolean containsKey(Collection.Key key);

	interface Key extends Serializable {

		/**
		 * return key as String
		 * @return string
		 */
		public String getString();

		/**
		 * return key as lower case String
		 * @return lower case string
		 */
		public String getLowerString();

		/**
		 * return key as upper case String
		 * @return upper case string
		 */
		public String getUpperString();

		/**
		 * return char at given position
		 * 
		 * @param index index
		 * @return character at given position
		 */
		public char charAt(int index);

		/**
		 * return lower case char a given position
		 * 
		 * @param index index
		 * @return lower case char from given position
		 */
		public char lowerCharAt(int index);

		/**
		 * return upper case char a given position
		 * 
		 * @param index index
		 * @return upper case char from given position
		 */
		public char upperCharAt(int index);

		/**
		 * compare to object, ignore case of input
		 * 
		 * @param key key
		 * @return is equal to given key?
		 */
		public boolean equalsIgnoreCase(Collection.Key key);

		public long hash();

		/**
		 * Returns the length of this string.
		 * 
		 * @return length of the string
		 */
		public int length();

		// Future add; returns a 64 bit based hashcode for the Key
		// public long hash();
	}
}