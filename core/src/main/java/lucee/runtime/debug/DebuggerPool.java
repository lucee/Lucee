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
package lucee.runtime.debug;

import java.util.Iterator;
import java.util.LinkedList;

import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;

public class DebuggerPool {

	// private Resource storage;
	private LinkedList<Struct> queue = new LinkedList<Struct>();
	// private List<Debugger> list=new ArrayList<Debugger>();

	public DebuggerPool(Resource storage) {
		// this.storage=storage;
	}

	public void store(PageContext pc, Debugger debugger) {
		if (ReqRspUtil.getScriptName(pc, pc.getHttpServletRequest()).indexOf("/lucee/") == 0) return;
		synchronized (queue) {
			try {
				queue.add((Struct) Duplicator.duplicate(debugger.getDebuggingData(pc, true), true));
			}
			catch (PageException e) {}

			while (queue.size() > ((ConfigWebImpl) pc.getConfig()).getDebugMaxRecordsLogged())
				queue.poll();
		}
	}

	public Array getData(PageContext pc) {
		Iterator<Struct> it;
		synchronized (queue) {
			it = queue.iterator();
		}
		Array arr = new ArrayImpl();
		while (it.hasNext()) {
			arr.appendEL(it.next());
		}
		return arr;
	}
	
	public void purge() {
		synchronized (queue) {
			queue.clear();
		}
	}

}
