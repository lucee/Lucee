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
package lucee.commons.io.res;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.filter.ResourceNameFilter;

/**
 * a Resource handle connection to different resources in an abstract form
 */
public interface Resource extends Serializable {

	public static final short ATTRIBUTE_HIDDEN = 1;
	public static final short ATTRIBUTE_SYSTEM = 2;
	public static final short ATTRIBUTE_ARCHIVE = 4;

	/**
	 * Tests whether the application can read the resource denoted by this abstract pathname.
	 * 
	 * @return <code>true</code> if and only if the resource specified by this abstract pathname exists
	 *         <em>and</em> can be read by the application; <code>false</code> otherwise
	 * 
	 */
	public abstract boolean isReadable();

	/**
	 * Tests whether the application can read the resource denoted by this abstract pathname.
	 * 
	 * @return <code>true</code> if and only if the resource specified by this abstract pathname exists
	 *         <em>and</em> can be read by the application; <code>false</code> otherwise
	 * @deprecated use instead <code>#isReadable()</code>
	 */
	@Deprecated
	public abstract boolean canRead();

	/**
	 * Tests whether the application can modify the resource denoted by this abstract pathname.
	 * 
	 * @return <code>true</code> if and only if the resource system actually contains a resource denoted
	 *         by this abstract pathname <em>and</em> the application is allowed to write to the
	 *         resource; <code>false</code> otherwise.
	 * 
	 */
	public abstract boolean isWriteable();

	/**
	 * Tests whether the application can modify the resource denoted by this abstract pathname.
	 * 
	 * @return <code>true</code> if and only if the resource system actually contains a resource denoted
	 *         by this abstract pathname <em>and</em> the application is allowed to write to the
	 *         resource; <code>false</code> otherwise.
	 * @deprecated use instead <code>#isWriteable()</code>
	 */
	@Deprecated
	public abstract boolean canWrite();

	/**
	 * Deletes the resource denoted by this abstract pathname. If this pathname denotes a directory,
	 * then the directory must be empty, when argument "force" is set to false, when argument "force" is
	 * set to true, also the children of the directory will be deleted.
	 * 
	 * @param force
	 * 
	 * @throws IOException if the file doesn't exists or can't delete
	 */
	public abstract void remove(boolean force) throws IOException;

	/**
	 * Deletes the resource denoted by this abstract pathname. If this pathname denotes a directory,
	 * then the directory must be empty, when argument "force" is set to false, when argument "force" is
	 * set to true, also the children oif the directory will be deleted.
	 * 
	 * if the file doesn't exists or can't delete
	 * 
	 * @deprecated replaced with method remove(boolean)
	 */
	@Deprecated
	public boolean delete();

	/**
	 * Tests whether the resource denoted by this abstract pathname exists.
	 * 
	 * @return <code>true</code> if and only if the resource denoted by this abstract pathname exists;
	 *         <code>false</code> otherwise
	 */
	public abstract boolean exists();

	/**
	 * Returns the absolute form of this abstract pathname.
	 * 
	 * @return The absolute abstract pathname denoting the same resource as this abstract pathname
	 */
	public abstract Resource getAbsoluteResource();

	/**
	 * Returns the absolute pathname string of this abstract pathname.
	 * 
	 * <p>
	 * If this abstract pathname is already absolute, then the pathname string is simply returned as if
	 * by the <code>{@link #getPath}</code> method.
	 * 
	 * @return The absolute pathname string denoting the same resource as this abstract pathname
	 * 
	 */
	public abstract String getAbsolutePath();

	/**
	 * Returns the canonical form of this abstract pathname.
	 * 
	 * @return The canonical pathname string denoting the same resource as this abstract pathname
	 * 
	 * @throws IOException If an I/O error occurs, which is possible because the construction of the
	 *             canonical pathname may require filesystem queries
	 * 
	 */
	public abstract Resource getCanonicalResource() throws IOException;

