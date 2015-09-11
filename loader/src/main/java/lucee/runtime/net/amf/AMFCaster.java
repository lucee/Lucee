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
package lucee.runtime.net.amf;

import java.util.Map;

import lucee.runtime.exp.PageException;

/**
 * Cast a CFML object to AMF Objects and the other way
 */
public interface AMFCaster {

	public void init(Map<String, String> arguments);

	/**
	 * cast cfml Object to AMF Object
	 * 
	 * @param o
	 * @throws PageException
	 */
	public Object toAMFObject(Object o) throws PageException;

	/**
	 * cast a amf Object to cfml Object
	 * 
	 * @param amf
	 * @throws PageException
	 */
	public Object toCFMLObject(Object amf) throws PageException;
}