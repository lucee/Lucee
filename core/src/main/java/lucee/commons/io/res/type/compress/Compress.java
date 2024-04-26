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
package lucee.commons.io.res.type.compress;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.compress.CompressUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;

public final class Compress {

	public static final int FORMAT_ZIP = CompressUtil.FORMAT_ZIP;
	public static final int FORMAT_TAR = CompressUtil.FORMAT_TAR;
	public static final int FORMAT_TGZ = CompressUtil.FORMAT_TGZ;
	public static final int FORMAT_TBZ2 = CompressUtil.FORMAT_TBZ2;

	private static final Map<String, SoftReference<Compress>> compressResources = new ConcurrentHashMap<String, SoftReference<Compress>>();
	private static final long CHECK_TIMEOUT = 5000;
	private static final long ONE_HOUR = 60L * 60L * 1000L;

	// private final static Map files=new WeakHashMap();

	private final Resource ffile;
	// private ResourceProvider ramProvider;
	private long syn = -1;
	private Resource root;
	private Synchronizer synchronizer;
	private long lastModified = -1;
	private long lastCheck = -1;

	private int format;
	private int mode;
	private boolean caseSensitive;
	private Resource temp;
	private long length;

	/**
	 * private Constructor of the class, will be invoked be getInstance
	 * 
	 * @param file
	 * @param format
	 * @param caseSensitive
	 * @throws IOException
	 */
	private Compress(Resource file, int format, boolean caseSensitive) throws IOException {
		this.ffile = file;
		this.format = format;
		this.mode = ffile.getMode();
		if (mode == 0) mode = 0777;
		load(this.caseSensitive = caseSensitive);
	}

	/**
	 * return zip instance matching the zipfile, singelton instance only 1 zip for one file
	 * 
	 * @param zipFile
	 * @param format
	 * @param caseSensitive
	 * @return
	 * @throws IOException
	 */
	public static Compress getInstance(Resource zipFile, int format, boolean caseSensitive) throws IOException {
		String key = zipFile.getAbsolutePath() + ":" + caseSensitive;
		SoftReference<Compress> tmp = compressResources.get(key);
		Compress compress = tmp == null ? null : tmp.get();
		if (compress == null) {
			synchronized (SystemUtil.createToken("compress", key)) {
				tmp = compressResources.get(key);
				compress = tmp == null ? null : tmp.get();
				if (compress == null) {
					compress = new Compress(zipFile, format, caseSensitive);
					compressResources.put(key, new SoftReference<Compress>(compress));
				}
			}
		}
		return compress;
	}

	private void load(boolean caseSensitivex) {

		long lastModified = ffile.lastModified();
		long length = ffile.length();
		if (root == null || !root.exists() || this.lastModified != lastModified || this.length != length) {
			String key = ffile.getAbsolutePath() + ":" + caseSensitive;
			synchronized (SystemUtil.createToken("compress", key)) {

				if (root == null || !root.exists() || (Math.max(this.lastModified, lastModified) - Math.min(this.lastModified, lastModified)) > 1000 || this.length != length) {
					Map<String, Boolean> args = new HashMap<String, Boolean>();
					args.put("case-sensitive", Caster.toBoolean(caseSensitive));
					if (temp == null) {
						String cid = "";
						Config config = ThreadLocalPageContext.getConfig();
						if (config != null) {
							cid = config.getIdentification().getId();
							temp = config.getTempDirectory();
						}
						if (temp == null) temp = SystemUtil.getTempDirectory();

						temp = temp.getRealResource("compress");
						temp = temp.getRealResource(HashUtil.create64BitHashAsString(cid + "-" + key, Character.MAX_RADIX));
						if (!temp.exists()) temp.mkdirs();
					}

					// remove all old dumps
					String name = HashUtil.create64BitHashAsString(Caster.toString(lastModified / 1000L) + ":" + Caster.toString(ffile.length()), Character.MAX_RADIX);
					root = temp.getRealResource(name);
					if ((lastModified / 1000L) > 0L && root.exists()) return;
					Resource[] old = temp.listResources();
					root.mkdirs();
					if (ffile.exists()) {
						try {
							CompressUtil.extract(format, ffile, root);
						}
						catch (IOException e) {
							LogUtil.log("compress", e);
						}
					}
					else {
						try {
							ffile.createFile(false);
						}
						catch (IOException e) {
						}
					}
					// remove all the old extracts
					if (old != null) {
						long olderThan = System.currentTimeMillis() + ONE_HOUR;
						for (Resource r: old) {
							ResourceUtil.deleteFileOlderThan(r, olderThan, null);
						}
					}
					this.lastModified = lastModified;
					this.length = length;
				}
			}
		}
	}

