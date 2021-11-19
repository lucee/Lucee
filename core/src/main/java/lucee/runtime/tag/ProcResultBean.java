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
package lucee.runtime.tag;

public final class ProcResultBean {
	private String name;
	private int resultset = 1;
	private int maxrows = -1;

	/**
	 * @return Returns the maxrows.
	 */
	public int getMaxrows() {
		return maxrows;
	}

	/**
	 * @param maxrows The maxrows to set.
	 */
	public void setMaxrows(int maxrows) {
		this.maxrows = maxrows;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the resultset.
	 */
	public int getResultset() {
		return resultset;
	}

	/**
	 * @param resultset The resultset to set.
	 */
	public void setResultset(int resultset) {
		this.resultset = resultset;
	}

}