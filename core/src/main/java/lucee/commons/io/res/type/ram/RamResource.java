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
package lucee.commons.io.res.type.ram;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lucee.commons.io.ModeUtil;
import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.util.ResourceSupport;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;

/**
 * a ram resource
 */
public final class RamResource extends ResourceSupport {

	private final RamResourceProviderOld provider;

	private final String parent;
	private final String name;
	private RamResourceCore _core;

	RamResource(RamResourceProviderOld provider, String path) {
		this.provider = provider;
		if (path.equals("/") || StringUtil.isEmpty(path)) {
			// if(path.equals("/")) {
			this.parent = null;
			this.name = "";
		}
		else {
			String[] pn = ResourceUtil.translatePathName(path);
			this.parent = pn[0];
			this.name = pn[1];
		}

	}

	private RamResource(RamResourceProviderOld provider, String parent, String name) {
		this.provider = provider;
		this.parent = parent;
		this.name = name;
	}

	RamResourceCore getCore() {
		if (_core == null || _core.getType() == 0) {
			_core = provider.getCore(getInnerPath());
		}
		return _core;
	}

	void removeCore() {
		if (_core == null) return;
		_core.remove();
		_core = null;
	}

	private RamResourceCore createCore(int type) throws IOException {
		return _core = provider.createCore(getInnerPath(), type);
	}

	@Override
	public String getPath() {
		return provider.getScheme().concat("://").concat(getInnerPath());
	}

