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

public class TreeItemBean {

	public static final int QUERY_AS_ROOT_YES = 1;
	public static final int QUERY_AS_ROOT_NO = 0;
	public static final int QUERY_AS_ROOT_CUSTOM = 2;

	public static final int IMG_CD = 10;
	public static final int IMG_COMPUTER = 11;
	public static final int IMG_DOCUMENT = 12;
	public static final int IMG_ELEMENT = 13;
	public static final int IMG_FLOPPY = 14;
	public static final int IMG_FOLDER = 15;
	public static final int IMG_FIXED = 16;
	public static final int IMG_REMOTE = 17;
	public static final int IMG_CUSTOM = 18;

	private String value;
	private String display;
	private String parent;
	private int img = IMG_FOLDER;
	private String imgCustom = null;
	private int imgOpen = IMG_FOLDER;
	private String imgOpenCustom;
	private String href;
	private String target;
	// private String query;
	// private int queryAsRoot=QUERY_AS_ROOT_YES;
	// private String queryAsRootCustom;
	boolean expand = true;

	/**
	 * @return the display
	 */
	public String getDisplay() {
		return display;
	}

	/**
	 * @param display the display to set
	 */
	public void setDisplay(String display) {
		this.display = display;
	}

	/**
	 * @return the expand
	 */
	public boolean isExpand() {
		return expand;
	}

	/**
	 * @param expand the expand to set
	 */
	public void setExpand(boolean expand) {
		this.expand = expand;
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
	 * @return the img
	 */
	public int getImg() {
		return img;
	}

	/**
	 * @param img the img to set
	 */
	public void setImg(int img) {
		this.img = img;
	}

	/**
	 * @return the imgCustom
	 */
	public String getImgCustom() {
		return imgCustom;
	}

	/**
	 * @param imgCustom the imgCustom to set
	 */
	public void setImgCustom(String imgCustom) {
		this.imgCustom = imgCustom;
	}

	/**
	 * @return the imgOpen
	 */
	public int getImgOpen() {
		return imgOpen;
	}

	/**
	 * @param imgOpen the imgOpen to set
	 */
	public void setImgOpen(int imgOpen) {
		this.imgOpen = imgOpen;
	}

	/**
	 * @return the imgOpenCustom
	 */
	public String getImgOpenCustom() {
		return imgOpenCustom;
	}

	/**
	 * @param imgOpenCustom the imgOpenCustom to set
	 */
	public void setImgOpenCustom(String imgOpenCustom) {
		this.imgOpenCustom = imgOpenCustom;
	}

	/**
	 * @return the parent
	 */
	public String getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(String parent) {
		this.parent = parent;
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
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}