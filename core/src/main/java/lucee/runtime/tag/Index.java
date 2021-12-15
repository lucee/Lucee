/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.tag;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.search.IndexResult;
import lucee.runtime.search.SearchCollection;
import lucee.runtime.search.SearchException;
import lucee.runtime.search.SearchIndex;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

/**
 * Populates collections with indexed data.
 **/
public final class Index extends TagImpl {

	private static final String[] EMPTY = new String[0];

	public static String[] EXTENSIONS = ArrayUtil.toArray(Constants.getTemplateExtensions(), "htm", "html", "dbm", "dbml");

	/** Specifies the index action. */
	private String action;

	/**
	 * Specifies the URL path for files if type = "file" and type = "path". When the collection is
	 ** searched with cfsearch, the pathname is automatically be prepended to filenames and returned as
	 ** the url attribute.
	 */
	private String urlpath;

	/** Specifies the type of entity being indexed. Default is CUSTOM. */
	private short type = -1;

	/**
	 * Title for collection; Query column name for type and a valid query name; Permits searching
	 * collections by title or displaying a separate title from the key
	 */
	private String title;

	private String language;

	/**
	 * Specifies the comma-separated list of file extensions that CFML uses to index files if type =
	 * "Path". Default is HTM, HTML, CFM, CFML, DBM, DBML. An entry of "*." returns files with no
	 * extension
	 */
	private String[] extensions = EXTENSIONS;

	/**  */
	private String key;

	/**
	 * A custom field you can use to store data during an indexing operation. Specify a query column
	 ** name for type and a query name.
	 */
	private String custom1;
	private long timeout = 10000;

	/**
	 * A custom field you can use to store data during an indexing operation. Usage is the same as for
	 * custom1.
	 */
	private String custom2;

	/**
	 * A custom field you can use to store data during an indexing operation. Usage is the same as for
	 * custom1.
	 */
	private String custom3;

	/**
	 * A custom field you can use to store data during an indexing operation. Usage is the same as for
	 * custom1.
	 */
	private String custom4;

	/** Specifies the name of the query against which the collection is generated. */
	private String query;

	/**
	 * Specifies a collection name. If you are indexing an external collection external = "Yes", specify
	 * the collection name, including fully qualified path.
	 */
	private SearchCollection collection;

	/**
	 * Yes or No. Yes specifies, if type = "Path", that directories below the path specified in key are
	 * included in the indexing operation.
	 */
	private boolean recurse;

	/**  */
	private String body;
	private String name;

	private String[] category = EMPTY;
	private String categoryTree = "";
	private String status;
	private String prefix;

	private boolean throwontimeout = false;

	@Override
	public void release() {
		super.release();
		action = null;
		urlpath = null;
		type = -1;
		title = null;
		language = null;
		extensions = EXTENSIONS;
		key = null;

		custom1 = null;
		custom2 = null;
		custom3 = null;
		custom4 = null;

		query = null;
		collection = null;
		recurse = false;
		body = null;
		name = null;

		category = EMPTY;
		categoryTree = "";
		status = null;
		prefix = null;
		timeout = 10000;
		throwontimeout = false;
	}

	/**
	 * set the value action Specifies the index action.
	 * 
	 * @param action value to set
	 **/
	public void setAction(String action) {
		this.action = action.toLowerCase().trim();
	}

	/**
	 * set the value urlpath Specifies the URL path for files if type = "file" and type = "path". When
	 * the collection is searched with cfsearch, the pathname is automatically be prepended to filenames
	 * and returned as the url attribute.
	 * 
	 * @param urlpath value to set
	 **/
	public void setUrlpath(String urlpath) {
		if (StringUtil.isEmpty(urlpath)) return;
		this.urlpath = urlpath.toLowerCase().trim();
	}

	/**
	 * set the value type Specifies the type of entity being indexed. Default is CUSTOM.
	 * 
	 * @param type value to set
	 * @throws PageException
	 **/
	public void setType(String type) throws PageException {
		if (type == null) return;
		try {
			this.type = toType(type);
		}
		catch (SearchException e) {
			throw Caster.toPageException(e);
		}
	}

