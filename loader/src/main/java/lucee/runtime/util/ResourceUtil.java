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
package lucee.runtime.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.exp.PageException;

public interface ResourceUtil {

	/**
	 * Field <code>FILE_SEPERATOR</code>
	 */
	public final char FILE_SEPERATOR = File.separatorChar;
	/**
	 * Field <code>FILE_ANTI_SEPERATOR</code>
	 */
	public final char FILE_ANTI_SEPERATOR = (FILE_SEPERATOR == '/') ? '\\' : '/';

	/**
	 * Field <code>TYPE_DIR</code>
	 */
	public final short TYPE_DIR = 0;

	/**
	 * Field <code>TYPE_FILE</code>
	 */
	public final short TYPE_FILE = 1;

	/**
	 * Field <code>LEVEL_FILE</code>
	 */
	public final short LEVEL_FILE = 0;
	/**
	 * Field <code>LEVEL_PARENT_FILE</code>
	 */
	public final short LEVEL_PARENT_FILE = 1;
	/**
	 * Field <code>LEVEL_GRAND_PARENT_FILE</code>
	 */
	public final short LEVEL_GRAND_PARENT_FILE = 2;

	/**
	 * cast a String (argument destination) to a File Object, if destination is not an absolute, file
	 * object will be relative to current position (get from PageContext) file must exist otherwise
	 * throw exception
	 * 
	 * @param pc Page Context to the current position in filesystem
	 * @param path relative or absolute path for file object
	 * @return file object from destination
	 * @throws PageException Page Exception
	 */
	public Resource toResourceExisting(PageContext pc, String path) throws PageException;

	/**
	 * cast a String (argument destination) to a File Object, if destination is not an absolute, file
	 * object will be relative to current position (get from PageContext) at least parent must exist
	 * 
	 * @param pc Page Context to the current position in filesystem
	 * @param destination relative or absolute path for file object
	 * @return file object from destination
	 * @throws PageException Page Exception
	 */
	public Resource toResourceExistingParent(PageContext pc, String destination) throws PageException;

	/**
	 * cast a String (argument destination) to a File Object, if destination is not an absolute, file
	 * object will be relative to current position (get from PageContext) existing file is preferred but
	 * dont must exist
	 * 
	 * @param pc Page Context to the current position in filesystem
	 * @param destination relative or absolute path for file object
	 * @return file object from destination
	 */
	public Resource toResourceNotExisting(PageContext pc, String destination);

	/**
	 * create a file if possible, return file if ok, otherwise return null
	 * 
	 * @param res file to touch
	 * @param level touch also parent and grand parent
	 * @param type is file or directory
	 * @return file if exists, otherwise null
	 */
	public Resource createResource(Resource res, short level, short type);

	/**
	 * sets an attribute to the resource
	 * 
	 * @param res Resource
	 * @param attributes Attributes
	 * @throws IOException IO Exception
	 */
	public void setAttribute(Resource res, String attributes) throws IOException;

	/**
	 * return the mime type of a file, does not check the extension of the file, it checks the header
	 * 
	 * @param res Resource
	 * @param defaultValue default value
	 * @return mime type of the file
	 * @deprecated use instead <code>getContentType</code>
	 */
	@Deprecated
	public String getMimeType(Resource res, String defaultValue);

	/**
	 * return the mime type of a byte array
	 * 
	 * @param barr Byte Array
	 * @param defaultValue default value
	 * @return mime type of the file
	 * @deprecated use instead <code>getContentType</code>
	 */
	@Deprecated
	public String getMimeType(byte[] barr, String defaultValue);

	/**
	 * check if file is a child of given directory
	 * 
	 * @param file file to search
	 * @param dir directory to search
	 * @return is inside or not
	 */
	public boolean isChildOf(Resource file, Resource dir);

	/**
	 * return differnce of one file to another if first is child of second otherwise return null
	 * 
	 * @param file file to search
	 * @param dir directory to search
	 * @return path to child
	 */
	public String getPathToChild(Resource file, Resource dir);

	/**
	 * get the Extension of a file resource
	 * 
	 * @param res Resource
	 * @return extension of file
	 * @deprecated use instead <code>getExtension(Resource res, String defaultValue);</code>
	 */
	@Deprecated
	public String getExtension(Resource res);

