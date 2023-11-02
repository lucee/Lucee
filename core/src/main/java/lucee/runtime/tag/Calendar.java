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

import javax.servlet.jsp.JspException;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.TagNotSupported;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.ListUtil;

public final class Calendar extends TagImpl {

	private static final String[] DAY_NAMES_DEFAULT = new String[] { "S", "M", "T", "W", "Th", "F", "S" };

	private static final String[] MONTH_NAMES_DEFAULT = new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November",
			"December" };

	private String name;
	private int height = -1;
	private int width = -1;
	private DateTime selectedDate;
	private DateTime startRange;
	private DateTime endRange;
	private boolean disabled;
	private String mask = "MM/DD/YYYY";
	private int firstDayOfWeek = 0;
	private String[] dayNames = DAY_NAMES_DEFAULT;
	private String[] monthNames = MONTH_NAMES_DEFAULT;
	private String style;
	private boolean enabled = true;
	private boolean visible = true;
	private String tooltip;
	private String onChange;
	private String onBlur;
	private String onFocus;

	public Calendar() throws ApplicationException {
		// TODO impl. tag Calendar
		throw new TagNotSupported("Calendar");
	}

	@Override
	public void release() {
		super.release();
		name = null;
		height = -1;
		width = -1;
		selectedDate = null;
		startRange = null;
		endRange = null;
		disabled = false;
		mask = "MM/DD/YYYY";
		firstDayOfWeek = 0;
		dayNames = DAY_NAMES_DEFAULT;
		monthNames = MONTH_NAMES_DEFAULT;
		style = null;
		enabled = true;
		visible = true;
		tooltip = null;
		onChange = null;
		onBlur = null;
		onFocus = null;
	}

	@Override
	public int doStartTag() throws JspException {
		return super.doStartTag();
	}

	/**
	 * @param dayNames the dayNames to set
	 */
	public void setDaynames(String listDayNames) {
		this.dayNames = ListUtil.listToStringArray(listDayNames, ',');
	}

	/**
	 * @param disabled the disabled to set
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @param endRange the endRange to set
	 */
	public void setEndrange(DateTime endRange) {
		this.endRange = endRange;
	}

	/**
	 * @param firstDayOfWeek the firstDayOfWeek to set
	 */
	public void setFirstdayofweek(double firstDayOfWeek) {
		this.firstDayOfWeek = (int) firstDayOfWeek;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(double height) {
		this.height = (int) height;
	}

	/**
	 * @param mask the mask to set
	 */
	public void setMask(String mask) {
		this.mask = mask;
	}

	/**
	 * @param monthNames the monthNames to set
	 */
	public void setMonthnames(String listMonthNames) {
		this.monthNames = monthNames;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param onBlur the onBlur to set
	 */
	public void setOnblur(String onBlur) {
		this.onBlur = onBlur;
	}

	/**
	 * @param onChange the onChange to set
	 */
	public void setOnchange(String onChange) {
		this.onChange = onChange;
	}

	/**
	 * @param onFocus the onFocus to set
	 */
	public void setOnfocus(String onFocus) {
		this.onFocus = onFocus;
	}

	/**
	 * @param selectedDate the selectedDate to set
	 */
	public void setSelecteddate(DateTime selectedDate) {
		this.selectedDate = selectedDate;
	}

	/**
	 * @param startRange the startRange to set
	 */
	public void setStartrange(DateTime startRange) {
		this.startRange = startRange;
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