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

import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.mail.EmailAttachment;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

/**
 * Can either attach a file or add a header to a message. It is nested within a cfmail tag. You can
 * use more than one cfmailparam tag within a cfmail tag.
 *
 *
 *
 **/
public final class MailParam extends TagImpl {

	/** Indicates the value of the header. */
	private String value = "";

	/**
	 * Attaches the specified file to the message. This attribute is mutually exclusive with the name
	 * attribute.
	 */
	private String file;

	/**
	 * Specifies the name of the header. Header names are case insensitive. This attribute is mutually
	 ** exclusive with the file attribute.
	 */
	private String name;
	private String fileName;

	private String type = "";
	private String disposition = null;
	private String contentID = null;
	private Boolean remove = false;
	private byte[] content = null;

	@Override
	public void release() {
		super.release();
		value = "";
		file = null;
		name = null;
		type = "";
		disposition = null;
		contentID = null;
		remove = null;
		content = null;
		fileName = null;
	}

	/**
	 * @param remove the remove to set
	 */
	public void setRemove(boolean remove) {
		this.remove = Caster.toBoolean(remove);
	}

	/**
	 * @param content the content to set
	 * @throws ExpressionException
	 */
	public void setContent(Object content) throws PageException {
		if (content instanceof String) this.content = ((String) content).getBytes();
		else this.content = Caster.toBinary(content);
	}

	/**
	 * @param type
	 */
	public void setType(String type) {
		type = type.toLowerCase().trim();

		if (type.equals("text")) type = "text/plain";
		else if (type.equals("plain")) type = "text/plain";
		else if (type.equals("html")) type = "text/html";
		else if (type.startsWith("multipart/")) return; // TODO see LDEV-570 maybe add support for content-type in the future

		this.type = type;
	}

	/**
	 * set the value value Indicates the value of the header.
	 * 
	 * @param value value to set
	 **/
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * set the value file Attaches the specified file to the message. This attribute is mutually
	 * exclusive with the name attribute.
	 * 
	 * @param strFile value to set
	 * @throws PageException
	 **/
	public void setFile(String strFile) throws PageException {
		this.file = strFile;
	}

	/**
	 * set the value name Specifies the name of the header. Header names are case insensitive. This
	 * attribute is mutually exclusive with the file attribute.
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	public void setFilename(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @param disposition The disposition to set.
	 * @throws ApplicationException
	 */
	public void setDisposition(String disposition) throws ApplicationException {
		disposition = disposition.trim().toLowerCase();
		if (disposition.equals("attachment")) this.disposition = EmailAttachment.ATTACHMENT;
		else if (disposition.equals("inline")) this.disposition = EmailAttachment.INLINE;
		else throw new ApplicationException("disposition must have one of the following values (attachment,inline)");

	}

	/**
	 * @param contentID The contentID to set.
	 */
	public void setContentid(String contentID) {
		this.contentID = contentID;
	}

	@Override
	public int doStartTag() throws PageException {

		if (content != null) {
			required("mailparam", "file", file);
			String id = "id-" + CreateUniqueId.invoke();
			String ext = ResourceUtil.getExtension(file, "tmp");

			if (StringUtil.isEmpty(fileName) && !StringUtil.isEmpty(file)) fileName = ListUtil.last(file, "/\\", true);

			Resource res = SystemUtil.getTempDirectory().getRealResource(id + "." + ext);
			if (res.exists()) ResourceUtil.removeEL(res, true);
			try {
				IOUtil.write(res, content);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
			this.file = ResourceUtil.getCanonicalPathEL(res);
			remove = true;
		}
		else if (!StringUtil.isEmpty(this.file)) {
			Resource res = ResourceUtil.toResourceNotExisting(pageContext, this.file);
			if (res != null) {
				if (res.exists()) pageContext.getConfig().getSecurityManager().checkFileLocation(res);
				this.file = ResourceUtil.getCanonicalPathEL(res);
			}
		}

		// check attributes
		boolean hasFile = !StringUtil.isEmpty(file);
		boolean hasName = !StringUtil.isEmpty(name);
		// both attributes
		if (hasName && hasFile) {
			throw new ApplicationException("Wrong Context for tag [MailParam], you cannot use the attributes [file] and [name] together");
		}
		// no attributes
		if (!hasName && !hasFile) {
			throw new ApplicationException("Wrong Context for tag [MailParam], one of the attributes [file] or [name] is required");
		}

		// get Mail Tag
		Tag parent = getParent();
		while (parent != null && !(parent instanceof Mail)) {
			parent = parent.getParent();
		}

		if (parent instanceof Mail) {
			Mail mail = (Mail) parent;
			mail.setParam(type, file, fileName, name, value, disposition, contentID, remove);
		}
		else {
			throw new ApplicationException("Wrong Context, tag [MailParam] must be inside a [Mail] tag");
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

}
