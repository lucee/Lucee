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

public final class Grid extends BodyTagImpl {

	/** Width value of the grid control, in pixels. */
	private double width;

	/**
	 * The name of a query column when the grid uses a query. The column specified becomes the Key
	 ** regardless of the select mode for the grid.
	 */
	private String hrefkey;

	/**
	 * If Yes, sort buttons are added to the grid control. When clicked, sort buttons perform a simple
	 ** text sort on the selected column. Default is No. Note that columns are sortable by clicking the
	 * column head, even if no sort button is displayed.
	 */
	private boolean sort;

	/** Yes or No. Yes displays column headers in the grid control. Default is Yes. */
	private boolean colheaders;

	/**
	 * Text color value for the grid control row headers. Entries are: black (default), magenta, cyan,
	 ** orange, darkgray, pink, gray, white, lightgray, yellow. A hex value can be entered in the form:
	 * rowHeaderTextColor = "##xxxxxx" Where x is 0-9 or A-F. Use two pound signs or no pound signs.
	 */
	private String rowheadertextcolor;

	/** Font to use for column data in the grid control. */
	private String font;

	/** Yes or No. Yes displays column header text in italic. Default is No. */
	private boolean colheaderitalic;

	/**
	 * Optional. Yes or No. Default is No. If Yes, automatically sets the width of each column so that
	 * all the columns are visible within the grid's specified width. All columns are initially set to
	 * equal widths. Users can resize any column. No horizontal scroll bars are available since all
	 * columns are visible. note that if you explicitly specify the width of a column and set autoWidth
	 * to Yes, CFML will set the column to the explicit width, if possible.
	 */
	private boolean autowidth;

	/** Background color for a selected item. See bgColor for color options. */
	private String selectcolor;

	/**
	 * Yes highlights links associated with a cfgrid with an href attribute value. No disables
	 ** highlight. Default is Yes.
	 */
	private boolean highlighthref;

	/** Yes displays grid control text in italic. Default is No. */
	private boolean italic;

	/**
	 * Yes or No. Yes enables row and column rules (lines) in the grid control. No suppresses rules.
	 ** Default is Yes.
	 */
	private boolean gridlines;

	/**
	 * Yes or No. If Yes, images are used for the Insert, delete, and Sort buttons rather than text.
	 ** Default is No.
	 */
	private boolean picturebar;

	/** Text to use for the delete action button. The default is delete. */
	private String deletebutton;

	/**
	 * Color value for text in the grid control. Options are: black (default), magenta, cyan, orange,
	 * darkgray, pink, gray, white, lightgray, yellow. A hex value can be entered in the form: textColor
	 * = "##xxxxxx" where x is 0-9 or A-F. Use two pound signs or no pound signs.
	 */
	private String textcolor;

	/** Text to use for the Insert action button. The default is Insert. */
	private String insertbutton;

	/**
	 * Number of pixels for the minimum row height of the grid control. Used with cfgridcolumn type =
	 * "Image", you can use rowHeight to define enough space for graphics to display in the row.
	 */
	private double rowheight;

	/** Selection mode for items in the grid control. */
	private String notsupported;

	/** Font size for text in the grid control, in points. */
	private double fontsize;

	/** The width, in pixels, of the row header column. */
	private double rowheaderwidth;

	/** The name of a JavaScript function to execute in the event of a failed validation. */
	private String onerror;

	/** Target attribute for href URL. */
	private String target;

	/** Font for the column header in the grid control. */
	private String colheaderfont;

	/** Enter Left, Right, or Center to position data in the grid within a column. Default is Left. */
	private String griddataalign;

	/** Enter Left, Right, or Center to position data within a column header. Default is Left. */
	private String colheaderalign;

	/** Height value of the grid control, in pixels. */
	private double height;

	/** Name of the query associated with the grid control. */
	private String query;

	/** Specifies the maximum number of rows to display in the grid. */
	private String maxrows;

