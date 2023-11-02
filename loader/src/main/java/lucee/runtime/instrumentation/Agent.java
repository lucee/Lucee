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
package lucee.runtime.instrumentation;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class Agent {
	private static Instrumentation instrumentation;

	public static void premain(final String agentArgs, final Instrumentation inst) {
		if (inst != null) instrumentation = inst;
	}

	public static void agentmain(final String agentArgs, final Instrumentation inst) {
		if (inst != null) instrumentation = inst;
	}

	public static Instrumentation getInstrumentation() throws IOException {
		if (instrumentation == null) throw new IOException("There is no Instrumentation class available");
		return instrumentation;
	}

	public static Instrumentation getInstrumentation(final Instrumentation defaultValue) {
		if (instrumentation == null) return defaultValue;
		return instrumentation;
	}
}