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
package lucee.runtime.security;

import lucee.runtime.converter.ScriptConvertable;
import lucee.runtime.exp.PageException;

/**
 * Credential interface
 */
public interface Credential extends ScriptConvertable {

	/**
	 * @return Returns the password.
	 */
	public abstract String getPassword();

	/**
	 * @return Returns the roles.
	 */
	public abstract String[] getRoles();

	/**
	 * @return Returns the username.
	 */
	public abstract String getUsername();

	/**
	 * encode rhe Credential to a Base64 String value
	 * 
	 * @return base64 encoded string
	 * @throws PageException
	 */
	public abstract String encode() throws PageException;

}