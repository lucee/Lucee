/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
package lucee.runtime;

public abstract class CIPage extends Page {

	private static final long serialVersionUID = -398015716783522906L;

	/*
	 * executed before the static constructor is executed to set the environment right
	 * 
	 * @param pc
	 * 
	 * @return
	 * 
	 * public abstract Variables beforeStaticConstructor(PageContext pc);
	 */

	/*
	 * executed after the static constructor is executed to reset the environment to previous state
	 * 
	 * @param pc
	 * 
	 * @return
	 * 
	 * public abstract void afterStaticConstructor(PageContext pc, Variables var);
	 */

	// public abstract String getComponentName();

}