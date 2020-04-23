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
package lucee.commons.io.res.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.filter.ResourceNameFilter;

public final class FileWrapper extends File implements Resource {

	private final Resource res;

	/**
	 * Constructor of the class
	 * 
	 * @param res
	 */
	private FileWrapper(Resource res) {
		super(res.getPath());
		this.res = res;
	}

	@Override
	public boolean canRead() {
		return res.canRead();
	}

	@Override
	public boolean canWrite() {
		return res.canWrite();
	}

	@Override
	public int compareTo(File pathname) {
		if (res instanceof File) ((File) res).compareTo(pathname);
		return res.getPath().compareTo(pathname.getPath());
	}

	@Override
	public boolean createNewFile() {
		return res.createNewFile();
	}

	@Override
	public boolean delete() {
		return res.delete();
	}

	@Override
	public void deleteOnExit() {
		if (res instanceof File) ((File) res).deleteOnExit();
	}

	@Override
	public boolean equals(Object obj) {
		return res.equals(obj);
	}

	@Override
	public boolean exists() {
		return res.exists();
	}

	@Override
	public File getAbsoluteFile() {
		if (res.isAbsolute()) return this;
		return new FileWrapper(res.getAbsoluteResource());
	}

	@Override
	public String getAbsolutePath() {
		return res.getAbsolutePath();
	}

	@Override
	public File getCanonicalFile() throws IOException {
		return new FileWrapper(res.getCanonicalResource());
	}

	@Override
	public String getCanonicalPath() throws IOException {
		return res.getCanonicalPath();
	}

	@Override
	public String getName() {
		return res.getName();
	}

	@Override
	public String getParent() {
		return res.getParent();
	}

	@Override
	public File getParentFile() {
		return new FileWrapper(this.getParentResource());
	}

	@Override
	public String getPath() {
		return res.getPath();
	}

	@Override
	public int hashCode() {
		return res.hashCode();
	}

	@Override
	public boolean isAbsolute() {
		return res.isAbsolute();
	}

	@Override
	public boolean isDirectory() {
		return res.isDirectory();
	}

	@Override
	public boolean isFile() {
		return res.isFile();
	}

	@Override
	public boolean isHidden() {
		return res.isHidden();
	}

	@Override
	public long lastModified() {
		return res.lastModified();
	}

	@Override
	public long length() {
		return res.length();
	}

	@Override
	public String[] list() {
		return res.list();
	}

	@Override
	public String[] list(FilenameFilter filter) {
		if (res instanceof File) ((File) res).list(filter);
		return list((ResourceNameFilter) new FileNameFilterWrapper(filter));
	}

	@Override
	public File[] listFiles() {
		// if(res instanceof File) return ((File)res).listFiles();
		return toFiles(listResources());
	}

	private File[] toFiles(Resource[] resources) {
		if (resources == null) return null;
		File[] files = new File[resources.length];
		for (int i = 0; i < resources.length; i++) {
			files[i] = new FileWrapper(resources[i]);
		}
		return files;
	}

	@Override
	public File[] listFiles(FileFilter filter) {
		// if(res instanceof File) return ((File)res).listFiles(filter);
		return toFiles(listResources(new FileFilterWrapper(filter)));
	}

	@Override
	public File[] listFiles(FilenameFilter filter) {
		// if(res instanceof File) return ((File)res).listFiles(filter);
		return toFiles(listResources(new FileNameFilterWrapper(filter)));
	}

	@Override
	public boolean mkdir() {
		return res.mkdir();
	}

	@Override
	public boolean mkdirs() {
		return res.mkdirs();
	}

	@Override
	public boolean renameTo(File dest) {
		try {
			if (res instanceof File) return ((File) res).renameTo(dest);
			if (dest instanceof Resource) return res.renameTo((Resource) dest);
			ResourceUtil.moveTo(this, ResourceUtil.toResource(dest), true);
			return true;
		}
		catch (IOException ioe) {
			return false;
		}
	}

