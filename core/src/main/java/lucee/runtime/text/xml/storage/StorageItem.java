/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.text.xml.storage;

import lucee.runtime.type.dt.Date;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.Time;

/**
 * An Object to store to XML File
 */
public abstract class StorageItem {

	/**
	 * gets a value from the storage item as String
	 * 
	 * @param key key of the value to get
	 * @return matching value
	 * @throws StorageException
	 */
	public String getString(String key) throws StorageException {
		throw new StorageException("there is no value with the key " + key);
	}

	/**
	 * gets a value from the storage item as int
	 * 
	 * @param key key of the value to get
	 * @return matching value
	 * @throws StorageException
	 */
	public int getInt(String key) throws StorageException {
		throw new StorageException("there is no value with the key " + key);
	}

	/**
	 * gets a value from the storage item as Date Object
	 * 
	 * @param key key of the value to get
	 * @return matching value
	 * @throws StorageException
	 */
	public Date getDate(String key) throws StorageException {
		throw new StorageException("there is no value with the key " + key);
	}

	/**
	 * gets a value from the storage item as Time Object
	 * 
	 * @param key key of the value to get
	 * @return matching value
	 * @throws StorageException
	 */
	public Time getTime(String key) throws StorageException {
		throw new StorageException("there is no value with the key " + key);
	}

	/**
	 * gets a value from the storage item as Date Object
	 * 
	 * @param key key of the value to get
	 * @return matching value
	 * @throws StorageException
	 */
	public DateTime getDateTime(String key) throws StorageException {
		throw new StorageException("there is no value with the key " + key);
	}

	/**
	 * sets a value to the storage item as String
	 * 
	 * @param key key of the value to set
	 * @param value value to set
	 * @throws StorageException
	 */
	public void setString(String key, String value) throws StorageException {
		throw new StorageException("key " + key + " is not supported for this item");
	}

	/**
	 * sets a value to the storage item as int
	 * 
	 * @param key key of the value to set
	 * @param value value to set
	 * @throws StorageException
	 */
	public void setInt(String key, int value) throws StorageException {
		throw new StorageException("key " + key + " is not supported for this item");
	}

	/**
	 * sets a value to the storage item as Date Object
	 * 
	 * @param key key of the value to set
	 * @param value value to set
	 * @throws StorageException
	 */
	public void setDate(String key, Date value) throws StorageException {
		throw new StorageException("key " + key + " is not supported for this item");
	}

	/**
	 * sets a value to the storage item as Time Object
	 * 
	 * @param key key of the value to set
	 * @param value value to set
	 * @throws StorageException
	 */
	public void setTime(String key, Time value) throws StorageException {
		throw new StorageException("key " + key + " is not supported for this item");
	}

	/**
	 * sets a value to the storage item as DateTime Object
	 * 
	 * @param key key of the value to set
	 * @param value value to set
	 * @throws StorageException
	 */
	public void setDateTime(String key, DateTime value) throws StorageException {
		throw new StorageException("key " + key + " is not supported for this item");
	}
}