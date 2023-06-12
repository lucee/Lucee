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
package lucee.runtime.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lucee.print;
import lucee.commons.io.SystemUtil;
import lucee.commons.lang.SerializableObject;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;

public class ThreadQueueImpl implements ThreadQueuePro {
	private final SerializableObject token = new SerializableObject();

	public final List<PageContext> list = new ArrayList<PageContext>();
	private int waiting = 0;

	private Integer max;

	private short mode;

	public ThreadQueueImpl(short mode, Integer max) {
		this.mode = mode;
		this.max = max;

	}

	@Override
	public void enter(PageContext pc) throws IOException {
		if (mode == MODE_DISABLED) return;
		try {
			synchronized (token) {
				waiting++;
			}
			_enter(pc);
		}
		finally {
			synchronized (token) {
				waiting--;
			}
		}
	}

	private void _enter(PageContext pc) throws IOException {
		ConfigPro ci = (ConfigPro) pc.getConfig();

		long start = System.currentTimeMillis();
		long timeout = ci.getQueueTimeout();
		if (timeout <= 0) timeout = pc.getRequestTimeout();

		while (true) {
			synchronized (token) {
				print.e("_enter(" + Thread.currentThread().getName() + "):" + list.size() + ":" + max);

				print.e("- timeout" + timeout);
				if (mode == MODE_DISABLED) return;
				int max = this.max == null ? ci.getQueueMax() : this.max.intValue();
				print.e("- max:" + max);
				print.e("- size:" + list.size());
				if (mode != MODE_BLOCKING && list.size() < max) {
					print.e("- ok(" + Thread.currentThread().getName() + "):" + list.size());
					list.add(pc);
					return;
				}
			}
			print.e("- wait(" + Thread.currentThread().getName() + "):" + timeout);
			if (timeout > 0) SystemUtil.wait(token, timeout);
			else SystemUtil.wait(token);

			print.e("- wake up(" + Thread.currentThread().getName() + "):" + timeout);

			if (timeout > 0 && (System.currentTimeMillis() - start) >= timeout) {
				print.e("- throw timeout(" + Thread.currentThread().getName() + "):" + timeout);
				throw new IOException("Concurrent request timeout (" + (System.currentTimeMillis() - start) + ") [" + timeout
						+ " ms] has occurred, server is too busy handling other requests. This timeout setting can be changed in the server administrator.");
			}
		}
	}

	@Override
	public void exit(PageContext pc) {
		if (mode == MODE_DISABLED) return;
		synchronized (token) {
			print.e("exit(" + Thread.currentThread().getName() + ")");
			if (mode == MODE_DISABLED) return;
			list.remove(pc);
			token.notify();
		}
	}

	@Override
	public int size() {
		return waiting;
	}

	@Override
	public void clear() {
		list.clear();
		token.notifyAll();
	}

	@Override
	public short setMode(short mode) {
		synchronized (token) {
			short prevMode = this.mode;
			// there are possible threads in the queue
			if (this.mode != MODE_DISABLED) clear();
			this.mode = mode;
			return prevMode;
		}
	}

	@Override
	public short getMode() {
		return mode;
	}
}