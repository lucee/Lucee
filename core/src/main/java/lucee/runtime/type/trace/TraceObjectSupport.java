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
package lucee.runtime.type.trace;

import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.debug.Debugger;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.type.Array;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;

public class TraceObjectSupport implements TraceObject {

	protected Object o;
	protected Debugger debugger;

	protected int type;
	protected String category;
	protected String text;

	public TraceObjectSupport(Debugger debugger, Object o, int type, String category, String text) {
		this.o = o;
		// this.log=log;
		this.type = type;
		this.category = category;
		this.text = text;
		this.debugger = debugger;
	}

	@Override

	public String toString() {

		return o.toString();
	}

	@Override
	public boolean equals(Object obj) {

		return o.equals(obj);
	}

	protected void log() {
		try {
			log(debugger, type, category, text, null, null);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	protected void log(Object varName) {
		try {
			log(debugger, type, category, text, varName.toString(), null);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	protected void log(Object varName, Object varValue) {
		try {
			log(debugger, type, category, text, varName.toString(), varValue.toString());
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public static void log(Debugger debugger, int type, String category, String text, String varName, String varValue) {

		StackTraceElement[] traces = new Exception().getStackTrace();

		int line = 0;
		String template = null;
		StackTraceElement trace = null;
		for (int i = 0; i < traces.length; i++) {
			trace = traces[i];
			template = trace.getFileName();
			if (trace.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java") || isDumpTemplate(template)) continue;
			line = trace.getLineNumber();
			break;
		}
		// print.e(t);
		if (line == 0) return;
		String action = type(traces[2].getMethodName());
		if (debugger != null) debugger.addTrace(type, category, text, template, line, action, varName, varValue);

	}

	private static boolean isDumpTemplate(String template) {
		template = ResourceUtil.removeExtension(template, template).toLowerCase();
		return template.endsWith("dump");
	}

	protected static String type(String type) {
		if (type.equals("appendEL")) return "append";
		if (type.equals("setEL")) return "set";
		if (type.equals("removeEL")) return "remove";
		if (type.equals("keys")) return "list";
		if (type.equals("toDumpData")) return "dump";

		return type;
	}

	protected PageContext pc() {
		return ThreadLocalPageContext.get();
	}

	public static TraceObject toTraceObject(Debugger debugger, Object obj, int type, String category, String text) {
		if (obj instanceof TraceObject) return (TraceObject) obj;
		else if (obj instanceof UDF) return new TOUDF(debugger, (UDF) obj, type, category, text);
		else if (obj instanceof Query) return new TOQuery(debugger, (Query) obj, type, category, text);
		else if (obj instanceof Array) return new TOArray(debugger, (Array) obj, type, category, text);
		else if (obj instanceof Struct) return new TOStruct(debugger, (Struct) obj, type, category, text);
		else if (obj instanceof DateTime) return new TODateTime(debugger, (DateTime) obj, type, category, text);

		return new TOObjects(debugger, obj, type, category, text);
	}

}