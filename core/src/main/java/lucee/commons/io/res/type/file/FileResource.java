package lucee.commons.io.res.type.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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
import lucee.commons.lang.Pair;
import lucee.commons.lang.types.RefDouble;
import lucee.commons.lang.types.RefDoubleImpl;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;

/**
 * Implementation of Resource for the local filesystem (java.io.File)
 */
public final class FileResource extends File implements Resource {

	private static final long serialVersionUID = -6856656594615376447L;
	private static final CopyOption[] COPY_OPTIONS = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };

	private final FileResourceProvider provider;

	private static Map<String, Pair<RefDouble, RefInteger>> logs = new ConcurrentHashMap<>();

	/**
	 * Constructor for the factory
	 * 
	 * @param pathname
	 */
	FileResource(FileResourceProvider provider, String pathname) {
		super(pathname);
		this.provider = provider;
		log(getAbsolutePath() + ":FileResource(String pathname)", SystemUtil.millis());
	}

	/**
	 * Inner constructor to create parent/child
	 * 
	 * @param parent
	 * @param child
	 */
	private FileResource(FileResourceProvider provider, File parent, String child) {
		super(parent, child);
		this.provider = provider;
		log(getAbsolutePath() + ":FileResource(File parent, String child)", SystemUtil.millis());
	}

	private static void log(String msg, double start) {
		double end = SystemUtil.millis();
		String key = msg + "|" + ExceptionUtil.getStacktrace(new Throwable(), false);
		Pair<RefDouble, RefInteger> pair = logs.get(key);
		if (pair == null) {
			synchronized (SystemUtil.createToken("file", key)) {
				pair = logs.get(key);
				if (pair == null) {
					logs.put(key, new Pair(new RefDoubleImpl(end - start), new RefIntegerImpl(1)));
					return;
				}
			}
		}
		pair.getName().plus(end - start);
		pair.getValue().plus(1);
	}

	public static Query getLogs() {
		Query q = new QueryImpl(new String[] { "path", "method", "stacktrace", "time", "count" }, 0, "logs");

		for (Entry<String, Pair<RefDouble, RefInteger>> e: logs.entrySet()) {
			int row = q.addRow();

			String val = e.getKey();
			int index = val.indexOf(':');
			q.setAtEL("path", row, val.substring(0, index));

			val = val.substring(index + 1);
			index = val.indexOf('|');
			q.setAtEL("method", row, val.substring(0, index));
			q.setAtEL("stacktrace", row, val.substring(index + 1));

			q.setAtEL("time", row, e.getValue().getName().toDouble());
			q.setAtEL("count", row, e.getValue().getValue().toDouble());
		}
		return q;
	}

	@Override
	public void copyFrom(Resource res, boolean append) throws IOException {
		double start = SystemUtil.millis();
		try {
			if (res instanceof File && (!append || !this.isFile())) {
				try {
					Files.copy(((File) res).toPath(), this.toPath(), COPY_OPTIONS);
					return;
				}
				catch (Exception exception) {
				}
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
		finally {
			log(getAbsolutePath() + ":copyFrom(Resource res, boolean append)", start);
		}
	}

	@Override
	public void copyTo(Resource res, boolean append) throws IOException {
		double start = SystemUtil.millis();
		try {
			if (res instanceof File && (!append || !res.isFile())) {
				try {
					Files.copy(this.toPath(), ((File) res).toPath(), COPY_OPTIONS);
					return;
				}
				catch (Exception exception) {
				}
			}

			IOUtil.copy(this, res.getOutputStream(append), true);
			boolean e = canExecute();
			boolean w = canWrite();
			boolean r = canRead();

			if (e && res instanceof File) ((File) res).setExecutable(true);
			if (w != res.canWrite()) res.setWritable(w);
			if (r != res.canRead()) res.setReadable(r);
		}
		finally {
			log(getAbsolutePath() + ":copyTo(Resource res, boolean append)", start);
		}
	}

	@Override
	public Resource getAbsoluteResource() {
		double start = SystemUtil.millis();
		try {
			return new FileResource(provider, getAbsolutePath());
		}
		finally {
			log(getAbsolutePath() + ":getAbsoluteResource()", start);
		}
	}

	@Override
	public Resource getCanonicalResource() throws IOException {
		double start = SystemUtil.millis();
		try {
			return new FileResource(provider, getCanonicalPath());
		}
		finally {
			log(getAbsolutePath() + ":getCanonicalResource()", start);
		}
	}

	@Override
	public Resource getParentResource() {
		double start = SystemUtil.millis();
		try {
			String p = getParent();
			if (p == null) return null;
			return new FileResource(provider, p);
		}
		finally {
			log(getAbsolutePath() + ":getParentResource()", start);
		}
	}

	@Override
	public Resource[] listResources() {
		double start = SystemUtil.millis();
		try {
			String[] files = list();
			if (files == null) return null;

			Resource[] resources = new Resource[files.length];
			for (int i = 0; i < files.length; i++) {
				resources[i] = getRealResource(files[i]);
			}
			return resources;
		}
		finally {
			log(getAbsolutePath() + ":listResources()", start);
		}
	}

	@Override
	public String[] list(ResourceFilter filter) {
		double start = SystemUtil.millis();
		try {
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
		finally {
			log(getAbsolutePath() + ":list(ResourceFilter filter)", start);
		}
	}

	@Override
	public Resource[] listResources(ResourceFilter filter) {
		double start = SystemUtil.millis();
		try {
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
		finally {
			log(getAbsolutePath() + ":listResources(ResourceFilter filter)", start);
		}
	}

	@Override
	public String[] list(ResourceNameFilter filter) {
		double start = SystemUtil.millis();
		try {
			String[] files = list();
			if (files == null) return null;
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < files.length; i++) {
				if (filter.accept(this, files[i])) list.add(files[i]);
			}
			return list.toArray(new String[list.size()]);
		}
		finally {
			log(getAbsolutePath() + ":list(ResourceNameFilter filter)", start);
		}
	}

	@Override
	public Resource[] listResources(ResourceNameFilter filter) {
		double start = SystemUtil.millis();
		try {
			String[] files = list();
			if (files == null) return null;

			List<Resource> list = new ArrayList<Resource>();
			for (int i = 0; i < files.length; i++) {
				if (filter.accept(this, files[i])) list.add(getRealResource(files[i]));
			}
			return list.toArray(new Resource[list.size()]);
		}
		finally {
			log(getAbsolutePath() + ":listResources(ResourceNameFilter filter)", start);
		}
	}

	@Override
	public void moveTo(Resource dest) throws IOException {
		double start = SystemUtil.millis();
		try {
			if (this.equals(dest)) return;
			boolean done = false;
			if (dest instanceof File) {
				provider.lock(this);
				try {
					if (dest.exists() && !dest.delete())
						throw new IOException("Can't move file [" + this.getAbsolutePath() + "] cannot remove existing file [" + dest.getAbsolutePath() + "]");

					done = super.renameTo((File) dest);
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
		finally {
			log(getAbsolutePath() + ":moveTo(Resource dest)", start);
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		double start = SystemUtil.millis();
		try {
			provider.read(this);
			try {
				return new BufferedInputStream(Files.newInputStream(toPath(), StandardOpenOption.READ));
			}
			catch (NoSuchFileException nsfe) {
				throw ExceptionUtil.toFileNotFoundException(nsfe);
			}
			catch (IOException ioe) {
				throw ioe;
			}
		}
		finally {
			log(getAbsolutePath() + ":getInputStream()", start);
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		double start = SystemUtil.millis();
		try {
			return getOutputStream(false);
		}
		finally {
			log(getAbsolutePath() + ":getOutputStream()", start);
		}
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws IOException {
		double start = SystemUtil.millis();
		try {
			provider.lock(this);
			try {
				if (!super.exists() && !super.createNewFile()) {
					try {
						Files.createFile(toPath());
					}
					catch (FileAlreadyExistsException faee) {
					}
				}
				return new BufferedOutputStream(new ResourceOutputStream(this, new FileOutputStream(this, append)));
			}
			catch (IOException ioe) {
				provider.unlock(this);
				throw ioe;
			}
		}
		finally {
			log(getAbsolutePath() + ":getOutputStream(boolean append)", start);
		}
	}

	@Override
	public void createFile(boolean createParentWhenNotExists) throws IOException {
		double start = SystemUtil.millis();
		try {
			provider.lock(this);
			try {
				if (createParentWhenNotExists) {
					File p = super.getParentFile();
					if (!p.exists()) Files.createDirectories(p.toPath());
				}
				Files.createFile(toPath());
			}
			finally {
				provider.unlock(this);
			}
		}
		finally {
			log(getAbsolutePath() + ":createFile(boolean createParentWhenNotExists)", start);
		}
	}

	@Override
	public void remove(boolean alsoRemoveChildren) throws IOException {
		double start = SystemUtil.millis();
		try {
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
		finally {
			log(getAbsolutePath() + ":remove(boolean alsoRemoveChildren)", start);
		}
	}

	@Override
	public String getReal(String realpath) {
		double start = SystemUtil.millis();
		try {
			if (realpath.length() <= 2) {
				if (realpath.length() == 0) return getPath();
				if (realpath.equals(".")) return getPath();
				if (realpath.equals("..")) return getParent();
			}
			return new FileResource(provider, this, realpath).getPath();
		}
		finally {
			log(getAbsolutePath() + ":getReal(String realpath)", start);
		}
	}

	@Override
	public Resource getRealResource(String realpath) {
		double start = SystemUtil.millis();
		try {
			if (realpath.length() <= 2) {
				if (realpath.length() == 0) return this;
				if (realpath.equals(".")) return this;
				if (realpath.equals("..")) return getParentResource();
			}
			return new FileResource(provider, this, realpath);
		}
		finally {
			log(getAbsolutePath() + ":getRealResource(String realpath)", start);
		}
	}

	public ContentType getContentType() {
		double start = SystemUtil.millis();
		try {
			return ResourceUtil.getContentType(this);
		}
		finally {
			log(getAbsolutePath() + ":getContentType()", start);
		}
	}

	@Override
	public void createDirectory(boolean createParentWhenNotExists) throws IOException {
		double start = SystemUtil.millis();
		try {
			provider.lock(this);
			try {
				if (createParentWhenNotExists) Files.createDirectories(toPath());
				else Files.createDirectory(toPath());
			}
			finally {
				provider.unlock(this);
			}
		}
		finally {
			log(getAbsolutePath() + ":createDirectory(boolean createParentWhenNotExists)", start);
		}
	}

	@Override
	public ResourceProvider getResourceProvider() {
		double start = SystemUtil.millis();
		try {
			return provider;
		}
		finally {
			log(getAbsolutePath() + ":getResourceProvider()", start);
		}
	}

	@Override
	public boolean isReadable() {
		double start = SystemUtil.millis();
		try {
			return canRead();
		}
		finally {
			log(getAbsolutePath() + ":isReadable()", start);
		}
	}

	@Override
	public boolean isWriteable() {
		double start = SystemUtil.millis();
		try {
			return canWrite();
		}
		finally {
			log(getAbsolutePath() + ":isWriteable()", start);
		}
	}

	@Override
	public boolean renameTo(Resource dest) {
		double start = SystemUtil.millis();
		try {
			try {
				moveTo(dest);
				return true;
			}
			catch (IOException e) {
			}
			return false;
		}
		finally {
			log(getAbsolutePath() + ":renameTo(Resource dest)", start);
		}
	}

	@Override
	public boolean isArchive() {
		double start = SystemUtil.millis();
		try {
			return getAttribute(ATTRIBUTE_ARCHIVE);
		}
		finally {
			log(getAbsolutePath() + ":isArchive()", start);
		}
	}

	@Override
	public boolean isSystem() {
		double start = SystemUtil.millis();
		try {
			return getAttribute(ATTRIBUTE_SYSTEM);
		}
		finally {
			log(getAbsolutePath() + ":isSystem()", start);
		}
	}

	@Override
	public int getMode() {
		double start = SystemUtil.millis();
		try {
			if (!exists()) return 0;
			if (SystemUtil.isUnix()) {
				try {
					String line = Command.execute("ls -ld " + getPath(), false).getOutput();

					line = line.trim();
					line = line.substring(0, line.indexOf(' '));
					return ModeUtil.toOctalMode(line);

				}
				catch (Exception e) {
				}

			}
			int mode = SystemUtil.isWindows() && exists() ? 0111 : 0;
			if (super.canRead()) mode += 0444;
			if (super.canWrite()) mode += 0222;
			return mode;
		}
		finally {
			log(getAbsolutePath() + ":getMode()", start);
		}
	}

	public static int getMode(Path path) {
		double start = SystemUtil.millis();
		try {
			if (!Files.exists(path)) return 0;
			if (SystemUtil.isUnix()) {
				try {
					String line = Command.execute("ls -ld " + path.toAbsolutePath().toString(), false).getOutput();

					line = line.trim();
					line = line.substring(0, line.indexOf(' '));
					return ModeUtil.toOctalMode(line);

				}
				catch (Exception e) {
				}

			}
			int mode = SystemUtil.isWindows() ? 0111 : 0;
			if (Files.isReadable(path)) mode += 0444;
			if (Files.isWritable(path)) mode += 0222;
			return mode;
		}
		finally {
			log(path.toAbsolutePath().toString() + ":getMode(Path path)", start);
		}
	}

	@Override
	public void setMode(int mode) throws IOException {
		double start = SystemUtil.millis();
		try {
			if (!SystemUtil.isUnix()) return;
			provider.lock(this);
			try {
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
		finally {
			log(getAbsolutePath() + ":setMode(int mode)", start);
		}
	}

	@Override
	public void setArchive(boolean value) throws IOException {
		double start = SystemUtil.millis();
		try {
			setAttribute(ATTRIBUTE_ARCHIVE, value);
		}
		finally {
			log(getAbsolutePath() + ":setArchive(boolean value)", start);
		}
	}

	@Override
	public void setHidden(boolean value) throws IOException {
		double start = SystemUtil.millis();
		try {
			setAttribute(ATTRIBUTE_HIDDEN, value);
		}
		finally {
			log(getAbsolutePath() + ":setHidden(boolean value)", start);
		}
	}

	@Override
	public void setSystem(boolean value) throws IOException {
		double start = SystemUtil.millis();
		try {
			setAttribute(ATTRIBUTE_SYSTEM, value);
		}
		finally {
			log(getAbsolutePath() + ":setSystem(boolean value)", start);
		}
	}

	@Override
	public boolean setReadable(boolean value) {
		double start = SystemUtil.millis();
		try {
			if (!SystemUtil.isUnix()) return false;
			try {
				setMode(ModeUtil.setReadable(getMode(), value));
				return true;
			}
			catch (IOException e) {
				return false;
			}
		}
		finally {
			log(getAbsolutePath() + ":setReadable(boolean value)", start);
		}
	}

	@Override
	public boolean setWritable(boolean value) {
		double start = SystemUtil.millis();
		try {
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
		finally {
			log(getAbsolutePath() + ":setWritable(boolean value)", start);
		}
	}

	@Override
	public boolean createNewFile() {
		double start = SystemUtil.millis();
		try {
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
		finally {
			log(getAbsolutePath() + ":createNewFile()", start);
		}
	}

	@Override
	public boolean canRead() {
		double start = SystemUtil.millis();
		try {
			try {
				provider.read(this);
			}
			catch (IOException e) {
				return false;
			}
			return super.canRead();
		}
		finally {
			log(getAbsolutePath() + ":canRead()", start);
		}
	}

	@Override
	public boolean canWrite() {
		double start = SystemUtil.millis();
		try {
			try {
				provider.read(this);
			}
			catch (IOException e) {
				return false;
			}
			return super.canWrite();
		}
		finally {
			log(getAbsolutePath() + ":canWrite()", start);
		}
	}

	@Override
	public boolean delete() {
		double start = SystemUtil.millis();
		try {
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
		finally {
			log(getAbsolutePath() + ":delete()", start);
		}
	}

	@Override
	public boolean exists() {
		double start = SystemUtil.millis();
		try {
			try {
				provider.read(this);
			}
			catch (IOException e) {
			}
			return super.exists();
		}
		finally {
			log(getAbsolutePath() + ":exists()", start);
		}
	}

	@Override
	public boolean isAbsolute() {
		double start = SystemUtil.millis();
		try {
			try {
				provider.read(this);
			}
			catch (IOException e) {
				return false;
			}
			return super.isAbsolute();
		}
		finally {
			log(getAbsolutePath() + ":isAbsolute()", start);
		}
	}

	@Override
	public boolean isDirectory() {
		double start = SystemUtil.millis();
		try {
			try {
				provider.read(this);
			}
			catch (IOException e) {
				return false;
			}
			return super.isDirectory();
		}
		finally {
			log(getAbsolutePath() + ":isDirectory()", start);
		}
	}

	@Override
	public boolean isFile() {
		double start = SystemUtil.millis();
		try {
			try {
				provider.read(this);
			}
			catch (IOException e) {
				return false;
			}
			return super.isFile();
		}
		finally {
			log(getAbsolutePath() + ":isFile()", start);
		}
	}

	@Override
	public boolean isHidden() {
		double start = SystemUtil.millis();
		try {
			try {
				provider.read(this);
			}
			catch (IOException e) {
				return false;
			}
			return super.isHidden();
		}
		finally {
			log(getAbsolutePath() + ":isHidden()", start);
		}
	}

	@Override
	public long lastModified() {
		double start = SystemUtil.millis();
		try {
			try {
				provider.read(this);
			}
			catch (IOException e) {
				return 0;
			}
			return super.lastModified();
		}
		finally {
			log(getAbsolutePath() + ":lastModified()", start);
		}
	}

	@Override
	public long length() {
		double start = SystemUtil.millis();
		try {
			try {
				provider.read(this);
			}
			catch (IOException e) {
				return 0;
			}
			return super.length();
		}
		finally {
			log(getAbsolutePath() + ":length()", start);
		}
	}

	@Override
	public String[] list() {
		double start = SystemUtil.millis();
		try {
			try {
				provider.read(this);
			}
			catch (IOException e) {
				return null;
			}
			return super.list();
		}
		finally {
			log(getAbsolutePath() + ":list()", start);
		}
	}

	@Override
	public boolean mkdir() {
		double start = SystemUtil.millis();
		try {
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
		finally {
			log(getAbsolutePath() + ":mkdir()", start);
		}
	}

	@Override
	public boolean mkdirs() {
		double start = SystemUtil.millis();
		try {
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
		finally {
			log(getAbsolutePath() + ":mkdirs()", start);
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
		double start = SystemUtil.millis();
		try {
			try {
				provider.lock(this);
				return super.setLastModified(time);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				return false;
			}
			finally {
				provider.unlock(this);
			}
		}
		finally {
			log(getAbsolutePath() + ":setLastModified(long time)", start);
		}
	}

	@Override
	public boolean setReadOnly() {
		double start = SystemUtil.millis();
		try {
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
		finally {
			log(getAbsolutePath() + ":setReadOnly()", start);
		}
	}

	@Override
	public boolean getAttribute(short attribute) {
		double start = SystemUtil.millis();
		try {
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
		finally {
			log(getAbsolutePath() + ":getAttribute(short attribute)", start);
		}
	}

	@Override
	public void setAttribute(short attribute, boolean value) throws IOException {
		double start = SystemUtil.millis();
		try {
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
		finally {
			log(getAbsolutePath() + ":setAttribute(short attribute, boolean value)", start);
		}
	}

	@Override
	public boolean equals(Object other) {
		double start = SystemUtil.millis();
		try {
			if (provider.isCaseSensitive()) return super.equals(other);
			if (!(other instanceof File)) return false;
			return getAbsolutePath().equalsIgnoreCase(((File) other).getAbsolutePath());
		}
		finally {
			log(getAbsolutePath() + ":equals(Object other)", start);
		}
	}
}