	/**
	 * Returns the canonical pathname string of this abstract pathname.
	 * 
	 * <p>
	 * A canonical pathname is both absolute and unique. The precise definition of canonical form is
	 * system-dependent. This method first converts this pathname to absolute form if necessary, as if
	 * by invoking the {@link #getAbsolutePath} method, and then maps it to its unique form in a
	 * system-dependent way.
	 * 
	 * <p>
	 * Every pathname that denotes an existing file or directory has a unique canonical form. Every
	 * pathname that denotes a nonexistent resource also has a unique canonical form. The canonical form
	 * of the pathname of a nonexistent file or directory may be different from the canonical form of
	 * the same pathname after the resource is created. Similarly, the canonical form of the pathname of
	 * an existing resource may be different from the canonical form of the same pathname after the
	 * resource is deleted.
	 * 
	 * @return The canonical pathname string denoting the same file or directory as this abstract
	 *         pathname
	 * 
	 * @throws IOException If an I/O error occurs, which is possible because the construction of the
	 *             canonical pathname may require filesystem queries
	 * 
	 */
	public abstract String getCanonicalPath() throws IOException;

	/**
	 * Returns the name of the resource denoted by this abstract pathname. This is just the last name in
	 * the pathname's name sequence. If the pathname's name sequence is empty, then the empty string is
	 * returned.
	 * 
	 * @return The name of the resource denoted by this abstract pathname, or the empty string if this
	 *         pathname's name sequence is empty
	 */
	public abstract String getName();

	/**
	 * Returns the pathname string of this abstract pathname's parent, or <code>null</code> if this
	 * pathname does not name a parent directory.
	 * 
	 * <p>
	 * The <em>parent</em> of an abstract pathname consists of the pathname's prefix, if any, and each
	 * name in the pathname's name sequence except for the last. If the name sequence is empty then the
	 * pathname does not name a parent directory.
	 * 
	 * @return The pathname string of the parent directory named by this abstract pathname, or
	 *         <code>null</code> if this pathname does not name a parent
	 */
	public abstract String getParent();

	/**
	 * Returns the abstract pathname of this abstract pathname's parent, or <code>null</code> if this
	 * pathname does not name a parent directory.
	 * 
	 * <p>
	 * The <em>parent</em> of an abstract pathname consists of the pathname's prefix, if any, and each
	 * name in the pathname's name sequence except for the last. If the name sequence is empty then the
	 * pathname does not name a parent directory.
	 * 
	 * @return The abstract pathname of the parent directory named by this abstract pathname, or
	 *         <code>null</code> if this pathname does not name a parent
	 * 
	 */
	public abstract Resource getParentResource();

	/**
	 * returns a resource path that is relative to the current resource
	 * 
	 * @param realpath relative path to get resource from
	 * @return relative resource path to the current
	 */
	public String getReal(String realpath);

	/**
	 * returns a resource that is relative to the current resource
	 * 
	 * @param relpath relative path to get resource from
	 * @return relative resource to the current
	 */
	public Resource getRealResource(String relpath);

	/**
	 * Converts this abstract pathname into a pathname string.
	 * 
	 * @return The string form of this abstract pathname
	 */
	public abstract String getPath();

	/**
	 * Tests whether this abstract pathname is absolute.
	 * 
	 * @return <code>true</code> if this abstract pathname is absolute, <code>false</code> otherwise
	 */
	public abstract boolean isAbsolute();

	/**
	 * Tests whether the resource denoted by this abstract pathname is a directory.
	 * 
	 * @return <code>true</code> if and only if the file denoted by this abstract pathname exists
	 *         <em>and</em> is a directory; <code>false</code> otherwise
	 */
	public abstract boolean isDirectory();

	/**
	 * Tests whether the file denoted by this abstract pathname is a normal file. A file is
	 * <em>normal</em> if it is not a directory and, in addition, satisfies other system-dependent
	 * criteria. Any non-directory file created by a Java application is guaranteed to be a normal file.
	 * 
	 * @return <code>true</code> if and only if the file denoted by this abstract pathname exists
	 *         <em>and</em> is a normal file; <code>false</code> otherwise
	 */
	public abstract boolean isFile();

	/**
	 * Tests whether the resource named by this abstract pathname is a hidden resource.
	 * 
	 * @return <code>true</code> if and only if the file denoted by this abstract pathname is hidden
	 * @deprecated use instead <code>{@link #getAttribute(short)}</code>
	 */
	@Deprecated
	public abstract boolean isHidden();

	/**
	 * Tests whether the resource named by this abstract pathname is an archive resource.
	 * 
	 * @return <code>true</code> if and only if the file denoted by this abstract pathname is an archive
	 * @deprecated use instead <code>{@link #getAttribute(short)}</code>
	 */
	@Deprecated
	public abstract boolean isArchive();

