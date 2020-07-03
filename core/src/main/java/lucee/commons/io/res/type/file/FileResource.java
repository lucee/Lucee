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
package lucee.commons.io.res.type.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.List;

import lucee.commons.cli.Command;
import lucee.commons.io.IOUtil;
import lucee.commons.io.ModeUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.io.res.util.ResourceOutputStream;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;

/**
 * Implementation og Resource for the local filesystem (java.io.File)
 */
public final class FileResource extends File implements Resource {

	private static final long serialVersionUID = -6856656594615376447L;
	private static final CopyOption[] COPY_OPTIONS = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };

	private final FileResourceProvider provider;

	/**
	 * Constructor for the factory
	 * 
	 * @param pathname
	 */
	FileResource(FileResourceProvider provider, String pathname) {
		super(pathname);
		this.provider = provider;
	}

	/**
	 * Inner Constr constructor to create parent/child
	 * 
	 * @param parent
	 * @param child
	 */
	private FileResource(FileResourceProvider provider, File parent, String child) {
		super(parent, child);
		this.provider = provider;
	}

	@Override
	public void copyFrom(Resource res, boolean append) throws IOException {

		if (res instanceof File && (!append || !this.isFile())) {
			try {
				Files.copy(((File) res).toPath(), this.toPath(), COPY_OPTIONS);
				return;
			}
			catch (Exception exception) {}
		}

		IOUtil.copy(res, this.getOutputStream(append), true);

		// executable?
		boolean e = res instanceof File && ((File) res).canExecute();
		boolean w = res.canWrite();
		boolean r = res.canRead();

		if (e) this.setExecutable(true);
		if (w != this.canWrite()) this.setWritable(w);
		if (r != this.canRead()) this.setReadable(r);
	}

	@Override
	public void copyTo(Resource res, boolean append) throws IOException {

		if (res instanceof File && (!append || !res.isFile())) {
			try {
				Files.copy(this.toPath(), ((File) res).toPath(), COPY_OPTIONS);
				return;
			}
			catch (Exception exception) {}
		}

		IOUtil.copy(this, res.getOutputStream(append), true);
		boolean e = canExecute();
		boolean w = canWrite();
		boolean r = canRead();

		if (e && res instanceof File) ((File) res).setExecutable(true);
		if (w != res.canWrite()) res.setWritable(w);
		if (r != res.canRead()) res.setReadable(r);

	}

	@Override
	public Resource getAbsoluteResource() {
		return new FileResource(provider, getAbsolutePath());
	}

	@Override
	public Resource getCanonicalResource() throws IOException {
		return new FileResource(provider, getCanonicalPath());
	}

	@Override
	public Resource getParentResource() {
		String p = getParent();
		if (p == null) return null;
		return new FileResource(provider, p);
	}

	@Override
	public Resource[] listResources() {
		String[] files = list();
		if (files == null) return null;

		Resource[] resources = new Resource[files.length];
		for (int i = 0; i < files.length; i++) {
			resources[i] = getRealResource(files[i]);
		}
		return resources;
	}

	@Override
	public String[] list(ResourceFilter filter) {
		String[] files = list();
		if (files == null) return null;

		List<String> list = new ArrayList<String>();
		FileResource res;
		for (int i = 0; i < files.length; i++) {
			res = new FileResource(provider, this, files[i]);
			if (filter.accept(res)) list.add(files[i]);
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public Resource[] listResources(ResourceFilter filter) {
		String[] files = list();
		if (files == null) return null;

		List<Resource> list = new ArrayList<Resource>();
		Resource res;
		for (int i = 0; i < files.length; i++) {
			res = getRealResource(files[i]);
			if (filter.accept(res)) list.add(res);
		}
		return list.toArray(new FileResource[list.size()]);
	}

	@Override
	public String[] list(ResourceNameFilter filter) {
		String[] files = list();
		if (files == null) return null;
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			if (filter.accept(this, files[i])) list.add(files[i]);
		}
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
	public void moveTo(Resource dest) throws IOException {
		if (this.equals(dest)) return;
		boolean done = false;
		if (dest instanceof File) {
			provider.lock(this);
			try {
				if (dest.exists() && !dest.delete())
					throw new IOException("Can't move file [" + this.getAbsolutePath() + "] cannot remove existing file [" + dest.getAbsolutePath() + "]");

				done = super.renameTo((File) dest);
				/*
				 * if(!super.renameTo((File)dest)) { throw new
				 * IOException("can't move file "+this.getAbsolutePath()+" to destination resource "+dest.
				 * getAbsolutePath()); }
				 */
			}
			finally {
				provider.unlock(this);
			}
		}
		if (!done) {
			ResourceUtil.checkMoveToOK(this, dest);
			IOUtil.copy(getInputStream(), dest, true);
			if (!this.delete()) {
				throw new IOException("Can't delete resource [" + this.getAbsolutePath() + "]");
			}
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		// provider.lock(this);
		provider.read(this);
		try {
			// return new BufferedInputStream(new ResourceInputStream(this,new FileInputStream(this)));
			return new BufferedInputStream(new FileInputStream(this));
		}
		catch (IOException ioe) {
			// provider.unlock(this);
			throw ioe;
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return getOutputStream(false);
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws IOException {
		provider.lock(this);
		try {
			if (!super.exists() && !super.createNewFile()) {
				throw new IOException("Can't create file [" + this + "]");
			}
			return new BufferedOutputStream(new ResourceOutputStream(this, new FileOutputStream(this, append)));
		}
		catch (IOException ioe) {
			provider.unlock(this);
			throw ioe;
		}
	}

	@Override
	public void createFile(boolean createParentWhenNotExists) throws IOException {
		provider.lock(this);
		try {
			if (createParentWhenNotExists) {
				File p = super.getParentFile();
				if (!p.exists()) p.mkdirs();
			}
			if (!super.createNewFile()) {
				if (super.isFile()) throw new IOException("Can't create file [" + this + "], file already exists");
				throw new IOException("Can't create file [" + this + "]");
			}
		}
		finally {
			provider.unlock(this);
		}
	}

	@Override
	public void remove(boolean alsoRemoveChildren) throws IOException {
		if (alsoRemoveChildren && isDirectory()) {
			Resource[] children = listResources();
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					children[i].remove(alsoRemoveChildren);
				}
			}
		}
		provider.lock(this);
		try {
			if (!super.delete()) {
				if (!super.exists()) throw new IOException("Can't delete file [" + this + "], file does not exist");
				if (!super.canWrite()) throw new IOException("Can't delete file [" + this + "], no access");
				throw new IOException("Can't delete file [" + this + "]");
			}
		}
		finally {
			provider.unlock(this);
		}
	}

	@Override
	public String getReal(String realpath) {
		if (realpath.length() <= 2) {
			if (realpath.length() == 0) return getPath();
			if (realpath.equals(".")) return getPath();
			if (realpath.equals("..")) return getParent();
		}
		return new FileResource(provider, this, realpath).getPath();
	}

	@Override
	public Resource getRealResource(String realpath) {
		if (realpath.length() <= 2) {
			if (realpath.length() == 0) return this;
			if (realpath.equals(".")) return this;
			if (realpath.equals("..")) return getParentResource();
		}
		return new FileResource(provider, this, realpath);
	}

	public ContentType getContentType() {
		return ResourceUtil.getContentType(this);
	}

	@Override
	public void createDirectory(boolean createParentWhenNotExists) throws IOException {
		provider.lock(this);
		try {
			if (createParentWhenNotExists ? !_mkdirs() : !super.mkdir()) {
				if (super.isDirectory()) throw new IOException("Can't create directory [" + this + "], directory already exists");
				throw new IOException("Can't create directory [" + this + "]");
			}
		}
		finally {
			provider.unlock(this);
		}
	}

	@Override
	public ResourceProvider getResourceProvider() {
		return provider;
	}

	@Override
	public boolean isReadable() {
		return canRead();
	}

	@Override
	public boolean isWriteable() {
		return canWrite();
	}

	@Override
	public boolean renameTo(Resource dest) {
		try {
			moveTo(dest);
			return true;
		}
		catch (IOException e) {}
		return false;
	}

	@Override
	public boolean isArchive() {
		return getAttribute(ATTRIBUTE_ARCHIVE);
	}

	@Override
	public boolean isSystem() {
		return getAttribute(ATTRIBUTE_SYSTEM);
	}

	@Override
	public int getMode() {
		if (!exists()) return 0;
		if (SystemUtil.isUnix()) {
			try {
				// TODO geht nur fuer file
				String line = Command.execute("ls -ld " + getPath(), false).getOutput();

				line = line.trim();
				line = line.substring(0, line.indexOf(' '));
				// print.ln(getPath());
				return ModeUtil.toOctalMode(line);

			}
			catch (Exception e) {}

		}
		int mode = SystemUtil.isWindows() && exists() ? 0111 : 0;
		if (super.canRead()) mode += 0444;
		if (super.canWrite()) mode += 0222;
		return mode;
	}

	@Override
	public void setMode(int mode) throws IOException {
		// TODO unter windows mit setReadable usw.
		if (!SystemUtil.isUnix()) return;
		provider.lock(this);
		try {
			// print.ln(ModeUtil.toStringMode(mode));
			if (Runtime.getRuntime().exec(new String[] { "chmod", ModeUtil.toStringMode(mode), getPath() }).waitFor() != 0)
				throw new IOException("chmod  [" + ModeUtil.toStringMode(mode) + "] [" + toString() + "] failed");
		}
		catch (InterruptedException e) {
			throw new IOException("Interrupted waiting for chmod [" + toString() + "]");
		}
		finally {
			provider.unlock(this);
		}
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
	public void setSystem(boolean value) throws IOException {
		setAttribute(ATTRIBUTE_SYSTEM, value);
	}

	@Override

	public boolean setReadable(boolean value) {
		if (!SystemUtil.isUnix()) return false;
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
		// setReadonly
		if (!value) {
			try {
				provider.lock(this);
				if (!super.setReadOnly()) throw new IOException("Can't set resource read-only");
			}
			catch (IOException ioe) {
				return false;
			}
			finally {
				provider.unlock(this);
			}
			return true;
		}

		if (SystemUtil.isUnix()) {
			// need no lock because get/setmode has one
			try {
				setMode(ModeUtil.setWritable(getMode(), value));
			}
			catch (IOException e) {
				return false;
			}
			return true;
		}

		try {
			provider.lock(this);
			Runtime.getRuntime().exec("attrib -R " + getAbsolutePath());
		}
		catch (IOException ioe) {
			return false;
		}
		finally {
			provider.unlock(this);
		}
		return true;
	}

	@Override
	public boolean createNewFile() {
		try {
			provider.lock(this);
			return super.createNewFile();
		}
		catch (IOException e) {
			return false;
		}
		finally {
			provider.unlock(this);
		}
	}

	@Override
	public boolean canRead() {
		try {
			provider.read(this);
		}
		catch (IOException e) {
			return false;
		}
		return super.canRead();
	}

	@Override
	public boolean canWrite() {
		try {
			provider.read(this);
		}
		catch (IOException e) {
			return false;
		}
		return super.canWrite();
	}

	@Override
	public boolean delete() {
		try {
			provider.lock(this);
			return super.delete();
		}
		catch (IOException e) {
			return false;
		}
		finally {
			provider.unlock(this);
		}
	}

	@Override
	public boolean exists() {
		try {
			provider.read(this);
		}
		catch (IOException e) {}

		return super.exists();
	}

	@Override
	public boolean isAbsolute() {
		try {
			provider.read(this);
		}
		catch (IOException e) {
			return false;
		}
		return super.isAbsolute();
	}

	@Override
	public boolean isDirectory() {
		try {
			provider.read(this);
		}
		catch (IOException e) {
			return false;
		}
		return super.isDirectory();
	}

	@Override
	public boolean isFile() {
		try {
			provider.read(this);
		}
		catch (IOException e) {
			return false;
		}
		return super.isFile();
	}

	@Override
	public boolean isHidden() {
		try {
			provider.read(this);
		}
		catch (IOException e) {
			return false;
		}
		return super.isHidden();
	}

	@Override
	public long lastModified() {
		try {
			provider.read(this);
		}
		catch (IOException e) {
			return 0;
		}
		return super.lastModified();
	}

	@Override
	public long length() {
		try {
			provider.read(this);
		}
		catch (IOException e) {
			return 0;
		}
		return super.length();
	}

	@Override
	public String[] list() {
		try {
			provider.read(this);
		}
		catch (IOException e) {
			return null;
		}
		return super.list();
	}

	@Override
	public boolean mkdir() {
		try {
			provider.lock(this);
			return super.mkdir();
		}
		catch (IOException e) {
			return false;
		}
		finally {
			provider.unlock(this);
		}
	}

	@Override
	public boolean mkdirs() {
		try {
			provider.lock(this);
			return _mkdirs();

		}
		catch (IOException e) {
			return false;
		}
		finally {
			provider.unlock(this);
		}
	}

	private boolean _mkdirs() {
		if (super.exists()) return false;
		if (super.mkdir()) return true;

		File parent = super.getParentFile();
		return (parent != null) && (parent.mkdirs() && super.mkdir());
	}

	@Override
	public boolean setLastModified(long time) {
		try {
			provider.lock(this);
			return super.setLastModified(time);
		}
		catch (Throwable t) {// IllegalArgumentException or IOException
			ExceptionUtil.rethrowIfNecessary(t);
			return false;
		}
		finally {
			provider.unlock(this);
		}

	}

	@Override
	public boolean setReadOnly() {
		try {
			provider.lock(this);
			return super.setReadOnly();
		}
		catch (IOException e) {
			return false;
		}
		finally {
			provider.unlock(this);
		}
	}

	@Override
	public boolean getAttribute(short attribute) {
		if (!SystemUtil.isWindows()) return false;

		try {
			provider.lock(this);
			DosFileAttributes attr = Files.readAttributes(this.toPath(), DosFileAttributes.class);
			if (attribute == ATTRIBUTE_ARCHIVE) {
				return attr.isArchive();
			}
			else if (attribute == ATTRIBUTE_HIDDEN) {
				return attr.isHidden();
			}
			else if (attribute == ATTRIBUTE_SYSTEM) {
				return attr.isSystem();
			}
			else {
				return false;
			}
		}
		catch (Exception e) {
			return false;
		}
		finally {
			provider.unlock(this);
		}
	}

	@Override
	public void setAttribute(short attribute, boolean value) throws IOException {
		if (!SystemUtil.isWindows()) return;

		provider.lock(this);
		try {
			if (attribute == ATTRIBUTE_ARCHIVE) {
				Files.setAttribute(this.toPath(), "dos:archive", value);
			}
			else if (attribute == ATTRIBUTE_HIDDEN) {
				Files.setAttribute(this.toPath(), "dos:hidden", value);
			}
			else if (attribute == ATTRIBUTE_SYSTEM) {
				Files.setAttribute(this.toPath(), "dos:system", value);
			}
		}
		catch (IOException e) {
			return;
		}
		finally {
			provider.unlock(this);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (provider.isCaseSensitive()) return super.equals(other);
		if (!(other instanceof File)) return false;
		return getAbsolutePath().equalsIgnoreCase(((File) other).getAbsolutePath());
	}
}
