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

import java.nio.charset.Charset;

import javax.servlet.jsp.tagext.Tag;

import lucee.commons.io.CharsetUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;

/**
 * Specifies one part of a multipart e-mail message. Can only be used in the cfmail tag. You can use
 * more than one cfmailpart tag within a cfmail tag
 *
 *
 *
 **/
public final class MailPart extends BodyTagImpl {

	lucee.runtime.net.mail.MailPart part = new lucee.runtime.net.mail.MailPart();

	@Override
	public void release() {
		super.release();
		part = new lucee.runtime.net.mail.MailPart();
	}

	/**
	 * @param type The type to set.
	 * @throws ApplicationException
	 */
	public void setType(String type) throws ApplicationException {
		part.setType(type);
	}

	/**
	 * @param charset The charset to set.
	 */
	public void setCharset(Charset charset) {
		part.setCharset(charset);
	}

	public void setCharset(String charset) {
		setCharset(CharsetUtil.toCharset(charset));
	}

	/**
	 * @param wraptext The wraptext to set.
	 */
	public void setWraptext(double wraptext) {
		part.setWraptext((int) wraptext);
	}

	@Override
	public int doStartTag() {

		return EVAL_BODY_BUFFERED;
	}

	@Override
	public void doInitBody() {

	}

	@Override
	public int doAfterBody() {
		part.setBody(bodyContent.getString());
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws PageException {

		getMail().addPart(part);
		/*
		 * String type = part.getType(); if(StringUtil.isEmpty(part.getCharset()))
		 * part.setCharset(mail.getCharset()); if(type!=null && (type.equals("text/plain") ||
		 * type.equals("plain") || type.equals("text"))){ part.isPlain(true); mail.setBodyPart(part); } else
		 * if(type!=null && (type.equals("text/html") || type.equals("html") || type.equals("htm"))){
		 * part.isHTML(true); mail.setBodyPart(part); } else {
		 * 
		 * getMail().setParam(type, null, "susi", part.getBody(), "inline", null); }
		 */
		// throw new ApplicationException("attribute type of tag mailpart has an invalid values","valid
		// values are [plain,text,html] but value is now ["+type+"]");

		return EVAL_PAGE;
	}

	private Mail getMail() throws ApplicationException {
		Tag parent = getParent();
		while (parent != null && !(parent instanceof Mail)) {
			parent = parent.getParent();
		}
		if (parent instanceof Mail) return (Mail) parent;
		throw new ApplicationException("Wrong Context, tag MailPart must be inside a Mail tag");
	}
}