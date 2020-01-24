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
package lucee.commons.io.res.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lucee.commons.digest.Hash;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.ContentTypeImpl;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.filter.DirectoryResourceFilter;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.IgnoreSystemFiles;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.io.res.type.http.HTTPResource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.system.ExpandPath;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

public final class ResourceUtil {

	public static final int MIMETYPE_CHECK_EXTENSION = 1;
	public static final int MIMETYPE_CHECK_HEADER = 2;

	/**
	 * Field <code>FILE_SEPERATOR</code>
	 */
	public static final char FILE_SEPERATOR = File.separatorChar;
	/**
	 * Field <code>FILE_ANTI_SEPERATOR</code>
	 */
	public static final char FILE_ANTI_SEPERATOR = (FILE_SEPERATOR == '/') ? '\\' : '/';

	/**
	 * Field <code>TYPE_DIR</code>
	 */
	public static final short TYPE_DIR = 0;

	/**
	 * Field <code>TYPE_FILE</code>
	 */
	public static final short TYPE_FILE = 1;

	/**
	 * Field <code>LEVEL_FILE</code>
	 */
	public static final short LEVEL_FILE = 0;
	/**
	 * Field <code>LEVEL_PARENT_FILE</code>
	 */
	public static final short LEVEL_PARENT_FILE = 1;
	/**
	 * Field <code>LEVEL_GRAND_PARENT_FILE</code>
	 */
	public static final short LEVEL_GRAND_PARENT_FILE = 2;

	public static final HashMap<String, String> EXT_MT = new HashMap<String, String>();
	static {
		EXT_MT.put("ai", "application/postscript");
		EXT_MT.put("aif", "audio/x-aiff");
		EXT_MT.put("aifc", "audio/x-aiff");
		EXT_MT.put("aiff", "audio/x-aiff");
		EXT_MT.put("au", "audio/basic");
		EXT_MT.put("avi", "video/x-msvideo");
		EXT_MT.put("bin", "application/octet-stream");
		EXT_MT.put("bmp", "image/x-ms-bmp");
		EXT_MT.put("cgm", "image/cgm");
		EXT_MT.put("cmx", "image/x-cmx");
		EXT_MT.put("csh", "application/x-csh");
		EXT_MT.put("cfm", "text/html");
		EXT_MT.put("cfml", "text/html");
		EXT_MT.put("css", "text/css");
		EXT_MT.put("doc", "application/msword");
		EXT_MT.put("docx", "application/msword");
		EXT_MT.put("eps", "application/postscript");
		EXT_MT.put("exe", "application/octet-stream");
		EXT_MT.put("gif", "image/gif");
		EXT_MT.put("gtar", "application/x-gtar");
		EXT_MT.put("hqx", "application/mac-binhex40");
		EXT_MT.put("htm", "text/html");
		EXT_MT.put("html", "text/html");
		EXT_MT.put("jpe", "image/jpeg");
		EXT_MT.put("jpeg", "image/jpeg");
		EXT_MT.put("jpg", "image/jpeg");
		EXT_MT.put("js", "text/javascript");
		EXT_MT.put("mmid", "x-music/x-midi");
		EXT_MT.put("mov", "video/quicktime");
		EXT_MT.put("mp2a", "audio/x-mpeg-2");
		EXT_MT.put("mp2v", "video/mpeg-2");
		EXT_MT.put("mp3", "audio/mpeg");
		EXT_MT.put("mp4", "video/mp4");
		EXT_MT.put("mpa", "audio/x-mpeg");
		EXT_MT.put("mpa2", "audio/x-mpeg-2");
		EXT_MT.put("mpeg", "video/mpeg");
		EXT_MT.put("mpega", "audio/x-mpeg");
		EXT_MT.put("mpg", "video/mpeg");
		EXT_MT.put("mpv2", "video/mpeg-2");
		EXT_MT.put("pbm", "image/x-portable-bitmap");
		EXT_MT.put("pcd", "image/x-photo-cd");
		EXT_MT.put("pdf", "application/pdf");
		EXT_MT.put("pgm", "image/x-portable-graymap");
		EXT_MT.put("pict", "image/x-pict");
		EXT_MT.put("pl", "application/x-perl");
		EXT_MT.put("png", "image/png");
		EXT_MT.put("php", "text/html");
		EXT_MT.put("pnm", "image/x-portable-anymap");
		EXT_MT.put("ppm", "image/x-portable-pixmap");
		EXT_MT.put("ppt", "application/vnd.ms-powerpoint");
		EXT_MT.put("pptx", "application/vnd.ms-powerpoint");
		EXT_MT.put("ps", "application/postscript");
		EXT_MT.put("qt", "video/quicktime");
		EXT_MT.put("rgb", "image/rgb");
		EXT_MT.put("rtf", "application/rtf");
		EXT_MT.put("sh", "application/x-sh");
		EXT_MT.put("sit", "application/x-stuffit");
		EXT_MT.put("swf", "application/x-shockwave-flash");
		EXT_MT.put("tar", "application/x-tar");
		EXT_MT.put("tcl", "application/x-tcl");
		EXT_MT.put("tif", "image/tiff");
		EXT_MT.put("tiff", "image/tiff");
		EXT_MT.put("txt", "text/plain");
		EXT_MT.put("wav", "audio/x-wav");
		EXT_MT.put("wma", "audio/x-ms-wma");
		EXT_MT.put("wmv", "video/x-ms-wmv");
		EXT_MT.put("xbm", "image/x-xbitmap");
		EXT_MT.put("xhtml", "application/xhtml+xml");
		EXT_MT.put("xls", "application/vnd.ms-excel");
		EXT_MT.put("xlsx", "application/vnd.ms-excel");
		EXT_MT.put("xpm", "image/x-xpixmap");
		EXT_MT.put("zip", "application/zip");

		String[] te = Constants.getTemplateExtensions();
		for (int i = 0; i < te.length; i++) {
			EXT_MT.put(te[i], "text/html");
		}
	}
	// private static Magic mimeTypeParser;