	/**
	 * Alignment value. Options are: Top, Left, Bottom, Baseline, Texttop, Absbottom, Middle, Absmiddle,
	 * Right.
	 */
	private String align;

	/** Vertical margin spacing above and below the grid control, in pixels. */
	private double vspace;

	/** Yes lets end users insert row data into the grid. Default is No. */
	private boolean insert;

	/**
	 * Background color value for the grid control. Entries are: black, magenta, cyan, orange, darkgray,
	 * pink, gray, white, lightgray, yellow. A hex value can be entered in the form: bgColor =
	 * "##xxxxxx" where x is 0-9 or A-F. Use either two pound signs or no pound signs.
	 */
	private String bgcolor;

	/**
	 * When used with href, Yes passes query string value of the selected tree item in the URL to the
	 ** application page specified in the cfform action attribute. Default is Yes.
	 */
	private boolean appendkey;

	/** A name for the grid element. */
	private String name;

	/** Text to use for the Sort button. Default is "A - Z". */
	private String sortascendingbutton;

	/** Yes or No. Yes displays row label text in italic. Default is No. */
	private boolean rowheaderitalic;

	/**
	 * The name of a JavaScript function used to validate user input. The form object, input object, and
	 * input object value are passed to the routine, which should return True if validation succeeds and
	 ** False otherwise.
	 */
	private String onvalidate;

	/**
	 * URL to associate with the grid item or a query column for a grid that is populated from a query.
	 ** If href is a query column, the href value is populated by the query. If href is not recognized as
	 * a query column, it is assumed that the href text is an actual HTML href.
	 */
	private String href;

	/** Yes or No. Yes displays column header text in boldface. Default is No. */
	private boolean colheaderbold;

	/** Yes lets end users delete row data from the grid. Default is No. */
	private boolean delete;

	/** Size for row label text in the grid control, in points. */
	private double rowheaderfontsize;

	/** Selection mode for items in the grid control. */
	private String selectmode;

	/** Yes or No. Yes displays row label text in boldface. Default is No. */
	private boolean rowheaderbold;

	/** Size for column header text in the grid control, in points. */
	private double colheaderfontsize;

	/** Enter Left, Right, or Center to position data within a row header. Default is Left. */
	private String rowheaderalign;

	/** Font to use for the row label. */
	private String rowheaderfont;

	/**
	 * Yes or No. Yes displays a column of numeric row labels in the grid control. Defaults to Yes.
	 */
	private boolean rowheaders;

	/** Yes displays grid control text in boldface. Default is No. */
	private boolean bold;

	/**
	 * Color value for the grid control column headers. Valid entries are: black (default), magenta,
	 ** cyan, orange, darkgray, pink, gray, white, lightgray, yellow.A hex value can be entered in the
	 * form: colHeaderTextColor = "##xxxxxx" where x is 0-9 or A-F. Use either two pound signs or no
	 * pound signs.
	 */
	private String colheadertextcolor;

	/** Horizontal margin spacing to the left and right of the grid control, in pixels. */
	private double hspace;

	/** Text to use for the Sort button. Default is "Z - A". */
	private String sortdescendingbutton;

	private int format;
	private boolean enabled;
	private String onchange;
	private String onblur;
	private String onfocus;
	private String style;
	private String tooltip;
	private boolean visible;