	private String getInnerPath() {
		if (parent == null) return "/";
		return parent.concat(name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getParent() {
		if (isRoot()) return null;
		return provider.getScheme().concat("://").concat(ResourceUtil.translatePath(parent, true, false));
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
	public void remove(boolean force) throws IOException {
		if (isRoot()) throw new IOException("Can't remove root resource [" + getPath() + "]");

		provider.read(this);
		RamResourceCore core = getCore();
		if (core == null) throw new IOException("Can't remove resource [" + getPath() + "],resource does not exist");

		Resource[] children = listResources();
		if (children != null && children.length > 0) {
			if (!force) {
				throw new IOException("Can't delete directory [" + getPath() + "], directory is not empty");
			}
			for (int i = 0; i < children.length; i++) {
				children[i].remove(true);
			}
		}
		removeCore();
	}

	@Override
	public boolean exists() {
		try {
			provider.read(this);
		}
		catch (IOException e) {
			return true;
		}
		return getCore() != null;
	}

	@Override
	public Resource getParentResource() {
		return getParentRamResource();
	}

	private RamResource getParentRamResource() {
		if (isRoot()) return null;
		return new RamResource(provider, parent);
	}

	@Override
	public Resource getRealResource(String realpath) {
		realpath = ResourceUtil.merge(getInnerPath(), realpath);
		if (realpath.startsWith("../")) return null;

		return new RamResource(provider, realpath);
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}

	@Override
	public boolean isDirectory() {
		return exists() && getCore().getType() == RamResourceCore.TYPE_DIRECTORY;
	}

	@Override
	public boolean isFile() {
		return exists() && getCore().getType() == RamResourceCore.TYPE_FILE;
	}

	@Override
	public long lastModified() {
		if (!exists()) return 0;
		return getCore().getLastModified();
	}

	@Override
	public long length() {
		if (!exists()) return 0;
		byte[] data = getCore().getData();
		if (data == null) return 0;
		return data.length;
	}

	@Override
	public String[] list() {
		if (!exists()) return null;
		RamResourceCore core = getCore();
		if (core.getType() != RamResourceCore.TYPE_DIRECTORY) return null;

		return core.getChildNames();
		/*
		 * List list = core.getChildren(); if(list==null && list.size()==0) return new String[0];
		 * 
		 * Iterator it = list.iterator(); String[] children=new String[list.size()]; RamResourceCore cc; int
		 * count=0; while(it.hasNext()) { cc=(RamResourceCore) it.next(); children[count++]=cc.getName(); }
		 * return children;
		 */
	}

	@Override
	public Resource[] listResources() {
		String[] list = list();
		if (list == null) return null;

		Resource[] children = new Resource[list.length];
		String p = getInnerPath();
		if (!isRoot()) p = p.concat("/");
		for (int i = 0; i < children.length; i++) {
			children[i] = new RamResource(provider, p, list[i]);
		}
		return children;
	}

	@Override
	public boolean setLastModified(long time) {
		if (!exists()) return false;
		getCore().setLastModified(time);
		return true;
	}

	@Override
	public boolean setReadOnly() {
		return setWritable(false);
	}

	@Override
	public void createFile(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists);
		provider.lock(this);
		try {
			createCore(RamResourceCore.TYPE_FILE);
		}
		finally {
			provider.unlock(this);
		}
	}

	@Override
	public void createDirectory(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateDirectoryOK(this, createParentWhenNotExists);
		provider.lock(this);
		try {
			createCore(RamResourceCore.TYPE_DIRECTORY);
		}
		finally {
			provider.unlock(this);
		}

	}

	@Override
	public InputStream getInputStream() throws IOException {
		ResourceUtil.checkGetInputStreamOK(this);

		provider.lock(this);
		RamResourceCore core = getCore();

		byte[] data = core.getData();
		if (data == null) data = new byte[0];
		provider.unlock(this);
		return new ByteArrayInputStream(data);
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws IOException {
		ResourceUtil.checkGetOutputStreamOK(this);
		provider.lock(this);
		return new RamOutputStream(this, append);
	}

	public ContentType getContentType() {
		return ResourceUtil.getContentType(this);
	}

	@Override
	public ResourceProvider getResourceProvider() {
		return provider;
	}

	@Override
	public String toString() {
		return getPath();
	}

	/**
	 * This is used by the MemoryResource too write back data to, that are written to outputstream
	 */
	class RamOutputStream extends ByteArrayOutputStream {

		private RamResource res;
		private boolean append;

		/**
		 * Constructor of the class
		 * 
		 * @param res
		 */
		public RamOutputStream(RamResource res, boolean append) {
			this.append = append;
			this.res = res;
		}

		@Override
		public void close() throws IOException {
			try {
				super.close();
				RamResourceCore core = res.getCore();
				if (core == null) core = res.createCore(RamResourceCore.TYPE_FILE);

				core.setData(this.toByteArray(), append);
			}
			finally {
				res.getResourceProvider().unlock(res);
			}
		}
	}

	@Override
	public boolean setReadable(boolean value) {
		if (!exists()) return false;
		try {
			setMode(ModeUtil.setReadable(getMode(), value));
			return true;
		}
		catch (IOException e) {
			return false;
		}

	}

	@Override
	public boolean setWritable(boolean value) {
		if (!exists()) return false;
		try {
			setMode(ModeUtil.setWritable(getMode(), value));
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	private boolean isRoot() {
		return parent == null;
	}

	@Override
	public int getMode() {
		if (!exists()) return 0;
		return getCore().getMode();
	}

	@Override
	public void setMode(int mode) throws IOException {
		if (!exists()) throw new IOException("Can't set mode on resource [" + this + "], resource does not exist");
		getCore().setMode(mode);
	}

	@Override
	public boolean getAttribute(short attribute) {
		if (!exists()) return false;
		return (getCore().getAttributes() & attribute) > 0;
	}

	@Override
	public void setAttribute(short attribute, boolean value) throws IOException {
		if (!exists()) throw new IOException("Can't get attributes on resource [" + this + "], resource does not exist");
		int attr = getCore().getAttributes();
		if (value) {
			if ((attr & attribute) == 0) attr += attribute;
		}
		else {
			if ((attr & attribute) > 0) attr -= attribute;
		}
		getCore().setAttributes(attr);
	}

}