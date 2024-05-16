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

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import lucee.commons.color.ColorCaster;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;

public class ClassicHTMLDumpWriter implements DumpWriter {

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

		String borderColor = table.getHighLightColor();
		String h1Color = table.getHighLightColor();
		String h2Color = table.getNormalColor();
		String normalColor = "white";
		try {
			Color color = ColorCaster.toColor(table.getHighLightColor());
			borderColor = ColorCaster.toHexString(color.darker().darker());
		}
		catch (ExpressionException e) {
		}

		String id = "_classic" + (count++);
		// prepare data
		DumpRow[] rows = table.getRows();
		int cols = 0;
		for (int i = 0; i < rows.length; i++)
			if (rows[i].getItems().length > cols) cols = rows[i].getItems().length;

		if (!inside) {
			writer.write("<script>");
			writer.write("function dumpOC(name){");
			writer.write("var tds=document.all?document.getElementsByTagName('tr'):document.getElementsByName('_'+name);");
			writer.write("var s=null;");
			writer.write("name='_'+name;");
			writer.write("for(var i=0;i<tds.length;i++) {");
			writer.write("if(document.all && tds[i].name!=name)continue;");
			writer.write("s=tds[i].style;");
			writer.write("if(s.display=='none') s.display='';");
			writer.write("else s.display='none';");
			writer.write("}");
			writer.write("}");
			writer.write("</script>");
		}

		TemplateLine tl = null;
		if (!inside) tl = SystemUtil.getCurrentContext(pc);
		String context = tl == null ? "" : tl.toString();

		writer.write(
				"<table" + (table.getWidth() != null ? " width=\"" + table.getWidth() + "\"" : "") + "" + (table.getHeight() != null ? " height=\"" + table.getHeight() + "\"" : "")
						+ " cellpadding=\"3\" cellspacing=\"0\" style=\"font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;font-size : 10;color :" + table.getFontColor()
						+ " ;empty-cells:show; border : 1px solid " + borderColor + ";\" >");

		// header
		if (!StringUtil.isEmpty(table.getTitle())) {
			writer.write("<tr><td title=\"" + context + "\" onclick=\"dumpOC('" + id + "')\" colspan=\"" + cols + "\" bgcolor=\"" + h1Color
					+ "\" style=\"color:white;border : 1px solid " + borderColor + ";\">");
			// isSetContext=true;
			String contextPath = "";
			pc = ThreadLocalPageContext.get(pc);
			if (pc != null) {
				contextPath = pc.getHttpServletRequest().getContextPath();
				if (contextPath == null) contextPath = "";
			}

			writer.write("<span style=\"font-weight:bold;\">" + (!StringUtil.isEmpty(table.getTitle()) ? table.getTitle() : "") + "</span>"
					+ (!StringUtil.isEmpty(table.getComment()) ? "<br>" + table.getComment() : "") + "</td></tr>");
		}
		else id = null;

		// items
		DumpData value;
		for (int i = 0; i < rows.length; i++) {
			if (id != null) writer.write("<tr name=\"_" + id + "\">");
			else writer.write("<tr>");
			DumpData[] items = rows[i].getItems();
			int hType = rows[i].getHighlightType();
			int comperator = 1;
			for (int y = 0; y < cols; y++) {
				if (y <= items.length - 1) value = items[y];
				else value = new SimpleDumpData("&nbsp;");
				boolean highLightIt = hType == -1 || ((hType & (comperator)) > 0);
				comperator *= 2;
				if (value == null) value = new SimpleDumpData("null");
				// else if(value.equals(""))value="&nbsp;";
				if (!inside) {
					writer.write("<td valign=\"top\" style=\"border : 1px solid " + borderColor + ";\" title=\"" + context + "\"");
				}
				else writer.write("<td valign=\"top\"");
				writer.write(" bgcolor=\"" + ((highLightIt) ? h2Color : normalColor) + "\" style=\"border : 1px solid " + borderColor + ";empty-cells:show;\">");
				writeOut(pc, value, writer, expand, true);
				writer.write("</td>");
			}
			writer.write("</tr>");
		}

		// footer
		writer.write("</table>");
		if (!expand) writer.write("<script>dumpOC('" + id + "');</script>");
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