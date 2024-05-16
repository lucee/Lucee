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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import lucee.commons.digest.MD5;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.compress.CompressUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.type.ram.RamResourceProviderOld;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;

public final class Compress {

	public static final int FORMAT_ZIP = CompressUtil.FORMAT_ZIP;
	public static final int FORMAT_TAR = CompressUtil.FORMAT_TAR;
	public static final int FORMAT_TGZ = CompressUtil.FORMAT_TGZ;
	public static final int FORMAT_TBZ2 = CompressUtil.FORMAT_TBZ2;

	// private final static Map files=new WeakHashMap();

	private final Resource ffile;
	// private ResourceProvider ramProvider;
	private long syn = -1;
	private Resource root;
	private Synchronizer synchronizer;
	private long lastMod = -1;
	private long lastCheck = -1;

	private int format;
	private int mode;
	private boolean caseSensitive;
	private Resource temp;

	/**
	 * private Constructor of the class, will be invoked be getInstance
	 * 
	 * @param file
	 * @param format
	 * @param caseSensitive
	 * @throws IOException
	 */
	public Compress(Resource file, int format, boolean caseSensitive) throws IOException {
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
		ConfigPro config = (ConfigPro) ThreadLocalPageContext.getConfig();
		return config.getCompressInstance(zipFile, format, caseSensitive);
	}

	private synchronized void load(boolean caseSensitive) throws IOException {
		long actLastMod = ffile.lastModified();
		lastMod = actLastMod;
		lastCheck = System.currentTimeMillis();
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
			temp = temp.getRealResource(MD5.getDigestAsString(cid + "-" + ffile.getAbsolutePath()));
			if (!temp.exists()) temp.mkdirs();
		}

		if (temp != null) {
			String name = Caster.toString(actLastMod) + ":" + Caster.toString(ffile.length());
			name = MD5.getDigestAsString(name, name);
			root = temp.getRealResource(name);
			if (actLastMod > 0 && root.exists()) return;

			ResourceUtil.removeChildrenEL(temp);
			// if(root!=null)ResourceUtil.removeChildrenEL(root);
			// String name=CreateUUID.invoke();
			// root=temp.getRealResource(name);
			root.mkdirs();
		}
		else {
			ResourceProvider ramProvider = new RamResourceProviderOld().init("ram", args);
			root = ramProvider.getResource("/");
		}
		_load();
	}

	private void _load() {
		if (ffile.exists()) {
			try {
				CompressUtil.extract(format, ffile, root);
			}
			catch (IOException e) {
			}
		}
		else {
			try {
				ffile.createFile(false);
			}
			catch (IOException e) {
			}
			lastMod = ffile.lastModified();
		}
	}

	public Resource getRamProviderResource(String path) throws IOException {
		long t = System.currentTimeMillis();
		if (t > lastCheck + 2000) {

			lastCheck = t;
			t = ffile.lastModified();
			if ((lastMod - t) > 10 || (t - lastMod) > 10 || root == null || !root.exists()) {
				lastMod = t;
				load(caseSensitive);
			}
		}
		return root.getRealResource(path);// ramProvider.getResource(path);
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