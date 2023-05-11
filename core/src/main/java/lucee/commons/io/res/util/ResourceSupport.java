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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.filter.ResourceNameFilter;

/**
 * Helper class to build resources
 */
public abstract class ResourceSupport implements Resource {

	@Override
	public void copyFrom(Resource res, boolean append) throws IOException {
		IOUtil.copy(res, this.getOutputStream(append), true);
	}

	@Override
	public void copyTo(Resource res, boolean append) throws IOException {
		IOUtil.copy(this, res.getOutputStream(append), true);
	}

	@Override
	public Resource getAbsoluteResource() {
		return this;
	}

	@Override
	public String getAbsolutePath() {
		return getPath();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return getOutputStream(false);
	}

	@Override
	public Resource getCanonicalResource() throws IOException {
		return this;
	}

	@Override
	public String getCanonicalPath() throws IOException {
		return getPath();
	}

	@Override
	public void moveTo(Resource dest) throws IOException {
		ResourceUtil.moveTo(this, dest, false);
	}

	@Override
	public String[] list(ResourceFilter filter) {
		String[] files = list();
		if (files == null) return null;
		List<String> list = new ArrayList<String>();
		Resource res;
		for (int i = 0; i < files.length; i++) {
			res = getRealResource(files[i]);
			if (filter.accept(res)) list.add(files[i]);
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public String[] list(ResourceNameFilter filter) {
		String[] lst = list();
		if (lst == null) return null;

		List<String> list = new ArrayList<String>();
		for (int i = 0; i < lst.length; i++) {
			if (filter.accept(getParentResource(), lst[i])) list.add(lst[i]);
		}
		if (list.size() == 0) return new String[0];
		if (list.size() == lst.length) return lst;
		return list.toArray(new String[list.size()]);
	}

	@Override
	public Resource[] listResources(ResourceNameFilter filter) {
		String[] files = list();
		if (files == null) return null;

		List<Resource> list = new ArrayList<Resource>();
		for (int i = 0; i < files.length; i++) {
			if (filter.accept(this, files[i])) list.add(getRealResource(files[i]));
		}
		return list.toArray(new Resource[list.size()]);
	}

	@Override
	public Resource[] listResources(ResourceFilter filter) {
		String[] files = list();
		if (files == null) return null;

		List<Resource> list = new ArrayList<Resource>();
		Resource res;
		for (int i = 0; i < files.length; i++) {
			res = this.getRealResource(files[i]);
			if (filter.accept(res)) list.add(res);
		}
		return list.toArray(new Resource[list.size()]);
	}

	@Override
	public String getReal(String realpath) {
		return getRealResource(realpath).getPath();
	}

	@Override
	public String[] list() {
		Resource[] children = listResources();
		if (children == null) return null;
		String[] rtn = new String[children.length];
		for (int i = 0; i < children.length; i++) {
			rtn[i] = children[i].getName();
		}
		return rtn;
	}

	@Override
	public boolean canRead() {
		return isReadable();
	}

	@Override
	public boolean canWrite() {
		return isWriteable();
	}

	@Override
	public boolean renameTo(Resource dest) {
		try {
			moveTo(dest);
			return true;
		}
		catch (IOException e) {
			return false;
		}

	}

	@Override
	public boolean createNewFile() {
		try {
			createFile(false);
			return true;
		}
		catch (IOException e) {
		}
		return false;
	}

	@Override
	public boolean mkdir() {
		try {
			createDirectory(false);
			return true;
		}
		catch (IOException e) {
		}
		return false;
	}

	@Override
	public boolean mkdirs() {
		try {
			createDirectory(true);
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	@Override
	public boolean delete() {
		try {
			remove(false);
			return true;
		}
		catch (IOException e) {
		}
		return false;
	}

	@Override
	public boolean isArchive() {
		return getAttribute(Resource.ATTRIBUTE_ARCHIVE);
	}

	@Override
	public boolean isSystem() {
		return getAttribute(Resource.ATTRIBUTE_SYSTEM);
	}

	@Override
	public boolean isHidden() {
		return getAttribute(Resource.ATTRIBUTE_HIDDEN);
	}

	@Override
	public void setArchive(boolean value) throws IOException {
		setAttribute(ATTRIBUTE_ARCHIVE, value);
	}

	@Override
	public void setHidden(boolean value) throws IOException {
		setAttribute(ATTRIBUTE_HIDDEN, value);
	}

	@Override
	public boolean setReadOnly() {
		return setWritable(false);
	}

	@Override
	public void setSystem(boolean value) throws IOException {
		setAttribute(ATTRIBUTE_SYSTEM, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Resource)) return false;
		Resource other = (Resource) obj;

		if (getResourceProvider() != other.getResourceProvider()) return false;

		if (getResourceProvider().isCaseSensitive()) {
			if (getPath().equals(other.getPath())) return true;
			return ResourceUtil.getCanonicalPathEL(this).equals(ResourceUtil.getCanonicalPathEL(other));
		}
		if (getPath().equalsIgnoreCase(other.getPath())) return true;
		return ResourceUtil.getCanonicalPathEL(this).equalsIgnoreCase(ResourceUtil.getCanonicalPathEL(other));

	}

	@Override
	public String toString() {
		return getPath();
	}

	@Override
	public boolean getAttribute(short attribute) {
		return false;
	}

	@Override
	public void setAttribute(short attribute, boolean value) throws IOException {
		throw new IOException("the resource [" + getPath() + "] does not support attributes");
	}
}