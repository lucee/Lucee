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

import lucee.commons.color.ColorCaster;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TagNotSupported;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.type.util.ListUtil;

/**
 * Used with cfgrid in a cfform, you use cfgridcolumn to specify column data in a cfgrid control.
 * Font and alignment attributes used in cfgridcolumn override any global font or alignment settings
 * defined in cfgrid.
 *
 *
 *
 **/
public final class GridColumn extends TagImpl {

	private GridColumnBean column = new GridColumnBean();

	public GridColumn() throws TagNotSupported {
		throw new TagNotSupported("GridColumn");
	}

	private String valuesdelimiter = ",";
	private String valuesdisplay;
	private String values;

	@Override
	public void release() {
		column = new GridColumnBean();
		valuesdelimiter = ",";
		valuesdisplay = null;
		values = null;
	}

	/**
	 * @param mask the mask to set
	 */
	public void setMask(String mask) {
		column.setMask(mask);
	}

	/**
	 * set the value display Yes or No. Use to hide columns. Default is Yes to display the column.
	 * 
	 * @param display value to set
	 **/
	public void setDisplay(boolean display) {
		column.setDisplay(display);
	}

	/**
	 * set the value width The width of the column, in pixels. Default is the width of the column head
	 * text.
	 * 
	 * @param width value to set
	 **/
	public void setWidth(double width) {
		column.setWidth((int) width);
	}

	/**
	 * set the value headerfontsize Font size to use for the column header, in pixels. Default is as
	 * specified by the orresponding attribute of cfgrid.
	 * 
	 * @param headerfontsize value to set
	 **/
	public void setHeaderfontsize(double headerfontsize) {
		column.setHeaderFontSize((int) headerfontsize);
	}

	/**
	 * set the value hrefkey The name of a query column when the grid uses a query. The column specified
	 * becomes the Key regardless of the select mode for the grid.
	 * 
	 * @param hrefkey value to set
	 **/
	public void setHrefkey(String hrefkey) {
		column.setHrefKey(hrefkey);
	}

	/**
	 * set the value target The name of the frame in which to open the link specified in href.
	 * 
	 * @param target value to set
	 **/
	public void setTarget(String target) {
		column.setTarget(target);
	}

	/**
	 * set the value values Formats cells in the column as drop down list boxes. lets end users select
	 * an item in a drop down list. Use the values attribute to specify the items you want to appear in
	 * the drop down list.
	 * 
	 * @param values value to set
	 **/
	public void setValues(String values) {
		this.values = values;
	}

	/**
	 * set the value headerfont Font to use for the column header. Default is as specified by the
	 * corresponding attribute of cfgrid.
	 * 
	 * @param headerfont value to set
	 **/
	public void setHeaderfont(String headerfont) {
		column.setHeaderFont(headerfont);
	}

	/**
	 * set the value font Font name to use for data in the column. Defaults is the font specified by
	 * cfgrid.
	 * 
	 * @param font value to set
	 **/
	public void setFont(String font) {
		column.setFont(font);
	}

	/**
	 * set the value italic Yes or No. Yes displays all grid control text in italic. Default is as
	 * specified by the corresponding attribute of cfgrid.
	 * 
	 * @param italic value to set
	 **/
	public void setItalic(boolean italic) {
		column.setItalic(italic);
	}

	/**
	 * set the value bgcolor Color value for the background of the grid column, or an expression you can
	 * use to manipulate grid column background color. Valid color entries are: black, magenta, cyan,
	 * orange, darkgray, pink, gray, white (default), lightgray, yellow.
	 * 
	 * @param bgcolor value to set
	 * @throws ExpressionException
	 **/
	public void setBgcolor(String bgcolor) throws ExpressionException {
		column.setBgColor(ColorCaster.toColor(bgcolor));
	}

	/**
	 * set the value valuesdisplay Used to map elements specified in the values attribute to a string of
	 * your choice to display in the drop down list. Enter comma separated strings and/or numeric
	 * range(s).
	 * 
	 * @param valuesdisplay value to set
	 **/
	public void setValuesdisplay(String valuesdisplay) {
		this.valuesdisplay = valuesdisplay;
	}

	/**
	 * set the value headeritalic Yes or No. Yes displays column header text in italic. Default is as
	 * specified by the corresponding attribute of cfgrid.
	 * 
	 * @param headeritalic value to set
	 **/
	public void setHeaderitalic(boolean headeritalic) {
		column.setHeaderItalic(headeritalic);
	}

