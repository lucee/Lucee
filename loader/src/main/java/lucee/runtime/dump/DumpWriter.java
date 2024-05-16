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

import java.io.IOException;
import java.io.Writer;

import lucee.runtime.PageContext;

/**
 * writes out dumpdata to a writer
 */
public interface DumpWriter {

	public static int DEFAULT_RICH = 0;
	public static int DEFAULT_PLAIN = 1;
	public static int DEFAULT_NONE = 2;

	/**
	 * @param pc Page Context
	 * @param data data
	 * @param writer writer
	 * @param expand expand
	 * @throws IOException IO Exception
	 */
	public void writeOut(PageContext pc, DumpData data, Writer writer, boolean expand) throws IOException;

	/**
	 * cast dumpdata to a string
	 * 
	 * @param pc Page Context
	 * @param data data
	 * @param expand expand
	 * @return string
	 */
	public String toString(PageContext pc, DumpData data, boolean expand);

}