	@Override
	public void release() {
		super.release();
		width = 0d;
		hrefkey = "";
		sort = false;
		colheaders = false;
		rowheadertextcolor = "";
		font = "";
		colheaderitalic = false;
		autowidth = false;
		selectcolor = "";
		highlighthref = false;
		italic = false;
		gridlines = false;
		picturebar = false;
		deletebutton = "";
		textcolor = "";
		insertbutton = "";
		rowheight = 0d;
		notsupported = "";
		fontsize = 0d;
		rowheaderwidth = 0d;
		onerror = "";
		target = "";
		colheaderfont = "";
		griddataalign = "";
		colheaderalign = "";
		height = 0d;
		query = "";
		maxrows = "";
		align = "";
		vspace = 0d;
		insert = false;
		bgcolor = "";
		appendkey = false;
		name = "";
		sortascendingbutton = "";
		rowheaderitalic = false;
		onvalidate = "";
		href = "";
		colheaderbold = false;
		delete = false;
		rowheaderfontsize = 0d;
		selectmode = "";
		rowheaderbold = false;
		colheaderfontsize = 0d;
		rowheaderalign = "";
		rowheaderfont = "";
		rowheaders = false;
		bold = false;
		colheadertextcolor = "";
		hspace = 0d;
		sortdescendingbutton = "";

		format = 0;
		enabled = true;
		onchange = null;
		onblur = null;
		onfocus = null;
		style = null;
		tooltip = null;
		visible = true;
	}

	/**
	 * constructor for the tag class
	 * 
	 * @throws TagNotSupported
	 **/
	public Grid() throws TagNotSupported {
		// TODO implement tag
		throw new TagNotSupported("grid");
	}

	/**
	 * set the value width Width value of the grid control, in pixels.
	 * 
	 * @param width value to set
	 **/
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * set the value hrefkey The name of a query column when the grid uses a query. The column specified
	 * becomes the Key regardless of the select mode for the grid.
	 * 
	 * @param hrefkey value to set
	 **/
	public void setHrefkey(String hrefkey) {
		this.hrefkey = hrefkey;
	}

	/**
	 * set the value sort If Yes, sort buttons are added to the grid control. When clicked, sort buttons
	 * perform a simple text sort on the selected column. Default is No. Note that columns are sortable
	 * by clicking the column head, even if no sort button is displayed.
	 * 
	 * @param sort value to set
	 **/
	public void setSort(boolean sort) {
		this.sort = sort;
	}

	/**
	 * set the value colheaders Yes or No. Yes displays column headers in the grid control. Default is
	 * Yes.
	 * 
	 * @param colheaders value to set
	 **/
	public void setColheaders(boolean colheaders) {
		this.colheaders = colheaders;
	}

	/**
	 * set the value rowheadertextcolor Text color value for the grid control row headers. Entries are:
	 * black (default), magenta, cyan, orange, darkgray, pink, gray, white, lightgray, yellow. A hex
	 * value can be entered in the form: rowHeaderTextColor = "##xxxxxx" Where x is 0-9 or A-F. Use two
	 * pound signs or no pound signs.
	 * 
	 * @param rowheadertextcolor value to set
	 **/
	public void setRowheadertextcolor(String rowheadertextcolor) {
		this.rowheadertextcolor = rowheadertextcolor;
	}

	/**
	 * set the value font Font to use for column data in the grid control.
	 * 
	 * @param font value to set
	 **/
	public void setFont(String font) {
		this.font = font;
	}

	/**
	 * set the value colheaderitalic Yes or No. Yes displays column header text in italic. Default is
	 * No.
	 * 
	 * @param colheaderitalic value to set
	 **/
	public void setColheaderitalic(boolean colheaderitalic) {
		this.colheaderitalic = colheaderitalic;
	}

	/**
	 * set the value autowidth Optional. Yes or No. Default is No. If Yes, automatically sets the width
	 * of each column so that all the columns are visible within the grid's specified width. All columns
	 * are initially set to equal widths. Users can resize any column. No horizontal scroll bars are
	 * available since all columns are visible. note that if you explicitly specify the width of a
	 * column and set autoWidth to Yes, CFML will set the column to the explicit width, if possible.
	 * 
	 * @param autowidth value to set
	 **/
	public void setAutowidth(boolean autowidth) {
		this.autowidth = autowidth;
	}

	/**
	 * set the value selectcolor Background color for a selected item. See bgColor for color options.
	 * 
	 * @param selectcolor value to set
	 **/
	public void setSelectcolor(String selectcolor) {
		this.selectcolor = selectcolor;
	}