	/**
	 * Tests whether the resource named by this abstract pathname is a system resource.
	 * 
	 * @return <code>true</code> if and only if the file denoted by this abstract pathname is a system
	 *         resource
	 * @deprecated use instead <code>{@link #getAttribute(short)}</code>
	 */
	@Deprecated
	public abstract boolean isSystem();

	/**
	 * Returns the time that the resource denoted by this abstract pathname was last modified.
	 * 
	 * @return A <code>long</code> value representing the time the file was last modified, measured in
	 *         milliseconds since the epoch (00:00:00 GMT, January 1, 1970), or <code>0L</code> if the
	 *         file does not exist or if an I/O error occurs
	 * 
	 */
	public abstract long lastModified();

	/**
	 * Returns the length of the resource denoted by this abstract pathname. The return value is
	 * unspecified if this pathname denotes a directory.
	 * 
	 * @return The length, in bytes, of the resource denoted by this abstract pathname, or
	 *         <code>0L</code> if the resource does not exist
	 * 
	 */
	public abstract long length();

	/**
	 * Returns an array of strings naming the files and directories in the directory denoted by this
	 * abstract pathname.
	 * 
	 * <p>
	 * If this abstract pathname does not denote a directory, then this method returns
	 * <code>null</code>. Otherwise an array of strings is returned, one for each file or directory in
	 * the directory. Names denoting the directory itself and the directory's parent directory are not
	 * included in the result. Each string is a file name rather than a complete path.
	 * 
	 * <p>
	 * There is no guarantee that the name strings in the resulting array will appear in any specific
	 * order; they are not, in particular, guaranteed to appear in alphabetical order.
	 * 
	 * @return An array of strings naming the files and directories in the directory denoted by this
	 *         abstract pathname. The array will be empty if the directory is empty. Returns
	 *         <code>null</code> if this abstract pathname does not denote a directory, or if an I/O
	 *         error occurs.
	 */
	public abstract String[] list();

	/**
	 * Returns an array of strings naming the files and directories in the directory denoted by this
	 * abstract pathname that satisfy the specified filter. The behavior of this method is the same as
	 * that of the <code>{@link #list()}</code> method, except that the strings in the returned array
	 * must satisfy the filter. If the given <code>filter</code> is <code>null</code> then all names are
	 * accepted. Otherwise, a name satisfies the filter if and only if the value <code>true</code>
	 * results when the <code>{@link
	 * ResourceNameFilter#accept}</code> method of the filter is invoked on this abstract pathname and
	 * the name of a file or directory in the directory that it denotes.
	 * 
	 * @param filter A resourcename filter
	 * 
	 * @return An array of strings naming the files and directories in the directory denoted by this
	 *         abstract pathname that were accepted by the given <code>filter</code>. The array will be
	 *         empty if the directory is empty or if no names were accepted by the filter. Returns
	 *         <code>null</code> if this abstract pathname does not denote a directory, or if an I/O
	 *         error occurs.
	 * 
	 */
	public abstract String[] list(ResourceNameFilter filter);

	public abstract String[] list(ResourceFilter filter);

	/**
	 * Returns an array of abstract pathnames denoting the files in the directory denoted by this
	 * abstract pathname.
	 * 
	 * <p>
	 * If this abstract pathname does not denote a directory, then this method returns
	 * <code>null</code>. Otherwise an array of <code>File</code> objects is returned, one for each file
	 * or directory in the directory. Therefore if this pathname is absolute then each resulting
	 * pathname is absolute; if this pathname is relative then each resulting pathname will be relative
	 * to the same directory.
	 * 
	 * <p>
	 * There is no guarantee that the name strings in the resulting array will appear in any specific
	 * order; they are not, in particular, guaranteed to appear in alphabetical order.
	 * 
	 * @return An array of abstract pathnames denoting the files and directories in the directory
	 *         denoted by this abstract pathname. The array will be empty if the directory is empty.
	 *         Returns <code>null</code> if this abstract pathname does not denote a directory, or if an
	 *         I/O error occurs.
	 */
	public abstract Resource[] listResources();

