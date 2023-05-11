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
package lucee.commons.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import lucee.commons.io.IOUtil;

public class PortChecker {

	public static boolean isActive(String host, int port) {
		Socket s = null;
		try {
			s = new Socket();
			s.setReuseAddress(true);
			SocketAddress sa = new InetSocketAddress(host, port);
			s.connect(sa, 3000);
			return true;
		}
		catch (IOException e) {
		}
		finally {
			IOUtil.closeEL(s);
		}
		return false;
	}

	public static Map<Integer, Boolean> portsTaken(int portFrom, int portTo) {
		Map<Integer, Boolean> result = new HashMap<Integer, Boolean>();
		for (int i = portFrom; i <= portTo; i++) {
			result.put(i, portTaken(i));
		}
		return result;
	}

	public static boolean portTaken(int port) {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
		}
		catch (IOException e) {
			return true;
		}
		finally {
			IOUtil.closeEL(socket);
		}
		return false;
	}
}