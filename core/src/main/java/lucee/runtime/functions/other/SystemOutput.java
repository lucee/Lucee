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
/**
 * Implements the CFML Function writeoutput
 */
package lucee.runtime.functions.other;

import java.io.PrintStream;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.dynamicEvaluation.Serialize;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

public final class SystemOutput implements Function {
	public static boolean call(PageContext pc, Object obj) throws PageException {
		return call(pc, obj, false, false);
	}

	public static boolean call(PageContext pc, Object obj, boolean addNewLine) throws PageException {
		return call(pc, obj, addNewLine, false);
	}

	public static boolean call(PageContext pc, Object obj, boolean addNewLine, boolean doErrorStream) throws PageException {
		String string;
		if (Decision.isSimpleValue(obj)) string = Caster.toString(obj);
		else {
			try {
				string = Serialize.call(pc, obj);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				string = obj.toString();
			}
		}
		PrintStream stream = CFMLEngineImpl.CONSOLE_OUT;
		// string+=":"+Thread.currentThread().getId();
		if (doErrorStream) stream = CFMLEngineImpl.CONSOLE_ERR;
		if (string != null) {
			if (StringUtil.indexOfIgnoreCase(string, "<print-stack-trace>") != -1) {
				String st = ExceptionUtil.getStacktrace(new Exception("Stack trace"), false);
				string = StringUtil.replace(string, "<print-stack-trace>", "\n" + st + "\n", true).trim();
			}
			if (StringUtil.indexOfIgnoreCase(string, "<hash-code>") != -1) {
				String st = obj.hashCode() + "";
				string = StringUtil.replace(string, "<hash-code>", st, true).trim();
			}
		}
		if (addNewLine) stream.println(string);
		else stream.print(string);

		return true;
	}
}