	/**
	 * cast a String (argument destination) to a File Object, if destination is not an absolute, file
	 * object will be relative to current position (get from PageContext) file must exist otherwise
	 * throw exception
	 * 
	 * @param pc Page Context to the current position in filesystem
	 * @param path relative or absolute path for file object
	 * @return file object from destination
	 * @throws ExpressionException
	 */
	public static Resource toResourceExisting(PageContext pc, String path) throws ExpressionException {
		return toResourceExisting(pc, path, pc.getConfig().allowRealPath());
	}

	public static Resource toResourceExisting(PageContext pc, String path, boolean allowRealpath, Resource defaultValue) {
		try {
			return toResourceExisting(pc, path, allowRealpath);
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
			return defaultValue;
		}
	}

	public static Resource toResourceExisting(PageContext pc, String path, boolean allowRealpath) throws ExpressionException {
		path = path.replace('\\', '/');
		Resource res = pc.getConfig().getResource(path);

		if (res.exists()) return res;
		else if (!allowRealpath) throw new ExpressionException("file or directory [" + path + "] does not exist");

		if (res.isAbsolute() && res.exists()) {
			return res;
		}
		if (StringUtil.startsWith(path, '/')) {
			PageContextImpl pci = (PageContextImpl) pc;
			ConfigWebImpl cwi = (ConfigWebImpl) pc.getConfig();
			PageSource[] sources = cwi.getPageSources(pci, ExpandPath.mergeMappings(pc.getApplicationContext().getMappings(), pc.getApplicationContext().getComponentMappings()),
					path, false, pci.useSpecialMappings(), true, false);
			if (!ArrayUtil.isEmpty(sources)) {

				for (int i = 0; i < sources.length; i++) {
					if (sources[i].exists()) return sources[i].getResource();
				}
			}
		}
		res = getRealResource(pc, path, res);
		if (res.exists()) return res;
		throw new ExpressionException("file or directory [" + path + "] does not exist");
	}

	public static Resource toResourceExisting(Config config, String path) throws ExpressionException {
		path = path.replace('\\', '/');
		Resource res = config.getResource(path);

		if (res.exists()) return res;
		throw new ExpressionException("file or directory [" + path + "] does not exist");
	}

	public static Resource toResourceExisting(Config config, String path, Resource defaultValue) {
		path = path.replace('\\', '/');
		Resource res = config.getResource(path);

		if (res.exists()) return res;
		return defaultValue;
	}

	public static Resource toResourceNotExisting(Config config, String path) {
		Resource res;
		path = path.replace('\\', '/');
		res = config.getResource(path);
		return res;
	}

	/**
	 * cast a String (argument destination) to a File Object, if destination is not an absolute, file
	 * object will be relative to current position (get from PageContext) at least parent must exist
	 * 
	 * @param pc Page Context to the current position in filesystem
	 * @param destination relative or absolute path for file object
	 * @return file object from destination
	 * @throws ExpressionException
	 */

	public static Resource toResourceExistingParent(PageContext pc, String destination) throws ExpressionException {
		return toResourceExistingParent(pc, destination, pc.getConfig().allowRealPath());
	}

	public static Resource toResourceExistingParent(PageContext pc, String destination, boolean allowRealpath) throws ExpressionException {
		destination = destination.replace('\\', '/');
		Resource res = pc.getConfig().getResource(destination);

		// not allow realpath
		if (!allowRealpath) {
			if (res.exists() || parentExists(res)) return res;
			throw new ExpressionException("parent directory [" + res.getParent() + "]  for file [" + destination + "] doesn't exist");

		}

		// allow realpath
		if (res.isAbsolute() && (res.exists() || parentExists(res))) {
			return res;
		}

		if (StringUtil.startsWith(destination, '/')) {
			PageContextImpl pci = (PageContextImpl) pc;
			ConfigWebImpl cwi = (ConfigWebImpl) pc.getConfig();
			PageSource[] sources = cwi.getPageSources(pci, ExpandPath.mergeMappings(pc.getApplicationContext().getMappings(), pc.getApplicationContext().getComponentMappings()),
					destination, false, pci.useSpecialMappings(), true);
			if (!ArrayUtil.isEmpty(sources)) {
				for (int i = 0; i < sources.length; i++) {
					if (sources[i].exists() || parentExists(sources[i])) {
						res = sources[i].getResource();
						if (res != null) return res;
					}
				}
			}
		}
		res = getRealResource(pc, destination, res);
		if (res != null && (res.exists() || parentExists(res))) return res;

		throw new ExpressionException("parent directory [" + res.getParent() + "]  for file [" + destination + "] doesn't exist");

	}

	/**
	 * cast a String (argument destination) to a File Object, if destination is not an absolute, file
	 * object will be relative to current position (get from PageContext) existing file is preferred but
	 * dont must exist
	 * 
	 * @param pc Page Context to the current position in filesystem
	 * @param destination relative or absolute path for file object
	 * @return file object from destination
	 */

	public static Resource toResourceNotExisting(PageContext pc, String destination) {
		return toResourceNotExisting(pc, destination, pc.getConfig().allowRealPath(), false);
	}

