/**
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package lucee.runtime.tag;

import static lucee.runtime.tag.util.FileUtil.NAMECONFLICT_ERROR;
import static lucee.runtime.tag.util.FileUtil.NAMECONFLICT_OVERWRITE;
import static lucee.runtime.tag.util.FileUtil.NAMECONFLICT_SKIP;
import static lucee.runtime.tag.util.FileUtil.NAMECONFLICT_UNDEFINED;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lucee.commons.io.ModeUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceMetaData;
import lucee.commons.io.res.filter.AndResourceFilter;
import lucee.commons.io.res.filter.DirectoryResourceFilter;
import lucee.commons.io.res.filter.FileResourceFilter;
import lucee.commons.io.res.filter.NotResourceFilter;
import lucee.commons.io.res.filter.OrResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.ModeObjectWrap;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.res.util.UDFFilter;
import lucee.commons.io.res.util.WildcardPatternFilter;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.tag.util.FileUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.KeyConstants;

/**
 * Handles interactions with directories.
 **/
public final class Directory extends TagImpl {

	public static final int TYPE_ALL = 0;
	public static final int TYPE_FILE = 1;
	public static final int TYPE_DIR = 2;

	public static final ResourceFilter DIRECTORY_FILTER = new DirectoryResourceFilter();
	public static final ResourceFilter FILE_FILTER = new FileResourceFilter();

	private static final Key MODE = KeyConstants._mode;
	private static final Key META = KeyConstants._meta;
	private static final Key DATE_LAST_MODIFIED = KeyConstants._dateLastModified;
	private static final Key ATTRIBUTES = KeyConstants._attributes;
	private static final Key DIRECTORY = KeyConstants._directory;

	public static final int LIST_INFO_QUERY_ALL = 1;
	public static final int LIST_INFO_QUERY_NAME = 2;
	public static final int LIST_INFO_ARRAY_NAME = 4;
	public static final int LIST_INFO_ARRAY_PATH = 8;

	public static final int NAMECONFLICT_DEFAULT = NAMECONFLICT_OVERWRITE; // default

	/**
	 * Optional for action = "list". Ignored by all other actions. File extension filter applied to
	 ** returned names. For example: *m. Only one mask filter can be applied at a time.
	 */
	// private final ResourceFilter filter=null;
	// private ResourceAndResourceNameFilter nameFilter=null;
	private ResourceFilter filter = null;

	private String pattern;
	private String patternDelimiters;

	/** The name of the directory to perform the action against. */
	private Resource directory;

	/** Defines the action to be taken with directory(ies) specified in directory. */
	private String action = "list";

	/**
	 * Optional for action = "list". Ignored by all other actions. The query columns by which to sort
	 ** the directory listing. Any combination of columns from query output can be specified in
	 * comma-separated list. You can specify ASC (ascending) or DESC (descending) as qualifiers for
	 * column names. ASC is the default
	 */
	private String sort;

	/**
	 * Used with action = "Create" to define the permissions for a directory on UNIX and Linux
	 ** platforms. Ignored on Windows. Options correspond to the octal values of the UNIX chmod command.
	 * From left to right, permissions are assigned for owner, group, and other.
	 */
	private int mode = -1;

	/**
	 * Required for action = "rename". Ignored by all other actions. The new name of the directory
	 ** specified in the directory attribute.
	 */
	private String strNewdirectory;

	/**
	 * Required for action = "list". Ignored by all other actions. Name of output query for directory
	 ** listing.
	 */
	private String name = null;

	private boolean recurse = false;

	private String serverPassword;

	private int type = TYPE_ALL;
	// private boolean listOnlyNames;
	private int listInfo = LIST_INFO_QUERY_ALL;
	// private int acl=S3Constants.ACL_UNKNOW;
	private Object acl = null;
	private String storage = null;
	private String destination;

	private int nameconflict = NAMECONFLICT_DEFAULT;

	private boolean createPath = true;

	@Override
	public void release() {
		super.release();
		acl = null;
		storage = null;

		type = TYPE_ALL;
		// filter=null;
		filter = null;
		destination = null;
		directory = null;
		action = "list";
		sort = null;
		mode = -1;
		strNewdirectory = null;
		name = null;
		recurse = false;
		serverPassword = null;
		listInfo = LIST_INFO_QUERY_ALL;

		nameconflict = NAMECONFLICT_DEFAULT;
		createPath = true;

		pattern = null;
		patternDelimiters = null;
	}

