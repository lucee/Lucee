package lucee.commons.io.watch;

import java.io.File;

public class FileInfo {
	public long lastModified;
	public long length;

	public FileInfo(long lastModified, long length) {
		this.lastModified = lastModified;
		this.length = length;
	}

	public FileInfo(File file) {
		this.lastModified = file.lastModified();
		this.length = file.length();
	}
}