	public static Resource toResourceNotExisting(PageContext pc, String destination, boolean allowRealpath, boolean checkComponentMappings) {
		destination = destination.replace('\\', '/');

		Resource res = pc.getConfig().getResource(destination);

		if (!allowRealpath || res.exists()) {
			return res;
		}

		boolean isUNC;
		if (!(isUNC = isUNCPath(destination)) && StringUtil.startsWith(destination, '/')) {
			PageContextImpl pci = (PageContextImpl) pc;
			ConfigWebImpl cwi = (ConfigWebImpl) pc.getConfig();
			PageSource[] sources = cwi.getPageSources(pci, ExpandPath.mergeMappings(pc.getApplicationContext().getMappings(), pc.getApplicationContext().getComponentMappings()),
					destination, false, pci.useSpecialMappings(), SystemUtil.isWindows(), checkComponentMappings);
			if (!ArrayUtil.isEmpty(sources)) {
				for (int i = 0; i < sources.length; i++) {
					res = sources[i].getResource();
					if (res != null) return res;
				}
			}
			// Resource res2 = pc.getPhysical(destination,SystemUtil.isWindows());
			// if(res2!=null) return res2;
		}
		if (isUNC) {
			res = pc.getConfig().getResource(destination.replace('/', '\\'));
		}
		else if (!destination.startsWith("..")) res = pc.getConfig().getResource(destination);
		if (res != null && res.isAbsolute()) return res;

		return getRealResource(pc, destination, res);
	}

	private static Resource getRealResource(PageContext pc, String destination, Resource defaultValue) {
		PageSource ps = pc.getCurrentPageSource();
		if (ps != null) {
			ps = ps.getRealPage(destination);

			if (ps != null) {
				Resource res = ps.getResource();
				if (res != null) return getCanonicalResourceEL(res);
			}

		}
		return defaultValue;
	}

	public static boolean isUNCPath(String path) {
		return SystemUtil.isWindows() && (path.startsWith("//") || path.startsWith("\\\\"));
	}

	/**
	 * translate the path of the file to an existing file path by changing case of letters Works only on
	 * Linux, because
	 * 
	 * Example Unix: we have an existing file with path "/usr/virtual/myFile.txt" now you call this
	 * method with path "/Usr/Virtual/myfile.txt" the result of the method will be
	 * "/usr/virtual/myFile.txt"
	 * 
	 * if there are more file with rhe same name but different cases Example: /usr/virtual/myFile.txt
	 * /usr/virtual/myfile.txt /Usr/Virtual/myFile.txt the nearest case wil returned
	 * 
	 * @param res
	 * @return file
	 */
	public static Resource toExactResource(Resource res) {
		res = getCanonicalResourceEL(res);
		if (res.getResourceProvider().isCaseSensitive()) {
			if (res.exists()) return res;
			return _check(res);

		}
		return res;
	}

	private static Resource _check(Resource file) {
		// todo cascade durch while ersetzten
		Resource parent = file.getParentResource();
		if (parent == null) return file;

		if (!parent.exists()) {
			Resource op = parent;
			parent = _check(parent);
			if (op == parent) return file;
			if ((file = parent.getRealResource(file.getName())).exists()) return file;
		}

		String[] files = parent.list();
		if (files == null) return file;
		String name = file.getName();
		for (int i = 0; i < files.length; i++) {
			if (name.equalsIgnoreCase(files[i])) return parent.getRealResource(files[i]);
		}
		return file;
	}

	/**
	 * create a file if possible, return file if ok, otherwise return null
	 * 
	 * @param res file to touch
	 * @param level touch also parent and grand parent
	 * @param type is file or directory
	 * @return file if exists, otherwise null
	 */
	public static Resource createResource(Resource res, short level, short type) {

		boolean asDir = type == TYPE_DIR;
		// File
		if (level >= LEVEL_FILE && res.exists() && ((res.isDirectory() && asDir) || (res.isFile() && !asDir))) {
			return getCanonicalResourceEL(res);
		}

		// Parent
		Resource parent = res.getParentResource();
		if (level >= LEVEL_PARENT_FILE && parent != null && parent.exists() && canRW(parent)) {
			if (asDir) {
				if (res.mkdirs()) return getCanonicalResourceEL(res);
			}
			else {
				if (createNewResourceEL(res)) return getCanonicalResourceEL(res);
			}
			return getCanonicalResourceEL(res);
		}

		// Grand Parent
		if (level >= LEVEL_GRAND_PARENT_FILE && parent != null) {
			Resource gparent = parent.getParentResource();
			if (gparent != null && gparent.exists() && canRW(gparent)) {
				if (asDir) {
					if (res.mkdirs()) return getCanonicalResourceEL(res);
				}
				else {
					if (parent.mkdirs() && createNewResourceEL(res)) return getCanonicalResourceEL(res);
				}
			}
		}
		return null;
	}

	public static void setAttribute(Resource res, String attributes) throws IOException {
		/*
		 * if(res instanceof File && SystemUtil.isWindows()) { if(attributes.length()>0) {
		 * attributes=ResourceUtil.translateAttribute(attributes);
		 * Runtime.getRuntime().exec("attrib "+attributes+" " + res.getAbsolutePath()); } } else {
		 */
		short[] flags = strAttrToBooleanFlags(attributes);

		if (flags[READ_ONLY] == YES) res.setWritable(false);
		else if (flags[READ_ONLY] == NO) res.setWritable(true);

		if (flags[HIDDEN] == YES) res.setAttribute(Resource.ATTRIBUTE_HIDDEN, true);// setHidden(true);
		else if (flags[HIDDEN] == NO) res.setAttribute(Resource.ATTRIBUTE_HIDDEN, false);// res.setHidden(false);

		if (flags[ARCHIVE] == YES) res.setAttribute(Resource.ATTRIBUTE_ARCHIVE, true);// res.setArchive(true);
		else if (flags[ARCHIVE] == NO) res.setAttribute(Resource.ATTRIBUTE_ARCHIVE, false);// res.setArchive(false);

		if (flags[SYSTEM] == YES) res.setAttribute(Resource.ATTRIBUTE_SYSTEM, true);// res.setSystem(true);
		else if (flags[SYSTEM] == NO) res.setAttribute(Resource.ATTRIBUTE_SYSTEM, false);// res.setSystem(false);

		// }
	}

	// private static final int NORMAL=0;
	private static final int READ_ONLY = 0;
	private static final int HIDDEN = 1;
	private static final int ARCHIVE = 2;
	private static final int SYSTEM = 3;

