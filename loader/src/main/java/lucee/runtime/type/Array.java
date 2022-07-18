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

import java.util.Comparator;
import java.util.List;

import lucee.runtime.exp.PageException;

/**
 * 
 */
public interface Array extends Collection, Cloneable, Objects {

	/**
	 * return dimension of the array
	 * 
	 * @return dimension of the array
	 */
	public int getDimension();

	/**
	 * return object a given position, key can only be an integer from 1 to array len
	 * 
	 * @param key key as integer
	 * @param defaultValue default value
	 * @return value at key position
	 */
	public Object get(int key, Object defaultValue);

	/**
	 * return object a given position, key can only be an integer from 1 to array len
	 * 
	 * @param key key as integer
	 * @return value at key position
	 * @throws PageException Page Exception
	 */
	public Object getE(int key) throws PageException;

	/**
	 * set value at defined position, on error return null
	 * 
	 * @param key key of the new value
	 * @param value value to set
	 * @return setted value
	 */
	public Object setEL(int key, Object value);

	/**
	 * set value at defined position
	 * 
	 * @param key key
	 * @param value value
	 * @return defined value
	 * @throws PageException Page Exception
	 */
	public Object setE(int key, Object value) throws PageException;

	/**
	 * @return return all array keys as int
	 */
	public int[] intKeys();

	/**
	 * insert a value add defined position
	 * 
	 * @param key position to insert
	 * @param value value to insert
	 * @return has done or not
	 * @throws PageException Page Exception
	 */
	public boolean insert(int key, Object value) throws PageException;

	/**
	 * append a new value to the end of the array
	 * 
	 * @param o value to insert
	 * @return inserted value
	 * @throws PageException Page Exception
	 */
	public Object append(Object o) throws PageException;

	public Object appendEL(Object o);

	/**
	 * add a new value to the begin of the array
	 * 
	 * @param o value to insert
	 * @return inserted value
	 * @throws PageException Page Exception
	 */
	public Object prepend(Object o) throws PageException;

	/**
	 * resize array to defined size
	 * 
	 * @param to new minimum size of the array
	 * @throws PageException Page Exception
	 */
	public void resize(int to) throws PageException;

	/**
	 * sort values of an array
	 * 
	 * @param sortType search type (text,textnocase,numeric)
	 * @param sortOrder (asc,desc)
	 * @throws PageException Page Exception
	 * @deprecated use instead <code>sort(Comparator comp)</code>
	 */
	@Deprecated
	public void sort(String sortType, String sortOrder) throws PageException;

	public void sortIt(@SuppressWarnings("rawtypes") Comparator comp);// this name was chosen to avoid conflict with java.util.List

	/**
	 * @return return array as native (Java) Object Array
	 */
	public Object[] toArray();

	/**
	 * @return return array as ArrayList
	 */
	// public ArrayList toArrayList();

	@SuppressWarnings("rawtypes")
	public List toList();

	/**
	 * removes a value ad defined key
	 * 
	 * @param key key to remove
	 * @return returns if value is removed or not
	 * @throws PageException Page Exception
	 */
	public Object removeE(int key) throws PageException;

	/**
	 * removes a value ad defined key
	 * 
	 * @param key key to remove
	 * @return returns if value is removed or not
	 */
	public Object removeEL(int key);

	/**
	 * contains this key
	 * 
	 * @param key key
	 * @return returns if collection has a key with given name
	 */
	public boolean containsKey(int key);
}