	/**
	 * get the Extension of a file resource
	 * 
	 * @param res Resource
	 * @param defaultValue default value
	 * @return extension of file
	 */
	public String getExtension(Resource res, String defaultValue);

	/**
	 * get the Extension of a file
	 * 
	 * @param strFile path to file
	 * @return extension of file
	 * @deprecated use instead <code>getExtension(String strFile, String defaultValue);</code>
	 */
	@Deprecated
	public String getExtension(String strFile);

	/**
	 * get the Extension of a file resource
	 * 
	 * @param strFile Path to resource
	 * @param defaultValue default value
	 * @return extension of file
	 */
	public String getExtension(String strFile, String defaultValue);

	/**
	 * copy a file or directory recursive (with his content)
	 * 
	 * @param src Source Resource
	 * @param trg Target Resource	 
	 * @throws IOException IO Exception
	 */
	public void copyRecursive(Resource src, Resource trg) throws IOException;

	/**
	 * copy a file or directory recursive (with his content)
	 * 
	 * @param src Source Resource
	 * @param trg Target Resource
	 * @param filter filter Filter
	 * @throws IOException IO Exception
	 */
	public void copyRecursive(Resource src, Resource trg, ResourceFilter filter) throws IOException;

	public void removeChildren(Resource res) throws IOException;

	public void removeChildren(Resource res, ResourceNameFilter filter) throws IOException;

	public void removeChildren(Resource res, ResourceFilter filter) throws IOException;

	public void moveTo(Resource src, Resource dest) throws IOException;

	/**
	 * return if Resource is empty, means is directory and has no children or an empty file, if not exist
	 * return false.
	 * 
	 * @param res Resource
	 * @return if the resource is empty
	 */
	public boolean isEmpty(Resource res);

	public boolean isEmptyDirectory(Resource res);

	public boolean isEmptyFile(Resource res);

	public String translatePath(String path, boolean slashAdBegin, boolean slashAddEnd);

	public String[] translatePathName(String path);

	public String merge(String parent, String child);

	public String removeScheme(String scheme, String path);

	/**
	 * check if directory creation is ok with the rules for the Resource interface, to not change this
	 * rules.
	 * 
	 * @param resource Resource
	 * @param createParentWhenNotExists create parent when not exists
	 * @throws IOException IO Exception
	 */
	public void checkCreateDirectoryOK(Resource resource, boolean createParentWhenNotExists) throws IOException;

	/**
	 * check if file creating is ok with the rules for the Resource interface, to not change this rules.
	 * 
	 * @param resource Resource
	 * @param createParentWhenNotExists create parent when not exists
	 * @throws IOException IO Exception
	 */
	public void checkCreateFileOK(Resource resource, boolean createParentWhenNotExists) throws IOException;

	/**
	 * check if copying a file is ok with the rules for the Resource interface, to not change this
	 * rules.
	 * 
	 * @param source Source Resource
	 * @param target Target Resource
	 * @throws IOException IO Exception
	 */
	public void checkCopyToOK(Resource source, Resource target) throws IOException;

	/**
	 * check if moveing a file is ok with the rules for the Resource interface, to not change this
	 * rules.
	 * 
	 * @param source Source Resource
	 * @param target Target Resource
	 * @throws IOException IO Exception
	 */
	public void checkMoveToOK(Resource source, Resource target) throws IOException;

	/**
	 * check if getting an inputstream of the file is ok with the rules for the Resource interface, to
	 * not change this rules.
	 * 
	 * @param resource Resource
	 * @throws IOException IO Exception
	 */
	public void checkGetInputStreamOK(Resource resource) throws IOException;

	/**
	 * check if getting an outputstream of the file is ok with the rules for the Resource interface, to
	 * not change this rules.
	 * 
	 * @param resource Resource
	 * @throws IOException IO Exception
	 */
	public void checkGetOutputStreamOK(Resource resource) throws IOException;