	/**
	 * Returns an array of abstract pathnames denoting the files and directories in the directory
	 * denoted by this abstract pathname that satisfy the specified filter. The behavior of this method
	 * is the same as that of the <code>{@link #listResources()}</code> method, except that the
	 * pathnames in the returned array must satisfy the filter. If the given <code>filter</code> is
	 * <code>null</code> then all pathnames are accepted. Otherwise, a pathname satisfies the filter if
	 * and only if the value <code>true</code> results when the
	 * <code>{@link ResourceFilter#accept(Resource)}</code> method of the filter is invoked on the
	 * pathname.
	 * 
	 * @param filter A resource filter
	 * 
	 * @return An array of abstract pathnames denoting the files and directories in the directory
	 *         denoted by this abstract pathname. The array will be empty if the directory is empty.
	 *         Returns <code>null</code> if this abstract pathname does not denote a directory, or if an
	 *         I/O error occurs.
	 * 
	 */

	public abstract Resource[] listResources(ResourceFilter filter);

	/**
	 * Returns an array of abstract pathnames denoting the files and directories in the directory
	 * denoted by this abstract pathname that satisfy the specified filter. The behavior of this method
	 * is the same as that of the <code>{@link #listResources()}</code> method, except that the
	 * pathnames in the returned array must satisfy the filter. If the given <code>filter</code> is
	 * <code>null</code> then all pathnames are accepted. Otherwise, a pathname satisfies the filter if
	 * and only if the value <code>true</code> results when the
	 * <code>{@link ResourceNameFilter#accept}</code> method of the filter is invoked on this abstract
	 * pathname and the name of a file or directory in the directory that it denotes.
	 * 
	 * @param filter A resourcename filter
	 * 
	 * @return An array of abstract pathnames denoting the files and directories in the directory
	 *         denoted by this abstract pathname. The array will be empty if the directory is empty.
	 *         Returns <code>null</code> if this abstract pathname does not denote a directory, or if an
	 *         I/O error occurs.
	 * 
	 */
	public abstract Resource[] listResources(ResourceNameFilter filter);

	/**
	 * Move/renames the file denoted by this abstract pathname.
	 * 
	 * <p>
	 * Many aspects of the behavior of this method are inherently platform-dependent: The rename
	 * operation might not be able to move a file from one filesystem to another, it might not be
	 * atomic, and it might not succeed if a file with the destination abstract pathname already exists.
	 * 
	 * @param dest The new abstract pathname for the named file
	 * @return has successfull renamed or not
	 * 
	 * @deprecated use instead <code>#moveTo(Resource)</code>
	 */
	@Deprecated
	public boolean renameTo(Resource dest);

	/**
	 * Move/renames the file denoted by this abstract pathname.
	 * 
	 * <p>
	 * Many aspects of the behavior of this method are inherently platform-dependent: The rename
	 * operation might not be able to move a file from one filesystem to another, it might not be
	 * atomic, and it might not succeed if a file with the destination abstract pathname already exists.
	 * 
	 * @param dest The new abstract pathname for the named file
	 * @throws IOException thrown when operation not done successfully
	 * 
	 * 
	 */
	public abstract void moveTo(Resource dest) throws IOException;

	/**
	 * Sets the last-modified time of the file or directory named by this abstract pathname.
	 * 
	 * <p>
	 * All platforms support file-modification times to the nearest second, but some provide more
	 * precision. The argument will be truncated to fit the supported precision. If the operation
	 * succeeds and no intervening operations on the file take place, then the next invocation of the
	 * <code>{@link #lastModified}</code> method will return the (possibly truncated) <code>time</code>
	 * argument that was passed to this method.
	 * 
	 * @param time The new last-modified time, measured in milliseconds since the epoch (00:00:00 GMT,
	 *            January 1, 1970)
	 * 
	 * @return <code>true</code> if and only if the operation succeeded; <code>false</code> otherwise
	 * 
	 * 
	 */
	public abstract boolean setLastModified(long time);

	/**
	 * Marks the file or directory named by this abstract pathname so that only read operations are
	 * allowed. After invoking this method the file or directory is guaranteed not to change until it is
	 * either deleted or marked to allow write access. Whether or not a read-only file or directory may
	 * be deleted depends upon the underlying system.
	 * 
	 * @return <code>true</code> if and only if the operation succeeded; <code>false</code> otherwise
	 * @deprecated use instead <code>{@link #setWritable(boolean)}</code>
	 * 
	 */
	@Deprecated
	public boolean setReadOnly();

	// public void setWritable(boolean value) throws IOException;
	public boolean setWritable(boolean writable);

	// public void setReadable(boolean value) throws IOException;
	public boolean setReadable(boolean readable);