	public void setCreatepath(boolean createPath) {
		this.createPath = createPath;
	}

	/**
	 * sets a filter
	 * 
	 * @param filter
	 * @throws PageException
	 **/
	public void setFilter(Object filter) throws PageException {

		if (filter instanceof UDF) this.setFilter((UDF) filter);
		else if (filter instanceof String) this.setFilter((String) filter);
	}

	public void setFilter(UDF filter) throws PageException {
		this.filter = UDFFilter.createResourceAndResourceNameFilter(filter);
	}

	public void setFilter(String pattern) {
		this.pattern = pattern;
	}

	public void setFilterdelimiters(String patternDelimiters) {
		this.patternDelimiters = patternDelimiters;
	}

	/**
	 * set the value acl used only for s3 resources, for all others ignored
	 * 
	 * @param acl value to set
	 * @throws ApplicationException
	 * @Deprecated only exists for backward compatibility to old ra files.
	 **/
	public void setAcl(String acl) throws ApplicationException {
		this.acl = acl;
	}

	public void setAcl(Object acl) {
		this.acl = acl;
	}

	public void setStoreacl(Object acl) {
		this.acl = acl;
	}

	/**
	 * set the value storage used only for s3 resources, for all others ignored
	 * 
	 * @param storage value to set
	 * @throws PageException
	 **/
	public void setStorage(String storage) throws PageException {
		this.storage = improveStorage(storage);
	}

	public void setStorelocation(String storage) throws PageException {
		setStorage(storage);
	}

	public static String improveStorage(String storage) throws ApplicationException {
		storage = improveStorage(storage, null);
		if (storage != null) return storage;

		throw new ApplicationException("invalid storage value, valid values are [eu,us,us-west]");
	}

	public static String improveStorage(String storage, String defaultValue) {
		storage = storage.toLowerCase().trim();
		if ("us".equals(storage)) return "us";
		if ("usa".equals(storage)) return "us";
		if ("u.s.".equals(storage)) return "us";
		if ("u.s.a.".equals(storage)) return "us";
		if ("united states of america".equals(storage)) return "us";

		if ("eu".equals(storage)) return "eu";
		if ("europe.".equals(storage)) return "eu";
		if ("european union.".equals(storage)) return "eu";
		if ("euro.".equals(storage)) return "eu";
		if ("e.u.".equals(storage)) return "eu";

		if ("us-west".equals(storage)) return "us-west";
		if ("usa-west".equals(storage)) return "us-west";

		return defaultValue;
	}

	public void setServerpassword(String serverPassword) {
		this.serverPassword = serverPassword;
	}

	public void setListinfo(String strListinfo) {
		strListinfo = strListinfo.trim().toLowerCase();
		this.listInfo = "name".equals(strListinfo) ? LIST_INFO_QUERY_NAME : LIST_INFO_QUERY_ALL;
	}

	/**
	 * set the value directory The name of the directory to perform the action against.
	 * 
	 * @param directory value to set
	 **/
	public void setDirectory(String directory) {

		this.directory = ResourceUtil.toResourceNotExisting(pageContext, directory);
		// print.ln(this.directory);
	}

	/**
	 * set the value action Defines the action to be taken with directory(ies) specified in directory.
	 * 
	 * @param action value to set
	 **/
	public void setAction(String action) {
		this.action = action.toLowerCase();
	}

	/**
	 * set the value sort Optional for action = "list". Ignored by all other actions. The query columns
	 * by which to sort the directory listing. Any combination of columns from query output can be
	 * specified in comma-separated list. You can specify ASC (ascending) or DESC (descending) as
	 * qualifiers for column names. ASC is the default
	 * 
	 * @param sort value to set
	 **/
	public void setSort(String sort) {
		if (sort.trim().length() > 0) this.sort = sort;
	}

