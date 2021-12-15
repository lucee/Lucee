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
package lucee.cli;

import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Closer extends Thread {

	private final String name;
	private final Registry reg;
	private final long idleTime;
	private final CLIInvokerImpl invoker;

	public Closer(final Registry reg, final CLIInvokerImpl invoker, final String name, final long idleTime) {
		this.reg = reg;
		this.name = name;
		this.idleTime = idleTime;
		this.invoker = invoker;
	}

	@Override
	public void run() {
		// idle
		do
			sleepEL(idleTime);
		while (invoker.lastAccess() + idleTime > System.currentTimeMillis());

		try {
			reg.unbind(name);
			UnicastRemoteObject.unexportObject(invoker, true);
		}
		catch (final Throwable t) {
			t.printStackTrace();
		}

	}

	private void sleepEL(final long millis) {
		try {
			sleep(millis);
		}
		catch (final Throwable t) {
			t.printStackTrace();
		}
	}

}