	@Override
	public boolean setLastModified(long time) {
		return res.setLastModified(time);
	}

	@Override
	public boolean setReadOnly() {
		return res.setReadOnly();
	}

	@Override
	public String toString() {
		return res.toString();
	}

	@Override
	public URI toURI() {
		if (res instanceof File) return ((File) res).toURI();
		return null;
	}

	@Override
	public URL toURL() throws MalformedURLException {
		if (res instanceof File) return ((File) res).toURL();
		return null;
	}

	@Override
	public void createDirectory(boolean createParentWhenNotExists) throws IOException {
		res.createDirectory(createParentWhenNotExists);
	}

	@Override
	public void createFile(boolean createParentWhenNotExists) throws IOException {
		res.createFile(createParentWhenNotExists);
	}

	@Override
	public Resource getAbsoluteResource() {
		return res.getAbsoluteResource();
	}

	@Override
	public Resource getCanonicalResource() throws IOException {
		return res.getCanonicalResource();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return res.getInputStream();
	}

	@Override
	public int getMode() {
		return res.getMode();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return res.getOutputStream();
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws IOException {
		return res.getOutputStream(append);
	}

	@Override
	public Resource getParentResource() {
		return res.getParentResource();
	}

	@Override
	public String getReal(String realpath) {
		return res.getReal(realpath);
	}

	@Override
	public Resource getRealResource(String realpath) {
		return res.getRealResource(realpath);
	}

	@Override
	public ResourceProvider getResourceProvider() {
		return res.getResourceProvider();
	}

	@Override
	public boolean isArchive() {
		return res.isArchive();
	}

	@Override
	public boolean isReadable() {
		return res.isReadable();
	}

	@Override
	public boolean isSystem() {
		return res.isSystem();
	}

	@Override
	public boolean isWriteable() {
		return res.isWriteable();
	}

	@Override
	public String[] list(ResourceNameFilter filter) {
		return res.list(filter);
	}

	@Override
	public String[] list(ResourceFilter filter) {
		return res.list(filter);
	}

	@Override
	public Resource[] listResources() {
		return res.listResources();
	}

	@Override
	public Resource[] listResources(ResourceFilter filter) {
		return res.listResources(filter);
	}

	@Override
	public Resource[] listResources(ResourceNameFilter filter) {
		return res.listResources(filter);
	}

	@Override
	public void moveTo(Resource dest) throws IOException {
		res.moveTo(dest);
	}

	@Override
	public void remove(boolean force) throws IOException {
		res.remove(force);
	}

	@Override
	public boolean renameTo(Resource dest) {
		return res.renameTo(dest);
	}

	@Override
	public void setMode(int mode) throws IOException {
		res.setMode(mode);
	}

	/**
	 * @param res
	 * @return
	 */
	public static File toFile(Resource res) {
		if (res instanceof File) return (File) res;
		return new FileWrapper(res);
	}

	@Override
	public void setArchive(boolean value) throws IOException {
		res.setArchive(value);
	}

	@Override
	public void setHidden(boolean value) throws IOException {
		res.setHidden(value);
	}

	@Override
	public void setSystem(boolean value) throws IOException {
		res.setSystem(value);
	}

	@Override
	public boolean getAttribute(short attribute) {
		return res.getAttribute(attribute);
	}

	@Override
	public void setAttribute(short attribute, boolean value) throws IOException {
		res.setAttribute(attribute, value);
	}

	@Override
	public boolean setReadable(boolean value) {
		return res.setReadable(value);
	}

	@Override
	public boolean setWritable(boolean value) {
		return res.setWritable(value);
	}

	@Override
	public void copyFrom(Resource res, boolean append) throws IOException {
		res.copyFrom(res, append);
	}

	@Override
	public void copyTo(Resource res, boolean append) throws IOException {
		res.copyTo(res, append);
	}

}