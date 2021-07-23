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

import static lucee.runtime.tag.util.FileUtil.NAMECONFLICT_ERROR;
import static lucee.runtime.tag.util.FileUtil.NAMECONFLICT_FORCEUNIQUE;
import static lucee.runtime.tag.util.FileUtil.NAMECONFLICT_MAKEUNIQUE;
import static lucee.runtime.tag.util.FileUtil.NAMECONFLICT_OVERWRITE;
import static lucee.runtime.tag.util.FileUtil.NAMECONFLICT_SKIP;
import static lucee.runtime.tag.util.FileUtil.NAMECONFLICT_UNDEFINED;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import lucee.commons.digest.Hash;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.ModeUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.NotResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ModeObjectWrap;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.MimeType;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.CacheHandlerCollectionImpl;
import lucee.runtime.cache.tag.CacheHandlerPro;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.file.FileCacheItem;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.functions.list.ListFirst;
import lucee.runtime.functions.list.ListLast;
import lucee.runtime.functions.other.CreateUUID;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.tag.util.FileUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateImpl;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.scope.Form;
import lucee.runtime.type.scope.FormItem;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

/**
 * Handles all interactions with files. The attributes you use with cffile depend on the value of
 * the action attribute. For example, if the action = "write", use the attributes associated with
 * writing a text file.
 *
 *
 *
 **/
public final class FileTag extends BodyTagImpl {

	private static final int ACTION_UNDEFINED = 0;
	private static final int ACTION_MOVE = 1;
	private static final int ACTION_WRITE = 2;
	private static final int ACTION_APPEND = 3;
	private static final int ACTION_READ = 4;
	private static final int ACTION_UPLOAD = 5;
	private static final int ACTION_UPLOAD_ALL = 6;
	private static final int ACTION_COPY = 7;
	private static final int ACTION_INFO = 8;
	private static final int ACTION_TOUCH = 9;
	private static final int ACTION_DELETE = 10;
	private static final int ACTION_READ_BINARY = 11;
	// private static final Key SET_ACL = KeyImpl.intern("setACL");
	private static final String DETAIL = "You can set a [allowedExtension] and a [blockedExtension] list as an argument/attribute with the tag [cffile] and the functions [fileUpload] and [fileUploadAll]. "
			+ "In addition you can configure this via the Application.cfc, [this.blockedExtForFileUpload] property, the [" + SystemUtil.SETTING_UPLOAD_EXT_BLOCKLIST
			+ "] System property or the [" + SystemUtil.convertSystemPropToEnvVar(SystemUtil.SETTING_UPLOAD_EXT_BLOCKLIST)
			+ "] Environment variable to allow this type of file to be uploaded.";

	// private static final String DEFAULT_ENCODING=Charset.getDefault();

	/** Type of file manipulation that the tag performs. */
	private int action;
	private static String actionValue;

	/** Absolute pathname of directory or file on web server. */
	private String strDestination;

	/** Content of the file to be created. */
	private Object output;

	/** Absolute pathname of file on web server. */
	private Resource file;

	/**
	 * Applies only to Solaris and HP-UX. Permissions. Octal values of UNIX chmod command. Assigned to
	 * owner, group, and other, respectively.
	 */
	private int mode = -1;

	/** Name of variable to contain contents of text file. */
	private String variable;

	/** Name of form field used to select the file. */
	private String filefield;

	/** Character set name for the file contents. */
	private CharSet charset = null;

	/** Yes: appends newline character to text written to file */
	private boolean addnewline = true;
	private boolean fixnewline = true;
	/**
	 * One attribute (Windows) or a comma-delimited list of attributes (other platforms) to set on the
	 * file. If omitted, the file's attributes are maintained.
	 */
	private String attributes;

	/**
	 * Absolute pathname of file on web server. On Windows, use backward slashes; on UNIX, use forward
	 * slashes.
	 */
	private Resource source;

	/** Action to take if filename is the same as that of a file in the directory. */
	private int nameconflict = NAMECONFLICT_UNDEFINED;

	/**
	 * Limits the MIME types to accept. Comma-delimited list. For example, to permit JPG and Microsoft
	 * Word file uploads: accept = "image/jpg, application/msword" The browser uses file extension to
	 * determine file type.
	 */
	private String accept;

	private boolean strict = true;
	private boolean createPath = false;

	private String result = null;

	private lucee.runtime.security.SecurityManager securityManager;

	private String serverPassword = null;
	private Object acl = null;

	private Object cachedWithin;
	private ResourceFilter allowedExtensions;
	private ResourceFilter blockedExtensions;

	@Override
	public void release() {
		super.release();
		acl = null;
		action = ACTION_UNDEFINED;
		actionValue = null;
		strDestination = null;
		output = null;
		file = null;
		mode = -1;
		variable = null;
		filefield = null;
		charset = null;
		addnewline = true;
		fixnewline = true;
		attributes = null;
		source = null;
		nameconflict = NAMECONFLICT_UNDEFINED;
		accept = null;
		allowedExtensions = null;
		blockedExtensions = null;
		strict = true;
		createPath = false;
		securityManager = null;
		result = null;
		serverPassword = null;
		cachedWithin = null;
	}

	public void setCachedwithin(Object cachedwithin) {
		if (StringUtil.isEmpty(cachedwithin)) return;
		this.cachedWithin = cachedwithin;
	}

