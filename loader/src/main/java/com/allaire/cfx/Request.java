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
package com.allaire.cfx;

/**
 * Alternative Implementation of Jeremy Allaire's Request Interface
 */
public interface Request {

	/**
	 * checks if attribute with this key exists
	 * 
	 * @param key key to check
	 * @return has key or not
	 */
	public boolean attributeExists(String key);

	/**
	 * @return if tags has set [debug] attribute
	 */
	public boolean debug();

	/**
	 * returns attribute matching key
	 * 
	 * @param key key to get
	 * @return value to key
	 */
	public String getAttribute(String key);

	/**
	 * returns attribute matching key
	 * 
	 * @param key key to get
	 * @param defaultValue return this value if key not exist
	 * @return value to key
	 */
	public String getAttribute(String key, String defaultValue);

	/**
	 * return all sattribute keys
	 * 
	 * @return all keys
	 */
	public String[] getAttributeList();

	/**
	 * returns attribute as int matching key
	 * 
	 * @param key key to get
	 * @return value to key
	 * @throws NumberFormatException thrown when fail to convert the value to a number
	 */
	public int getIntAttribute(String key) throws NumberFormatException;

	/**
	 * returns attribute as int matching key
	 * 
	 * @param key key to get
	 * @param defaultValue return this value if key not exist
	 * @return value to key
	 */
	public int getIntAttribute(String key, int defaultValue);

	/**
	 * return given query
	 * 
	 * @return return given query
	 */
	public Query getQuery();

	/**
	 * returns all the settings
	 * 
	 * @param key key to get setting for
	 * @return settings
	 */
	public String getSetting(String key);

}