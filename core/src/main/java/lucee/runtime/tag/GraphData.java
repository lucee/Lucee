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

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.tag.TagImpl;

/**
 * Specifies a data point to be displayed by a cfgraph tag.
 *
 *
 *
 **/
public final class GraphData extends TagImpl {

	/**
	 * The item label for the data point. The item labels appear on the horizontal axis of Line and Bar
	 * charts, the vertical axis of Horizontal Bar charts, and in the legend of Pie charts.
	 */
	private String item;

	/**
	 * The color to use when graphing the data point. The default is to use the values from the cfgraph
	 ** tag colorlist attribute or the built-in default list of colors. Line graphs ignore this
	 * attribute.
	 */
	private String color;

	/** Value to be represented by the data point. */
	private String value;

	/**
	 * A URL to load when the user clicks the data point. This attribute works with Pie, Bar, and
	 ** HorizontalBar charts. This attribute has an effect only if the graph is in Flash file format.
	 */
	private String url;

	/**
	 * constructor for the tag class
	 **/
	public GraphData() throws ExpressionException {
		throw new ExpressionException("tag cfgraphdata is deprecated");
	}

	/**
	 * set the value item The item label for the data point. The item labels appear on the horizontal
	 * axis of Line and Bar charts, the vertical axis of Horizontal Bar charts, and in the legend of Pie
	 * charts.
	 * 
	 * @param item value to set
	 **/
	public void setItem(String item) {
		this.item = item;
	}

	/**
	 * set the value color The color to use when graphing the data point. The default is to use the
	 * values from the cfgraph tag colorlist attribute or the built-in default list of colors. Line
	 * graphs ignore this attribute.
	 * 
	 * @param color value to set
	 **/
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * set the value value Value to be represented by the data point.
	 * 
	 * @param value value to set
	 **/
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * set the value url A URL to load when the user clicks the data point. This attribute works with
	 * Pie, Bar, and HorizontalBar charts. This attribute has an effect only if the graph is in Flash
	 * file format.
	 * 
	 * @param url value to set
	 **/
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int doStartTag() {
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		super.release();
		item = "";
		color = "";
		value = "";
		url = "";
	}
}