	/**
	 * check if removing the file is ok with the rules for the Resource interface, to not change this
	 * rules.
	 * 
	 * @param resource Resource
	 * @throws IOException IO Exception
	 */
	public void checkRemoveOK(Resource resource) throws IOException;

	@Deprecated
	public String toString(Resource r, String charset) throws IOException;

	public String toString(Resource r, Charset charset) throws IOException;

	public String contractPath(PageContext pc, String path);

	public Resource getHomeDirectory();

	public Resource getSystemDirectory();

	public Resource getTempDirectory();

	public String parsePlaceHolder(String path);

	public ResourceFilter getExtensionResourceFilter(String extension, boolean allowDir);

	public ResourceFilter getExtensionResourceFilter(String extensions[], boolean allowDir);

	public ContentType getContentType(Resource file);

	/**
	 * cast a String (argument destination) to a File Object, if destination is not an absolute, file
	 * object will be relative to current position (get from PageContext) at least parent must exist
	 * 
	 * @param pc Page Context to the current position in filesystem
	 * @param destination relative or absolute path for file object
	 * @param allowRealpath allow real path
	 * @return file object from destination
	 * @throws PageException Page Exception
	 */
	public Resource toResourceExistingParent(PageContext pc, String destination, boolean allowRealpath) throws PageException;

	public Resource toResourceNotExisting(PageContext pc, String destination, boolean allowRealpath, boolean checkComponentMappings);

	public boolean isUNCPath(String path);

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
	 * @param res Resources
	 * @return file
	 */
	public Resource toExactResource(Resource res);

	public String prettifyPath(String path);

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
	public String getCanonicalPathSilent(Resource res);

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
	public Resource getCanonicalResourceSilent(Resource res);

	/**
	 * creates a new File
	 * 
	 * @param res Resource
	 * @return was successfull
	 */
	public boolean createNewResourceSilent(Resource res);

	/**
	 * similar to linux bash function touch, create file if not exist otherwise change last modified
	 * date
	 * 
	 * @param res Resource
	 * @throws IOException IO Exception
	 */
	public void touch(Resource res) throws IOException;

	public void clear(Resource res) throws IOException;

	/**
	 * change extension of file and return new file
	 * 
	 * @param file Resource
	 * @param newExtension New file extension
	 * @return file with new Extension
	 */
	public Resource changeExtension(Resource file, String newExtension);

	/**
	 * delete the content of a directory
	 * 
	 * @param src Resource
	 * @param filter Filter
	 */
	public void deleteContent(Resource src, ResourceFilter filter);

	public void copy(Resource src, Resource trg) throws IOException;

	public void removeChildrenSilent(Resource res, ResourceNameFilter filter);

	public void removeChildrenSilent(Resource res, ResourceFilter filter);

	public void removeChildrenSilent(Resource res);

	public void removeSilent(Resource res, boolean force);

	public void createFileSilent(Resource res, boolean force);

	public void createDirectorySilent(Resource res, boolean force);

	/**
	 * return the size of the Resource, other than method length of Resource this method return the size
	 * of all files in a directory
	 * 
	 * @param res Resource
	 * @param filter Filter
	 * @return the size of the directory
	 */
	public long getRealSize(Resource res, ResourceFilter filter);

	public int getChildCount(Resource res, ResourceFilter filter);

	/**
	 * return Boolean. True when directory is empty, Boolean. FALSE when directory is not empty and null if
	 * directory does not exist
	 * 
	 * @param res Resource
	 * @param filter Filter
	 * @return Returns if the Directory is empty.
	 */
	public boolean isEmptyDirectory(Resource res, ResourceFilter filter);

	public void deleteEmptyFolders(Resource res) throws IOException;

	public Resource getResource(PageContext pc, PageSource ps, Resource defaultValue);

	public int directrySize(Resource dir, ResourceFilter filter);

	public int directrySize(Resource dir, ResourceNameFilter filter);

	public String[] names(Resource[] resources);

	public Resource[] merge(Resource[] srcs, Resource... trgs);

	public void removeEmptyFolders(Resource dir) throws IOException;

	public List<Resource> listRecursive(Resource res, ResourceFilter filter);

	public char getSeparator(ResourceProvider rp);

	public ResourceProvider getFileResourceProvider();
}