	public static short toType(String type) throws SearchException {
		type = type.toLowerCase().trim();
		if (type.equals("custom")) return SearchIndex.TYPE_CUSTOM;
		else if (type.equals("query")) return SearchIndex.TYPE_CUSTOM;
		else if (type.equals("file")) return SearchIndex.TYPE_FILE;
		else if (type.equals("path")) return SearchIndex.TYPE_PATH;
		else if (type.equals("url")) return SearchIndex.TYPE_URL;
		else throw new SearchException("invalid value for attribute type [" + type + "]");
	}

	/**
	 * @param timeout the timeout in seconds
	 * @throws ApplicationException
	 */
	public void setTimeout(double timeout) throws ApplicationException {

		this.timeout = (long) (timeout * 1000D);
		if (this.timeout < 0) throw new ApplicationException("attribute timeout must contain a positive number");
		if (timeout == 0) timeout = 1;
	}

	/**
	 * set the value throwontimeout Yes or No. Specifies how timeout conditions are handled. If the
	 * value is Yes, an exception is generated to provide notification of the timeout. If the value is
	 * No, execution continues. Default is Yes.
	 * 
	 * @param throwontimeout value to set
	 **/
	public void setThrowontimeout(boolean throwontimeout) {
		this.throwontimeout = throwontimeout;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * set the value title Title for collection; Query column name for type and a valid query name;
	 * Permits searching collections by title or displaying a separate title from the key
	 * 
	 * @param title value to set
	 **/
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * set the value custom1 A custom field you can use to store data during an indexing operation.
	 * Specify a query column name for type and a query name.
	 * 
	 * @param custom1 value to set
	 **/
	public void setCustom1(String custom1) {
		this.custom1 = custom1;
	}

	/**
	 * set the value language
	 * 
	 * @param language value to set
	 **/
	public void setLanguage(String language) {
		if (StringUtil.isEmpty(language)) return;
		this.language = Collection.validateLanguage(language);
	}

	/**
	 * set the value external
	 * 
	 * @param external value to set
	 * @throws ApplicationException
	 **/
	public void setExternal(boolean external) {
		// DeprecatedUtil.tagAttribute(pageContext,"Index", "external");
	}

	/**
	 * set the value extensions
	 * 
	 * @param extensions value to set
	 * @throws PageException
	 **/
	public void setExtensions(String extensions) throws PageException {
		if (extensions == null) return;
		this.extensions = ListUtil.toStringArrayTrim(ListUtil.listToArray(extensions, ','));
	}

	/**
	 * set the value key
	 * 
	 * @param key value to set
	 **/
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * set the value custom2 A custom field you can use to store data during an indexing operation.
	 * Usage is the same as for custom1.
	 * 
	 * @param custom2 value to set
	 **/
	public void setCustom2(String custom2) {
		this.custom2 = custom2;
	}

	/**
	 * @param custom3 The custom3 to set.
	 */
	public void setCustom3(String custom3) {
		this.custom3 = custom3;
	}

	/**
	 * @param custom4 The custom4 to set.
	 */
	public void setCustom4(String custom4) {
		this.custom4 = custom4;
	}

	/**
	 * set the value query Specifies the name of the query against which the collection is generated.
	 * 
	 * @param query value to set
	 **/
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * set the value collection Specifies a collection name. If you are indexing an external collection
	 * external = "Yes", specify the collection name, including fully qualified path.
	 * 
	 * @param collection value to set
	 * @throws PageException
	 **/
	public void setCollection(String collection) throws PageException {
		try {
			this.collection = pageContext.getConfig().getSearchEngine(pageContext).getCollectionByName(collection.toLowerCase().trim());
		}
		catch (SearchException e) {
			throw Caster.toPageException(e);
		}

	}

	/**
	 * set the value recurse Yes or No. Yes specifies, if type = "Path", that directories below the path
	 * specified in key are included in the indexing operation.
	 * 
	 * @param recurse value to set
	 **/
	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}

	/**
	 * set the value body
	 * 
	 * @param body value to set
	 **/
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @param category the category to set
	 * @throws ApplicationException
	 */
	public void setCategory(String listCategories) {
		if (listCategories == null) return;
		this.category = ListUtil.trimItems(ListUtil.listToStringArray(listCategories, ','));
	}

