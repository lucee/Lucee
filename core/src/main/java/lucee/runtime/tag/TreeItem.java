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

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.TagNotSupported;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;

public class TreeItem extends TagImpl {

	private String value = null;
	private String display = null;
	private String parent = null;
	private String strImg = null;
	private int intImg = TreeItemBean.IMG_FOLDER;
	private String strImgOpen = null;
	private int intImgOpen = TreeItemBean.IMG_FOLDER;
	private String href = null;
	private String target = null;
	private String query = null;
	private String strQueryAsRootCustom = null;
	private int intQueryAsRoot = TreeItemBean.QUERY_AS_ROOT_YES;
	private boolean expand = true;

	public TreeItem() throws TagNotSupported {
		throw new TagNotSupported("TreeItem");
	}

	@Override
	public void release() {
		value = null;
		display = null;
		parent = null;
		strImg = null;
		intImg = TreeItemBean.IMG_FOLDER;
		strImgOpen = null;
		intImgOpen = TreeItemBean.IMG_FOLDER;
		href = null;
		target = null;
		query = null;
		strQueryAsRootCustom = null;
		intQueryAsRoot = TreeItemBean.QUERY_AS_ROOT_YES;
		expand = true;
	}

	/**
	 * @param display the display to set
	 */
	public void setDisplay(String display) {
		this.display = display;
	}

	/**
	 * @param expand the expand to set
	 */
	public void setExpand(boolean expand) {
		this.expand = expand;
	}

	/**
	 * @param href the href to set
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * @param img the img to set
	 */
	public void setImg(String img) {
		this.strImg = img;
		this.intImg = toIntImg(img);
	}

	/**
	 * @param imgopen the imgopen to set
	 */
	public void setImgopen(String imgopen) {
		this.strImgOpen = imgopen;
		this.intImgOpen = toIntImg(imgopen);
	}

	private int toIntImg(String img) {
		img = img.trim().toLowerCase();
		if ("cd".equals(img)) return TreeItemBean.IMG_CD;
		else if ("computer".equals(img)) return TreeItemBean.IMG_COMPUTER;
		else if ("document".equals(img)) return TreeItemBean.IMG_DOCUMENT;
		else if ("element".equals(img)) return TreeItemBean.IMG_ELEMENT;
		else if ("folder".equals(img)) return TreeItemBean.IMG_FOLDER;
		else if ("floppy".equals(img)) return TreeItemBean.IMG_FLOPPY;
		else if ("fixed".equals(img)) return TreeItemBean.IMG_FIXED;
		else if ("remote".equals(img)) return TreeItemBean.IMG_REMOTE;
		return TreeItemBean.IMG_CUSTOM;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(String parent) {
		this.parent = parent;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * @param queryAsRoot the queryAsRoot to set
	 */
	public void setQueryasroot(String queryAsRoot) {
		strQueryAsRootCustom = queryAsRoot;

		Boolean b = Caster.toBoolean(queryAsRoot, null);
		if (b == null) intQueryAsRoot = TreeItemBean.QUERY_AS_ROOT_CUSTOM;
		else intQueryAsRoot = b.booleanValue() ? TreeItemBean.QUERY_AS_ROOT_YES : TreeItemBean.QUERY_AS_ROOT_NO;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int doStartTag() throws ApplicationException {
		Tree tree = getTree();

		if (display == null) display = value;
		if (query != null) doStartTagQuery(tree);
		else doStartTagNormal(tree);

		return SKIP_BODY;
	}

	private void doStartTagQuery(Tree tree) {
		// TODO Auto-generated method stub

	}

	private void doStartTagNormal(Tree tree) {
		TreeItemBean bean = new TreeItemBean();
		bean.setDisplay(display);
		bean.setExpand(expand);
		bean.setHref(href);
		bean.setImg(intImg);
		bean.setImgCustom(strImg);
		bean.setImgOpen(intImgOpen);
		bean.setImgOpenCustom(strImgOpen);
		bean.setParent(parent);
		bean.setTarget(target);
		bean.setValue(value);

		tree.addTreeItem(bean);
	}

	private Tree getTree() throws ApplicationException {
		Tag parent = getParent();
		while (parent != null && !(parent instanceof Tree)) {
			parent = parent.getParent();
		}

		if (parent instanceof Tree) return (Tree) parent;
		throw new ApplicationException("Wrong Context, tag TreeItem must be inside a Tree tag");

	}
}