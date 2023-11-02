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

import lucee.runtime.exp.TagNotSupported;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.type.util.ListUtil;

/**
 * Lets you define a cfgrid that does not use a query as source for row data. If a query attribute
 * is specified in cfgrid, the cfgridrow tags are ignored.
 *
 *
 *
 **/
public final class GridRow extends TagImpl {

	public GridRow() throws TagNotSupported {
		throw new TagNotSupported("GridRow");
	}

	/**
	 * A comma-separated list of column values. If a column value contains a comma character, it must be
	 * escaped with a second comma character.
	 */
	private String[] data;

	@Override
	public void release() {
		super.release();
		data = null;
	}

	/**
	 * set the value data A comma-separated list of column values. If a column value contains a comma
	 * character, it must be escaped with a second comma character.
	 * 
	 * @param data value to set
	 **/
	public void setData(String data) {
		this.data = ListUtil.listToStringArray(data, ',');
	}

	@Override
	public int doStartTag() {
		// provide to parent
		Tag parent = this;
		do {
			parent = parent.getParent();
			if (parent instanceof Grid) {
				((Grid) parent).addRow(data);
				break;
			}
		}
		while (parent != null);

		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

}