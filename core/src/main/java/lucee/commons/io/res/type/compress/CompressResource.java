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
package lucee.commons.io.res.type.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.util.ResourceSupport;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;

public final class CompressResource extends ResourceSupport {

	private final CompressResourceProvider provider;
	private final Compress zip;
	private final String path;
	private final String name;
	private final String parent;
	private final boolean caseSensitive;

	/**
	 * Constructor of the class
	 * 
	 * @param provider
	 * @param zip
	 * @param path
	 * @param caseSensitive
	 */
	CompressResource(CompressResourceProvider provider, Compress zip, String path, boolean caseSensitive) {
		if (StringUtil.isEmpty(path)) path = "/";
		this.provider = provider;
		this.zip = zip;
		this.path = path;

		if ("/".equals(path)) {
			this.parent = null;
			this.name = "";
		}
		else {
			String[] pn = ResourceUtil.translatePathName(path);
			this.parent = pn[0];
			this.name = pn[1];
		}

		this.caseSensitive = caseSensitive;
	}

	/**
	 * @return return ram resource that contain the data
	 * @throws IOException
	 */
	private Resource getRamResource() {
		try {
			return zip.getRamProviderResource(path);
		}
		catch (IOException e) {
			throw ExceptionUtil.toRuntimeException(e);
		}
	}

	@Override
	public boolean exists() {
		try {
			provider.read(this);
		}
		catch (IOException e) {
			return false;
		}
		return getRamResource().exists();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		ResourceUtil.checkGetInputStreamOK(this);
		return getRamResource().getInputStream();
	}

	public Resource getCompressResource() {
		return zip.getCompressFile();
	}

	public String getCompressPath() {
		return path;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getParent() {
		if (StringUtil.isEmpty(parent)) return null;
		return provider.getScheme().concat("://").concat(zip.getCompressFile().getPath()).concat("!").concat(parent);
	}

	@Override
	public Resource getParentResource() {
		if (StringUtil.isEmpty(parent)) return null;
		return new CompressResource(provider, zip, parent, caseSensitive);
	}

	@Override
	public String getPath() {
		return provider.getScheme().concat("://").concat(zip.getCompressFile().getPath()).concat("!").concat(path);
	}

	@Override
	public Resource getRealResource(String realpath) {
		realpath = ResourceUtil.merge(path, realpath);
		if (realpath.startsWith("../")) return null;
		return new CompressResource(provider, zip, realpath, caseSensitive);
	}

	@Override
	public ResourceProvider getResourceProvider() {
		return provider;
	}

	@Override
	public boolean isAbsolute() {
		return getRamResource().isAbsolute();
	}

	@Override
	public boolean isDirectory() {
		return getRamResource().isDirectory();
	}

	@Override
	public boolean isFile() {
		return getRamResource().isFile();
	}

	@Override
	public boolean isReadable() {
		return getRamResource().isReadable();
	}

	@Override
	public boolean isWriteable() {
		return getRamResource().isWriteable();
	}

	@Override
	public long lastModified() {
		return getRamResource().lastModified();
	}

	@Override
	public long length() {
		return getRamResource().length();
	}

	@Override
	public Resource[] listResources() {
		String[] names = list();
		if (names == null) return null;
		Resource[] children = new Resource[names.length];
		for (int i = 0; i < children.length; i++) {
			children[i] = new CompressResource(provider, zip, path.concat("/").concat(names[i]), caseSensitive);
		}
		return children;
	}

	@Override
	public String[] list() {
		return getRamResource().list();
	}

	@Override
	public void remove(boolean force) throws IOException {
		Resource rr = getRamResource();
		if (rr.getParent() == null) throw new IOException("Can't remove root resource [" + getPath() + "]");

		if (!rr.exists()) throw new IOException("Can't remove resource [" + getPath() + "], resource does not exist");

		Resource[] children = listResources();
		if (children != null && children.length > 0) {
			if (!force) {
				throw new IOException("Can't delete directory [" + getPath() + "], directory is not empty");
			}
			for (int i = 0; i < children.length; i++) {
				children[i].remove(true);
			}
		}
		rr.remove(force);
	}

	@Override
	public boolean setLastModified(long time) {
		boolean lm = getRamResource().setLastModified(time);
		zip.synchronize(provider.async);
		return lm;
	}

	@Override
	public void createDirectory(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateDirectoryOK(this, createParentWhenNotExists);
		getRamResource().createDirectory(createParentWhenNotExists);
		zip.synchronize(provider.async);
	}

	@Override
	public void createFile(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists);
		getRamResource().createFile(createParentWhenNotExists);
		zip.synchronize(provider.async);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		ResourceUtil.checkGetOutputStreamOK(this);
		// Resource res = getRamResource();
		// Resource p = res.getParentResource();
		// if(p!=null && !p.exists())p.mkdirs();
		return new CompressOutputStreamSynchronizer(getRamResource().getOutputStream(), zip, provider.async);
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws IOException {
		return new CompressOutputStreamSynchronizer(getRamResource().getOutputStream(append), zip, provider.async);
	}

	@Override
	public int getMode() {
		return getRamResource().getMode();
	}

	@Override
	public void setMode(int mode) throws IOException {
		getRamResource().setMode(mode);
		zip.synchronize(provider.async);

	}

	@Override
	public boolean setReadable(boolean value) {
		if (!isFile()) return false;
		getRamResource().setReadable(value);
		zip.synchronize(provider.async);
		return true;
	}

	@Override
	public boolean setWritable(boolean value) {
		if (!isFile()) return false;
		getRamResource().setWritable(value);
		zip.synchronize(provider.async);
		return true;
	}

}