	public Resource getRamProviderResource(String path) {
		long current = System.currentTimeMillis();
		if (current > lastCheck + CHECK_TIMEOUT) {
			lastCheck = current;
			load(caseSensitive);
		}
		return root.getRealResource(path);
	}

	/**
	 * @return the zipFile
	 */
	public Resource getCompressFile() {
		return ffile;
	}

	public synchronized void synchronize(boolean async) {
		if (!async) {
			doSynchronize();
			return;
		}
		syn = System.currentTimeMillis();
		if (synchronizer == null || !synchronizer.isRunning()) {
			synchronizer = new Synchronizer(this, 100);
			synchronizer.start();
		}
	}

	private void doSynchronize() {
		try {
			CompressUtil.compress(format, root.listResources(), ffile, 777);
			// ramProvider=null;
		}
		catch (IOException e) {
		}
	}

	class Synchronizer extends Thread {
		private Compress zip;
		private int interval;
		private boolean running = true;

		public Synchronizer(Compress zip, int interval) {
			this.zip = zip;
			this.interval = interval;
		}

		@Override
		public void run() {
			if (FORMAT_TAR == format) runTar(ffile);
			if (FORMAT_TGZ == format) runTGZ(ffile);
			else runZip(ffile);

		}

		private void runTGZ(Resource res) {
			GZIPOutputStream gos = null;
			InputStream tmpis = null;
			Resource tmp = SystemUtil.getTempDirectory().getRealResource(System.currentTimeMillis() + "_.tgz");
			try {
				gos = new GZIPOutputStream(res.getOutputStream());
				// wait for sync
				while (true) {
					sleepEL();
					if (zip.syn + interval <= System.currentTimeMillis()) break;
				}
				// sync
				tmpis = tmp.getInputStream();
				CompressUtil.compressTar(root.listResources(), tmp, -1);
				CompressUtil.compressGZip(tmpis, gos);
			}
			catch (IOException e) {
			}
			finally {
				IOUtil.closeEL(gos);
				IOUtil.closeEL(tmpis);
				tmp.delete();
				running = false;
			}
		}

		private void runTar(Resource res) {
			TarArchiveOutputStream tos = null;
			try {
				tos = new TarArchiveOutputStream(res.getOutputStream());
				tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
				// wait for sync
				while (true) {
					sleepEL();
					if (zip.syn + interval <= System.currentTimeMillis()) break;
				}
				// sync
				CompressUtil.compressTar(root.listResources(), tos, -1);
			}
			catch (IOException e) {
			}
			finally {
				IOUtil.closeEL(tos);
				running = false;
			}
		}

		private void runZip(Resource res) {
			ZipOutputStream zos = null;
			try {
				zos = new ZipOutputStream(res.getOutputStream());
				// wait for sync
				while (true) {
					sleepEL();
					if (zip.syn + interval <= System.currentTimeMillis()) break;
				}
				// sync
				CompressUtil.compressZip(root.listResources(), zos, null);
			}
			catch (IOException e) {
			}
			finally {
				IOUtil.closeEL(zos);
				running = false;
			}
		}

		private void sleepEL() {
			try {
				sleep(interval);
			}
			catch (InterruptedException e) {
			}
		}

		public boolean isRunning() {
			return running;
		}
	}
}