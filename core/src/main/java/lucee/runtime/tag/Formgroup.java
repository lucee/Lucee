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

import lucee.runtime.exp.TagNotSupported;
import lucee.runtime.ext.tag.BodyTagImpl;

public final class Formgroup extends BodyTagImpl {

	private int type;
	private Query query;
	private int startrow = 0;
	private int maxrows = -1;
	private String label;
	private String style;
	private int selectedIndex = -1;
	private int width = -1;
	private int height = -1;
	private boolean enabled = true;
	private boolean visible = true;
	private String onChange;
	private String tooltip;
	private String id;

	public Formgroup() throws TagNotSupported {
		throw new TagNotSupported("formgroup");
		// TODO impl tag formgroup
	}

	@Override
	public void release() {
		super.release();
		query = null;
		startrow = 0;
		maxrows = -1;
		label = null;
		style = null;
		selectedIndex = -1;
		width = -1;
		height = -1;
		enabled = true;
		visible = true;
		onChange = null;
		tooltip = null;
		id = null;

	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		// this.type = type;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @param maxrows the maxrows to set
	 */
	public void setMaxrows(double maxrows) {
		this.maxrows = (int) maxrows;
	}

	/**
	 * @param onChange the onChange to set
	 */
	public void setOnchange(String onChange) {
		this.onChange = onChange;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(String queryName) {
		// this.query = query;
	}

	/**
	 * @param selectedIndex the selectedIndex to set
	 */
	public void setSelectedindex(double selectedIndex) {
		this.selectedIndex = (int) selectedIndex;
	}

	/**
	 * @param startrow the startrow to set
	 */
	public void setStartrow(double startrow) {
		this.startrow = (int) startrow;
	}

	/**
	 * @param style the style to set
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 * @param tooltip the tooltip to set
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(double width) {
		this.width = (int) width;
	}

}