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
package lucee.runtime.net.ftp;

import java.io.IOException;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.arrays.ArrayMerge;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ListUtil;

/**
 * represent a ftp path
 */
public final class FTPPath implements Dumpable {

	private String path;
	private String name;
	// private Array arrPath;

	/**
	 * @param current
	 * @param relpath
	 * @throws PageException
	 * @throws IOException
	 */
	public FTPPath(AFTPClient client, String relpath) throws PageException, IOException {
		relpath = relpath.replace('\\', '/');
		Array relpathArr = ListUtil.listToArrayTrim(relpath, '/');

		// relpath is absolute
		if (relpath.startsWith("/")) {
			init(relpathArr);
			return;
		}
		String current;
		if (client == null) current = "";
		else current = client.printWorkingDirectory().replace('\\', '/');
		Array parentArr = ListUtil.listToArrayTrim(current, '/');

		// Single Dot .
		if (relpathArr.size() > 0 && relpathArr.get(1, "").equals(".")) {
			relpathArr.removeEL(1);
		}

		// Double Dot ..
		while (relpathArr.size() > 0 && relpathArr.get(1, "").equals("..")) {
			relpathArr.removeEL(1);
			if (parentArr.size() > 0) {
				parentArr.removeEL(parentArr.size());
			}
			else {
				parentArr.prepend("..");
			}
		}
		ArrayMerge.append(parentArr, relpathArr);
		init(parentArr);
	}

	private void init(Array arr) throws PageException {
		if (arr.size() > 0) {
			this.name = (String) arr.get(arr.size(), "");
			arr.removeEL(arr.size());
			this.path = '/' + ListUtil.arrayToList(arr, "/") + '/';
		}
		else {
			this.path = "/";
			this.name = "";
		}
		// this.arrPath=arr;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the path.
	 */
	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return path + name;// +" - "+"path("+getPath()+");"+"name("+getName()+");"+"parent("+getParentPath()+");";
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable table = new DumpTable("string", "#ff6600", "#ffcc99", "#000000");
		table.appendRow(1, new SimpleDumpData("FTPPath"), new SimpleDumpData(toString()));
		return table;
	}
}