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

import javax.servlet.jsp.tagext.Tag;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.tag.TagImpl;

/**
 * Defines table column header, width, alignment, and text. Used only inside a cftable.
 *
 *
 *
 **/
public final class Col extends TagImpl {

	/**
	 * Double-quote delimited text that determines what displays in the column. The rules for the text
	 ** attribute are identical to the rules for cfoutput sections; it can consist of a combination of
	 ** literal text, HTML tags, and query record set field references. You can embed hyperlinks, image
	 ** references, and input controls in columns.
	 */
	private String text = "";

	/**
	 * The width of the column in characters (the default is 20). If the length of the data displayed
	 * exceeds the width value, the data is truncated to fit.
	 */
	private int width = -1;

	/** Column alignment, Left, Right, or Center. */
	private short align = Table.ALIGN_LEFT;

	/** The text for the column's header. */
	private String header = "";

	@Override
	public void release() {
		super.release();
		text = "";
		width = -1;
		align = Table.ALIGN_LEFT;
		header = "";
	}

	/**
	 * set the value text Double-quote delimited text that determines what displays in the column. The
	 * rules for the text attribute are identical to the rules for cfoutput sections; it can consist of
	 * a combination of literal text, HTML tags, and query record set field references. You can embed
	 * hyperlinks, image references, and input controls in columns.
	 * 
	 * @param text value to set
	 **/
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * set the value width The width of the column in characters (the default is 20). If the length of
	 * the data displayed exceeds the width value, the data is truncated to fit.
	 * 
	 * @param width value to set
	 **/
	public void setWidth(double width) {
		this.width = (int) width;
		if (this.width < 0) this.width = -1;
	}

	/**
	 * set the value align Column alignment, Left, Right, or Center.
	 * 
	 * @param align value to set
	 * @throws ApplicationException
	 **/
	public void setAlign(String align) throws ApplicationException {
		align = StringUtil.toLowerCase(align);
		if (align.equals("left")) this.align = Table.ALIGN_LEFT;
		else if (align.equals("center")) this.align = Table.ALIGN_CENTER;
		else if (align.equals("right")) this.align = Table.ALIGN_RIGHT;
		else throw new ApplicationException("value [" + align + "] of attribute align from tag col is invalid", "valid values are [left, center, right]");
	}

	/**
	 * set the value header The text for the column's header.
	 * 
	 * @param header value to set
	 **/
	public void setHeader(String header) {
		this.header = header;
	}

	@Override
	public int doStartTag() throws ExpressionException, ApplicationException {
		Tag parent = getParent();
		while (parent != null && !(parent instanceof Table)) {
			parent = parent.getParent();
		}

		if (parent instanceof Table) {
			Table table = (Table) parent;
			table.setCol(header, text, align, width);
		}
		else throw new ApplicationException("invalid context for tag col, tag must be inside a table tag");

		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}