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
package lucee.runtime.tag;

import java.io.IOException;

import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;

/**
 * Stops the time from starttag to endtag
 *
 *
 *
 **/
public final class Stopwatch extends BodyTagImpl {

	private String label;
	private long time;
	private String variable;

	@Override
	public void release() {
		super.release();
		label = null;
		time = 0L;
		variable = null;
	}

	/**
	 * Label of the Stopwatch
	 * 
	 * @param label sets the Label of the Stopwatch
	 **/
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Variable Name to write result to it
	 * 
	 * @param variable variable name
	 */
	public void setVariable(String variable) {
		this.variable = variable;
	}

	@Override
	public int doStartTag() {
		time = System.currentTimeMillis();
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws PageException {
		long exe = (System.currentTimeMillis() - time);

		if (variable != null) {
			pageContext.setVariable(variable, new Double(exe));
		}
		else {
			DumpTable table = new DumpTable("#ff9900", "#ffcc00", "#000000");
			table.appendRow(1, new SimpleDumpData(label == null ? "Stopwatch" : label), new SimpleDumpData(exe));
			DumpWriter writer = pageContext.getConfig().getDefaultDumpWriter(DumpWriter.DEFAULT_RICH);
			try {

				pageContext.forceWrite(writer.toString(pageContext, table, true));
			}
			catch (IOException e) {
			}
		}
		return EVAL_PAGE;
	}

	@Override
	public void doInitBody() {

	}

	@Override
	public int doAfterBody() {
		return SKIP_BODY;
	}
}