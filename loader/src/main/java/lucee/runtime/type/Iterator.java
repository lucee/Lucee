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

import lucee.runtime.exp.PageException;

/**
 * Interface for a simple Iterator
 */
public interface Iterator {

	/**
	 * set the intern pointer of the iterator to the next position, return true if next position exist
	 * otherwise false.
	 * 
	 * @return boolean
	 * @throws PageException thrown when fail to execute action
	 * @deprecated use instead <code>{@link #next(int)}</code>
	 */
	@Deprecated
	public boolean next() throws PageException;

	/**
	 * set the intern pointer of the iterator to the next position, return true if next position exist
	 * otherwise false.
	 * 
	 * @param pid pointer id
	 * @return boolean
	 * @throws PageException thrown when fail to execute action
	 */
	public boolean next(int pid) throws PageException;

	public boolean previous(int pid);

	/**
	 * reset the intern pointer
	 * 
	 * @throws PageException thrown when fail to reset
	 * @deprecated use instead <code>{@link #reset(int)}</code>
	 */
	@Deprecated
	public void reset() throws PageException;

	/**
	 * 
	 * reset the intern pointer
	 * 
	 * @param pid pointer id
	 * @throws PageException thrown when fail to reset
	 */
	public void reset(int pid) throws PageException;

	/**
	 * return recordcount of the iterator object
	 * 
	 * @return int
	 */
	public int getRecordcount();

	/**
	 * return the current position of the internal pointer
	 * 
	 * @param pid pointer id
	 * @return int
	 */
	public int getCurrentrow(int pid);

	/**
	 * 
	 * set the internal pointer to defined position
	 * 
	 * @param index index
	 * @param pid pointer id
	 * @return if it was successful or not
	 * @throws PageException thrown when fail to execute action
	 */
	public boolean go(int index, int pid) throws PageException;

	/**
	 * @return returns if iterator is empty or not
	 */
	public boolean isEmpty();

	// public ArrayList column(String strColumn)throws PageException;

	// public String[] row(int number);
}