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
package lucee.runtime.functions.system;

import java.util.Iterator;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.arrays.ArraySlice;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.KeyConstants;

/**
 * returns the root of this current Page Context
 */
public final class CallStackGet implements Function {

	private static final long serialVersionUID = -5853145189662102420L;
	static final Collection.Key LINE_NUMBER = KeyImpl.getInstance("LineNumber");

	public static Object call(PageContext pc) {
		Array arr = new ArrayImpl();
		_getTagContext(pc, arr, new Exception("Stack trace"), LINE_NUMBER);
		return arr;
	}

	public static Object call(PageContext pc, String type) throws PageException {
		return call(pc, type, 0.0, 0.0);
	}

	public static Object call(PageContext pc, String type, double offset) throws PageException {
		return call(pc, type, offset, 0.0);
	}

	public static Object call(PageContext pc, String type, double offset, double maxFrames) throws PageException {
		Array arr = (Array) call(pc);

		if (offset > 0 || maxFrames > 0) {
			int sliceFrom = (int)offset + 1;
			int sliceTo   = (maxFrames > 0) ? (int)(maxFrames + offset) : 0;
			arr = ArraySlice.get(arr, sliceFrom, sliceTo);
		}

		if (type.equalsIgnoreCase("array")) return arr;

		if (type.equalsIgnoreCase("json")) {
			try {
				return new JSONConverter(true, null).serialize(pc, arr, SerializationSettings.SERIALIZE_AS_ROW);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				throw Caster.toPageException(t);
			}
		}

		StringBuilder sb = new StringBuilder(64 * arr.size());
		Struct struct;
		String func;

		Iterator it = arr.valueIterator();

		if (type.equalsIgnoreCase("text") || type.equalsIgnoreCase("string")) {
			while (it.hasNext()) {
				struct = (Struct) it.next();
				sb.append((String) struct.get(KeyConstants._template));
				func = (String) struct.get(KeyConstants._function);
				if (!func.isEmpty()) {
					sb.append('.').append(func).append("()");
				}
				sb.append(':').append(((Double) struct.get(LINE_NUMBER)).intValue());
				if (it.hasNext()) sb.append("; ");
			}
			return sb.toString();
		}

		if (type.equalsIgnoreCase("html")) {
			sb.append("<ul class='-lucee-array'>");
			while (it.hasNext()) {
				struct = (Struct) it.next();
				sb.append("<li>");
				sb.append((String) struct.get(KeyConstants._template));
				func = (String) struct.get(KeyConstants._function);
				if (!func.isEmpty()) {
					sb.append('.').append(func).append("()");
				}
				sb.append(':').append(((Double) struct.get(LINE_NUMBER)).intValue());
				sb.append("</li>");
			}
			sb.append("</ul>");
			return sb.toString();
		}

		throw new FunctionException(pc, CallStackGet.class.getSimpleName(), 1, "type", "Argument type [" + type + "] is not valid.  Valid types are: [array], text, html, json.");
	}

	public static void _getTagContext(PageContext pc, Array tagContext, Throwable t, Collection.Key lineNumberName) {
		// Throwable root = t.getRootCause();
		Throwable cause = t.getCause();
		if (cause != null) _getTagContext(pc, tagContext, cause, lineNumberName);
		StackTraceElement[] traces = t.getStackTrace();

		UDF[] udfs = ((PageContextImpl) pc).getUDFs();

		int line = 0;
		String template;
		Struct item;
		StackTraceElement trace = null;
		String functionName, methodName;
		int index = udfs.length - 1;
		for (int i = 0; i < traces.length; i++) {
			trace = traces[i];
			template = trace.getFileName();
			if (trace.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java")) continue;
			methodName = trace.getMethodName();
			if (methodName != null && methodName.startsWith("udfCall") && index > -1) functionName = udfs[index--].getFunctionName();

			else functionName = "";

			item = new StructImpl();
			line = trace.getLineNumber();
			item.setEL(KeyConstants._function, functionName);
			/*
			 * template is now an absolute path try { template=ExpandPath.call(pc, template); } catch
			 * (PageException e) {}
			 */
			item.setEL(KeyConstants._template, abs((PageContextImpl) pc, template));
			item.setEL(lineNumberName, new Double(line));
			tagContext.appendEL(item);
		}
	}

	private static String abs(PageContextImpl pc, String template) {
		ConfigWeb config = pc.getConfig();

		Resource res = config.getResource(template);
		if (res.exists()) return template;

		PageSource ps = pc == null ? null : pc.getPageSource(template);
		res = ps == null ? null : ps.getPhyscalFile();
		if (res == null || !res.exists()) {
			res = config.getResource(ps.getDisplayPath());
			if (res != null && res.exists()) return res.getAbsolutePath();
		}
		else return res.getAbsolutePath();
		return template;
	}
}