	/**
	 * set the value name A name for the grid column element. If the grid uses a query, the column name
	 * must specify the name of a query column.
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		column.setName(name);
	}

	/**
	 * set the value href URL to associate with the grid item. You can specify a URL that is relative to
	 * the current page
	 * 
	 * @param href value to set
	 **/
	public void setHref(String href) {
		column.setHref(href);
	}

	/**
	 * set the value type
	 * 
	 * @param type value to set
	 **/
	public void setType(String type) {
		column.setType(type);
	}

	/**
	 * set the value valuesdelimiter Character to use as a delimiter in the values and valuesDisplay
	 * attributes. Default is "," (comma).
	 * 
	 * @param valuesdelimiter value to set
	 **/
	public void setValuesdelimiter(String valuesdelimiter) {
		this.valuesdelimiter = valuesdelimiter;
	}

	/**
	 * set the value numberformat The format for displaying numeric data in the grid. For information
	 * about mask characters, see "numberFormat mask characters".
	 * 
	 * @param numberformat value to set
	 **/
	public void setNumberformat(String numberformat) {
		column.setNumberFormat(numberformat);
	}

	/**
	 * set the value header Text for the column header. The value of header is used only when the cfgrid
	 * colHeaders attribute is Yes (or omitted, since it defaults to Yes).
	 * 
	 * @param header value to set
	 **/
	public void setHeader(String header) {
		column.setHeader(header);
	}

	/**
	 * set the value textcolor Color value for grid element text in the grid column, or an expression
	 * you can use to manipulate text color in grid column elements. Valid color entries are: black
	 * (default), magenta, cyan, orange, arkgray, pink, gray, white, lightgray, yellow
	 * 
	 * @param textcolor value to set
	 * @throws ExpressionException
	 **/
	public void setTextcolor(String textcolor) throws ExpressionException {
		column.setTextColor(ColorCaster.toColor(textcolor));
	}

	/**
	 * set the value select Yes or No. Yes lets end users select a column in a grid control. When No,
	 * the column cannot be edited, even if the cfgrid insert or delete attributes are enabled. The
	 * value of the select attribute is ignored if the cfgrid selectMode attribute is set to Row or
	 * Browse.
	 * 
	 * @param select value to set
	 **/
	public void setSelect(boolean select) {
		column.setSelect(select);
	}

	/**
	 * set the value headeralign Alignment for the column header text. Default is as specified by
	 * cfgrid.
	 * 
	 * @param headeralign value to set
	 **/
	public void setHeaderalign(String headeralign) {
		column.setHeaderAlign(headeralign);
	}

	/**
	 * set the value dataalign Alignment for column data. Entries are: left, center, or right. Default
	 * is as specified by cfgrid.
	 * 
	 * @param dataalign value to set
	 **/
	public void setDataalign(String dataalign) {
		column.setDataAlign(dataalign);
	}

	/**
	 * set the value bold Yes or No. Yes displays all grid control text in boldface. Default is as
	 * specified by the corresponding attribute of cfgrid.
	 * 
	 * @param bold value to set
	 **/
	public void setBold(boolean bold) {
		column.setBold(bold);
	}

	/**
	 * set the value headerbold Yes or No. Yes displays header text in boldface. Default is as specified
	 * by the corresponding attribute of cfgrid.
	 * 
	 * @param headerbold value to set
	 **/
	public void setHeaderbold(boolean headerbold) {
		column.setHeaderBold(headerbold);
	}

	/**
	 * set the value colheadertextcolor Color value for the grid control column header text. Entries
	 * are: black (default), magenta, cyan, orange, darkgray, pink, gray, white, lightgray, yellow.
	 * 
	 * @param headertextcolor value to set
	 * @throws ExpressionException
	 **/
	public void setHeadertextcolor(String headertextcolor) throws ExpressionException {
		column.setHeaderTextColor(ColorCaster.toColor(headertextcolor));
	}

	/**
	 * set the value fontsize Font size for text in the column. Default is the font specified by cfgrid.
	 * 
	 * @param fontsize value to set
	 **/
	public void setFontsize(double fontsize) {
		column.setFontSize((int) fontsize);
	}

	@Override
	public int doStartTag() throws PageException {

		if (!StringUtil.isEmpty(values)) column.setValues(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(values, valuesdelimiter)));
		if (!StringUtil.isEmpty(valuesdisplay)) column.setValuesDisplay(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(valuesdisplay, valuesdelimiter)));

		// provide to parent
		Tag parent = this;
		do {
			parent = parent.getParent();
			if (parent instanceof Grid) {
				((Grid) parent).addColumn(column);
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