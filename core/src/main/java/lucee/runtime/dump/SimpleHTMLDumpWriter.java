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
package lucee.runtime.dump;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;

public class SimpleHTMLDumpWriter implements DumpWriter {

	private static int count = 0;

	@Override
	public void writeOut(PageContext pc, DumpData data, Writer writer, boolean expand) throws IOException {
		writeOut(pc, data, writer, expand, false);
	}

	private void writeOut(PageContext pc, DumpData data, Writer writer, boolean expand, boolean inside) throws IOException {

		if (data == null) return;
		if (!(data instanceof DumpTable)) {
			writer.write(StringUtil.escapeHTML(data.toString()));
			return;
		}
		DumpTable table = (DumpTable) data;

		// prepare data
		DumpRow[] rows = table.getRows();
		int cols = 0;
		for (int i = 0; i < rows.length; i++)
			if (rows[i].getItems().length > cols) cols = rows[i].getItems().length;

		TemplateLine tl = null;
		if (!inside) tl = SystemUtil.getCurrentContext(null);
		String context = tl == null ? "" : tl.toString();

		if (rows.length == 1 && rows[0].getItems().length == 2) {
			DumpData d = rows[0].getItems()[1];
			if (!(d instanceof DumpTable)) {
				writer.write(StringUtil.escapeHTML(d.toString()));
				return;
			}
		}

		writer.write("<table  cellpadding=\"1\" cellspacing=\"0\" " + (table.getWidth() != null ? " width=\"" + table.getWidth() + "\"" : "") + ""
				+ (table.getHeight() != null ? " height=\"" + table.getHeight() + "\"" : "") + " border=\"1\">");

		// header
		if (!StringUtil.isEmpty(table.getTitle())) {
			writer.write("<tr><td title=\"" + context + "\" colspan=\"" + cols + "\">");
			// isSetContext=true;
			String contextPath = "";
			pc = ThreadLocalPageContext.get(pc);
			if (pc != null) {
				contextPath = pc.getHttpServletRequest().getContextPath();
				if (contextPath == null) contextPath = "";
			}

			writer.write("<b>" + (!StringUtil.isEmpty(table.getTitle()) ? table.getTitle() : "") + "</b>"
					+ (!StringUtil.isEmpty(table.getComment()) ? "<br>" + table.getComment() : "") + "</td></tr>");
		}

		// items
		DumpData value;
		for (int i = 0; i < rows.length; i++) {
			writer.write("<tr>");
			DumpData[] items = rows[i].getItems();
			// int comperator=1;
			for (int y = 0; y < cols; y++) {
				if (y <= items.length - 1) value = items[y];
				else value = new SimpleDumpData("&nbsp;");
				// comperator*=2;
				if (value == null) value = new SimpleDumpData("null");
				// else if(value.equals(""))value="&nbsp;";
				if (!inside) {
					writer.write("<td title=\"" + context + "\">");
				}
				else writer.write("<td>");
				writeOut(pc, value, writer, expand, true);
				writer.write("</td>");
			}
			writer.write("</tr>");
		}

		// footer
		writer.write("</table>");
	}

	@Override
	public String toString(PageContext pc, DumpData data, boolean expand) {
		StringWriter sw = new StringWriter();
		try {
			writeOut(pc, data, sw, expand);
		}
		catch (IOException e) {
			return "";
		}
		return sw.toString();
	}

}