	/**
	 * Creates a new, empty file named by this abstract pathname if and only if a file with this name
	 * does not yet exist. The check for the existence of the file and the creation of the file if it
	 * does not exist are a single operation that is atomic with respect to all other filesystem
	 * activities that might affect the file.
	 * 
	 * @return <code>true</code> if the named file does not exist and was successfully created;
	 *         <code>false</code> if the named file already exists
	 * 
	 * @deprecated use instead <code>#createFile(boolean)</code>
	 */
	@Deprecated
	public boolean createNewFile();

	/**
	 * Creates a new, empty file named by this abstract pathname if and only if a file with this name
	 * does not yet exist. The check for the existence of the file and the creation of the file if it
	 * does not exist are a single operation that is atomic with respect to all other filesystem
	 * activities that might affect the file.
	 * 
	 * @param createParentWhenNotExists create parent when not exist
	 * 
	 * 
	 * @throws IOException If an I/O error occurred
	 */
	public void createFile(boolean createParentWhenNotExists) throws IOException;

	/**
	 * Creates the directory named by this abstract pathname.
	 * 
	 * @return <code>true</code> if and only if the directory was created; <code>false</code> otherwise
	 * @deprecated use <code>#createDirectory(boolean)</code>
	 */
	@Deprecated
	public boolean mkdir();

	/**
	 * Creates the directory named by this abstract pathname, including any necessary but nonexistent
	 * parent directories. Note that if this operation fails it may have succeeded in creating some of
	 * the necessary parent directories.
	 * 
	 * @return <code>true</code> if and only if the directory was created, along with all necessary
	 *         parent directories; <code>false</code> otherwise
	 * @deprecated use <code>#createDirectory(boolean)</code>
	 */
	@Deprecated
	public boolean mkdirs();

	/**
	 * Creates the directory named by this abstract pathname, including any necessary but nonexistent
	 * parent directories if flag "createParentWhenNotExists" is set to true. Note that if this
	 * operation fails it may have succeeded in creating some of the necessary parent directories.
	 * 
	 * @param createParentWhenNotExists throws Exception when can't create directory
	 */
	public void createDirectory(boolean createParentWhenNotExists) throws IOException;

	public InputStream getInputStream() throws IOException;

	public OutputStream getOutputStream() throws IOException;

	/**
	 * copy current resource data to given resource
	 * 
	 * @param res resource to copy to
	 * @param append do append value to existing data or overwrite
	 */
	public void copyTo(Resource res, boolean append) throws IOException;

	/**
	 * copy data of given resource to current
	 * 
	 * @param res resource to copy from
	 * @param append do append value to existing data or overwrite
	 */
	public void copyFrom(Resource res, boolean append) throws IOException;

	public OutputStream getOutputStream(boolean append) throws IOException;

	public abstract ResourceProvider getResourceProvider();

	public int getMode();

	public void setMode(int mode) throws IOException;

	/**
	 * sets hidden attribute of the resource
	 * 
	 * @param value value to set
	 * @throws IOException thrown when no access to change the value or the resource doesn't exist
	 * @deprecated use instead <code>{@link #setAttribute(short, boolean)}</code>
	 */
	@Deprecated
	public void setHidden(boolean value) throws IOException;

	/**
	 * sets system attribute of the resource
	 * 
	 * @param value value to set
	 * @throws IOException thrown when no access to change the value or the resource doesn't exist
	 * @deprecated use instead <code>{@link #setAttribute(short, boolean)}</code>
	 */
	@Deprecated
	public void setSystem(boolean value) throws IOException;

	/**
	 * sets archive attribute of the resource
	 * 
	 * @param value value to set
	 * @throws IOException thrown when no access to change the value or the resource doesn't exist
	 * @deprecated use instead <code>{@link #setAttribute(short, boolean)}</code>
	 */
	@Deprecated
	public void setArchive(boolean value) throws IOException;

	/**
	 * sets an attribute on the resource if supported otherwise it will ign
	 * 
	 * @param attribute which attribute (Resource.ATTRIBUTE_*)
	 * @param value value to set
	 * @throws IOException thrown when no access to change the value, when attributes are not supported
	 *             or the resource doesn't exist
	 */
	public void setAttribute(short attribute, boolean value) throws IOException;

	/**
	 * return value of a specific attribute
	 * 
	 * @param attribute attribute to get the value for
	 * @return value of the attribute
	 */
	public boolean getAttribute(short attribute);
}