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
package lucee.runtime.net.ipsettings;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * this class represents the * match so it is used as a root of the node tree and matches all
 * addresses
 *
 * @param <T>
 */
public final class IPRangeNodeRoot<T> extends IPRangeNode<T> {

	public IPRangeNodeRoot() throws UnknownHostException {

		super("0");
	}

	@Override
	public boolean isInRange(InetAddress addr) {

		return true;
	}

	@Override
	public boolean containsRange(IPRangeNode other) {

		return true;
	}

}