	/**
	 * @param categoryTree the categoryTree to set
	 * @throws ApplicationException
	 */
	public void setCategorytree(String categoryTree) {
		if (categoryTree == null) return;
		categoryTree = categoryTree.replace('\\', '/').trim();
		if (StringUtil.startsWith(categoryTree, '/')) categoryTree = categoryTree.substring(1);
		if (!StringUtil.endsWith(categoryTree, '/') && categoryTree.length() > 0) categoryTree += "/";
		this.categoryTree = categoryTree;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * @param status the status to set
	 * @throws ApplicationException
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public int doStartTag() throws PageException {
		// SerialNumber sn = pageContext.getConfig().getSerialNumber();
		// if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY)
		// throw new SecurityException("no access to this functionality with the "+sn.getStringVersion()+"
		// version of Lucee");

		try {
			if (action.equals("purge")) doPurge();
			else if (action.equals("update")) doUpdate();
			else if (action.equals("delete")) doDelete();
			else if (action.equals("refresh")) doRefresh();
			else if (action.equals("list")) doList();

			else throw new ApplicationException("invalid action name [" + action + "]", "valid action names are [list,update, delete, purge, refresh]");
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		return SKIP_BODY;
	}

	/**
	 * @throws PageException
	 * @throws SearchException
	 * @throws IOException
	 * 
	 */
	private void doRefresh() throws PageException, SearchException, IOException {
		doPurge();
		doUpdate();
	}

	private void doList() throws ApplicationException, PageException {
		required("index", action, "name", name);
		pageContext.setVariable(name, collection.getIndexesAsQuery());
	}

	/**
	 * delete a collection
	 * 
	 * @throws PageException
	 * @throws SearchException
	 */
	private void doDelete() throws PageException, SearchException {
		required("index", action, "collection", collection);
		if (type != SearchIndex.TYPE_CUSTOM) required("index", action, "key", key);

		// no type defined
		if (type == -1) {
			if (query != null) {
				type = SearchIndex.TYPE_CUSTOM;
			}
			else {
				Resource file = null;
				try {
					file = ResourceUtil.toResourceExisting(pageContext, key);
					pageContext.getConfig().getSecurityManager().checkFileLocation(file);
				}
				catch (ExpressionException e) {
				}

				if (file != null && file.exists() && file.isFile()) type = SearchIndex.TYPE_FILE;
				else if (file != null && file.exists() && file.isDirectory()) type = SearchIndex.TYPE_PATH;
				else {
					try {
						new URL(key);
						type = SearchIndex.TYPE_URL;
					}
					catch (MalformedURLException e) {
					}
				}
			}
		}

		collection.deleteIndex(pageContext, key, type, query);
	}

	/**
	 * purge a collection
	 * 
	 * @throws PageException
	 * @throws SearchException
	 */
	private void doPurge() throws PageException, SearchException {
		required("index", action, "collection", collection);
		collection.purge();
	}

	/**
	 * update a collection
	 * 
	 * @throws PageException
	 * @throws SearchException
	 * @throws IOException
	 */
	private void doUpdate() throws PageException, SearchException, IOException {
		// check attributes
		required("index", action, "collection", collection);
		required("index", action, "key", key);

		if (type == -1) type = (query == null) ? SearchIndex.TYPE_FILE : SearchIndex.TYPE_CUSTOM;

		if (type == SearchIndex.TYPE_CUSTOM) {
			required("index", action, "body", body);
			// required("index",action,"query",query);
		}
		IndexResult result;

		result = collection.index(pageContext, key, type, urlpath, title, body, language, extensions, query, recurse, categoryTree, category, timeout, custom1, custom2, custom3,
				custom4);
		if (!StringUtil.isEmpty(status)) pageContext.setVariable(status, toStruct(result));
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	private Struct toStruct(IndexResult result) {
		Struct sct = new StructImpl();
		sct.setEL("deleted", new Double(result.getCountDeleted()));
		sct.setEL("inserted", new Double(result.getCountInserted()));
		sct.setEL("updated", new Double(result.getCountUpdated()));
		return sct;
	}

}