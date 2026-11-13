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

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;

/**
 * Keeps the debug data of the last requests (limited by "debuggingMaxRecordsLogged") in memory.
 * The data is only softly referenced (LDEV-2673), so the GC can free it when memory gets low
 * instead of Lucee running into a low memory situation. Entries that have been cleared by the GC
 * are skipped and removed.
 */
public final class DebuggerPool {

	// private LinkedList<Struct> queue = new LinkedList<Struct>();
	private final Queue<SoftReference<Struct>> queue = new ConcurrentLinkedQueue<>();

	public DebuggerPool(Resource storage) {
		// this.storage=storage;
	}

	public void store(PageContext pc, DebuggerImpl debugger) {
		if (ReqRspUtil.getScriptName(pc, pc.getHttpServletRequest()).indexOf("/lucee/") == 0) return;

		try {
			queue.add(new SoftReference<Struct>(debugger.getDebuggingData(pc, true)));
		}
		catch (PageException e) {
			LogUtil.log(pc, "debugger", e);
		}

		// entries cleared by the GC do not count
		queue.removeIf(ref -> ref.get() == null);

		// on overflow
		while (queue.size() > ((ConfigWebPro) pc.getConfig()).getDebugMaxRecordsLogged()) {
			queue.poll();
		}
	}

	public Array getData(PageContext pc) {
		Array arr = new ArrayImpl();
		Iterator<SoftReference<Struct>> it = queue.iterator();
		SoftReference<Struct> ref;
		Struct data;
		while (it.hasNext()) {
			ref = it.next();
			data = ref.get(); // read only once, it can be cleared at any time
			if (data == null) it.remove();
			else arr.appendEL(data);
		}
		return arr;
	}

	public void purge() {
		queue.clear();
	}

	/**
	 * number of entries currently held, including entries cleared by the GC that are not yet removed
	 */
	public int size() {
		return queue.size();
	}

	/**
	 * clears all references the way the GC does under memory pressure, the cleared entries are removed
	 * with the next store or read. Mainly used by tests, because the GC can not be triggered
	 * deterministically.
	 */
	public void clearReferences() {
		for (SoftReference<Struct> ref: queue) {
			ref.clear();
		}
	}
}
