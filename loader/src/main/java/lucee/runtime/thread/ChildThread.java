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
package lucee.runtime.thread;

public abstract class ChildThread extends Thread {

	private static ThreadGroup group = new ThreadGroup("cfthread");
	private static int count = 0;

	public abstract String getTagName();

	// public PageContext getParent();

	public abstract long getStartTime();

	/**
	 * this method is invoked when thread is terminated by user interaction
	 */
	public abstract void terminated();

	public ChildThread() {
		super(group, null, "cfthread-" + (count < 0 ? count = 0 : count++));
	}
}