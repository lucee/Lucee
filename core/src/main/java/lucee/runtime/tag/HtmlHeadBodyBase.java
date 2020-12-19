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
import java.util.Map;
import java.util.TreeMap;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagTryCatchFinallyImpl;
import lucee.runtime.op.Caster;

/**
 * base class for both cfhtmlhead and cfhtmlbody
 */
public abstract class HtmlHeadBodyBase extends BodyTagTryCatchFinallyImpl {

	private final static String REQUEST_ATTRIBUTE_PREFIX = "REQUEST_ATTRIBUTE_IDMAP_";

	/**
	 * The text to add to the 'head' area of an HTML page. Everything inside the quotation marks is
	 * placed in the 'head' section
	 */
	protected String text = null;
	protected String variable = null;

	private String action = null;
	private String id = null;
	private boolean force = getDefaultForce();

	@Override
	public void release() {
		super.release();
		text = null;
		variable = null;
		action = null;
		id = null;
		force = getDefaultForce();
	}

	public abstract boolean getDefaultForce();

	public abstract String getTagName();

	public abstract void actionAppend() throws IOException, ApplicationException;

	public abstract void actionFlush() throws IOException;

	public abstract void actionRead() throws IOException, PageException;

	public abstract void actionReset() throws IOException;

	public abstract void actionWrite() throws IOException, ApplicationException;

	/**
	 * @param variable the variable to set
	 */
	public void setVariable(String variable) {
		this.variable = variable;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		if (StringUtil.isEmpty(action, true)) return;

		this.action = action.trim().toLowerCase();
	}

	/**
	 * set the value text The text to add to the 'head' area of an HTML page. Everything inside the
	 * quotation marks is placed in the 'head' section
	 *
	 * @param text value to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	@Override
	public int doStartTag() throws PageException {

		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws PageException {
		processTag();
		return SKIP_BODY;
	}

	@Override
	public int doAfterBody() throws PageException {

		if (StringUtil.isEmpty(text) && bodyContent != null) {
			text = bodyContent.getString();
		}
		if (bodyContent != null) bodyContent.clearBody();

		return SKIP_BODY;
	}

	protected void processTag() throws PageException {

		try {

			if (StringUtil.isEmpty(action, true) || action.equals("append")) {
				required(getTagName(), "text", text);
				if (isValid()) actionAppend();
			}
			else if (action.equals("reset")) {
				resetIdMap();
				actionReset();
			}
			else if (action.equals("write")) {
				required(getTagName(), "text", text);
				resetIdMap();
				if (isValid()) // call isValid() to register the id if set
					actionWrite();
			}
			else if (action.equals("read")) actionRead();
			else if (action.equals("flush")) actionFlush();
			else throw new ApplicationException("invalid value [" + action + "] for attribute [action]", "supported actions are [append, read, reset, write, flush]");
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 *
	 * @return - true if the id was not set or was set and was not used yet in the request. if it was
	 *         not set -- register it for future calls of the tag
	 */
	protected boolean isValid() {

		if (!force && pageContext instanceof PageContextImpl && ((PageContextImpl) pageContext).isSilent()) return false;

		if (StringUtil.isEmpty(id)) return true;

		Map m = getIdMap();

		boolean result = !m.containsKey(id);

		if (result) m.put(id, Boolean.TRUE);

		return result;
	}

	protected Map getIdMap() {

		String reqAttr = REQUEST_ATTRIBUTE_PREFIX + getTagName();

		Map result = (Map) pageContext.getRequest().getAttribute(reqAttr);

		if (result == null) {

			result = new TreeMap(String.CASE_INSENSITIVE_ORDER);
			pageContext.getRequest().setAttribute(reqAttr, result);
		}

		return result;
	}

	protected void resetIdMap() {

		String reqAttr = REQUEST_ATTRIBUTE_PREFIX + getTagName();

		pageContext.getRequest().setAttribute(reqAttr, null);
	}
}