	/**
	 * set the value highlighthref Yes highlights links associated with a cfgrid with an href attribute
	 * value. No disables highlight. Default is Yes.
	 * 
	 * @param highlighthref value to set
	 **/
	public void setHighlighthref(boolean highlighthref) {
		this.highlighthref = highlighthref;
	}

	/**
	 * set the value italic Yes displays grid control text in italic. Default is No.
	 * 
	 * @param italic value to set
	 **/
	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	/**
	 * set the value gridlines Yes or No. Yes enables row and column rules (lines) in the grid control.
	 * No suppresses rules. Default is Yes.
	 * 
	 * @param gridlines value to set
	 **/
	public void setGridlines(boolean gridlines) {
		this.gridlines = gridlines;
	}

	/**
	 * set the value picturebar Yes or No. If Yes, images are used for the Insert, delete, and Sort
	 * buttons rather than text. Default is No.
	 * 
	 * @param picturebar value to set
	 **/
	public void setPicturebar(boolean picturebar) {
		this.picturebar = picturebar;
	}

	/**
	 * set the value deletebutton Text to use for the delete action button. The default is delete.
	 * 
	 * @param deletebutton value to set
	 **/
	public void setDeletebutton(String deletebutton) {
		this.deletebutton = deletebutton;
	}

	/**
	 * set the value textcolor Color value for text in the grid control. Options are: black (default),
	 * magenta, cyan, orange, darkgray, pink, gray, white, lightgray, yellow. A hex value can be entered
	 * in the form: textColor = "##xxxxxx" where x is 0-9 or A-F. Use two pound signs or no pound signs.
	 * 
	 * @param textcolor value to set
	 **/
	public void setTextcolor(String textcolor) {
		this.textcolor = textcolor;
	}

	/**
	 * set the value insertbutton Text to use for the Insert action button. The default is Insert.
	 * 
	 * @param insertbutton value to set
	 **/
	public void setInsertbutton(String insertbutton) {
		this.insertbutton = insertbutton;
	}

	/**
	 * set the value rowheight Number of pixels for the minimum row height of the grid control. Used
	 * with cfgridcolumn type = "Image", you can use rowHeight to define enough space for graphics to
	 * display in the row.
	 * 
	 * @param rowheight value to set
	 **/
	public void setRowheight(double rowheight) {
		this.rowheight = rowheight;
	}

	/**
	 * set the value notsupported Selection mode for items in the grid control.
	 * 
	 * @param notsupported value to set
	 **/
	public void setNotsupported(String notsupported) {
		this.notsupported = notsupported;
	}

	/**
	 * set the value fontsize Font size for text in the grid control, in points.
	 * 
	 * @param fontsize value to set
	 **/
	public void setFontsize(double fontsize) {
		this.fontsize = fontsize;
	}

	/**
	 * set the value rowheaderwidth The width, in pixels, of the row header column.
	 * 
	 * @param rowheaderwidth value to set
	 **/
	public void setRowheaderwidth(double rowheaderwidth) {
		this.rowheaderwidth = rowheaderwidth;
	}

	/**
	 * set the value onerror The name of a JavaScript function to execute in the event of a failed
	 * validation.
	 * 
	 * @param onerror value to set
	 **/
	public void setOnerror(String onerror) {
		this.onerror = onerror;
	}

	/**
	 * set the value target Target attribute for href URL.
	 * 
	 * @param target value to set
	 **/
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * set the value colheaderfont Font for the column header in the grid control.
	 * 
	 * @param colheaderfont value to set
	 **/
	public void setColheaderfont(String colheaderfont) {
		this.colheaderfont = colheaderfont;
	}

	/**
	 * set the value griddataalign Enter Left, Right, or Center to position data in the grid within a
	 * column. Default is Left.
	 * 
	 * @param griddataalign value to set
	 **/
	public void setGriddataalign(String griddataalign) {
		this.griddataalign = griddataalign;
	}