	// private static final int IGNORE=0;
	private static final int NO = 1;
	private static final int YES = 2;

	private static short[] strAttrToBooleanFlags(String attributes) throws IOException {

		String[] arr;
		try {
			arr = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(attributes.toLowerCase(), ','));
		}
		catch (PageException e) {
			arr = new String[0];
		}

		boolean hasNormal = false;
		boolean hasReadOnly = false;
		boolean hasHidden = false;
		boolean hasArchive = false;
		boolean hasSystem = false;

		for (int i = 0; i < arr.length; i++) {
			String str = arr[i].trim().toLowerCase();
			if (str.equals("readonly") || str.equals("read-only") || str.equals("+r")) hasReadOnly = true;
			else if (str.equals("normal") || str.equals("temporary")) hasNormal = true;
			else if (str.equals("hidden") || str.equals("+h")) hasHidden = true;
			else if (str.equals("system") || str.equals("+s")) hasSystem = true;
			else if (str.equals("archive") || str.equals("+a")) hasArchive = true;
			else throw new IOException("invalid attribute definition [" + str + "]");
		}

		short[] flags = new short[4];

		if (hasReadOnly) flags[READ_ONLY] = YES;
		else if (hasNormal) flags[READ_ONLY] = NO;

		if (hasHidden) flags[HIDDEN] = YES;
		else if (hasNormal) flags[HIDDEN] = NO;

		if (hasSystem) flags[SYSTEM] = YES;
		else if (hasNormal) flags[SYSTEM] = NO;

		if (hasArchive) flags[ARCHIVE] = YES;
		else if (hasNormal) flags[ARCHIVE] = NO;

