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
package lucee.runtime.dump;

import java.util.ArrayList;
import java.util.List;

/**
 * class to generate Lucee HTML Boxes for dumps
 */
public class DumpTable implements DumpData {

	private final List<DumpRow> rows = new ArrayList<DumpRow>();
	private String title;
	private String comment;
	private String highLightColor;
	private String normalColor;
	private String borderColor;
	private String fontColor;
	private String width;
	private String height;
	private final String type;
	private String id;
	private String ref;

	public DumpTable(final String highLightColor, final String normalColor, final String borderColor) {
		this(null, highLightColor, normalColor, borderColor, borderColor);
	}

	public DumpTable(final String type, final String highLightColor, final String normalColor, final String borderColor) {
		this(type, highLightColor, normalColor, borderColor, borderColor);
	}

	public DumpTable(final String type, final String highLightColor, final String normalColor, final String borderColor, final String fontColor) {
		this.highLightColor = highLightColor;
		this.normalColor = normalColor;
		this.borderColor = borderColor;
		this.fontColor = fontColor;
		this.type = type;
	}

	/**
	 * @return returns if the box has content or not
	 */
	public boolean isEmpty() {
		return rows.isEmpty();
	}

	/**
	 * clear all data set in the HTMLBox
	 */
	public void clear() {
		rows.clear();
	}

	/**
	 * @param title sets the title of the HTML Box
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * returns the title of the DumpTable, if not defined returns null
	 * 
	 * @return title of the DumpTable
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param comment sets the comment of the HTML Box
	 */
	public void setComment(final String comment) {
		this.comment = comment;
	}

	/**
	 * returns the comment of the DumpTable, if not defined returns null
	 * 
	 * @return title of the DumpTable
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param width sets the With of the HTML Box, can be a number or a percentage value
	 */
	public void setWidth(final String width) {
		this.width = width;
	}

	/**
	 * @param height sets the Height of the HTML Box, can be a number or a percentage value
	 */
	public void setHeight(final String height) {
		this.height = height;
	}

	/**
	 * @return the borderColor
	 */
	public String getBorderColor() {
		return borderColor;
	}

	/**
	 * @param borderColor the borderColor to set
	 */
	public void setBorderColor(final String borderColor) {
		this.borderColor = borderColor;
	}

	/**
	 * @return the fontColor
	 */
	public String getFontColor() {
		return fontColor;
	}

	/**
	 * @param fontColor the fontColor to set
	 */
	public void setFontColor(final String fontColor) {
		this.fontColor = fontColor;
	}

	/**
	 * @return the highLightColor
	 */
	public String getHighLightColor() {
		return highLightColor;
	}

	/**
	 * @param highLightColor the highLightColor to set
	 */
	public void setHighLightColor(final String highLightColor) {
		this.highLightColor = highLightColor;
	}

	/**
	 * @return the normalColor
	 */
	public String getNormalColor() {
		return normalColor;
	}

	/**
	 * @param normalColor the normalColor to set
	 */
	public void setNormalColor(final String normalColor) {
		this.normalColor = normalColor;
	}

	/**
	 * @return the height
	 */
	public String getHeight() {
		return height;
	}

	/**
	 * @return the rows
	 */
	public DumpRow[] getRows() {
		return rows.toArray(new DumpRow[rows.size()]);
	}

	public void appendRow(final DumpRow row) {
		rows.add(row);
	}

	public void appendRow(final int highlightType, final DumpData item1) {
		appendRow(new DumpRow(highlightType, new DumpData[] { item1 }));
	}

	public void appendRow(final int highlightType, final DumpData item1, final DumpData item2) {
		appendRow(new DumpRow(highlightType, new DumpData[] { item1, item2 }));
	}

	public void appendRow(final int highlightType, final DumpData item1, final DumpData item2, final DumpData item3) {
		appendRow(new DumpRow(highlightType, new DumpData[] { item1, item2, item3 }));
	}

	public void appendRow(final int highlightType, final DumpData item1, final DumpData item2, final DumpData item3, final DumpData item4) {
		appendRow(new DumpRow(highlightType, new DumpData[] { item1, item2, item3, item4 }));
	}

	public void appendRow(final int highlightType, final DumpData item1, final DumpData item2, final DumpData item3, final DumpData item4, final DumpData item5) {
		appendRow(new DumpRow(highlightType, new DumpData[] { item1, item2, item3, item4, item5 }));
	}

	public void appendRow(final int highlightType, final DumpData item1, final DumpData item2, final DumpData item3, final DumpData item4, final DumpData item5,
			final DumpData item6) {
		appendRow(new DumpRow(highlightType, new DumpData[] { item1, item2, item3, item4, item5, item6 }));
	}

	public void prependRow(final DumpRow row) {
		rows.add(0, row);
	}

	/**
	 * @return the width
	 */
	public String getWidth() {
		return width;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setRef(final String ref) {
		this.ref = ref;
	}

	public String getRef() {
		return ref;
	}
}