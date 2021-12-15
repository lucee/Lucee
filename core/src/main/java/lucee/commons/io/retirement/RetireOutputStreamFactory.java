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
package lucee.commons.io.retirement;

import java.util.ArrayList;
import java.util.List;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;

public class RetireOutputStreamFactory {

	static List<RetireOutputStream> list = new ArrayList<RetireOutputStream>();
	private static RetireThread thread;
	private static boolean closed = false;

	/**
	 * close existing threads and stops opening new onces
	 */
	public static void close() {
		if (thread != null && thread.isAlive()) {
			thread.close = true;
			closed = true;
			SystemUtil.notify(thread);
			SystemUtil.stop(thread);
		}
	}

	static void startThread(long timeout) {
		if (timeout < 1000) timeout = 1000;
		if (thread == null || !thread.isAlive()) {
			thread = new RetireThread(timeout);
			thread.start();
		}
		else if (thread.sleepTime > timeout) {
			thread.sleepTime = timeout;
			SystemUtil.notify(thread);
		}
	}

	static class RetireThread extends Thread {

		public long sleepTime;
		public boolean close = false;

		public RetireThread(long sleepTime) {
			this.sleepTime = sleepTime;
		}

		@Override
		public void run() {
			while (true) {
				try {
					if (list.size() == 0) break;
					synchronized (this) {
						this.wait(sleepTime);
					}
					RetireOutputStream[] arr = list.toArray(new RetireOutputStream[list.size()]); // not using iterator to avoid ConcurrentModificationException
					for (int i = 0; i < arr.length; i++) {
						if (arr[i] == null) continue;
						if (close) arr[i].retireNow();
						else arr[i].retire();
					}
					if (close) break;
				}
				catch (InterruptedException ie) {
					LogUtil.log(null, "file", ie);
					break;
				}
				catch (Exception e) {
					LogUtil.log(null, "file", e);
				}
			}
			thread = null;
		}
	}

	public static boolean isClosed() {
		return closed;
	}
}