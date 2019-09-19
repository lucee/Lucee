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
package lucee.transformer.bytecode.statement.tag;

import lucee.transformer.Factory;
import lucee.transformer.Position;

public abstract class TagGroup extends TagBase {

	public static final short TAG_LOOP = 1;
	public static final short TAG_OUTPUT = 2;

	private int numberIterator = -1;
	private int query = -1;
	private int group = -1;
	private int pid;

	public TagGroup(Factory f, Position start, Position end) {
		super(f, start, end);
		// TODO Auto-generated constructor stub
	}

	public abstract short getType();

	// public abstract boolean hasQuery();

	// public abstract boolean hasGroup();

	public final int getNumberIterator() {
		return numberIterator;
	}

	public final void setNumberIterator(int numberIterator) {
		this.numberIterator = numberIterator;
	}

	public final boolean hasNumberIterator() {
		return numberIterator != -1;
	}

	/**
	 * returns if output has query
	 * 
	 * @return has query
	 */
	public final boolean hasQuery() {
		return getAttribute("query") != null;
	}

	/**
	 * returns if output has query
	 * 
	 * @return has query
	 */
	public final boolean hasGroup() {
		return getAttribute("group") != null;
	}

	public final int getQuery() {
		return query;
	}

	public final void setQuery(int query) {
		this.query = query;
	}

	public final int getGroup() {
		return group;
	}

	public final void setGroup(int group) {
		this.group = group;
	}

	public final int getPID() {
		return pid;
	}

	public final void setPID(int pid) {
		this.pid = pid;
	}
}