	/**
	 * set the value mode Used with action = "Create" to define the permissions for a directory on UNIX
	 * and Linux platforms. Ignored on Windows. Options correspond to the octal values of the UNIX chmod
	 * command. From left to right, permissions are assigned for owner, group, and other.
	 * 
	 * @param mode value to set
	 * @throws PageException
	 **/
	public void setMode(String mode) throws PageException {
		try {
			this.mode = ModeUtil.toOctalMode(mode);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * set the value newdirectory Required for action = "rename". Ignored by all other actions. The new
	 * name of the directory specified in the directory attribute.
	 * 
	 * @param newdirectory value to set
	 **/
	public void setNewdirectory(String newdirectory) {
		// this.newdirectory=ResourceUtil.toResourceNotExisting(pageContext ,newdirectory);
		this.strNewdirectory = newdirectory;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * set the value name Required for action = "list". Ignored by all other actions. Name of output
	 * query for directory listing.
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param recurse The recurse to set.
	 */
	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}

	/**
	 * set the value nameconflict Action to take if destination directory is the same as that of a file
	 * in the directory.
	 * 
	 * @param nameconflict value to set
	 * @throws ApplicationException
	 **/
	public void setNameconflict(String nameconflict) throws ApplicationException {

		this.nameconflict = FileUtil.toNameConflict(nameconflict, NAMECONFLICT_UNDEFINED | NAMECONFLICT_ERROR | NAMECONFLICT_OVERWRITE | NAMECONFLICT_SKIP, NAMECONFLICT_DEFAULT);
	}

	@Override
	public int doStartTag() throws PageException {

		if (this.filter == null && !StringUtil.isEmpty(this.pattern)) this.filter = new WildcardPatternFilter(pattern, patternDelimiters);

		// securityManager = pageContext.getConfig().getSecurityManager();
		if (action.equals("list")) {
			Object res = actionList(pageContext, directory, serverPassword, type, filter, listInfo, recurse, sort);
			if (!StringUtil.isEmpty(name) && res != null) pageContext.setVariable(name, res);
		}
		else if (action.equals("create")) actionCreate(pageContext, directory, serverPassword, createPath, mode, acl, storage, nameconflict);
		else if (action.equals("delete")) actionDelete(pageContext, directory, recurse, serverPassword);
		else if (action.equals("forcedelete")) actionDelete(pageContext, directory, true, serverPassword);
		else if (action.equals("rename")) {
			String res = actionRename(pageContext, directory, strNewdirectory, serverPassword, createPath, acl, storage);
			if (!StringUtil.isEmpty(name) && res != null) pageContext.setVariable(name, res);
		}
		else if (action.equals("copy")) {
			if (StringUtil.isEmpty(destination, true) && !StringUtil.isEmpty(strNewdirectory, true)) {
				destination = strNewdirectory.trim();
			}
			actionCopy(pageContext, directory, destination, serverPassword, createPath, acl, storage, filter, recurse, nameconflict);
		}
		else throw new ApplicationException("invalid action [" + action + "] for the tag directory");

		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	/**
	 * list all files and directories inside a directory
	 * 
	 * @throws PageException
	 */
	public static Object actionList(PageContext pageContext, Resource directory, String serverPassword, int type, ResourceFilter filter, int listInfo, boolean recurse, String sort)
			throws PageException {
		// check directory
		SecurityManager securityManager = pageContext.getConfig().getSecurityManager();
		securityManager.checkFileLocation(pageContext.getConfig(), directory, serverPassword);

		if (type != TYPE_ALL) {
			ResourceFilter typeFilter = (type == TYPE_DIR) ? DIRECTORY_FILTER : FILE_FILTER;
			if (filter == null) filter = typeFilter;
			else filter = new AndResourceFilter(new ResourceFilter[] { typeFilter, filter });
		}

		// create query Object
		String[] names = new String[] { "name", "size", "type", "dateLastModified", "attributes", "mode", "directory" };
		String[] types = new String[] { "VARCHAR", "DOUBLE", "VARCHAR", "DATE", "VARCHAR", "VARCHAR", "VARCHAR" };

		boolean hasMeta = directory instanceof ResourceMetaData;
		if (hasMeta) {
			names = new String[] { "name", "size", "type", "dateLastModified", "attributes", "mode", "directory", "meta" };
			types = new String[] { "VARCHAR", "DOUBLE", "VARCHAR", "DATE", "VARCHAR", "VARCHAR", "VARCHAR", "OBJECT" };
		}

		boolean typeArray = (listInfo == LIST_INFO_ARRAY_NAME) || (listInfo == LIST_INFO_ARRAY_PATH);
		boolean namesOnly = (listInfo == LIST_INFO_ARRAY_NAME) || (listInfo == LIST_INFO_QUERY_NAME);
		Array array = null;
		Object rtn;

		Query query = new QueryImpl(namesOnly ? new String[] { "name" } : names, namesOnly ? new String[] { "VARCHAR" } : types, 0, "query");

		if (typeArray) {
			rtn = array = new ArrayImpl();
		}
		else {
			rtn = query;
		}

		if (!directory.exists()) {
			if (directory instanceof FileResource) return rtn;
			throw new ApplicationException("directory [" + directory.toString() + "] doesn't exist");
		}
		if (!directory.isDirectory()) {
			if (directory instanceof FileResource) return rtn;
			throw new ApplicationException("file [" + directory.toString() + "] exists, but isn't a directory");
		}
		if (!directory.isReadable()) {
			if (directory instanceof FileResource) return rtn;
			throw new ApplicationException("no access to read directory [" + directory.toString() + "]");
		}

		long startNS = System.nanoTime();

		try {

			if (namesOnly) {
				if (typeArray) {
					_fillArrayPathOrName(array, directory, filter, 0, recurse, namesOnly);
					return array;
				}

				// Query Name, available via the cfdirectory tag but not via directoryList()
				if (recurse || type != TYPE_ALL) _fillQueryNamesRec("", query, directory, filter, 0, recurse);
				else _fillQueryNames(query, directory, filter, 0);
			}
			else {
				// Query All
				_fillQueryAll(query, directory, filter, 0, hasMeta, recurse);
			}
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}

		// sort
		if (sort != null && query != null) {
			String[] arr = sort.toLowerCase().split(",");
			for (int i = arr.length - 1; i >= 0; i--) {
				try {
					String[] col = arr[i].trim().split("\\s+");
					if (col.length == 1) query.sort(col[0].trim());
					else if (col.length == 2) {
						String order = col[1].toLowerCase().trim();
						if (order.equals("asc")) query.sort(col[0], lucee.runtime.type.Query.ORDER_ASC);
						else if (order.equals("desc")) query.sort(col[0], lucee.runtime.type.Query.ORDER_DESC);
						else throw new ApplicationException("invalid order type [" + col[1] + "]");
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
		}

		query.setExecutionTime(System.nanoTime() - startNS);

		if (typeArray) {
			java.util.Iterator it = query.getIterator();
			while (it.hasNext()) {
				Struct row = (Struct) it.next();
				if (namesOnly) array.appendEL(row.get("name"));
				else array.appendEL(row.get("directory") + lucee.commons.io.FileUtil.FILE_SEPERATOR_STRING + row.get("name"));
			}
		}

		return rtn;
	}

	private static int _fillQueryAll(Query query, Resource directory, ResourceFilter filter, int count, boolean hasMeta, boolean recurse) throws PageException, IOException {
		Resource[] list = directory.listResources();

		if (list == null || list.length == 0) return count;
		String dir = directory.getCanonicalPath();
		// fill data to query
		// query.addRow(list.length);
		boolean isDir;
		for (int i = 0; i < list.length; i++) {
			if (filter == null || filter.accept(list[i])) {
				query.addRow(1);
				count++;
				query.setAt(KeyConstants._name, count, list[i].getName());
				isDir = list[i].isDirectory();
				query.setAt(KeyConstants._size, count, new Double(isDir ? 0 : list[i].length()));
				query.setAt(KeyConstants._type, count, isDir ? "Dir" : "File");
				if (directory.getResourceProvider().isModeSupported()) {

					query.setAt(MODE, count, new ModeObjectWrap(list[i]));
				}
				query.setAt(DATE_LAST_MODIFIED, count, new Date(list[i].lastModified()));
				query.setAt(ATTRIBUTES, count, getFileAttribute(list[i], true));

				if (hasMeta) {
					query.setAt(META, count, ((ResourceMetaData) list[i]).getMetaData());
				}

				query.setAt(DIRECTORY, count, dir);
			}
			if (recurse && list[i].isDirectory()) count = _fillQueryAll(query, list[i], filter, count, hasMeta, recurse);
		}
		return count;
	}

	// this method only exists for performance reasion
	private static int _fillQueryNames(Query query, Resource directory, ResourceFilter filter, int count) throws PageException {
		if (filter == null || filter instanceof ResourceNameFilter) {
			ResourceNameFilter rnf = filter == null ? null : (ResourceNameFilter) filter;
			String[] list = directory.list();
			if (list == null || list.length == 0) return count;
			for (int i = 0; i < list.length; i++) {
				if (rnf == null || rnf.accept(directory, list[i])) {
					query.addRow(1);
					count++;
					query.setAt(KeyConstants._name, count, list[i]);
				}
			}
		}
		else {
			Resource[] list = directory.listResources();
			if (list == null || list.length == 0) return count;
			for (int i = 0; i < list.length; i++) {
				if (filter == null || filter.accept(list[i])) {
					query.addRow(1);
					count++;
					query.setAt(KeyConstants._name, count, list[i].getName());
				}
			}
		}
		return count;
	}

	private static int _fillQueryNamesRec(String parent, Query query, Resource directory, ResourceFilter filter, int count, boolean recurse) throws PageException {
		Resource[] list = directory.listResources();
		if (list == null || list.length == 0) return count;
		for (int i = 0; i < list.length; i++) {
			if (filter == null || filter.accept(list[i])) {
				query.addRow(1);
				count++;
				query.setAt(KeyConstants._name, count, parent.concat(list[i].getName()));

			}
			if (recurse && list[i].isDirectory()) count = _fillQueryNamesRec(parent + list[i].getName() + "/", query, list[i], filter, count, recurse);
		}
		return count;
	}

	private static int _fillArrayPathOrName(Array arr, Resource directory, ResourceFilter filter, int count, boolean recurse, boolean onlyName) throws PageException {
		Resource[] list = directory.listResources();
		if (list == null || list.length == 0) return count;
		for (int i = 0; i < list.length; i++) {
			if (filter == null || filter.accept(list[i])) {
				arr.appendEL(onlyName ? list[i].getName() : list[i].getAbsolutePath());
				count++;

			}
			if (recurse && list[i].isDirectory()) count = _fillArrayPathOrName(arr, list[i], filter, count, recurse, onlyName);
		}
		return count;
	}

	// this method only exists for performance reason
	private static int _fillArrayName(Array arr, Resource directory, ResourceFilter filter, int count) {
		if (filter == null || filter instanceof ResourceNameFilter) {
			ResourceNameFilter rnf = filter == null ? null : (ResourceNameFilter) filter;
			String[] list = directory.list();
			if (list == null || list.length == 0) return count;
			for (int i = 0; i < list.length; i++) {
				if (rnf == null || rnf.accept(directory, list[i])) {
					arr.appendEL(list[i]);
				}
			}
		}
		else {
			Resource[] list = directory.listResources();
			if (list == null || list.length == 0) return count;
			for (int i = 0; i < list.length; i++) {
				if (filter.accept(list[i])) {
					arr.appendEL(list[i].getName());
				}
			}
		}

		return count;
	}

	/**
	 * create a directory
	 * 
	 * @throws PageException
	 */
	public static void actionCreate(PageContext pc, Resource directory, String serverPassword, boolean createPath, int mode, Object acl, String storage, int nameConflict)
			throws PageException {

		SecurityManager securityManager = pc.getConfig().getSecurityManager();
		securityManager.checkFileLocation(pc.getConfig(), directory, serverPassword);

		if (directory.exists()) {
			if (directory.isDirectory()) {
				if (nameConflict == NAMECONFLICT_SKIP) return;

				throw new ApplicationException("directory [" + directory.toString() + "] already exists");
			}
			else if (directory.isFile()) throw new ApplicationException("can't create directory [" + directory.toString() + "], a file exists with the same name");
		}
		// if(!directory.mkdirs()) throw new ApplicationException("can't create directory
		// ["+directory.toString()+"]");
		List<Resource> files = new ArrayList<>();
		try {
			Resource parent = directory.getParentResource();
			while (!parent.exists()) {
				files.add(parent);
				parent = parent.getParentResource();
			}
			directory.createDirectory(createPath);
		}
		catch (IOException ioe) {
			throw Caster.toPageException(ioe);
		}

		// set S3 stuff
		setS3Attrs(pc, directory, acl, storage);

		// Set Mode
		if (mode != -1 && createPath) {
			try {
				for(Resource file : files) {
					file.setMode(mode);
				}
				//directory.setMode(mode);
				// FileUtil.setMode(directory,mode);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}
	}

	public static void setS3Attrs(PageContext pc, Resource res, Object acl, String storage) throws PageException {
		String scheme = res.getResourceProvider().getScheme();

		if ("s3".equalsIgnoreCase(scheme)) {
			// ACL
			if (acl != null) {
				try {
					// old way
					if (Decision.isString(acl)) {
						Reflector.callMethod(res, "setACL", new Object[] { improveACL(Caster.toString(acl)) });
					}
					// new way
					else {
						BIF bif = CFMLEngineFactory.getInstance().getClassUtil().loadBIF(pc, "StoreSetACL");
						bif.invoke(pc, new Object[] { res.getAbsolutePath(), acl });
					}
				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
			}
			// STORAGE
			if (storage != null) {
				Reflector.callMethod(res, "setStorage", new Object[] { storage });
			}
		}
	}

	public static String improveACL(String acl) throws ApplicationException {
		acl = acl.toLowerCase().trim();
		if ("public-read".equals(acl)) return "public-read";
		if ("publicread".equals(acl)) return "public-read";
		if ("public_read".equals(acl)) return "public-read";

		if ("public-read-write".equals(acl)) return "public-read-write";
		if ("publicreadwrite".equals(acl)) return "public-read-write";
		if ("public_read_write".equals(acl)) return "public-read-write";

		if ("private".equals(acl)) return "private";

		if ("authenticated-read".equals(acl)) return "authenticated-read";
		if ("authenticated_read".equals(acl)) return "authenticated-read";
		if ("authenticatedread".equals(acl)) return "authenticated-read";

		throw new ApplicationException("invalid acl value, valid values are [public-read, private, public-read-write, authenticated-read]");
	}

	/**
	 * delete directory
	 * 
	 * @param dir
	 * @param forceDelete
	 * @throws PageException
	 */
	public static void actionDelete(PageContext pc, Resource dir, boolean forceDelete, String serverPassword) throws PageException {
		SecurityManager securityManager = pc.getConfig().getSecurityManager();
		securityManager.checkFileLocation(pc.getConfig(), dir, serverPassword);

		// directory doesn't exist
		if (!dir.exists()) {
			if (dir.isDirectory()) throw new ApplicationException("directory [" + dir.toString() + "] doesn't exist");
			else if (dir.isFile()) throw new ApplicationException("file [" + dir.toString() + "] doesn't exist and isn't a directory");
		}

		// check if file
		if (dir.isFile()) throw new ApplicationException("can't delete [" + dir.toString() + "], it isn't a directory, it's a file");

		// check directory is empty
		Resource[] dirList = dir.listResources();
		if (dirList != null && dirList.length > 0 && forceDelete == false) throw new ApplicationException("directory [" + dir.toString() + "] is not empty","set recurse=true to delete sub-directories and files too");
		
		// delete directory
		try {
			dir.remove(forceDelete);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * rename a directory to a new Name
	 * 
	 * @throws PageException
	 */
	public static String actionRename(PageContext pc, Resource directory, String strNewdirectory, String serverPassword, boolean createPath, Object acl, String storage)
			throws PageException {
		// check directory
		SecurityManager securityManager = pc.getConfig().getSecurityManager();
		securityManager.checkFileLocation(pc.getConfig(), directory, serverPassword);

		if (!directory.exists()) throw new ApplicationException("the directory [" + directory.toString() + "] doesn't exist");
		if (!directory.isDirectory()) throw new ApplicationException("the file [" + directory.toString() + "] exists, but it isn't a directory");
		if (!directory.canRead()) throw new ApplicationException("no access to read directory [" + directory.toString() + "]");

		if (strNewdirectory == null) throw new ApplicationException("the attribute [newDirectory] is not defined");

		// real to source
		Resource newdirectory = toDestination(pc, strNewdirectory, directory);

		securityManager.checkFileLocation(pc.getConfig(), newdirectory, serverPassword);
		if (newdirectory.exists()) throw new ApplicationException("new directory [" + newdirectory.toString() + "] already exists");
		if (createPath) {
			newdirectory.getParentResource().mkdirs();

		}
		try {
			directory.moveTo(newdirectory);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}

		// set S3 stuff
		setS3Attrs(pc, directory, acl, storage);
		return newdirectory.toString();

	}

	public static void actionCopy(PageContext pc, Resource directory, String strDestination, String serverPassword, boolean createPath, Object acl, String storage,
			final ResourceFilter filter, boolean recurse, int nameconflict) throws PageException {
		// check directory
		SecurityManager securityManager = pc.getConfig().getSecurityManager();
		securityManager.checkFileLocation(pc.getConfig(), directory, serverPassword);

		if (!directory.exists()) throw new ApplicationException("directory [" + directory.toString() + "] doesn't exist");
		if (!directory.isDirectory()) throw new ApplicationException("file [" + directory.toString() + "] exists, but isn't a directory");
		if (!directory.canRead()) throw new ApplicationException("no access to read directory [" + directory.toString() + "]");

		if (StringUtil.isEmpty(strDestination)) throw new ApplicationException("attribute [destination] is not defined");

		// real to source
		Resource newdirectory = toDestination(pc, strDestination, directory);

		if (nameconflict == NAMECONFLICT_ERROR && newdirectory.exists()) throw new ApplicationException("new directory [" + newdirectory.toString() + "] already exists");

		securityManager.checkFileLocation(pc.getConfig(), newdirectory, serverPassword);

		try {
			boolean clearEmpty = false;
			// has already a filter
			ResourceFilter f = null;
			if (filter != null) {
				if (!recurse) {
					f = new AndResourceFilter(new ResourceFilter[] { filter, new NotResourceFilter(DirectoryResourceFilter.FILTER) });
				}
				else {
					clearEmpty = true;
					f = new OrResourceFilter(new ResourceFilter[] { filter, DirectoryResourceFilter.FILTER });
				}
			}
			else {
				if (!recurse) f = new NotResourceFilter(DirectoryResourceFilter.FILTER);
			}
			if (!createPath) {
				Resource p = newdirectory.getParentResource();
				if (p != null && !p.exists()) throw new ApplicationException("parent directory for [" + newdirectory + "] doesn't exist");
			}
			ResourceUtil.copyRecursive(directory, newdirectory, f);
			if (clearEmpty) ResourceUtil.removeEmptyFolders(newdirectory, f == null ? null : new NotResourceFilter(filter));

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new ApplicationException(t.getMessage());
		}

		// set S3 stuff
		setS3Attrs(pc, directory, acl, storage);

	}

	private static Resource toDestination(PageContext pageContext, String path, Resource source) {
		if (source != null && path.indexOf(File.separatorChar) == -1 && path.indexOf('/') == -1 && path.indexOf('\\') == -1) {
			Resource p = source.getParentResource();
			if (p != null) return p.getRealResource(path);
		}
		return ResourceUtil.toResourceNotExisting(pageContext, path);
	}

	private static String getFileAttribute(Resource file, boolean exists) {
		return exists && !file.isWriteable() ? "R".concat(file.isHidden() ? "H" : "") : file.isHidden() ? "H" : "";
	}

	/**
	 * @param strType the type to set
	 */
	public void setType(String strType) throws ApplicationException {
		if (StringUtil.isEmpty(strType)) return;
		type = toType(strType);
	}

	public static int toType(String strType) throws ApplicationException {
		strType = strType.trim().toLowerCase();
		if ("all".equals(strType)) return TYPE_ALL;
		else if ("dir".equals(strType)) return TYPE_DIR;
		else if ("directory".equals(strType)) return TYPE_DIR;
		else if ("file".equals(strType)) return TYPE_FILE;
		else throw new ApplicationException("invalid type [" + strType + "], valid types are [all,directory,file]");
	}

}
