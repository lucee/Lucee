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

import java.util.ArrayList;
import java.util.List;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.TagNotSupported;
import lucee.runtime.ext.tag.BodyTagImpl;

public final class Tree extends BodyTagImpl {

	private String name;
	private boolean required;
	private String delimiter;
	private String completepath;
	private boolean appendkey;
	private boolean highlightref;
	private String onvalidate;
	private String message;
	private String onerror;
	private String lookandfeel;
	private String font;
	private double fontsize;
	private boolean italic;
	private boolean bold;
	private double height;
	private double width;
	private double vspace;
	private String align;
	private boolean border;
	private boolean hscroll;
	private boolean vscroll;
	private String notsupported;
	private String onblur;
	private String onfocus;
	private String format;
	private String onchange;
	private String style;
	private String tooltip;
	private boolean visible;

	private String enabled;
	private List items = new ArrayList();

	public Tree() throws ApplicationException {
		throw new TagNotSupported("tree");
	}

	/**
	 * @param align the align to set
	 */
	public void setAlign(String align) {
		this.align = align;
	}

	/**
	 * @param appendkey the appendkey to set
	 */
	public void setAppendkey(boolean appendkey) {
		this.appendkey = appendkey;
	}

	/**
	 * @param bold the bold to set
	 */
	public void setBold(boolean bold) {
		this.bold = bold;
	}

	/**
	 * @param border the border to set
	 */
	public void setBorder(boolean border) {
		this.border = border;
	}

	/**
	 * @param completepath the completepath to set
	 */
	public void setCompletepath(String completepath) {
		this.completepath = completepath;
	}

	/**
	 * @param delimiter the delimiter to set
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	/**
	 * @param font the font to set
	 */
	public void setFont(String font) {
		this.font = font;
	}

	/**
	 * @param fontsize the fontsize to set
	 */
	public void setFontsize(double fontsize) {
		this.fontsize = fontsize;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(double height) {
		this.height = height;
	}

	/**
	 * @param highlightref the highlightref to set
	 */
	public void setHighlightref(boolean highlightref) {
		this.highlightref = highlightref;
	}

	/**
	 * @param hscroll the hscroll to set
	 */
	public void setHscroll(boolean hscroll) {
		this.hscroll = hscroll;
	}

	/**
	 * @param italic the italic to set
	 */
	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	/**
	 * @param lookandfeel the lookandfeel to set
	 */
	public void setLookandfeel(String lookandfeel) {
		this.lookandfeel = lookandfeel;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param notsupported the notsupported to set
	 */
	public void setNotsupported(String notsupported) {
		this.notsupported = notsupported;
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
	 * @param onerror the onerror to set
	 */
	public void setOnerror(String onerror) {
		this.onerror = onerror;
	}

	/**
	 * @param onfocus the onfocus to set
	 */
	public void setOnfocus(String onfocus) {
		this.onfocus = onfocus;
	}

	/**
	 * @param onvalidate the onvalidate to set
	 */
	public void setOnvalidate(String onvalidate) {
		this.onvalidate = onvalidate;
	}

	/**
	 * @param required the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
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
	 * @param vscroll the vscroll to set
	 */
	public void setVscroll(boolean vscroll) {
		this.vscroll = vscroll;
	}

	/**
	 * @param vspace the vspace to set
	 */
	public void setVspace(double vspace) {
		this.vspace = vspace;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(double width) {
		this.width = width;
	}

	public void addTreeItem(TreeItemBean item) {
		items.add(item);
	}

}