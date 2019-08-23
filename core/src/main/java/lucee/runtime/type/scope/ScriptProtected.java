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
package lucee.runtime.type.scope;

import lucee.runtime.listener.ApplicationContext;

public interface ScriptProtected {

	public static final int UNDEFINED = 0;
	public static final int YES = 1;
	public static final int NO = 2;

	/**
	 * @return returns if the values of the scope are already protected against cross site scripting
	 */
	public boolean isScriptProtected();

	/**
	 * transform the string values of the scope do a script protecting way
	 */
	public void setScriptProtecting(ApplicationContext ac, boolean scriptProtecting);

}