	/**
	 * set the value colheaderalign Enter Left, Right, or Center to position data within a column
	 * header. Default is Left.
	 * 
	 * @param colheaderalign value to set
	 **/
	public void setColheaderalign(String colheaderalign) {
		this.colheaderalign = colheaderalign;
	}

	/**
	 * set the value height Height value of the grid control, in pixels.
	 * 
	 * @param height value to set
	 **/
	public void setHeight(double height) {
		this.height = height;
	}

	/**
	 * set the value query Name of the query associated with the grid control.
	 * 
	 * @param query value to set
	 **/
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * set the value maxrows Specifies the maximum number of rows to display in the grid.
	 * 
	 * @param maxrows value to set
	 **/
	public void setMaxrows(String maxrows) {
		this.maxrows = maxrows;
	}

	/**
	 * set the value align Alignment value. Options are: Top, Left, Bottom, Baseline, Texttop,
	 * Absbottom, Middle, Absmiddle, Right.
	 * 
	 * @param align value to set
	 **/
	public void setAlign(String align) {
		this.align = align;
	}

	/**
	 * set the value vspace Vertical margin spacing above and below the grid control, in pixels.
	 * 
	 * @param vspace value to set
	 **/
	public void setVspace(double vspace) {
		this.vspace = vspace;
	}

	/**
	 * set the value insert Yes lets end users insert row data into the grid. Default is No.
	 * 
	 * @param insert value to set
	 **/
	public void setInsert(boolean insert) {
		this.insert = insert;
	}

	/**
	 * set the value bgcolor Background color value for the grid control. Entries are: black, magenta,
	 * cyan, orange, darkgray, pink, gray, white, lightgray, yellow. A hex value can be entered in the
	 * form: bgColor = "##xxxxxx" where x is 0-9 or A-F. Use either two pound signs or no pound signs.
	 * 
	 * @param bgcolor value to set
	 **/
	public void setBgcolor(String bgcolor) {
		this.bgcolor = bgcolor;
	}

	/**
	 * set the value appendkey When used with href, Yes passes query string value of the selected tree
	 * item in the URL to the application page specified in the cfform action attribute. Default is Yes.
	 * 
	 * @param appendkey value to set
	 **/
	public void setAppendkey(boolean appendkey) {
		this.appendkey = appendkey;
	}

	/**
	 * set the value name A name for the grid element.
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * set the value sortascendingbutton Text to use for the Sort button. Default is "A - Z".
	 * 
	 * @param sortascendingbutton value to set
	 **/
	public void setSortascendingbutton(String sortascendingbutton) {
		this.sortascendingbutton = sortascendingbutton;
	}

	/**
	 * set the value rowheaderitalic Yes or No. Yes displays row label text in italic. Default is No.
	 * 
	 * @param rowheaderitalic value to set
	 **/
	public void setRowheaderitalic(boolean rowheaderitalic) {
		this.rowheaderitalic = rowheaderitalic;
	}

	/**
	 * set the value onvalidate The name of a JavaScript function used to validate user input. The form
	 * object, input object, and input object value are passed to the routine, which should return True
	 * if validation succeeds and False otherwise.
	 * 
	 * @param onvalidate value to set
	 **/
	public void setOnvalidate(String onvalidate) {
		this.onvalidate = onvalidate;
	}

	/**
	 * set the value href URL to associate with the grid item or a query column for a grid that is
	 * populated from a query. If href is a query column, the href value is populated by the query. If
	 * href is not recognized as a query column, it is assumed that the href text is an actual HTML
	 * href.
	 * 
	 * @param href value to set
	 **/
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * set the value colheaderbold Yes or No. Yes displays column header text in boldface. Default is
	 * No.
	 * 
	 * @param colheaderbold value to set
	 **/
	public void setColheaderbold(boolean colheaderbold) {
		this.colheaderbold = colheaderbold;
	}

