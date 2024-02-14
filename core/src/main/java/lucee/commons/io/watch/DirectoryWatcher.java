package lucee.commons.io.watch;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.print;

public class DirectoryWatcher {

	private File root;
	private long lastModified;

	private Map<File, FileInfo> directories = new ConcurrentHashMap<>();
	private Map<File, FileInfo> files = new ConcurrentHashMap<>();
	private long pause;

	public DirectoryWatcher(File root, long pause) {
		this.root = root;
		this.pause = pause;
		collect(directories, files, root);
		this.lastModified = System.currentTimeMillis();

		execute();
	}

	private void execute() {
		while (true) {
			long start = System.currentTimeMillis();
			boolean cleanup = false;
			for (File d: directories.keySet()) {
				// directory change
				if (lastModified < d.lastModified()) {
					print.e("directory changed!");
					print.e(d);
					recollect(directories, files, d);
					lastModified = d.lastModified();
					cleanup = true;
				}
			}
			if (cleanup) {
				// List<File> mods = new ArrayList<File>();
				for (File d: files.keySet()) {
					if (!d.exists()) {
						print.e("file removed!");
						print.e(d);
						files.remove(d);
						// mods.add(d);
					}
				}
				/*
				 * if (mods.size() > 0) { for (File m: mods) { files.remove(m); } }
				 */
			}
			for (File d: files.keySet()) {
				if (lastModified < d.lastModified()) {
					print.e("file changed!");
					print.e(d);
					files.put(d, new FileInfo(d));
					lastModified = d.lastModified();
				}
			}

			print.e(directories.size() + ":" + files.size() + " ->" + (System.currentTimeMillis() - start));
			try {
				Thread.sleep(pause);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void collect(Map<File, FileInfo> directories, Map<File, FileInfo> files, File dir) {
		directories.put(dir, new FileInfo(dir));

		for (File f: dir.listFiles()) {
			if (f.isDirectory()) collect(directories, files, f);
			else if (f.getName().endsWith(".cfm") || f.getName().endsWith(".cfc")) files.put(f, new FileInfo(f));
		}
	}

	private void recollect(Map<File, FileInfo> directories, Map<File, FileInfo> files, File dir) {
		FileInfo fi = directories.get(dir);
		if (fi == null) {
			print.e("new directory!");
			print.e(dir);
			directories.put(dir, new FileInfo(dir));
		}
		else if (fi.lastModified != dir.lastModified()) {
			print.e("directory changed (timestamp)");
			print.e(dir);
			directories.put(dir, new FileInfo(dir));
		}
		else if (fi.length != dir.length()) {
			print.e("directory changed (length)");
			print.e(dir);
			directories.put(dir, new FileInfo(dir));
		}

		for (File f: dir.listFiles()) {
			if (f.isDirectory()) recollect(directories, files, f);
			else if (f.getName().endsWith(".cfm") || f.getName().endsWith(".cfc")) {
				fi = files.get(f);
				if (fi == null) {
					print.e("new file!!!");
					print.e(f);
					files.put(f, new FileInfo(f));
				}
				else if (fi.lastModified != f.lastModified()) {
					print.e("file changed (timestamp)");
					print.e(f);
					files.put(f, new FileInfo(f));
				}
				else if (fi.length != f.length()) {
					print.e("file changed (length)");
					print.e(f);
					files.put(f, new FileInfo(f));
				}

			}
		}
	}

	public static void main(String[] args) {
		File root = new File("/Users/mic/Test/test-cfconfig/webapps/ROOT/");
		root = new File("/Users/mic/Projects/Distrokid/distrokid");
		new DirectoryWatcher(root, 5000L);
	}

}