	/**
	 * set the value action Type of file manipulation that the tag performs.
	 * 
	 * @param strAction value to set
	 **/
	public void setAction(String strAction) throws ApplicationException {
		actionValue = strAction;
		strAction = strAction.toLowerCase();
		if (strAction.equals("move") || strAction.equals("rename")) action = ACTION_MOVE;
		else if (strAction.equals("copy")) action = ACTION_COPY;
		else if (strAction.equals("delete")) action = ACTION_DELETE;
		else if (strAction.equals("read")) action = ACTION_READ;
		else if (strAction.equals("readbinary")) action = ACTION_READ_BINARY;
		else if (strAction.equals("write")) action = ACTION_WRITE;
		else if (strAction.equals("append")) action = ACTION_APPEND;
		else if (strAction.equals("upload")) action = ACTION_UPLOAD;
		else if (strAction.equals("uploadall")) action = ACTION_UPLOAD_ALL;
		else if (strAction.equals("info")) action = ACTION_INFO;
		else if (strAction.equals("touch")) action = ACTION_TOUCH;
		else throw new ApplicationException("Invalid value [" + strAction + "] for attribute action",
				"supported actions are: [info,move,rename,copy,delete,read,readbinary,write,append,upload,uploadall,touch]");
	}

	/**
	 * set the value destination Absolute pathname of directory or file on web server.
	 * 
	 * @param destination value to set
	 **/
	public void setDestination(String destination) {
		this.strDestination = destination;// ResourceUtil.toResourceNotExisting(pageContext ,destination);
	}

	/**
	 * set the value output Content of the file to be created.
	 * 
	 * @param output value to set
	 **/
	public void setOutput(Object output) {
		if (output == null) this.output = "";
		else this.output = output;
	}

	/**
	 * set the value file Absolute pathname of file on web server.
	 * 
	 * @param file value to set
	 **/
	public void setFile(String file) {
		this.file = ResourceUtil.toResourceNotExisting(pageContext, file);

	}

	/**
	 * set the value mode Applies only to Solaris and HP-UX. Permissions. Octal values of UNIX chmod
	 * command. Assigned to owner, group, and other, respectively.
	 * 
	 * @param mode value to set
	 * @throws PageException
	 **/
	public void setMode(String mode) throws PageException {
		this.mode = toMode(mode);
	}

