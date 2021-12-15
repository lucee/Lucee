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
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.util.KeyConstants;

// TODO tag textarea
// attribute html macht irgendwie keinen sinn, aber auch unter neo nicht

public final class Textarea extends Input implements BodyTag {
	private static final String BASE_PATH = null; // TODO
	private static final String STYLE_XML = null;
	private static final String TEMPLATE_XML = null;
	private static final String SKIN = "default";
	private static final String TOOLBAR = "default";

	private static final int WRAP_OFF = 0;
	private static final int WRAP_HARD = 1;
	private static final int WRAP_SOFT = 2;
	private static final int WRAP_PHYSICAL = 3;
	private static final int WRAP_VIRTUAL = 4;

	private BodyContent bodyContent = null;

	private String basepath = BASE_PATH;
	private String fontFormats = null;
	private String fontNames = null;
	private String fontSizes = null;

	private boolean html = false;
	private boolean richText = false;
	private String skin = SKIN;
	private String stylesXML = STYLE_XML;
	private String templatesXML = TEMPLATE_XML;
	private String toolbar = TOOLBAR;
	private boolean toolbarOnFocus = false;
	private int wrap = WRAP_OFF;

	@Override
	public void release() {
		super.release();
		bodyContent = null;

		basepath = BASE_PATH;
		fontFormats = null;
		fontNames = null;
		fontSizes = null;

		html = false;
		richText = false;
		skin = SKIN;
		stylesXML = STYLE_XML;
		templatesXML = TEMPLATE_XML;
		toolbar = TOOLBAR;
		toolbarOnFocus = false;
		wrap = WRAP_OFF;
	}

	public void setCols(double cols) throws PageException {
		attributes.set("cols", Caster.toString(cols));
	}

	public void setRows(double rows) throws PageException {
		attributes.set("rows", Caster.toString(rows));
	}

	public void setBasepath(String basepath) {
		this.basepath = basepath;
	}

	public void setFontFormats(String fontFormats) {
		this.fontFormats = fontFormats;
	}

	public void setFontNames(String fontNames) {
		this.fontNames = fontNames;
	}

	public void setFontSizes(String fontSizes) {
		this.fontSizes = fontSizes;
	}

	public void setHtml(boolean html) {
		this.html = html;
	}

	public void setRichtext(boolean richText) {
		this.richText = richText;
	}

	public void setSkin(String skin) {
		this.skin = skin;
	}

	public void setStylesxml(String stylesXML) {
		this.stylesXML = stylesXML;
	}

	public void setTemplatesxml(String templatesXML) {
		this.templatesXML = templatesXML;
	}

	public void setToolbar(String toolbar) {
		this.toolbar = toolbar;
	}

	public void setToolbaronfocus(boolean toolbarOnFocus) {
		this.toolbarOnFocus = toolbarOnFocus;
	}

	public void setWrap(String strWrap) throws ExpressionException {
		strWrap = strWrap.trim().toLowerCase();
		if ("hard".equals(strWrap)) wrap = WRAP_HARD;
		else if ("soft".equals(strWrap)) wrap = WRAP_SOFT;
		else if ("off".equals(strWrap)) wrap = WRAP_OFF;
		else if ("physical".equals(strWrap)) wrap = WRAP_PHYSICAL;
		else if ("virtual".equals(strWrap)) wrap = WRAP_VIRTUAL;
		else throw new ExpressionException("invalid value [" + strWrap + "] for attribute wrap, valid values are [hard,soft,off,physical,virtual]");
	}

	@Override
	void draw() throws IOException, PageException {

		// value
		String attrValue = null;
		String bodyValue = null;
		String value = "";
		if (bodyContent != null) bodyValue = bodyContent.getString();
		if (attributes.containsKey(KeyConstants._value)) attrValue = Caster.toString(attributes.get(KeyConstants._value, null));

		// check values
		if (!StringUtil.isEmpty(bodyValue) && !StringUtil.isEmpty(attrValue)) {
			throw new ApplicationException("the value of tag can't be set twice (tag body and attribute value)");
		}
		else if (!StringUtil.isEmpty(bodyValue)) {
			value = enc(bodyValue);
		}
		else if (!StringUtil.isEmpty(attrValue)) {
			value = enc(attrValue);
		}
		// id
		if (StringUtil.isEmpty(attributes.get(KeyConstants._id, null))) attributes.set(KeyConstants._id, StringUtil.toVariableName((String) attributes.get(KeyConstants._name)));

		// start output
		pageContext.forceWrite("<textarea");

		Iterator<Entry<Key, Object>> it = attributes.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			pageContext.forceWrite(" ");
			pageContext.forceWrite(e.getKey().getString());
			pageContext.forceWrite("=\"");
			pageContext.forceWrite(enc(Caster.toString(e.getValue())));
			pageContext.forceWrite("\"");
		}

		if (passthrough != null) {
			pageContext.forceWrite(" ");
			pageContext.forceWrite(passthrough);
		}
		pageContext.forceWrite(">");
		pageContext.forceWrite(value);
		pageContext.forceWrite("</textarea>");
	}

	@Override
	public int doStartTag() {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public void setBodyContent(BodyContent bodyContent) {
		this.bodyContent = bodyContent;
	}

	@Override
	public void doInitBody() throws JspException {
	}

	@Override
	public int doAfterBody() throws JspException {
		return SKIP_BODY;
	}

	public void hasBody(boolean hasBody) {

	}
}