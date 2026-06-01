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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.concurrency;

import lucee.commons.io.SystemUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

/**
 * parses and represents the execution mode of the "parallel" argument used by the iteration functions
 * (each, map, filter, some, every).
 */
public final class ParallelUtil {

	/** not parallel, the closure is executed sequentially on the calling thread */
	public static final short PARALLEL_NONE = 0;
	/** parallel execution on regular (platform) threads */
	public static final short PARALLEL_THREAD = 1;
	/** parallel execution on virtual threads */
	public static final short PARALLEL_VIRTUAL = 2;

	private ParallelUtil() {
	}

	/**
	 * translates the "parallel" argument into one of the PARALLEL_* modes.
	 * 
	 * <ul>
	 * <li>"none" (or null/empty) -&gt; {@link #PARALLEL_NONE}</li>
	 * <li>"thread"/"threads"/"platform"/"regular" -&gt; {@link #PARALLEL_THREAD}</li>
	 * <li>"virtual" -&gt; {@link #PARALLEL_VIRTUAL}</li>
	 * </ul>
	 * 
	 * the boolean values "true"/"false" are deprecated and only kept for backward compatibility; "false"
	 * maps to {@link #PARALLEL_NONE} and "true" follows the same default as the cfthread tag's [virtual]
	 * attribute ({@link #PARALLEL_THREAD}, or {@link #PARALLEL_VIRTUAL} when "lucee.thread.virtual" is enabled).
	 * 
	 * @param parallel the raw argument value
	 * @return the matching parallel mode
	 * @throws PageException if the value is not a recognised mode
	 */
	public static short toParallel(String parallel) throws PageException {
		if (parallel == null) return PARALLEL_NONE;
		String str = parallel.trim();
		if (str.isEmpty()) return PARALLEL_NONE;

		// "true"/"false" are deprecated, only kept for backward compatibility, the string modes should be used instead
		Boolean b = Caster.toBoolean(str, null);
		if (b != null) {
			if (!b.booleanValue()) return PARALLEL_NONE;
			// "true" follows the same default as the cfthread tag's [virtual] attribute
			return Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.thread.virtual", "false"), false) ? PARALLEL_VIRTUAL : PARALLEL_THREAD;
		}

		switch (str.toLowerCase()) {
		case "none":
		case "sequential":
		case "single":
			return PARALLEL_NONE;
		case "thread":
		case "threads":
		case "platform":
		case "regular":
			return PARALLEL_THREAD;
		case "virtual":
			return PARALLEL_VIRTUAL;
		default:
			throw new ExpressionException("invalid value [" + parallel + "] for argument [parallel], valid values are [none, thread, virtual]");
		}
	}

	/**
	 * @param parallel a parallel mode
	 * @return true when the mode runs the closure concurrently (thread or virtual)
	 */
	public static boolean isParallel(short parallel) {
		return parallel != PARALLEL_NONE;
	}

	/**
	 * @param parallel a parallel mode
	 * @return true when the mode uses virtual threads
	 */
	public static boolean isVirtual(short parallel) {
		return parallel == PARALLEL_VIRTUAL;
	}
}