		return flags;
	}

	/**
	 * sets attributes of a file on Windows system
	 * 
	 * @param res
	 * @param attributes
	 * @throws PageException
	 * @throws IOException
	 */
	public static String translateAttribute(String attributes) throws IOException {
		short[] flags = strAttrToBooleanFlags(attributes);

		StringBuilder sb = new StringBuilder();
		if (flags[READ_ONLY] == YES) sb.append(" +R");
		else if (flags[READ_ONLY] == NO) sb.append(" -R");

		if (flags[HIDDEN] == YES) sb.append(" +H");
		else if (flags[HIDDEN] == NO) sb.append(" -H");

		if (flags[SYSTEM] == YES) sb.append(" +S");
		else if (flags[SYSTEM] == NO) sb.append(" -S");

		if (flags[ARCHIVE] == YES) sb.append(" +A");
		else if (flags[ARCHIVE] == NO) sb.append(" -A");

		return sb.toString();
	}

	/*
	 * * translate a path in a proper form example susi\petere -> /susi/peter
	 * 
	 * @param path
	 * 
	 * @return path / public static String translatePath(String path) { /*path=prettifyPath(path);
	 * if(path.indexOf('/')!=0)path='/'+path; int index=path.lastIndexOf('/'); // remove slash at the
	 * end if(index==path.length()-1) path=path.substring(0,path.length()-1); return path;* / return
	 * translatePath(path, true, false); }
	 */

	/*
	 * * translate a path in a proper form example susi\petere -> susi/peter/
	 * 
	 * @param path
	 * 
	 * @return path / public static String translatePath2x(String path) { /*path=prettifyPath(path);
	 * if(path.indexOf('/')==0)path=path.substring(1); int index=path.lastIndexOf('/'); // remove slash
	 * at the end if(index!=path.length()-1) path=path+'/';* / return translatePath(path, false, true);
	 * }
	 */

	public static String translatePath(String path, boolean slashAdBegin, boolean slashAddEnd) {
		path = prettifyPath(path);

		// begin
		if (slashAdBegin) {
			if (path.indexOf('/') != 0) path = '/' + path;
		}
		else {
			if (path.indexOf('/') == 0) path = path.substring(1);
		}

		// end
		int index = path.lastIndexOf('/');
		if (slashAddEnd) {
			if (index != path.length() - 1) path = path + '/';
		}
		else {
			if (index == path.length() - 1 && index > -1) path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	/**
	 * translate a path in a proper form and cut name away example susi\petere -> /susi/ and peter
	 * 
	 * @param path
	 * @return
	 */
	public static String[] translatePathName(String path) {
		path = prettifyPath(path);
		if (path.indexOf('/') != 0) path = '/' + path;
		int index = path.lastIndexOf('/');
		// remove slash at the end
		if (index == path.length() - 1) path = path.substring(0, path.length() - 1);

		index = path.lastIndexOf('/');
		String name;
		if (index == -1) {
			name = path;
			path = "/";
		}
		else {
			name = path.substring(index + 1);
			path = path.substring(0, index + 1);
		}
		return new String[] { path, name };
	}

	public static String prettifyPath(String path) {
		if (path == null) return null;
		path = path.replace('\\', '/');
		return StringUtil.replace(path, "//", "/", false);
		// TODO /aaa/../bbb/
	}

	public static String removeScheme(String scheme, String path) {
		if (path.indexOf("://") == scheme.length() && StringUtil.startsWithIgnoreCase(path, scheme)) path = path.substring(3 + scheme.length());
		return path;
	}

	/**
	 * merge to path parts to one
	 * 
	 * @param parent
	 * @param child
	 * @return
	 */
	public static String merge(String parent, String child) {
		if (child.length() <= 2) {
			if (child.length() == 0) return parent;
			if (child.equals(".")) return parent;
			if (child.equals("..")) child = "../";
		}

		parent = translatePath(parent, true, false);
		child = prettifyPath(child);// child.replace('\\', '/');

		if (child.startsWith("./")) child = child.substring(2);
		if (StringUtil.startsWith(child, '/')) return parent.concat(child);
		if (!StringUtil.startsWith(child, '.')) return parent.concat("/").concat(child);

		while (child.startsWith("../")) {
			parent = pathRemoveLast(parent);
			child = child.substring(3);
		}
		if (StringUtil.startsWith(child, '/')) return parent.concat(child);
		return parent.concat("/").concat(child);
	}

	private static String pathRemoveLast(String path) {
		if (path.length() == 0) return "..";

		else if (path.endsWith("..")) {
			return path.concat("/..");
		}
		return path.substring(0, path.lastIndexOf('/'));
	}

	/**
	 * Returns the canonical form of this abstract pathname.
	 * 
	 * @param res file to get canonical form from it
	 *
	 * @return The canonical pathname string denoting the same file or directory as this abstract
	 *         pathname
	 *
	 * @throws SecurityException If a required system property value cannot be accessed.
	 */
	public static String getCanonicalPathEL(Resource res) {
		try {
			return res.getCanonicalPath();
		}
		catch (IOException e) {
			return res.toString();
		}
	}

	/**
	 * Returns the canonical form of this abstract pathname.
	 * 
	 * @param res file to get canonical form from it
	 *
	 * @return The canonical pathname string denoting the same file or directory as this abstract
	 *         pathname
	 *
	 * @throws SecurityException If a required system property value cannot be accessed.
	 */
	public static Resource getCanonicalResourceEL(Resource res) {
		if (res == null) return res;
		try {
			return res.getCanonicalResource();
		}
		catch (IOException e) {
			return res.getAbsoluteResource();
		}
	}

	/**
	 * creates a new File
	 * 
	 * @param res
	 * @return was successfull
	 */
	public static boolean createNewResourceEL(Resource res) {
		try {
			res.createFile(false);
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	public static boolean exists(Resource res) {
		return res != null && res.exists();
	}

	/**
	 * check if file is read and writable
	 * 
	 * @param res
	 * @return is or not
	 */
	public static boolean canRW(Resource res) {
		return res.isReadable() && res.isWriteable();
	}

	/**
	 * similar to linux bash function touch, create file if not exist otherwise change last modified
	 * date
	 * 
	 * @param res
	 * @throws IOException
	 */
	public static void touch(Resource res) throws IOException {
		if (res.exists()) {
			res.setLastModified(System.currentTimeMillis());
		}
		else {
			res.createFile(true);
		}
	}

	public static void touch(File res) throws IOException {
		if (res.exists()) {
			res.setLastModified(System.currentTimeMillis());
		}
		else {
			res.mkdirs();
			res.createNewFile();
		}
	}

	public static void clear(Resource res) throws IOException {
		if (res.exists()) {
			IOUtil.write(res, new byte[0]);
		}
		else {
			res.createFile(true);
		}
	}

	/**
	 * return the mime type of a file, dont check extension
	 * 
	 * @param res
	 * @param defaultValue
	 * @return mime type of the file
	 */
	public static String getMimeType(Resource res, String defaultValue) {
		return IOUtil.getMimeType(res, defaultValue);
	}

	public static String getMimeType(Resource res, String fileName, String defaultValue) {
		return IOUtil.getMimeType(res, fileName, defaultValue);
	}

	/**
	 * check if file is a child of given directory
	 * 
	 * @param file file to search
	 * @param dir directory to search
	 * @return is inside or not
	 */
	public static boolean isChildOf(Resource file, Resource dir) {
		while (file != null) {
			if (file.equals(dir)) return true;
			file = file.getParentResource();
		}
		return false;
	}

	/**
	 * return diffrents of one file to another if first is child of second otherwise return null
	 * 
	 * @param file file to search
	 * @param dir directory to search
	 */
	public static String getPathToChild(Resource file, Resource dir) {
		if (dir == null || !file.getResourceProvider().getScheme().equals(dir.getResourceProvider().getScheme())) return null;
		boolean isFile = file.isFile();
		String str = "/";
		while (file != null) {
			if (file.equals(dir)) {
				if (isFile) return str.substring(0, str.length() - 1);
				return str;
			}
			str = "/" + file.getName() + str;
			file = file.getParentResource();
		}
		return null;
	}

	/**
	 * get the Extension of a file
	 * 
	 * @param res
	 * @return extension of file
	 */
	public static String getExtension(Resource res, String defaultValue) {
		return getExtension(res.getName(), defaultValue);
	}

	/**
	 * get the Extension of a file
	 * 
	 * @param strFile
	 * @return extension of file
	 */
	public static String getExtension(String strFile, String defaultValue) {
		int pos = strFile.lastIndexOf('.');
		if (pos == -1) return defaultValue;
		return strFile.substring(pos + 1);
	}

	public static String getName(String strFileName) {
		int pos = strFileName.lastIndexOf('.');
		if (pos == -1) return strFileName;
		return strFileName.substring(0, pos);
	}

	/**
	 * split a FileName in Parts
	 * 
	 * @param fileName
	 * @return new String[]{name[,extension]}
	 */
	public static String[] splitFileName(String fileName) {
		int pos = fileName.lastIndexOf('.');
		if (pos == -1) {
			return new String[] { fileName };
		}
		return new String[] { fileName.substring(0, pos), fileName.substring(pos + 1) };
	}

	/**
	 * change extension of file and return new file
	 * 
	 * @param file
	 * @param newExtension
	 * @return file with new Extension
	 */
	public static Resource changeExtension(Resource file, String newExtension) {
		String ext = getExtension(file, null);
		if (ext == null) return file.getParentResource().getRealResource(file.getName() + '.' + newExtension);
		// new File(file.getParentFile(),file.getName()+'.'+newExtension);
		String name = file.getName();
		return file.getParentResource().getRealResource(name.substring(0, name.length() - ext.length()) + newExtension);
		// new File(file.getParentFile(),name.substring(0,name.length()-ext.length())+newExtension);
	}

	/**
	 * @param res delete the content of a directory
	 */

	public static void deleteContent(Resource src, ResourceFilter filter) {
		_deleteContent(src, filter, false);
	}

	public static void _deleteContent(Resource src, ResourceFilter filter, boolean deleteDirectories) {
		if (src.isDirectory()) {
			Resource[] files = filter == null ? src.listResources() : src.listResources(filter);
			for (int i = 0; i < files.length; i++) {
				_deleteContent(files[i], filter, true);
				if (deleteDirectories) {
					try {
						src.remove(false);
					}
					catch (IOException e) {}
				}
			}

		}
		else if (src.isFile()) {
			src.delete();
		}
	}

	/**
	 * copy a file or directory recursive (with his content)
	 * 
	 * @param res file or directory to delete
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void copyRecursive(Resource src, Resource trg) throws IOException {
		copyRecursive(src, trg, null);
	}

	/**
	 * copy a file or directory recursive (with his content)
	 * 
	 * @param src
	 * @param trg
	 * @param filter
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void copyRecursive(Resource src, Resource trg, ResourceFilter filter) throws IOException {
		// print.out(src);
		// print.out(trg);
		if (!src.exists()) return;
		if (src.isDirectory()) {
			if (!trg.exists()) trg.createDirectory(true);
			Resource[] files = filter == null ? src.listResources() : src.listResources(filter);
			if (files != null) for (int i = 0; i < files.length; i++) {
				copyRecursive(files[i], trg.getRealResource(files[i].getName()), filter);
			}
		}
		else if (src.isFile()) {
			touch(trg);
			IOUtil.copy(src, trg);
		}
	}

	public static void copy(Resource src, Resource trg) throws IOException {
		if (src.equals(trg)) return;
		ResourceUtil.checkCopyToOK(src, trg);
		IOUtil.copy(src, trg);
	}

	/**
	 * return if parent file exists
	 * 
	 * @param res file to check
	 * @return parent exists?
	 */
	private static boolean parentExists(Resource res) {
		res = res.getParentResource();
		return res != null && res.exists();
	}

	private static boolean parentExists(PageSource ps) {
		PageSource p = ((PageSourceImpl) ps).getParent();
		return p != null && p.exists();
	}

	public static void removeChildren(Resource res) throws IOException {
		removeChildren(res, (ResourceFilter) null);
	}

	public static void removeChildren(Resource res, ResourceNameFilter filter) throws IOException {
		Resource[] children = filter == null ? res.listResources() : res.listResources(filter);
		if (children == null) return;

		for (int i = 0; i < children.length; i++) {
			children[i].remove(true);
		}
	}

	public static void removeChildren(Resource res, ResourceFilter filter) throws IOException {
		Resource[] children = filter == null ? res.listResources() : res.listResources(filter);
		if (children == null) return;

		for (int i = 0; i < children.length; i++) {
			children[i].remove(true);
		}
	}

	public static void removeChildrenEL(Resource res, ResourceNameFilter filter) {
		try {
			removeChildren(res, filter);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public static void removeChildrenEL(Resource res, ResourceFilter filter) {
		try {
			removeChildren(res, filter);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public static void removeChildrenEL(Resource res) {
		try {
			removeChildren(res);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public static void removeEL(Resource res, boolean force) {
		try {
			res.remove(force);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public static void createFileEL(Resource res, boolean force) {
		try {
			res.createFile(force);
		}
		catch (IOException e) {}
	}

	public static void createDirectoryEL(Resource res, boolean force) {
		try {
			res.createDirectory(force);
		}
		catch (IOException e) {}
	}

	public static ContentType getContentType(Resource resource) {
		// TODO make this part of an interface
		if (resource instanceof HTTPResource) {
			try {
				return ((HTTPResource) resource).getContentType();
			}
			catch (IOException e) {}
		}
		InputStream is = null;
		try {
			is = resource.getInputStream();
			return new ContentTypeImpl(is);
		}
		catch (IOException e) {
			return ContentTypeImpl.APPLICATION_UNKNOW;
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	public static ContentType getContentType(Resource resource, ContentType defaultValue) {
		if (resource instanceof HTTPResource) {
			try {
				return ((HTTPResource) resource).getContentType();
			}
			catch (IOException e) {}
		}
		InputStream is = null;
		try {
			is = resource.getInputStream();
			return new ContentTypeImpl(is);
		}
		catch (IOException e) {
			return defaultValue;
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	public static void moveTo(Resource src, Resource dest, boolean useResourceMethod) throws IOException {
		ResourceUtil.checkMoveToOK(src, dest);

		if (src.isFile()) {
			try {
				if (useResourceMethod) src.moveTo(dest);
			}
			catch (IOException e) {
				useResourceMethod = false;
			}
			if (!useResourceMethod) {
				if (!dest.exists()) dest.createFile(false);
				IOUtil.copy(src, dest);
				src.remove(false);
			}
		}
		else {
			if (!dest.exists()) dest.createDirectory(false);
			Resource[] children = src.listResources();
			for (int i = 0; i < children.length; i++) {
				moveTo(children[i], dest.getRealResource(children[i].getName()), useResourceMethod);
			}
			src.remove(false);
		}
		dest.setLastModified(System.currentTimeMillis());
	}

	/**
	 * return the size of the Resource, other than method length of Resource this method return the size
	 * of all files in a directory
	 * 
	 * @param collectionDir
	 * @return
	 */
	public static long getRealSize(Resource res) {
		return getRealSize(res, null);
	}

	/**
	 * return the size of the Resource, other than method length of Resource this method return the size
	 * of all files in a directory
	 * 
	 * @param collectionDir
	 * @return
	 */
	public static long getRealSize(Resource res, ResourceFilter filter) {
		if (res.isFile()) {
			return res.length();
		}
		else if (res.isDirectory()) {
			long size = 0;
			Resource[] children = filter == null ? res.listResources() : res.listResources(filter);
			for (int i = 0; i < children.length; i++) {
				size += getRealSize(children[i]);
			}
			return size;
		}

		return 0;
	}

	public static int getChildCount(Resource res) {
		return getChildCount(res, null);
	}

	public static int getChildCount(Resource res, ResourceFilter filter) {
		if (res.isFile()) {
			return 1;
		}
		else if (res.isDirectory()) {
			int size = 0;
			Resource[] children = filter == null ? res.listResources() : res.listResources(filter);
			for (int i = 0; i < children.length; i++) {
				size += getChildCount(children[i]);
			}
			return size;
		}

		return 0;
	}

	/**
	 * return if Resource is empty, means is directory and has no children or an empty file, if not
	 * exist return false.
	 * 
	 * @param res
	 * @return
	 */
	public static boolean isEmpty(Resource res) {
		return isEmptyDirectory(res, null) || isEmptyFile(res);
	}

	/**
	 * return Boolean.True when directory is empty, Boolean.FALSE when directory s not empty and null if
	 * directory does not exists
	 * 
	 * @param res
	 * @return
	 */
	public static boolean isEmptyDirectory(Resource res, ResourceFilter filter) {
		if (res.isDirectory()) {
			Resource[] children = filter == null ? res.listResources() : res.listResources(filter);
			if (children == null || children.length == 0) return true;

			for (int i = 0; i < children.length; i++) {
				if (children[i].isFile()) return false;
				if (children[i].isDirectory() && !isEmptyDirectory(children[i], filter)) return false;
			}

		}
		return true;
	}

	public static boolean isEmptyFile(Resource res) {
		if (res.isFile()) {
			return res.length() == 0;
		}
		return false;
	}

	public static Resource toResource(File file) {
		return ResourcesImpl.getFileResourceProvider().getResource(file.getPath());
	}

	/**
	 * list children of all given resources
	 * 
	 * @param resources
	 * @return
	 */
	public static Resource[] listResources(Resource[] resources, ResourceFilter filter) {
		int count = 0;
		Resource[] children;
		ArrayList<Resource[]> list = new ArrayList<Resource[]>();
		for (int i = 0; i < resources.length; i++) {
			children = filter == null ? resources[i].listResources() : resources[i].listResources(filter);
			if (children != null) {
				count += children.length;
				list.add(children);
			}
			else list.add(new Resource[0]);
		}
		Resource[] rtn = new Resource[count];
		int index = 0;
		for (int i = 0; i < resources.length; i++) {
			children = list.get(i);
			for (int y = 0; y < children.length; y++) {
				rtn[index++] = children[y];
			}
		}
		// print.out(rtn);
		return rtn;
	}

	public static Resource[] listResources(Resource res, ResourceFilter filter) {
		return filter == null ? res.listResources() : res.listResources(filter);
	}

	public static void deleteFileOlderThan(Resource res, long date, ExtensionResourceFilter filter) {
		if (res.isFile()) {
			if (res.lastModified() <= date) res.delete();
		}
		else if (res.isDirectory()) {
			Resource[] children = filter == null ? res.listResources() : res.listResources(filter);
			for (int i = 0; i < children.length; i++) {
				deleteFileOlderThan(children[i], date, filter);
			}
		}
	}

	/**
	 * check if directory creation is ok with the rules for the Resource interface, to not change this
	 * rules.
	 * 
	 * @param resource
	 * @param createParentWhenNotExists
	 * @throws IOException
	 */
	public static void checkCreateDirectoryOK(Resource resource, boolean createParentWhenNotExists) throws IOException {
		if (resource.exists()) {
			if (resource.isFile()) throw new IOException("can't create directory [" + resource.getPath() + "], it already exists as a file");
			if (resource.isDirectory()) throw new IOException("can't create directory [" + resource.getPath() + "], directory already exists");
		}

		Resource parent = resource.getParentResource();
		// when there is a parent but the parent does not exist
		if (parent != null) {
			if (!parent.exists()) {
				if (createParentWhenNotExists) parent.createDirectory(true);
				else throw new IOException("can't create file [" + resource.getPath() + "], missing parent directory");
			}
			else if (parent.isFile()) {
				throw new IOException("can't create directory [" + resource.getPath() + "], parent is a file");
			}
		}
	}

	/**
	 * check if file creating is ok with the rules for the Resource interface, to not change this rules.
	 * 
	 * @param resource
	 * @param createParentWhenNotExists
	 * @throws IOException
	 */
	public static void checkCreateFileOK(Resource resource, boolean createParentWhenNotExists) throws IOException {
		if (resource.exists()) {
			if (resource.isDirectory()) throw new IOException("can't create file [" + resource.getPath() + "], it already exists as a directory");
			if (resource.isFile()) throw new IOException("can't create file [" + resource.getPath() + "], file already exists");
		}

		Resource parent = resource.getParentResource();
		// when there is a parent but the parent does not exist
		if (parent != null) {
			if (!parent.exists()) {
				if (createParentWhenNotExists) parent.createDirectory(true);
				else throw new IOException("can't create file [" + resource.getPath() + "], missing parent directory");
			}
			else if (parent.isFile()) {
				throw new IOException("can't create file [" + resource.getPath() + "], the specified parent directory is a file");
			}
		}
	}

	/**
	 * check if copying a file is ok with the rules for the Resource interface, to not change this
	 * rules.
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void checkCopyToOK(Resource source, Resource target) throws IOException {
		if (!source.isFile()) {
			if (source.isDirectory()) throw new IOException("can't copy [" + source.getPath() + "] to [" + target.getPath() + "], source is a directory");
			throw new IOException("can't copy [" + source.getPath() + "] to [" + target.getPath() + "], source file doesn't exist");
		}
		else if (target.isDirectory()) {
			throw new IOException("can't copy [" + source.getPath() + "] to [" + target.getPath() + "], target is a directory");
		}
	}

	/**
	 * check if moveing a file is ok with the rules for the Resource interface, to not change this
	 * rules.
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void checkMoveToOK(Resource source, Resource target) throws IOException {
		if (!source.exists()) {
			throw new IOException("can't move [" + source.getPath() + "] to [" + target.getPath() + "], source file doesn't exist");
		}
		if (source.isDirectory() && target.isFile()) throw new IOException("can't move [" + source.getPath() + "] directory to [" + target.getPath() + "], target is a file");
		if (source.isFile() && target.isDirectory()) throw new IOException("can't move [" + source.getPath() + "] file to [" + target.getPath() + "], target is a directory");
	}

	/**
	 * check if getting an inputstream of the file is ok with the rules for the Resource interface, to
	 * not change this rules.
	 * 
	 * @param resource
	 * @throws IOException
	 */
	public static void checkGetInputStreamOK(Resource resource) throws IOException {
		if (!resource.exists()) throw new IOException("file [" + resource.getPath() + "] does not exist");

		if (resource.isDirectory()) throw new IOException("can't read directory [" + resource.getPath() + "] as a file");

	}

	/**
	 * check if getting an outputstream of the file is ok with the rules for the Resource interface, to
	 * not change this rules.
	 * 
	 * @param resource
	 * @throws IOException
	 */
	public static void checkGetOutputStreamOK(Resource resource) throws IOException {
		if (resource.exists() && !resource.isWriteable()) {
			throw new IOException("can't write to file [" + resource.getPath() + "],file is readonly");
		}
		if (resource.isDirectory()) throw new IOException("can't write directory [" + resource.getPath() + "] as a file");
		if (!resource.getParentResource().exists())
			throw new IOException("can't write file [" + resource.getPath() + "] as a file, missing parent directory [" + resource.getParent() + "]");
	}

	/**
	 * check if removing the file is ok with the rules for the Resource interface, to not change this
	 * rules.
	 * 
	 * @param resource
	 * @throws IOException
	 */
	public static void checkRemoveOK(Resource resource) throws IOException {
		if (!resource.exists()) throw new IOException("can't delete resource [" + resource + "], resource does not exist");
		if (!resource.canWrite()) throw new IOException("can't delete resource [" + resource + "], no write access");

	}

	public static void deleteEmptyFolders(Resource res) throws IOException {
		if (res.isDirectory()) {
			Resource[] children = res.listResources();
			for (int i = 0; i < children.length; i++) {
				deleteEmptyFolders(children[i]);
			}
			if (res.listResources().length == 0) {
				res.remove(false);
			}
		}
	}

	/**
	 * if the pageSource is based on an archive, translate the source to a zip:// Resource
	 * 
	 * @return return the Resource matching this PageSource
	 * @param pc the Page Context Object
	 * @deprecated use instead <code>PageSource.getResourceTranslated(PageContext)</code>
	 */
	@Deprecated
	public static Resource getResource(PageContext pc, PageSource ps) throws PageException {
		return ps.getResourceTranslated(pc);
	}

	public static Resource getResource(PageContext pc, PageSource ps, Resource defaultValue) {
		try {
			return ps.getResourceTranslated(pc);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static int directrySize(Resource dir, ResourceFilter filter) {
		if (dir == null || !dir.isDirectory()) return 0;
		if (filter == null) return dir.list().length;
		return ArrayUtil.size(dir.list(filter));
	}

	public static int directrySize(Resource dir, ResourceNameFilter filter) {
		if (dir == null || !dir.isDirectory()) return 0;
		if (filter == null) return dir.list().length;
		return ArrayUtil.size(dir.list(filter));
	}

	public static String[] names(Resource[] resources) {
		String[] names = new String[resources.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = resources[i].getName();
		}
		return names;
	}

	public static Resource[] merge(Resource[] srcs, Resource... trgs) {
		java.util.List<Resource> list = new ArrayList<Resource>();

		if (srcs != null) {
			for (int i = 0; i < srcs.length; i++) {
				list.add(srcs[i]);
			}
		}
		if (trgs != null) {
			for (int i = 0; i < trgs.length; i++) {
				if (!list.contains(trgs[i])) list.add(trgs[i]);
			}
		}
		return list.toArray(new Resource[list.size()]);
	}

	public static void removeEmptyFolders(Resource dir, ResourceFilter filter) throws IOException {
		if (!dir.isDirectory()) return;

		Resource[] children = dir.listResources(IgnoreSystemFiles.INSTANCE);

		if (!ArrayUtil.isEmpty(children)) {
			boolean hasFiles = false;
			for (int i = 0; i < children.length; i++) {
				if (children[i].isDirectory()) removeEmptyFolders(children[i], filter);
				else if (children[i].isFile()) {
					hasFiles = true;
				}
			}
			if (!hasFiles) {
				children = dir.listResources(IgnoreSystemFiles.INSTANCE);
			}
		}
		if (ArrayUtil.isEmpty(children) && (filter == null || filter.accept(dir))) dir.remove(true);
	}

	public static List<Resource> listRecursive(Resource res, ResourceFilter filter) {
		List<Resource> list = new ArrayList<Resource>();
		listRecursive(list, res, filter);
		return list;
	}

	private static void listRecursive(List<Resource> list, Resource res, ResourceFilter filter) {
		if (res == null) return;
		if (filter == null || filter.accept(res)) list.add(res);

		if (!res.isDirectory()) return;
		Resource[] children = res.listResources(DirectoryResourceFilter.FILTER);
		if (children != null) for (int i = 0; i < children.length; i++) {
			listRecursive(children[i], filter);
		}
	}

	public static char getSeparator(ResourceProvider rp) {
		if (rp instanceof ResourceProviderPro) return ((ResourceProviderPro) rp).getSeparator();
		return '/';
	}

	public static String removeExtension(String filename, String defaultValue) {
		int index = filename.lastIndexOf('.');
		if (index == -1) return defaultValue;
		return filename.substring(0, index);
	}

	public static String checksum(Resource res) throws NoSuchAlgorithmException, IOException {
		return Hash.md5(res);
	}

}
