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
package lucee.runtime.dump;

/**
 * this class is to hold all information to a dumpwriter together in a single class, Dumpwriter,
 * name and if it is a default.
 */
public class DumpWriterEntry {
	private String name;
	private DumpWriter writer;
	private int defaultType;

	public DumpWriterEntry(int defaultType, String name, DumpWriter writer) {
		// print.err(name+":"+defaultType);
		this.defaultType = defaultType;
		this.name = name;
		this.writer = writer;
	}

	/**
	 * @return the def
	 */
	public int getDefaultType() {
		return defaultType;
	}

	/**
	 * @param def the def to set
	 */
	public void setDefaultType(int defaultType) {
		this.defaultType = defaultType;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the writer
	 */
	public DumpWriter getWriter() {
		return writer;
	}

}