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

import lucee.runtime.PageContext;
import lucee.runtime.type.Struct;

/**
 * abstract class for all scopes
 */
public interface Scope extends Struct {

	/**
	 * Scope Undefined
	 */
	public static final int SCOPE_UNDEFINED = 0;
	/**
	 * Scope Variables
	 */
	public static final int SCOPE_VARIABLES = 1;
	/**
	 * Scope Request
	 */
	public static final int SCOPE_REQUEST = 2;
	/**
	 * Scope URL
	 */
	public static final int SCOPE_URL = 3;
	/**
	 * Scope Form
	 */
	public static final int SCOPE_FORM = 4;
	/**
	 * Scope Client
	 */
	public static final int SCOPE_CLIENT = 5;
	/**
	 * Scope Cookie
	 */
	public static final int SCOPE_COOKIE = 6;
	/**
	 * Scope Session
	 */
	public static final int SCOPE_SESSION = 7;
	/**
	 * Scope Application
	 */
	public static final int SCOPE_APPLICATION = 8;
	/**
	 * Scope Arguments
	 */
	public static final int SCOPE_ARGUMENTS = 9;
	/**
	 * Scope CGI
	 */
	public static final int SCOPE_CGI = 10;
	/**
	 * Scope Server
	 */
	public static final int SCOPE_SERVER = 11;

	/**
	 * Scope Local
	 */
	public static final int SCOPE_LOCAL = 12;

	/**
	 * Scope Caller
	 */
	public static final int SCOPE_CALLER = 13;

	public static final int SCOPE_CLUSTER = 14;

	public static final int SCOPE_VAR = 15;

	public static final int SCOPE_COUNT = 16;

	/**
	 * return if the scope is Initialized
	 * 
	 * @return scope is init
	 */
	public boolean isInitalized();

	/**
	 * Initialize Scope
	 * 
	 * @param pc Page Context
	 */
	public void initialize(PageContext pc);

	/**
	 * release scope for reuse
	 * 
	 * @param pc Page Context
	 */
	public void release(PageContext pc);

	/**
	 * @return return the scope type (SCOPE_SERVER, SCOPE_SESSION usw.)
	 */
	public int getType();

	/**
	 * @return return the scope type as a String (server,session usw.)
	 */
	public String getTypeAsString();

}