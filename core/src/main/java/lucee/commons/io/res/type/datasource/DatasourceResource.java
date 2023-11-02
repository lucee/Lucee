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
package lucee.commons.io.res.type.datasource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.ModeUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.type.datasource.DatasourceResourceProvider.ConnectionData;
import lucee.commons.io.res.util.ResourceSupport;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.type.util.ArrayUtil;

public final class DatasourceResource extends ResourceSupport {

	private DatasourceResourceProvider provider;
	private String parent;
	private String name;
	private ConnectionData data;
	private int fullPathHash;
	private int pathHash;

	/**
	 * Constructor of the class
	 * 
	 * @param provider
	 * @param data
	 * @param path
	 */
	DatasourceResource(DatasourceResourceProvider provider, ConnectionData data, String path) {
		this.provider = provider;
		this.data = data;
		if ("/".equals(path)) {
			this.parent = null;
			this.name = "";
		}
		else {
			String[] pn = ResourceUtil.translatePathName(path);
			this.parent = pn[0];
			this.name = pn[1];
		}
	}

	private int fullPathHash() {
		if (fullPathHash == 0) fullPathHash = getInnerPath().hashCode();
		return fullPathHash;
	}

	private int pathHash() {
		if (pathHash == 0 && parent != null) pathHash = parent.hashCode();
		return pathHash;
	}

	private Attr attr() {
		return provider.getAttr(data, fullPathHash(), parent, name);
	}

	private boolean isRoot() {
		return parent == null;
	}

	@Override
	public void createDirectory(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateDirectoryOK(this, createParentWhenNotExists);
		provider.create(data, fullPathHash(), pathHash(), parent, name, Attr.TYPE_DIRECTORY);

	}

	@Override
	public void createFile(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists);
		provider.create(data, fullPathHash(), pathHash(), parent, name, Attr.TYPE_FILE);
	}

	@Override
	public void remove(boolean force) throws IOException {
		ResourceUtil.checkRemoveOK(this);
		if (isRoot()) throw new IOException("Can't remove root resource [" + getPath() + "]");

		Resource[] children = listResources();
		if (children != null && children.length > 0) {
			if (!force) {
				throw new IOException("Can't delete directory [" + getPath() + "], directory is not empty");
			}
			for (int i = 0; i < children.length; i++) {
				children[i].remove(true);
			}
		}
		provider.delete(data, fullPathHash(), parent, name);
	}

	@Override
	public boolean exists() {
		return attr().exists();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		ResourceUtil.checkGetInputStreamOK(this);
		return provider.getInputStream(data, fullPathHash(), parent, name);
	}

	@Override
	public int getMode() {
		return attr().getMode();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws IOException {
		ResourceUtil.checkGetOutputStreamOK(this);
		byte[] barr = null;

		if (append && !provider.concatSupported(data) && isFile()) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtil.copy(getInputStream(), baos, true, true);
				barr = baos.toByteArray();
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}

		OutputStream os = provider.getOutputStream(data, fullPathHash(), pathHash(), parent, name, append);
		if (!ArrayUtil.isEmpty(barr)) IOUtil.copy(new ByteArrayInputStream(barr), os, true, false);
		return os;
	}

	@Override
	public String getParent() {
		if (isRoot()) return null;
		String p = (StringUtil.isEmpty(parent)) ? "/" : parent;
		return provider.getScheme().concat("://").concat(data.key()).concat(ResourceUtil.translatePath(p, true, false));

	}

	@Override
	public Resource getParentResource() {
		return getParentDatasourceResource();
	}

	private DatasourceResource getParentDatasourceResource() {
		if (isRoot()) return null;
		return new DatasourceResource(provider, data, parent);
	}

	@Override
	public String getPath() {
		return provider.getScheme().concat("://").concat(data.key()).concat(getInnerPath());
	}

	private String getInnerPath() {
		if (parent == null) return "/";
		return parent.concat(name);
	}

	@Override
	public Resource getRealResource(String realpath) {
		realpath = ResourceUtil.merge(getInnerPath(), realpath);
		if (realpath.startsWith("../")) return null;

		return new DatasourceResource(provider, data, realpath);
	}

	@Override
	public ResourceProvider getResourceProvider() {
		return provider;
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}

	@Override
	public boolean isDirectory() {
		return attr().isDirectory();
	}

	@Override
	public boolean isFile() {
		return attr().isFile();
	}

	@Override
	public boolean isReadable() {
		return ModeUtil.isReadable(getMode());
	}

	@Override
	public boolean isWriteable() {
		return ModeUtil.isWritable(getMode());
	}

	@Override
	public long lastModified() {
		return attr().getLastModified();
	}

	@Override
	public long length() {
		return attr().size();
	}

	@Override
	public Resource[] listResources() {
		if (!attr().isDirectory()) return null;

		String path;
		if (parent == null) path = "/";
		else path = parent.concat(name).concat("/");

		Attr[] children = null;
		try {
			children = provider.getAttrs(data, path.hashCode(), path);
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
		if (children == null) return new Resource[0];
		Resource[] attrs = new Resource[children.length];
		for (int i = 0; i < children.length; i++) {
			// TODO optimieren, alle attr mitgeben
			attrs[i] = new DatasourceResource(provider, data, path + children[i].getName());
		}
		return attrs;
	}

	@Override
	public boolean setLastModified(long time) {
		if (!exists()) return false;
		return provider.setLastModified(data, fullPathHash(), parent, name, time);
	}

	@Override
	public void setMode(int mode) throws IOException {
		if (!exists()) throw new IOException("can't set mode on resource [" + this + "], resource does not exist");
		provider.setMode(data, fullPathHash(), parent, name, mode);
	}

	@Override
	public void moveTo(Resource dest) throws IOException {
		super.moveTo(dest);// TODO
	}

	@Override
	public boolean setReadable(boolean readable) {
		if (!exists()) return false;
		try {
			setMode(ModeUtil.setReadable(getMode(), readable));
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	@Override
	public boolean setWritable(boolean writable) {
		if (!exists()) return false;
		try {
			setMode(ModeUtil.setWritable(getMode(), writable));
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return getPath();
	}

}