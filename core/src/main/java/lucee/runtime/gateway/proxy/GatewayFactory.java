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
package lucee.runtime.gateway.proxy;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.gateway.Gateway;

// FUTURE remove this class
public class GatewayFactory {

	public static Gateway toGateway(Object obj) throws ApplicationException {
		if (obj instanceof Gateway) return (Gateway) obj;
		throw new ApplicationException("the class [" + obj.getClass().getName() + "] does not implement the interface [" + Gateway.class.getName()
				+ "], make sure you have not multiple implementation of that interface in your classpath");

	}
}