	/**
	 * set the value delete Yes lets end users delete row data from the grid. Default is No.
	 * 
	 * @param delete value to set
	 **/
	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	/**
	 * set the value rowheaderfontsize Size for row label text in the grid control, in points.
	 * 
	 * @param rowheaderfontsize value to set
	 **/
	public void setRowheaderfontsize(double rowheaderfontsize) {
		this.rowheaderfontsize = rowheaderfontsize;
	}

	/**
	 * set the value selectmode Selection mode for items in the grid control.
	 * 
	 * @param selectmode value to set
	 **/
	public void setSelectmode(String selectmode) {
		this.selectmode = selectmode;
	}

	/**
	 * set the value rowheaderbold Yes or No. Yes displays row label text in boldface. Default is No.
	 * 
	 * @param rowheaderbold value to set
	 **/
	public void setRowheaderbold(boolean rowheaderbold) {
		this.rowheaderbold = rowheaderbold;
	}

	/**
	 * set the value colheaderfontsize Size for column header text in the grid control, in points.
	 * 
	 * @param colheaderfontsize value to set
	 **/
	public void setColheaderfontsize(double colheaderfontsize) {
		this.colheaderfontsize = colheaderfontsize;
	}

	/**
	 * set the value rowheaderalign Enter Left, Right, or Center to position data within a row header.
	 * Default is Left.
	 * 
	 * @param rowheaderalign value to set
	 **/
	public void setRowheaderalign(String rowheaderalign) {
		this.rowheaderalign = rowheaderalign;
	}

	/**
	 * set the value rowheaderfont Font to use for the row label.
	 * 
	 * @param rowheaderfont value to set
	 **/
	public void setRowheaderfont(String rowheaderfont) {
		this.rowheaderfont = rowheaderfont;
	}

	/**
	 * set the value rowheaders Yes or No. Yes displays a column of numeric row labels in the grid
	 * control. Defaults to Yes.
	 * 
	 * @param rowheaders value to set
	 **/
	public void setRowheaders(boolean rowheaders) {
		this.rowheaders = rowheaders;
	}

	/**
	 * set the value bold Yes displays grid control text in boldface. Default is No.
	 * 
	 * @param bold value to set
	 **/
	public void setBold(boolean bold) {
		this.bold = bold;
	}

	/**
	 * set the value colheadertextcolor Color value for the grid control column headers. Valid entries
	 * are: black (default), magenta, cyan, orange, darkgray, pink, gray, white, lightgray, yellow.A hex
	 * value can be entered in the form: colHeaderTextColor = "##xxxxxx" where x is 0-9 or A-F. Use
	 * either two pound signs or no pound signs.
	 * 
	 * @param colheadertextcolor value to set
	 **/
	public void setColheadertextcolor(String colheadertextcolor) {
		this.colheadertextcolor = colheadertextcolor;
	}

	/**
	 * set the value hspace Horizontal margin spacing to the left and right of the grid control, in
	 * pixels.
	 * 
	 * @param hspace value to set
	 **/
	public void setHspace(double hspace) {
		this.hspace = hspace;
	}

	/**
	 * set the value sortdescendingbutton Text to use for the Sort button. Default is "Z - A".
	 * 
	 * @param sortdescendingbutton value to set
	 **/
	public void setSortdescendingbutton(String sortdescendingbutton) {
		this.sortdescendingbutton = sortdescendingbutton;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {
		// this.format = format;
	}

	/**
	 * @param onblur the onblur to set
	 */
	public void setOnblur(String onblur) {
		this.onblur = onblur;
	}

	/**
	 * @param onchange the onchange to set
	 */
	public void setOnchange(String onchange) {
		this.onchange = onchange;
	}

	/**
	 * @param onfocus the onfocus to set
	 */
	public void setOnfocus(String onfocus) {
		this.onfocus = onfocus;
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

	@Override
	public int doStartTag() {
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	@Override
	public void doInitBody() {

	}

	public void addRow(String[] data) {

	}

	public void addColumn(GridColumnBean column) {

	}

}