	public static int toMode(String mode) throws PageException {
		if (StringUtil.isEmpty(mode, true)) return -1;
		try {
			return ModeUtil.toOctalMode(mode);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * set the value variable Name of variable to contain contents of text file.
	 * 
	 * @param variable value to set
	 **/
	public void setVariable(String variable) {
		this.variable = variable;
	}

	/**
	 * set the value filefield Name of form field used to select the file.
	 * 
	 * @param filefield value to set
	 **/
	public void setFilefield(String filefield) {
		this.filefield = filefield;
	}

	/**
	 * set the value charset Character set name for the file contents.
	 * 
	 * @param charset value to set
	 **/
	public void setCharset(String charset) {
		if (StringUtil.isEmpty(charset)) return;
		this.charset = CharsetUtil.toCharSet(charset.trim());
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

	public void setServerpassword(String serverPassword) {
		this.serverPassword = serverPassword;
	}

	/**
	 * set the value addnewline Yes: appends newline character to text written to file
	 * 
	 * @param addnewline value to set
	 **/
	public void setAddnewline(boolean addnewline) {
		this.addnewline = addnewline;
	}

	public void setAllowedextensions(Object oExtensions) throws PageException {
		if (StringUtil.isEmpty(oExtensions)) return;
		this.allowedExtensions = FileUtil.toExtensionFilter(oExtensions);
	}

	public void setBlockedextensions(Object oExtensions) throws PageException {
		if (StringUtil.isEmpty(oExtensions)) return;
		this.blockedExtensions = FileUtil.toExtensionFilter(oExtensions);
	}

	/**
	 * set the value attributes One attribute (Windows) or a comma-delimited list of attributes (other
	 * platforms) to set on the file. If omitted, the file's attributes are maintained.
	 * 
	 * @param attributes value to set
	 **/
	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	/**
	 * set the value source Absolute pathname of file on web server. On Windows, use backward slashes;
	 * on UNIX, use forward slashes.
	 * 
	 * @param source value to set
	 **/
	public void setSource(String source) {
		this.source = ResourceUtil.toResourceNotExisting(pageContext, source);
	}

	/**
	 * set the value nameconflict Action to take if filename is the same as that of a file in the
	 * directory.
	 * 
	 * @param nameconflict value to set
	 * @throws ApplicationException
	 **/
	public void setNameconflict(String nameconflict) throws ApplicationException {

		this.nameconflict = FileUtil.toNameConflict(nameconflict);
	}

	/**
	 * set the value accept Limits the MIME types to accept. Comma-delimited list. For example, to
	 * permit JPG and Microsoft Word file uploads: accept = "image/jpg, application/msword" The browser
	 * uses file extension to determine file type.
	 * 
	 * @param accept value to set
	 **/
	public void setAccept(String accept) {
		this.accept = accept;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	public void setCreatepath(boolean createPath) {
		this.createPath = createPath;
	}

	/**
	 * @param result The result to set.
	 */
	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public int doStartTag() throws PageException {

		if (charset == null) charset = CharsetUtil.toCharSet(((PageContextImpl) pageContext).getResourceCharset());

		securityManager = pageContext.getConfig().getSecurityManager();

		switch (action) {
		case ACTION_MOVE:
			actionMove(pageContext, securityManager, source, strDestination, nameconflict, serverPassword, acl, mode, attributes);
			break;
		case ACTION_COPY:
			actionCopy(pageContext, securityManager, source, strDestination, nameconflict, serverPassword, acl, mode, attributes);
			break;
		case ACTION_DELETE:
			actionDelete();
			break;
		case ACTION_READ:
			actionRead(false);
			break;
		case ACTION_READ_BINARY:
			actionRead(true);
			break;
		case ACTION_UPLOAD:
			actionUpload();
			break;
		case ACTION_UPLOAD_ALL:
			actionUploadAll();
			break;
		case ACTION_INFO:
			actionInfo();
			break;
		case ACTION_TOUCH:
			actionTouch(pageContext, securityManager, file, serverPassword, createPath, acl, mode, attributes);
			break;
		case ACTION_UNDEFINED:
			throw new ApplicationException("Missing attribute action"); // should never happens

		// write and append
		default:
			return EVAL_BODY_BUFFERED;
		}
		return SKIP_BODY;
	}

	@Override
	public int doAfterBody() throws ApplicationException {
		if (action == ACTION_APPEND || action == ACTION_WRITE) {
			String body = bodyContent.getString();
			if (!StringUtil.isEmpty(body)) {
				if (!StringUtil.isEmpty(output)) throw new ApplicationException("If a body is defined for the tag [file], the attribute [output] is not allowed");
				output = body;
			}
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws PageException {
		switch (action) {
		case ACTION_APPEND:
			actionAppend();
			break;
		case ACTION_WRITE:
			actionWrite();
			break;
		}

		return EVAL_PAGE;
	}

	public void hasBody(boolean hasBody) {
		if (output == null && hasBody) output = "";
	}

	/**
	 * move source file to destination path or file
	 * 
	 * @throws PageException
	 */
	public static void actionMove(PageContext pageContext, lucee.runtime.security.SecurityManager securityManager, Resource source, String strDestination, int nameconflict,
			String serverPassword, Object acl, int mode, String attributes) throws PageException {
		if (nameconflict == NAMECONFLICT_UNDEFINED) nameconflict = NAMECONFLICT_OVERWRITE;

		if (source == null) throw new ApplicationException("Attribute [source] is required for tag [file], when the action is [" + actionValue + "]","Required attributes for action [" + actionValue + "] is [source, destination]");
		if (StringUtil.isEmpty(strDestination)) throw new ApplicationException("Attribute [destination] is required for tag [file], when the action is [" + actionValue + "]","Required attributes for action [" + actionValue + "] is [source, destination]");

		Resource destination = toDestination(pageContext, strDestination, source);

		securityManager.checkFileLocation(pageContext.getConfig(), source, serverPassword);
		securityManager.checkFileLocation(pageContext.getConfig(), destination, serverPassword);
		if (source.equals(destination)) return;

		// source
		if (!source.exists()) throw new ApplicationException("Source file [" + source.toString() + "] doesn't exist");
		else if (!source.isFile()) throw new ApplicationException("Source file [" + source.toString() + "] isn't a file");
		// else if (!source.isReadable() || !source.isWriteable()) throw new ApplicationException("no access
		// to source file [" + source.toString() + "]");

		// destination
		if (destination.isDirectory()) destination = destination.getRealResource(source.getName());
		if (destination.exists()) {
			// SKIP
			if (nameconflict == NAMECONFLICT_SKIP) return;
			// OVERWRITE
			else if (nameconflict == NAMECONFLICT_OVERWRITE) destination.delete();
			// MAKEUNIQUE
			else if (nameconflict == NAMECONFLICT_MAKEUNIQUE) destination = makeUnique(destination);
			// FORCEUNIQUE
			else if (nameconflict == NAMECONFLICT_FORCEUNIQUE) destination = forceUnique(destination);
			// ERROR
			else throw new ApplicationException("Destination file [" + destination.toString() + "] already exists");
		}

		setACL(pageContext, destination, acl);
		try {
			source.moveTo(destination);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new ApplicationException(t.getMessage());
		}

		setMode(destination, mode);
		setAttributes(destination, attributes);
	}

	private static Resource toDestination(PageContext pageContext, String path, Resource source) {
		if (source != null && path.indexOf(File.separatorChar) == -1 && path.indexOf('/') == -1 && path.indexOf('\\') == -1) {
			Resource p = source.getParentResource();
			if (p != null) return p.getRealResource(path);
		}
		return ResourceUtil.toResourceNotExisting(pageContext, path);
	}

	/**
	 * copy source file to destination file or path
	 * 
	 * @throws PageException
	 */
	public static void actionCopy(PageContext pageContext, lucee.runtime.security.SecurityManager securityManager, Resource source, String strDestination, int nameconflict,
			String serverPassword, Object acl, int mode, String attributes) throws PageException {
		if (nameconflict == NAMECONFLICT_UNDEFINED) nameconflict = NAMECONFLICT_OVERWRITE;

		if (source == null) throw new ApplicationException("Attribute [source] is required for tag [file], when the action is [" + actionValue + "]","Required attributes for action [" + actionValue + "] is [source, destination]");
		if (StringUtil.isEmpty(strDestination)) throw new ApplicationException("Attribute [destination] is required for tag [file], when the action is [" + actionValue + "]","Required attributes for action [" + actionValue + "] is [source, destination]");

		Resource destination = toDestination(pageContext, strDestination, source);

		securityManager.checkFileLocation(pageContext.getConfig(), source, serverPassword);
		securityManager.checkFileLocation(pageContext.getConfig(), destination, serverPassword);

		// source
		if (!source.exists()) throw new ApplicationException("Source file [" + source.toString() + "] doesn't exist");
		else if (!source.isFile()) throw new ApplicationException("Source file [" + source.toString() + "] is not a file");
		else if (!source.canRead()) throw new ApplicationException("Access Denied to source file [" + source.toString() + "]");

		// destination
		if (destination.isDirectory()) destination = destination.getRealResource(source.getName());
		if (destination.exists()) {
			// SKIP
			if (nameconflict == NAMECONFLICT_SKIP) return;
			// SKIP
			else if (nameconflict == NAMECONFLICT_OVERWRITE) destination.delete();
			// MAKEUNIQUE
			else if (nameconflict == NAMECONFLICT_MAKEUNIQUE) destination = makeUnique(destination);
			// FORCEUNIQUE
			else if (nameconflict == NAMECONFLICT_FORCEUNIQUE) destination = forceUnique(destination);
			// ERROR
			else throw new ApplicationException("Destination file [" + destination.toString() + "] already exists");
		}

		setACL(pageContext, destination, acl);
		try {
			IOUtil.copy(source, destination);
		}
		catch (IOException e) {

			ApplicationException ae = new ApplicationException("Can't copy file [" + source + "] to [" + destination + "]", e.getMessage());
			ae.setStackTrace(e.getStackTrace());
			throw ae;
		}

		setMode(destination, mode);
		setAttributes(destination, attributes);
	}

	private static void setACL(PageContext pc, Resource res, Object acl) throws PageException {
		String scheme = res.getResourceProvider().getScheme();
		if ("s3".equalsIgnoreCase(scheme)) {
			Directory.setS3Attrs(pc, res, acl, null);
		}
	}

	private static Resource makeUnique(Resource res) {
		String name = ResourceUtil.getName(res);
		String ext = ResourceUtil.getExtension(res, "");
		if (!StringUtil.isEmpty(ext)) ext = "." + ext;
		while (res.exists()) {
			res = res.getParentResource().getRealResource(name + HashUtil.create64BitHashAsString(CreateUUID.invoke(), Character.MAX_RADIX) + ext);
		}

		return res;
	}

	private static Resource forceUnique(Resource res) {
		String name = ResourceUtil.getName(res);
		String ext = ResourceUtil.getExtension(res, "");
		if (!StringUtil.isEmpty(ext)) ext = "." + ext;
		while (res.exists()) {
			res = res.getParentResource().getRealResource(name + "_" + HashUtil.create64BitHashAsString(CreateUUID.invoke(), Character.MAX_RADIX) + ext);
		}
		return res;
	}

	/**
	 * copy source file to destination file or path
	 * 
	 * @throws PageException
	 */
	private void actionDelete() throws PageException {
		checkFile(pageContext, securityManager, file, serverPassword, false, false, false, false);
		setACL(pageContext, file, acl);
		try {
			if (!file.delete()) throw new ApplicationException("Can't delete file [" + file + "]");
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new ApplicationException(t.getMessage());
		}
	}

	/**
	 * read source file
	 * 
	 * @throws PageException
	 */
	private void actionRead(boolean isBinary) throws PageException {

		if (variable == null) throw new ApplicationException("Attribute [variable] is required for tag [file], when the action is [" + actionValue + "]","Required attributes for action [" + actionValue + "] is [file, variable]");
		if (file == null) throw new ApplicationException("Attribute [file] is required for tag [file], when the action is [" + actionValue + "]","Required attributes for action [" + actionValue + "] is [file, variable]");

		// check if we can use cache
		if (StringUtil.isEmpty(cachedWithin)) {
			Object tmp = ((PageContextImpl) pageContext).getCachedWithin(ConfigWeb.CACHEDWITHIN_FILE);
			if (tmp != null) setCachedwithin(tmp);
		}

		String cacheId = createCacheId(isBinary);
		CacheHandler cacheHandler = null;

		if (cachedWithin != null) {

			cacheHandler = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_FILE, null).getInstanceMatchingObject(cachedWithin, null);

			if (cacheHandler instanceof CacheHandlerPro) {

				CacheItem cacheItem = ((CacheHandlerPro) cacheHandler).get(pageContext, cacheId, cachedWithin);

				if (cacheItem instanceof FileCacheItem) {
					pageContext.setVariable(variable, ((FileCacheItem) cacheItem).getData());
					return;
				}
			}
			else if (cacheHandler != null) { // TODO this else block can be removed when all cache handlers implement CacheHandlerPro

				CacheItem cacheItem = cacheHandler.get(pageContext, cacheId);

				if (cacheItem instanceof FileCacheItem) {
					pageContext.setVariable(variable, ((FileCacheItem) cacheItem).getData());
					return;
				}
			}
		}

		// cache not found, process and cache result if needed
		checkFile(pageContext, securityManager, file, serverPassword, false, false, true, false);

		try {
			long start = System.nanoTime();
			Object data = isBinary ? IOUtil.toBytes(file) : IOUtil.toString(file, CharsetUtil.toCharset(charset));
			pageContext.setVariable(variable, data);

			if (cacheHandler != null) cacheHandler.set(pageContext, cacheId, cachedWithin, FileCacheItem.getInstance(file.getAbsolutePath(), data, System.nanoTime() - start));

		}
		catch (IOException e) {
			throw new ApplicationException("Can't read file [" + file.toString() + "]", e.getMessage());
		}
	}

	private String createCacheId(boolean binary) {
		return CacheHandlerCollectionImpl.createId(file, binary);
	}

	/**
	 * write to the source file
	 * 
	 * @throws PageException
	 */
	private void actionWrite() throws PageException {
		if (output == null) throw new ApplicationException("Attribute [output] is required for tag [file], when the action is [" + actionValue + "]","Action [" + actionValue + "] requires a tag body or attribute [output]");
		boolean created = checkFile(pageContext, securityManager, file, serverPassword, createPath, true, false, true);
		if (file.exists() && !created) {
			// Error
			if (nameconflict == NAMECONFLICT_ERROR) throw new ApplicationException("Destination file [" + file + "] already exists");
			// SKIP
			else if (nameconflict == NAMECONFLICT_SKIP) return;
			// OVERWRITE
			else if (nameconflict == NAMECONFLICT_OVERWRITE) file.delete();
			// MAKEUNIQUE
			else if (nameconflict == NAMECONFLICT_MAKEUNIQUE) file = makeUnique(file);
		}

		setACL(pageContext, file, acl);
		try {
			if (output instanceof InputStream) {
				IOUtil.copy((InputStream) output, file, false);
			}
			else if (Decision.isCastableToBinary(output, false)) {
				IOUtil.copy(new ByteArrayInputStream(Caster.toBinary(output)), file, true);
			}
			else {
				String content = Caster.toString(output);
				if (fixnewline) content = doFixNewLine(content);
				if (addnewline) content += SystemUtil.getOSSpecificLineSeparator();

				IOUtil.write(file, content, CharsetUtil.toCharset(charset), false);

			}
		}
		catch (UnsupportedEncodingException e) {
			throw new ApplicationException("Unsupported Charset Definition [" + charset + "]", e.getMessage());
		}
		catch (IOException e) {

			throw new ApplicationException("Can't write file [" + file.getAbsolutePath() + "]", e.getMessage());
		}
		setMode(file, mode);
		setAttributes(file, attributes);
	}

	/**
	 * write to the source file
	 * 
	 * @param attributes
	 * @param mode
	 * @param acl
	 * @param serverPassword, booleancreatePath
	 * @throws PageException
	 */
	public static void actionTouch(PageContext pageContext, SecurityManager securityManager, Resource file, String serverPassword, boolean createPath, Object acl, int mode,
			String attributes) throws PageException {
		checkFile(pageContext, securityManager, file, serverPassword, createPath, true, true, true);

		setACL(pageContext, file, acl);
		try {
			ResourceUtil.touch(file);
		}
		catch (IOException e) {

			throw new ApplicationException("Failed to touch file [" + file.getAbsolutePath() + "]", e.getMessage());
		}
		setMode(file, mode);
		setAttributes(file, attributes);
	}

	/**
	 * append data to source file
	 * 
	 * @throws PageException
	 */
	private void actionAppend() throws PageException {
		if (output == null) throw new ApplicationException("Attribute [output] is required for tag [file], when the action is [" + actionValue + "]","Action [" + actionValue + "] requires a tag body or attribute [output]");
		checkFile(pageContext, securityManager, file, serverPassword, createPath, true, false, true);

		setACL(pageContext, file, acl);
		try {
			if (!file.exists()) file.createNewFile();
			String content = Caster.toString(output);
			if (fixnewline) content = doFixNewLine(content);
			if (addnewline) content += SystemUtil.getOSSpecificLineSeparator();
			IOUtil.write(file, content, CharsetUtil.toCharset(charset), true);

		}
		catch (UnsupportedEncodingException e) {
			throw new ApplicationException("Unsupported Charset Definition [" + charset + "]", e.getMessage());
		}
		catch (IOException e) {
			throw new ApplicationException("Can't append file", e.getMessage());
		}
		setMode(file, mode);
		setAttributes(file, attributes);
	}

	private String doFixNewLine(String content) {
		// TODO replace new line with system new line
		return content;
	}

	/**
	 * list all files and directories inside a directory
	 * 
	 * @throws PageException
	 */
	private void actionInfo() throws PageException {
		if (variable == null) throw new ApplicationException("Attribute [variable] is required for tag [file], when the action is [" + actionValue + "]","Required attribute for action [" + actionValue + "] is [file, variable]");
		pageContext.setVariable(variable, getInfo(pageContext, file, serverPassword));

	}

	public static Struct getInfo(PageContext pc, Resource file, String serverPassword) throws PageException {
		SecurityManager sm = pc.getConfig().getSecurityManager();
		checkFile(pc, sm, file, serverPassword, false, false, false, false);

		File files = new File(Caster.toString(file));
		BasicFileAttributes attr;
		Struct sct = new StructImpl();

		// fill data to query
		sct.setEL(KeyConstants._name, file.getName());
		sct.setEL(KeyConstants._size, Long.valueOf(file.length()));
		sct.setEL(KeyConstants._type, file.isDirectory() ? "Dir" : "File");
		if (file instanceof File) sct.setEL("execute", ((File) file).canExecute());
		sct.setEL("read", file.canRead());
		sct.setEL("write", file.canWrite());
		try {
			attr = Files.readAttributes(files.toPath(), BasicFileAttributes.class);
			sct.set("fileCreated", new DateTimeImpl(pc, attr.creationTime().toMillis(), false));
		}
		catch (Exception e) {
		}
		sct.setEL("dateLastModified", new DateTimeImpl(pc, file.lastModified(), false));
		sct.setEL("attributes", getFileAttribute(file));
		if (SystemUtil.isUnix()) sct.setEL(KeyConstants._mode, new ModeObjectWrap(file));

		try {
			sct.setEL(KeyConstants._checksum, Hash.md5(file));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		/*
		 * try { BufferedImage bi = ImageUtil.toBufferedImage(file, null); if(bi!=null) { Struct img =new
		 * StructImpl(); img.setEL(KeyConstants._width,new Double(bi.getWidth()));
		 * img.setEL(KeyConstants._height,new Double(bi.getHeight())); sct.setEL(KeyConstants._img,img); } }
		 * catch(Exception e) {}
		 */
		return sct;
	}

	private static String getFileAttribute(Resource file) {
		return file.exists() && !file.canWrite() ? "R".concat(file.isHidden() ? "H" : "") : file.isHidden() ? "H" : "";
	}

	/**
	 * read source file
	 * 
	 * @throws PageException
	 */

	public void actionUpload() throws PageException {
		FormItem item = getFormItem(pageContext, filefield);
		Struct cffile = _actionUpload(pageContext, securityManager, item, strDestination, nameconflict, accept, allowedExtensions, blockedExtensions, strict, mode, attributes, acl,
				serverPassword);
		if (StringUtil.isEmpty(result)) {
			pageContext.undefinedScope().set(KeyConstants._file, cffile);
			pageContext.undefinedScope().set("cffile", cffile);
		}
		else {
			pageContext.setVariable(result, cffile);
		}
	}

	public static Struct actionUpload(PageContext pageContext, lucee.runtime.security.SecurityManager securityManager, String filefield, String strDestination, int nameconflict,
			String accept, ResourceFilter allowedExtensions, ResourceFilter blockedExtensions, boolean strict, int mode, String attributes, Object acl, String serverPassword)
			throws PageException {
		FormItem item = getFormItem(pageContext, filefield);
		return _actionUpload(pageContext, securityManager, item, strDestination, nameconflict, accept, allowedExtensions, blockedExtensions, strict, mode, attributes, acl,
				serverPassword);
	}

	public void actionUploadAll() throws PageException {
		Array arr = actionUploadAll(pageContext, securityManager, strDestination, nameconflict, accept, allowedExtensions, blockedExtensions, strict, mode, attributes, acl,
				serverPassword);
		if (StringUtil.isEmpty(result)) {
			Struct sct;
			if (arr != null && arr.size() > 0) sct = (Struct) arr.getE(1);
			else sct = new StructImpl();

			pageContext.undefinedScope().set(KeyConstants._file, sct);
			pageContext.undefinedScope().set("cffile", sct);
		}
		else {
			pageContext.setVariable(result, arr);
		}
	}

	public static Array actionUploadAll(PageContext pageContext, lucee.runtime.security.SecurityManager securityManager, String strDestination, int nameconflict, String accept,
			ResourceFilter allowedExtensions, ResourceFilter blockedExtensions, boolean strict, int mode, String attributes, Object acl, String serverPassword)
			throws PageException {
		FormItem[] items = getFormItems(pageContext);
		Struct sct = null;
		Array arr = new ArrayImpl();
		for (int i = 0; i < items.length; i++) {
			sct = _actionUpload(pageContext, securityManager, items[i], strDestination, nameconflict, accept, allowedExtensions, blockedExtensions, strict, mode, attributes, acl,
					serverPassword);
			arr.appendEL(sct);
		}
		return arr;
	}

	private static Struct _actionUpload(PageContext pageContext, lucee.runtime.security.SecurityManager securityManager, FormItem formItem, String strDestination, int nameconflict,
			String accept, ResourceFilter allowedExtensions, ResourceFilter blockedExtensions, boolean strict, int mode, String attributes, Object acl, String serverPassword)
			throws PageException {
		if (nameconflict == NAMECONFLICT_UNDEFINED) nameconflict = NAMECONFLICT_ERROR;

		boolean fileWasRenamed = false;
		boolean fileWasAppended = false;
		boolean fileExisted = false;
		boolean fileWasOverwritten = false;

		// set cffile struct
		Struct cffile = new StructImpl();

		long length = formItem.getResource().length();
		cffile.set("timecreated", new DateTimeImpl(pageContext.getConfig()));
		cffile.set("timelastmodified", new DateTimeImpl(pageContext.getConfig()));
		cffile.set("datelastaccessed", new DateImpl(pageContext));
		cffile.set("oldfilesize", Long.valueOf(length));
		cffile.set("filesize", Long.valueOf(length));

		// client file
		String strClientFile = formItem.getName();
		while (strClientFile.indexOf('\\') != -1)
			strClientFile = strClientFile.replace('\\', '/');
		Resource clientFile = pageContext.getConfig().getResource(strClientFile);
		String clientFileName = clientFile.getName();

		// content type
		String contentType = ResourceUtil.getMimeType(formItem.getResource(), clientFile.getName(), formItem.getContentType());
		cffile.set("contenttype", ListFirst.call(pageContext, contentType, "/", false, 1));
		cffile.set("contentsubtype", ListLast.call(pageContext, contentType, "/", false, 1));

		// check file type
		checkContentType(contentType, accept, allowedExtensions, blockedExtensions, clientFile, strict, pageContext.getApplicationContext());

		cffile.set("clientdirectory", getParent(clientFile));
		cffile.set("clientfile", clientFile.getName());
		cffile.set("clientfileext", ResourceUtil.getExtension(clientFile, ""));
		cffile.set("clientfilename", ResourceUtil.getName(clientFile));

		// check destination
		if (StringUtil.isEmpty(strDestination)) throw new ApplicationException("Attribute [destination] is required for tag [file], when action is [" + actionValue + "]");

		Resource destination = toDestination(pageContext, strDestination, null);
		securityManager.checkFileLocation(pageContext.getConfig(), destination, serverPassword);

		if (destination.isDirectory()) destination = destination.getRealResource(clientFileName);
		else if (!destination.exists() && (strDestination.endsWith("/") || strDestination.endsWith("\\"))) destination = destination.getRealResource(clientFileName);
		else if (!clientFileName.equalsIgnoreCase(destination.getName())) {
			if (ResourceUtil.getExtension(destination, null) == null) destination = destination.getRealResource(clientFileName);
			else fileWasRenamed = true;
		}

		// check parent destination -> directory of the desinatrion
		Resource parentDestination = destination.getParentResource();

		if (!parentDestination.exists()) {
			Resource pp = parentDestination.getParentResource();
			if (pp == null || !pp.exists())
				throw new ApplicationException("Attribute [destination] has an invalid value [" + destination + "], directory [" + parentDestination + "] doesn't exist");
			try {
				parentDestination.createDirectory(true);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}
		else if (!parentDestination.canWrite()) throw new ApplicationException("can't write to destination directory [" + parentDestination + "], no access to write");

		// set server variables
		cffile.set("serverdirectory", getParent(destination));
		cffile.set("serverfile", destination.getName());
		cffile.set("serverfileext", ResourceUtil.getExtension(destination, null));
		cffile.set("serverfilename", ResourceUtil.getName(destination));
		cffile.set("attemptedserverfile", destination.getName());

		// check nameconflict
		if (destination.exists()) {
			fileExisted = true;
			if (nameconflict == NAMECONFLICT_ERROR) {
				throw new ApplicationException("Destination file [" + destination + "] already exists");
			}
			else if (nameconflict == NAMECONFLICT_SKIP) {
				cffile.set("fileexisted", Caster.toBoolean(fileExisted));
				cffile.set("filewasappended", Boolean.FALSE);
				cffile.set("filewasoverwritten", Boolean.FALSE);
				cffile.set("filewasrenamed", Boolean.FALSE);
				cffile.set("filewassaved", Boolean.FALSE);
				return cffile;
			}
			else if (nameconflict == NAMECONFLICT_MAKEUNIQUE) {
				destination = makeUnique(destination);
				fileWasRenamed = true;

				// if(fileWasRenamed) {
				cffile.set("serverdirectory", getParent(destination));
				cffile.set("serverfile", destination.getName());
				cffile.set("serverfileext", ResourceUtil.getExtension(destination, ""));
				cffile.set("serverfilename", ResourceUtil.getName(destination));
				cffile.set("attemptedserverfile", destination.getName());
				// }
			}
			else if (nameconflict == NAMECONFLICT_FORCEUNIQUE) {
				destination = forceUnique(destination);
				fileWasRenamed = true;

				cffile.set("serverdirectory", getParent(destination));
				cffile.set("serverfile", destination.getName());
				cffile.set("serverfileext", ResourceUtil.getExtension(destination, ""));
				cffile.set("serverfilename", ResourceUtil.getName(destination));
				cffile.set("attemptedserverfile", destination.getName());
			}
			else if (nameconflict == NAMECONFLICT_OVERWRITE) {
				// fileWasAppended=true;
				fileWasOverwritten = true;
				if (!destination.delete()) if (destination.exists()) // hier hatte ich concurrent problem das damit ausgeraeumt ist
					throw new ApplicationException("Can't delete destination file [" + destination + "]");
			}
			// for "overwrite" no action is neded

		}

		setACL(pageContext, destination, acl);
		try {
			destination.createNewFile();
			IOUtil.copy(formItem.getResource(), destination);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}

		// Set cffile/file struct

		cffile.set("fileexisted", Caster.toBoolean(fileExisted));
		cffile.set("filewasappended", Caster.toBoolean(fileWasAppended));
		cffile.set("filewasoverwritten", Caster.toBoolean(fileWasOverwritten));
		cffile.set("filewasrenamed", Caster.toBoolean(fileWasRenamed));
		cffile.set("filewassaved", Boolean.TRUE);

		setMode(destination, mode);
		setAttributes(destination, attributes);
		return cffile;
	}

	/**
	 * check if the content type is permitted
	 * 
	 * @param contentType
	 * @throws PageException
	 */
	private static void checkContentType(String contentType, String accept, ResourceFilter allowedExtensions, ResourceFilter blockedExtensions, Resource clientFile, boolean strict,
			ApplicationContext appContext) throws PageException {
		String ext = ResourceUtil.getExtension(clientFile, "");

		// check extension
		if (!StringUtil.isEmpty(ext, true)) {
			boolean extensionAccepted = false;
			ext = FileUtil.toExtensions(ext);

			// allowed
			if (allowedExtensions != null) {
				if (!allowedExtensions.accept(clientFile)) throw new ApplicationException(
						"Upload of files with extension [" + ext
								+ "] is not permitted. The tag cffile/function fileUpload[All] only allows the following extensions in this context [" + allowedExtensions + "].",
						DETAIL);
				else extensionAccepted = true;
			}

			// blocked (when explicitly allowed we not have to check if blocked)
			if (!extensionAccepted) {
				if (blockedExtensions != null) {
					if (blockedExtensions.accept(clientFile)) {
						throw new ApplicationException("Upload of files with extension [" + ext
								+ "] is not permitted. The tag cffile/function fileUpload[All] does not allow the following extensions in this context [" + blockedExtensions
								+ "].", DETAIL);
					}
					else extensionAccepted = Boolean.TRUE;
				}
				else {
					String blocklistedTypes = ((ApplicationContextSupport) appContext).getBlockedExtForFileUpload();
					if (StringUtil.isEmpty(blocklistedTypes))
						blocklistedTypes = SystemUtil.getSystemPropOrEnvVar(SystemUtil.SETTING_UPLOAD_EXT_BLACKLIST, SystemUtil.DEFAULT_UPLOAD_EXT_BLOCKLIST);
					if (StringUtil.isEmpty(blocklistedTypes))
						blocklistedTypes = SystemUtil.getSystemPropOrEnvVar(SystemUtil.SETTING_UPLOAD_EXT_BLOCKLIST, SystemUtil.DEFAULT_UPLOAD_EXT_BLOCKLIST);
					NotResourceFilter filter = new NotResourceFilter(new ExtensionResourceFilter(blocklistedTypes));
					if (!filter.accept(clientFile)) throw new ApplicationException("Upload of files with extension [" + ext + "] is not permitted.", DETAIL);
				}
			}
		}
		else ext = null;

		// mimetype
		if (StringUtil.isEmpty(accept, true)) return;
		MimeType mt = MimeType.getInstance(contentType), sub;
		Array whishedTypes = ListUtil.listToArrayRemoveEmpty(accept, ',');
		int len = whishedTypes.size();
		for (int i = 1; i <= len; i++) {
			String whishedType = Caster.toString(whishedTypes.getE(i)).trim().toLowerCase();
			if (whishedType.equals("*")) return;
			// check mimetype
			if (ListUtil.len(whishedType, "/", true) == 2) {
				sub = MimeType.getInstance(whishedType);
				if (mt.match(sub)) return;
			}

			// check extension
			if (ext != null && !strict) {
				if (whishedType.startsWith("*.")) whishedType = whishedType.substring(2);
				if (whishedType.startsWith(".")) whishedType = whishedType.substring(1);
				if (ext.equals(whishedType)) return;
			}
		}
		if (strict && ListUtil.listContainsNoCase(StringUtil.emptyIfNull(accept), "." + ext, ",", false, false) != -1)
			throw new ApplicationException("When the value of the attribute STRICT is TRUE, only MIME types are allowed in the attribute(s): ACCEPT.",
					" set [" + accept + "] to MIME type.");
		else throw new ApplicationException("The MIME type of the uploaded file [" + contentType + "] was rejected by the server.",
				" Only the following type(s) are allowed, [" + StringUtil.emptyIfNull(accept) + "].  Verify that you are uploading a file of the appropriate type. ");
	}

	/**
	 * rreturn fileItem matching to filefiled definition or throw an exception
	 * 
	 * @return FileItem
	 * @throws ApplicationException
	 */
	private static FormItem getFormItem(PageContext pageContext, String filefield) throws PageException {
		// check filefield
		if (StringUtil.isEmpty(filefield)) {
			FormItem[] items = getFormItems(pageContext);
			if (ArrayUtil.isEmpty(items)) throw new ApplicationException("No uploaded files in found in Form");
			return items[0];
		}

		PageException pe = pageContext.formScope().getInitException();
		if (pe != null) throw pe;
		lucee.runtime.type.scope.Form upload = pageContext.formScope();
		FormItem fileItem = upload.getUploadResource(filefield);
		if (fileItem == null) {
			FormItem[] items = upload.getFileItems();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < items.length; i++) {
				if (i != 0) sb.append(", ");
				sb.append(items[i].getFieldName());
			}
			String add = ".";
			if (sb.length() > 0) add = ", valid field names are [" + sb + "].";

			if (pageContext.formScope().get(filefield, null) == null) throw new ApplicationException("Form field [" + filefield + "] is not a file field" + add);
			throw new ApplicationException("Form field [" + filefield + "] doesn't exist or has no content" + add);
		}

		return fileItem;
	}

	private static FormItem[] getFormItems(PageContext pageContext) throws PageException {
		PageException pe = pageContext.formScope().getInitException();
		if (pe != null) throw pe;

		Form scope = pageContext.formScope();
		return scope.getFileItems();
	}

	private static String getParent(Resource res) {
		Resource parent = res.getParentResource();
		// print.out("res:"+res);
		// print.out("parent:"+parent);
		if (parent == null) return "";
		return ResourceUtil.getCanonicalPathEL(parent);
	}

	private static boolean checkFile(PageContext pc, SecurityManager sm, Resource file, String serverPassword, boolean createParent, boolean create, boolean canRead,
			boolean canWrite) throws PageException {
		boolean created = false;
		if (file == null) throw new ApplicationException("Attribute [file] is required for tag [file], when the action is [" + actionValue + "]");

		sm.checkFileLocation(pc.getConfig(), file, serverPassword);
		if (!file.exists()) {
			if (create) {
				Resource parent = file.getParentResource();
				if (parent != null && !parent.exists()) {
					if (createParent) parent.mkdirs();
					else throw new ApplicationException("Parent directory for [" + file + "] doesn't exist");
				}
				try {
					created = true;
					file.createFile(false);
				}
				catch (IOException e) {
					throw new ApplicationException("Invalid file [" + file + "]", e.getMessage());
				}
			}
			// else if (!file.isFile()) throw new ApplicationException("Source file [" + file.toString() + "] is not a file");
			else throw new ApplicationException("Source file [" + file.toString() + "] doesn't exist");
		}
		else if (!file.isFile()) throw new ApplicationException("Source file [" + file.toString() + "] is not a file");
		else if (canRead && !file.canRead()) throw new ApplicationException("Read access denied to source file [" + file.toString() + "]");
		else if (canWrite && !file.canWrite()) throw new ApplicationException("Write access denied to source file [" + file.toString() + "]");

		return created;
	}

	/**
	 * set attributes on file
	 * 
	 * @param file
	 * @throws PageException
	 */
	private static void setAttributes(Resource file, String attributes) throws PageException {
		if (!SystemUtil.isWindows() || StringUtil.isEmpty(attributes)) return;
		try {
			ResourceUtil.setAttribute(file, attributes);
		}
		catch (IOException e) {
			throw new ApplicationException("Can't change attributes of file [" + file + "]", e.getMessage());
		}
	}

	/**
	 * change mode of given file
	 * 
	 * @param file
	 * @throws ApplicationException
	 */
	private static void setMode(Resource file, int mode) throws ApplicationException {
		if (mode == -1 || SystemUtil.isWindows()) return;
		try {
			file.setMode(mode);
			// FileUtil.setMode(file,mode);
		}
		catch (IOException e) {
			throw new ApplicationException("Can't change mode of file [" + file + "]", e.getMessage());
		}
	}

	/**
	 * @param fixnewline the fixnewline to set
	 */
	public void setFixnewline(boolean fixnewline) {
		this.fixnewline = fixnewline;
	}
}
