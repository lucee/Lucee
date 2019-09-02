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

import java.io.Serializable;

import lucee.runtime.PageContext;

/**
 * this interface make an object printable, also to a simple object
 */
public interface Dumpable extends Serializable {

	/**
	 * method to print out information to an object as HTML
	 * 
	 * @param pageContext page context object
	 * @param maxlevel max level to display
	 * @param properties properties data
	 * @return dump object to display
	 */
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties);
}