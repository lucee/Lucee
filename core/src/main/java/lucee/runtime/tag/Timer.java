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

import lucee.runtime.PageSource;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
//import lucee.runtime.debug.DebuggerPro;

public final class Timer extends BodyTagImpl {

	private static final int TYPE_DEBUG = 0;
	private static final int TYPE_INLINE = 1;
	private static final int TYPE_OUTLINE = 2;
	private static final int TYPE_COMMENT = 3;

	private static final int UNIT_NANO = 1;
	private static final int UNIT_MILLI = 2;
	private static final int UNIT_MICRO = 4;
	private static final int UNIT_SECOND = 8;

	private String label = "";
	private int type = TYPE_DEBUG;
	private int unit = UNIT_MILLI;
	private String unitDesc = "ms";
	//private double time;
	private long time;
	private long exe;
	private String variable;

	@Override
	public void release() {
		super.release();
		type = TYPE_DEBUG;
		unit = UNIT_MILLI;
		label = "";
		unitDesc = "ms";
		variable = null;
	}	

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @param type the type to set
	 * @throws ApplicationException
	 */
	public void setType(String strType) throws ApplicationException {
		strType = strType.toLowerCase().trim();
		if ("comment".equals(strType)) type = TYPE_COMMENT;
		else if ("debug".equals(strType)) type = TYPE_DEBUG;
		else if ("inline".equals(strType)) type = TYPE_INLINE;
		else if ("outline".equals(strType)) type = TYPE_OUTLINE;
		else throw new ApplicationException("Tag [timer] has an invalid value [" + strType + "] for attribute [type], valid values are [comment, debug, inline, outline]");
	}

	/**
	 * @param unit the unit to set
	 * @throws ApplicationException
	 */
	public void setUnit(String strUnit) throws ApplicationException {
		if (!StringUtil.isEmpty(strUnit, true)) {
			char c = strUnit.charAt(0);
			if (c == 'n' || c == 'N') {
				this.unit = UNIT_NANO;
				this.unitDesc = "ns";
				return;
			} else if (c == 'm' || c == 'M') {
				if ("micro".equalsIgnoreCase(strUnit.trim())) {
					this.unit = UNIT_MICRO;
					this.unitDesc = "us";
					return;
				}
				this.unit = UNIT_MILLI;
				this.unitDesc = "ms"; // default
				return;
			} else if (c == 's' || c == 'S') {
				this.unit = UNIT_SECOND;
				this.unitDesc = "s";
				return;
			}
			throw new ApplicationException("Tag [timer] has an invalid value [" + strUnit + "] for attribute [unit], valid values are [nano, micro, milli, second]");
		} 
		this.unit = UNIT_MILLI;
		this.unitDesc = "ms"; // default
	}

	/**
	 * Set the value variable, tThe name of the variable in which to save the execution time into
	 * tag.
	 * 
	 * @param variable value to set
	 **/
	public void setVariable(String variable) {
		this.variable = variable;
	}

	private long getCurrentTime() {
		switch (this.unit){
			case UNIT_NANO:
				return System.nanoTime();
			case UNIT_MICRO:
				return System.nanoTime() / 1000;
			case UNIT_SECOND:
				return System.currentTimeMillis() / 1000;
			default:
				return System.currentTimeMillis();
		}
	}	

	@Override
	public int doStartTag() {
		time = getCurrentTime();
		if (TYPE_OUTLINE == type) {
			try {
				pageContext.write("<fieldset class=\"cftimer\">");
			}
			catch (IOException e) {
			}
		}
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws PageException {
		try {
			_doEndTag();
		}
		catch (IOException e) {
		}
		return EVAL_PAGE;
	}

	public void _doEndTag() throws IOException, PageException {
		exe = Caster.toLong(getCurrentTime() - time);
		if (!StringUtil.isEmpty(variable, true)) pageContext.setVariable(variable, exe);

		if (TYPE_INLINE == type) {
			pageContext.write("" + label + ": " + time + unitDesc + "");
		}
		else if (TYPE_OUTLINE == type) {
			pageContext.write("<legend align=\"top\">" + label + ": " + time + unitDesc + "</legend></fieldset>");
		}
		else if (TYPE_COMMENT == type) {
			pageContext.write("<!-- " + label + ": " + time + unitDesc + " -->");
		}
		else if (TYPE_DEBUG == type) {
			if (pageContext.getConfig().debug()) {
				PageSource curr = pageContext.getCurrentTemplatePageSource();
				// TODO need to include unitDesc?
				//((DebuggerPro) pageContext.getDebugger()).addTimer(label, exe, curr == null ? "unknown template" : curr.getDisplayPath());
				pageContext.getDebugger().addTimer(label, exe, curr == null ? "unknown template" : curr.getDisplayPath());
				// TODO pass in line number , curr == null ? 0 : curr.getLine()
			}
		}
	}

	@Override
	public void doInitBody() {

	}

	@Override
	public int doAfterBody() {
		return SKIP_BODY;
	}

}