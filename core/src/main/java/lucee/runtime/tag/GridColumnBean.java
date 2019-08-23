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

import java.awt.Color;

public class GridColumnBean {

	private boolean display;
	private int width;
	private String header;
	private String headerFont;
	private boolean headerItalic;
	private boolean headerBold;
	private int headerFontSize;
	private Color headerTextColor;
	private String headerAlign;
	private String href;
	private String hrefKey;
	private String target;
	private String[] values;
	private String[] valuesDisplay;
	private String font;
	private int fontSize;
	private boolean italic;
	private Color bgColor;
	private String name;
	private String type;
	private String numberFormat;
	private Color textColor;
	private boolean select;
	private String dataAlign;
	private boolean bold;
	private String mask;

	/**
	 * @return the bgColor
	 */
	public Color getBgColor() {
		return bgColor;
	}

	/**
	 * @param bgColor the bgColor to set
	 */
	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	/**
	 * @return the bold
	 */
	public boolean isBold() {
		return bold;
	}

	/**
	 * @param bold the bold to set
	 */
	public void setBold(boolean bold) {
		this.bold = bold;
	}

	/**
	 * @return the dataAlign
	 */
	public String getDataAlign() {
		return dataAlign;
	}

	/**
	 * @param dataAlign the dataAlign to set
	 */
	public void setDataAlign(String dataAlign) {
		this.dataAlign = dataAlign;
	}

	/**
	 * @return the display
	 */
	public boolean isDisplay() {
		return display;
	}

	/**
	 * @param display the display to set
	 */
	public void setDisplay(boolean display) {
		this.display = display;
	}

	/**
	 * @return the font
	 */
	public String getFont() {
		return font;
	}

	/**
	 * @param font the font to set
	 */
	public void setFont(String font) {
		this.font = font;
	}

	/**
	 * @return the fontSize
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * @param fontSize the fontSize to set
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * @param header the header to set
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * @return the headerAlign
	 */
	public String getHeaderAlign() {
		return headerAlign;
	}

	/**
	 * @param headerAlign the headerAlign to set
	 */
	public void setHeaderAlign(String headerAlign) {
		this.headerAlign = headerAlign;
	}

	/**
	 * @return the headerBold
	 */
	public boolean isHeaderBold() {
		return headerBold;
	}

	/**
	 * @param headerBold the headerBold to set
	 */
	public void setHeaderBold(boolean headerBold) {
		this.headerBold = headerBold;
	}

	/**
	 * @return the headerFont
	 */
	public String getHeaderFont() {
		return headerFont;
	}

	/**
	 * @param headerFont the headerFont to set
	 */
	public void setHeaderFont(String headerFont) {
		this.headerFont = headerFont;
	}

	/**
	 * @return the headerFontSize
	 */
	public int getHeaderFontSize() {
		return headerFontSize;
	}

	/**
	 * @param headerFontSize the headerFontSize to set
	 */
	public void setHeaderFontSize(int headerFontSize) {
		this.headerFontSize = headerFontSize;
	}

	/**
	 * @return the headerItalic
	 */
	public boolean isHeaderItalic() {
		return headerItalic;
	}

	/**
	 * @param headerItalic the headerItalic to set
	 */
	public void setHeaderItalic(boolean headerItalic) {
		this.headerItalic = headerItalic;
	}

	/**
	 * @return the headerTextColor
	 */
	public Color getHeaderTextColor() {
		return headerTextColor;
	}

	/**
	 * @param headerTextColor the headerTextColor to set
	 */
	public void setHeaderTextColor(Color headerTextColor) {
		this.headerTextColor = headerTextColor;
	}

	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}

	/**
	 * @param href the href to set
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * @return the hrefKey
	 */
	public String getHrefKey() {
		return hrefKey;
	}

	/**
	 * @param hrefKey the hrefKey to set
	 */
	public void setHrefKey(String hrefKey) {
		this.hrefKey = hrefKey;
	}

	/**
	 * @return the italic
	 */
	public boolean isItalic() {
		return italic;
	}

	/**
	 * @param italic the italic to set
	 */
	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	/**
	 * @return the mask
	 */
	public String getMask() {
		return mask;
	}

	/**
	 * @param mask the mask to set
	 */
	public void setMask(String mask) {
		this.mask = mask;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the numberFormat
	 */
	public String getNumberFormat() {
		return numberFormat;
	}

	/**
	 * @param numberFormat the numberFormat to set
	 */
	public void setNumberFormat(String numberFormat) {
		this.numberFormat = numberFormat;
	}

	/**
	 * @return the select
	 */
	public boolean isSelect() {
		return select;
	}

	/**
	 * @param select the select to set
	 */
	public void setSelect(boolean select) {
		this.select = select;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @return the textColor
	 */
	public Color getTextColor() {
		return textColor;
	}

	/**
	 * @param textColor the textColor to set
	 */
	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the values
	 */
	public String[] getValues() {
		return values;
	}

	/**
	 * @param values the values to set
	 */
	public void setValues(String[] values) {
		this.values = values;
	}

	/**
	 * @return the valuesDisplay
	 */
	public String[] getValuesDisplay() {
		return valuesDisplay;
	}

	/**
	 * @param valuesDisplay the valuesDisplay to set
	 */
	public void setValuesDisplay(String[] valuesDisplay) {
		this.valuesDisplay = valuesDisplay;
	}

	/**
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}
}