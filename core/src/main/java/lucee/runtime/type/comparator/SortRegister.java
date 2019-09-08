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
package lucee.runtime.type.comparator;

/**
 * a value of an array with information of old position in array
 */
public final class SortRegister {

	private Object value;
	private int oldPosition;

	/**
	 * constructor of the class
	 * 
	 * @param pos
	 * @param value
	 */
	public SortRegister(int pos, Object value) {
		this.value = value;
		oldPosition = pos;
	}

	/**
	 * @return Returns the oldPosition.
	 */
	public int getOldPosition() {
		return oldPosition;
	}

	/**
	 * @param oldPosition The oldPosition to set.
	 */
	public void setOldPosition(int oldPosition) {
		this.oldPosition = oldPosition;
	}

	/**
	 * @return Returns the value.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value.toString();
	}
}