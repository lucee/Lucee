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
package lucee.runtime.timer;

/**
 * Implementation of a simple Stopwatch
 */
public class Stopwatch {

	public static final int UNIT_MILLI = 1;
	public static final int UNIT_NANO = 2;
	// public static final int UNIT_MICRO=4;

	private long start;
	private int count = 0;
	private long total = 0;
	boolean isRunning;
	private boolean useNano;

	public Stopwatch(int unit) {
		useNano = unit == UNIT_NANO;
	}

	/**
	 * start the watch
	 */
	public void start() {
		isRunning = true;
		start = _time();
	}

	private long _time() {
		return useNano ? System.nanoTime() : System.currentTimeMillis();
	}

	/**
	 * stops the watch
	 * 
	 * @return returns the current time or 0 if watch not was running
	 */
	public long stop() {
		if (isRunning) {
			long time = _time() - start;
			total += time;
			count++;
			isRunning = false;
			return time;

		}
		return 0;
	}

	/**
	 * @return returns the current time or 0 if watch is not running
	 */
	public long time() {
		if (isRunning) return _time() - start;
		return 0;
	}

	/**
	 * @return returns the total elapsed time
	 */
	public long totalTime() {
		return total + time();
	}

	/**
	 * @return returns how many start and stop was making
	 */
	public int count() {
		return count;
	}

	/**
	 * resets the stopwatch
	 */
	public void reset() {
		start = 0;
		count = 0;
		total = 0;
		isRunning = false;
	}
}