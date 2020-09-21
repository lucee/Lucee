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
package lucee.runtime.sql.exp;

public abstract class ExpressionSupport implements Expression {

	private int index;
	private String alias;
	private boolean directionBackward;

	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public String getAlias() {
		if (alias == null) return "column_" + (getIndex() - 1);
		return alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public boolean hasAlias() {
		return alias != null;
	}

	@Override
	public boolean hasIndex() {
		return index != 0;
	}

	@Override
	public void setDirectionBackward(boolean b) {
		directionBackward = b;
	}

	/**
	 * @return the directionBackward
	 */
	@Override
	public boolean isDirectionBackward() {
		return directionBackward;
	}

	@Override
	public void reset() {}

}