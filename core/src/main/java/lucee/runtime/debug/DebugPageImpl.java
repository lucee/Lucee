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

import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.dump.SimpleDumpData;

/**
 *
 *
 * To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
public final class DebugPageImpl implements Dumpable, DebugPage {

	private int count;
	private Resource file;

	private int min;
	private int max;
	private int all;
	private long time;

	// private long time;

	/**
	 * @param file
	 */
	public DebugPageImpl(Resource file) {
		this.file = file;
	}

	@Override
	public void set(long t) {
		this.time = t;
		if (count == 0) {
			min = (int) time;
			max = (int) time;
		}
		else {
			if (min > time) min = (int) time;
			if (max < time) max = (int) time;
		}
		all += time;

		count++;
	}

	@Override
	public int getMinimalExecutionTime() {
		return min;
	}

	@Override
	public int getMaximalExecutionTime() {
		return max;
	}

	@Override
	public int getAverageExecutionTime() {
		return all / count;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Resource getFile() {
		return file;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable table = new DumpTable("#cccc66", "#cccc99", "#000000");
		table.setTitle(file.getAbsolutePath());
		table.appendRow(1, new SimpleDumpData("min (ms)"), new SimpleDumpData(min));
		table.appendRow(1, new SimpleDumpData("avg (ms)"), new SimpleDumpData(getAverageExecutionTime()));
		table.appendRow(1, new SimpleDumpData("max (ms)"), new SimpleDumpData(max));
		table.appendRow(1, new SimpleDumpData("total (ms)"), new SimpleDumpData(all));
		return table;
	}

}