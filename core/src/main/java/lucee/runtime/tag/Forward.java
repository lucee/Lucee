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

import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;

/**
 * Used to: Abort the processing of the currently executing CFML custom tag, exit the template
 * within the currently executing CFML custom tag and reexecute a section of code within the
 * currently executing CFML custom tag
 *
 **/
public final class Forward extends TagImpl {

	private String template;

	/**
	 * @param template The template to set.
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	@Override
	public int doStartTag() throws PageException {
		try {
			pageContext.forward(template);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

		/*
		 * RequestDispatcher disp = pageContext. getHttpServletRequest().getRequestDispatcher(template); try
		 * { disp.forward(pageContext. getHttpServletRequest(),pageContext. getHttpServletResponse()); }
		 * catch (Exception e) { throw Caster.toPageException(e); }
		 */
		return SKIP_BODY;
	}

}