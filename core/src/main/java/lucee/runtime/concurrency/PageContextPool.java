/**
 * Copyright (c) 2026, Lucee Association Switzerland. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package lucee.runtime.concurrency;

import java.io.ByteArrayOutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ExecutionLog;
import lucee.runtime.engine.ExecutionLogSupport;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.thread.ThreadUtil;

/**
 * A borrow/return pool of cloned {@link PageContextImpl} instances shared by all tasks of a single
 * parallel iteration (each/map/filter/some/every). Instead of cloning (and releasing) a PageContext
 * per element, tasks borrow a clone, use it, and return it for reuse. The number of clones therefore
 * never exceeds the peak concurrency of the operation (≤ the concurrency limit for bounded modes)
 * instead of growing with the element count.
 *
 * <p>
 * Every clone created is tracked so it can be released in {@link #close()} once the operation finished.
 * Each clone is paired with its own {@link ByteArrayOutputStream} so per-task output can be captured
 * in isolation.
 * </p>
 */
public final class PageContextPool {

	/** a borrowed clone together with the output buffer its writer is bound to */
	public static final class Entry {
		public final PageContextImpl pc;
		public final ByteArrayOutputStream baos;

		private Entry(PageContextImpl pc, ByteArrayOutputStream baos) {
			this.pc = pc;
			this.baos = baos;
		}
	}

	private final PageContext parent;
	private final Queue<Entry> free = new ConcurrentLinkedQueue<Entry>();
	private final Queue<Entry> all = new ConcurrentLinkedQueue<Entry>();
	private volatile boolean closed = false;

	public PageContextPool(PageContext parent) {
		this.parent = parent;
	}

	/**
	 * @return a clone ready to use; an idle one is reused when available, otherwise a fresh clone is
	 *         created. The returned entry's output buffer is reset.
	 */
	public Entry borrow() {
		Entry e = free.poll();
		if (e != null) {
			e.baos.reset();
			return e;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ThreadLocalPageContext.register(parent);
		PageContextImpl pc = ThreadUtil.clonePageContext(parent, baos, false, false, false);

		// capture spawn offset for the execution log, same as a per-element clone would
		ExecutionLog execLog = pc.getExecutionLog();
		if (execLog instanceof ExecutionLogSupport) {
			((ExecutionLogSupport) execLog).setSpawnOffsetNano(System.nanoTime() - ((PageContextImpl) parent).getStartTimeNS());
		}
		e = new Entry(pc, baos);
		all.add(e);
		return e;
	}

	/**
	 * returns a clone for reuse by a later task. Only call this for clones left in a clean, reusable
	 * state; a clone whose task failed must not be returned (it is released by {@link #close()}).
	 *
	 * @param e the entry previously obtained from {@link #borrow()}
	 */
	public void giveBack(Entry e) {
		if (closed || e == null) return;
		free.add(e);
	}

	/**
	 * releases every clone created by this pool. Idempotent.
	 */
	public void close() {
		if (closed) return;
		closed = true;
		Entry e;
		while ((e = all.poll()) != null) {
			try {
				e.pc.getConfig().getFactory().releasePageContext(e.pc);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				LogUtil.log(parent, "concurrency", t);
			}
		}
		free.clear();
	}
}
