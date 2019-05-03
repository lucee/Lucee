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
package lucee.runtime.debug;

import lucee.runtime.PageSource;
import lucee.runtime.op.Caster;

public abstract class DebugEntrySupport implements DebugEntry {

	private static final long serialVersionUID = -2495816599745340388L;

	private static int _id = 1;
	private String id;

	private long exeTime;
	private String path;
	private int count = 1;
	private long min = 0;
	private long max = 0;

	/**
	 * constructor of the class
	 * 
	 * @param source
	 * @param key
	 */
	protected DebugEntrySupport(PageSource source) {
		this.path = source == null ? "" : source.getDisplayPath();
		id = Caster.toString(++_id);
	}

	@Override
	public long getExeTime() {
		return positiv(exeTime);
	}

	@Override
	public void updateExeTime(long exeTime) {
		if (exeTime >= 0) {
			if (count == 1 || min > exeTime) min = exeTime;
			if (max < exeTime) max = exeTime;

			this.exeTime += exeTime;
		}
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getId() {
		return id;
	}

	/**
	 * increment the inner counter
	 */
	protected void countPP() {
		count++;

	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public long getMax() {
		return positiv(max);
	}

	@Override
	public long getMin() {
		return positiv(min);
	}

	protected long positiv(long time) {
		if (time < 0) return 0;
		return time;
	}

}