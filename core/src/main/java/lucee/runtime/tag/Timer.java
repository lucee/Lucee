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

public final class Timer extends BodyTagImpl {

	private static final int TYPE_DEBUG = 0;
	private static final int TYPE_INLINE = 1;
	private static final int TYPE_OUTLINE = 2;
	private static final int TYPE_COMMENT = 3;

	private String label = "";
	private int type = TYPE_DEBUG;
	private long time;

	@Override
	public void release() {
		super.release();
		type = TYPE_DEBUG;
		label = "";
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
		else throw new ApplicationException("invalid value [" + strType + "] for attribute [type], valid values are [comment,debug,inline,outline]");
	}

	@Override
	public int doStartTag() {
		time = System.currentTimeMillis();
		if (TYPE_OUTLINE == type) {
			try {
				pageContext.write("<fieldset class=\"cftimer\">");
			}
			catch (IOException e) {}
		}
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws PageException {
		try {
			_doEndTag();
		}
		catch (IOException e) {}
		return EVAL_PAGE;
	}

	public void _doEndTag() throws IOException {
		long exe = (System.currentTimeMillis() - time);
		if (TYPE_INLINE == type) {

			pageContext.write("" + label + ": " + exe + "ms");
		}
		else if (TYPE_OUTLINE == type) {
			pageContext.write("<legend align=\"top\">" + label + ": " + exe + "ms</legend></fieldset>");
		}
		else if (TYPE_COMMENT == type) {
			pageContext.write("<!-- " + label + ": " + exe + "ms -->");
		}
		else if (TYPE_DEBUG == type) {

			if (pageContext.getConfig().debug()) {
				PageSource curr = pageContext.getCurrentTemplatePageSource();
				pageContext.getDebugger().addTimer(label, exe, curr == null ? "unknown template" : curr.getDisplayPath());
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