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

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.search.SearchCollection;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.search.SearchException;

/**
 * Allows you to create and administer Collections.
 **/
public final class Collection extends TagImpl {

	/** Specifies the action to perform. */
	private String action = "list";

	/**  */
	private Resource path;

	/** Specifies a collection name or an alias if action = "map" */
	private String collection;

	/** Name of the output variable (action=list) */
	private String name;

	/** language of the collection (operators,stopwords) */
	private String language = "english";

	// private boolean categories=false;

	@Override
	public void release() {
		super.release();
		action = "list";
		path = null;
		collection = null;
		name = null;
		language = "english";
		// categories=false;
	}

	/**
	 * @param categories the categories to set
	 * @throws ApplicationException
	 */
	public void setCategories(boolean categories) {
		// Lucee always support categories
		// this.categories = categories;
	}

	/**
	 * set the value action Specifies the action to perform.
	 * 
	 * @param action value to set
	 **/
	public void setAction(String action) {
		if (action == null) return;
		this.action = action.toLowerCase().trim();
	}

	public void setEngine(String engine) {
		// This setter only exists for compatibility reasons to other CFML engines, the attribute is
		// completely ignored.
	}

	/**
	 * set the value path
	 * 
	 * @param path value to set
	 * @throws PageException
	 **/
	public void setPath(String strPath) throws PageException {
		if (strPath == null) return;
		this.path = ResourceUtil.toResourceNotExisting(pageContext, strPath.trim());

		pageContext.getConfig().getSecurityManager().checkFileLocation(this.path);

		if (!this.path.exists()) {
			Resource parent = this.path.getParentResource();
			if (parent != null && parent.exists()) this.path.mkdirs();
			else {
				throw new ApplicationException("attribute path of the tag collection must be an existing directory");
			}
		}
		else if (!this.path.isDirectory()) throw new ApplicationException("attribute path of the tag collection must be an existing directory");
	}

	/**
	 * set the value collection Specifies a collection name or an alias if action = "map"
	 * 
	 * @param collection value to set
	 **/
	public void setCollection(String collection) {
		if (collection == null) return;
		this.collection = collection.toLowerCase().trim();
	}

	/**
	 * set the value name
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		if (name == null) return;
		this.name = name.toLowerCase().trim();
	}

	/**
	 * set the value language
	 * 
	 * @param language value to set
	 **/
	public void setLanguage(String language) {
		if (language == null) return;
		this.language = validateLanguage(language);
	}

	public static String validateLanguage(String language) {
		if (StringUtil.isEmpty(language, true)) return "english";
		language = language.toLowerCase().trim();
		if ("standard".equals(language)) return "english";
		return language;
	}

	@Override
	public int doStartTag() throws PageException {
		// SerialNumber sn = pageContext.getConfig().getSerialNumber();
		// if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY)
		// throw new SecurityException("no access to this functionality with the "+sn.getStringVersion()+"
		// version of Lucee");

		try {
			if (action.equals("create")) doCreate();
			else if (action.equals("repair")) doRepair();
			else if (action.equals("delete")) doDelete();
			else if (action.equals("optimize")) doOptimize();
			else if (action.equals("list")) doList();
			else if (action.equals("map")) doMap();
			else if (action.equals("categorylist")) doCategoryList();

			else throw new ApplicationException("Invalid value [" + action + "] for attribute action.", "allowed values are [create,repair,map,delete,optimize,list ]");
		}
		catch (SearchException e) {
			throw Caster.toPageException(e);
		}
		return SKIP_BODY;
	}

	/**
	 * @throws SearchException
	 * @throws PageException
	 * 
	 */
	private void doMap() throws SearchException, PageException {
		required("collection", action, "collection", collection);
		required("collection", action, "path", path);
		getCollection().map(path);
	}

	/**
	 * Creates a query in the PageContext containing all available Collections of the current
	 * searchStorage
	 * 
	 * @throws ApplicationException
	 * @throws PageException
	 * @throws SearchException
	 * 
	 */
	private void doList() throws PageException, SearchException {
		required("collection", action, "name", name);
		// if(StringUtil.isEmpty(name))throw new ApplicationException("for action list attribute name is
		// required");
		pageContext.setVariable(name, getSearchEngine().getCollectionsAsQuery());
	}

	private void doCategoryList() throws PageException, SearchException {
		// check attributes
		required("collection", action, "collection", collection);
		required("collection", action, "name", name);
		pageContext.setVariable(name, getCollection().getCategoryInfo());
	}

	/**
	 * Optimizes the Collection
	 * 
	 * @throws SearchException
	 * @throws PageException
	 * 
	 */
	private void doOptimize() throws SearchException, PageException {
		required("collection", action, "collection", collection);
		getCollection().optimize();
	}

	/**
	 * Deletes a Collection
	 * 
	 * @throws SearchException
	 * @throws PageException
	 * 
	 */
	private void doDelete() throws SearchException, PageException {
		required("collection", action, "collection", collection);
		getCollection().delete();
	}

	/**
	 * 
	 * @throws SearchException
	 * @throws PageException
	 * 
	 */
	private void doRepair() throws SearchException, PageException {
		required("collection", action, "collection", collection);
		getCollection().repair();
	}

	/**
	 * Creates a new collection
	 * 
	 * @throws SearchException
	 * @throws PageException
	 * 
	 */
	private void doCreate() throws SearchException, PageException {
		required("collection", action, "collection", collection);
		required("collection", action, "path", path);
		getSearchEngine().createCollection(collection, path, language, SearchEngine.DENY_OVERWRITE);
	}

	/**
	 * Returns the Searchstorage defined in the Environment
	 * 
	 * @return searchStorage
	 * @throws PageException
	 */
	private SearchEngine getSearchEngine() throws PageException {
		return pageContext.getConfig().getSearchEngine(pageContext);
	}

	/**
	 * the collection matching the collection name
	 * 
	 * @return collection
	 * @throws SearchException
	 * @throws PageException
	 */
	private SearchCollection getCollection() throws SearchException, PageException {
		return getSearchEngine().getCollectionByName(collection);
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}