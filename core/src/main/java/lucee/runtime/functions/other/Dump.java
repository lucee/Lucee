/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
/**
 * Implements the CFML Function dump
 */
package lucee.runtime.functions.other;

import java.io.IOException;
import java.util.Set;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public final class Dump implements Function {

	private static final int OUTPUT_TYPE_NONE = 0;
	private static final int OUTPUT_TYPE_BROWSER = 1;
	private static final int OUTPUT_TYPE_CONSOLE = 2;
	private static final int OUTPUT_TYPE_RESOURCE = 3;

	// private static final int FORMAT_TYPE_HTML = 0;
	// private static final int FORMAT_TYPE_TEXT = 1;

	public static String call(PageContext pc, Object object) throws PageException {
		return call(pc, object, null, true, 9999, null, null, null, null, 9999, true, true);
	}

	public static String call(PageContext pc, Object object, String label) throws PageException {
		return call(pc, object, label, true, 9999, null, null, null, null, 9999, true, true);
	}

	public static String call(PageContext pc, Object object, String label, boolean expand) throws PageException {
		return call(pc, object, label, expand, 9999, null, null, null, null, 9999, true, true);
	}

	public static String call(PageContext pc, Object object, String label, boolean expand, double maxLevel) throws PageException {
		return call(pc, object, label, expand, maxLevel, null, null, null, null, 9999, true, true);
	}

	public static String call(PageContext pc, Object object, String label, boolean expand, double maxLevel, String show) throws PageException {
		return call(pc, object, label, expand, maxLevel, show, null, null, null, 9999, true, true);
	}

	public static String call(PageContext pc, Object object, String label, boolean expand, double maxLevel, String show, String hide) throws PageException {
		return call(pc, object, label, expand, maxLevel, show, hide, null, null, 9999, true, true);
	}

	public static String call(PageContext pc, Object object, String label, boolean expand, double maxLevel, String show, String hide, String output) throws PageException {
		return call(pc, object, label, expand, maxLevel, show, hide, output, null, 9999, true, true);
	}

	public static String call(PageContext pc, Object object, String label, boolean expand, double maxLevel, String show, String hide, String output, String format)
			throws PageException {
		return call(pc, object, label, expand, maxLevel, show, hide, output, format, 9999, true, true);
	}

	public static String call(PageContext pc, Object object, String label, boolean expand, double maxLevel, String show, String hide, String output, String format, double keys)
			throws PageException {
		return call(pc, object, label, expand, maxLevel, show, hide, output, format, keys, true, true);
	}

	public static String call(PageContext pc, Object object, String label, boolean expand, double maxLevel, String show, String hide, String output, String format, double keys,
			boolean metainfo) throws PageException {
		return call(pc, object, label, expand, maxLevel, show, hide, output, format, keys, metainfo, true);
	}

	public static String call(PageContext pc, Object object, String label, boolean expand, double maxLevel, String show, String hide, String output, String format, double keys,
			boolean metainfo, boolean showUDFs) throws PageException {
		if (show != null && "all".equalsIgnoreCase(show.trim())) show = null;
		if (hide != null && "all".equalsIgnoreCase(hide.trim())) hide = null;

		// String context = getContext();
		// PageContext pcc = pc;
		try {

			// output
			int defType = DumpWriter.DEFAULT_RICH;
			int outputType = OUTPUT_TYPE_NONE;
			Resource outputRes = null;
			if (!StringUtil.isEmpty(output, true)) {
				output = output.trim();
				if ("browser".equalsIgnoreCase(output)) {
					outputType = OUTPUT_TYPE_BROWSER;
					defType = DumpWriter.DEFAULT_RICH;
				}
				else if ("console".equalsIgnoreCase(output)) {
					outputType = OUTPUT_TYPE_CONSOLE;
					defType = DumpWriter.DEFAULT_PLAIN;
				}
				else {
					outputType = OUTPUT_TYPE_RESOURCE;
					defType = DumpWriter.DEFAULT_RICH;
					outputRes = ResourceUtil.toResourceNotExisting(pc, output);
				}
			}

			// format
			DumpWriter writer = pc.getConfig().getDumpWriter(format, defType);

			Set<String> setShow = (show != null) ? ListUtil.listToSet(show.toLowerCase(), ",", true) : null;
			Set<String> setHide = (hide != null) ? ListUtil.listToSet(hide.toLowerCase(), ",", true) : null;

			DumpProperties properties = new DumpProperties((int) maxLevel, setShow, setHide, (int) keys, metainfo, showUDFs);
			DumpData dd = DumpUtil.toDumpData(object, pc, (int) maxLevel, properties);

			if (!StringUtil.isEmpty(label)) {
				DumpTable table = new DumpTable("#ffffff", "#cccccc", "#000000");
				table.appendRow(1, new SimpleDumpData(label));
				// table.appendRow(1,new SimpleDumpData(getContext()));
				table.appendRow(0, dd);
				dd = table;
			}

			boolean isText = "text".equalsIgnoreCase(format);// formatType==FORMAT_TYPE_TEXT
			if (OUTPUT_TYPE_BROWSER == outputType || outputType == OUTPUT_TYPE_NONE) {
				if (isText) pc.forceWrite("<pre>");
				pc.forceWrite(writer.toString(pc, dd, expand));
				if (isText) pc.forceWrite("</pre>");
			}
			else if (OUTPUT_TYPE_CONSOLE == outputType) System.out.println(writer.toString(pc, dd, expand));
			else if (OUTPUT_TYPE_RESOURCE == outputType)
				IOUtil.write(outputRes, writer.toString(pc, dd, expand) + "\n************************************************************************************\n",
						((PageContextImpl) pc).getResourceCharset(), true);

		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}

		return "";
	}
	/*
	 * public static String getContext() { //Throwable cause = t.getCause(); StackTraceElement[] traces
	 * = new Exception().getStackTrace();
	 * 
	 * int line=0; String template; StackTraceElement trace=null; for(int i=0;i<traces.length;i++) {
	 * trace=traces[i]; template=trace.getFileName(); if((line=trace.getLineNumber())<=0 ||
	 * template==null || ResourceUtil.getExtension(template,"").equals("java")) continue; return
	 * template+":"+line; } return null; }
	 */
}