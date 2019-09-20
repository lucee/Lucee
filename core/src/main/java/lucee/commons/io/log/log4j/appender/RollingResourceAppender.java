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
package lucee.commons.io.log.log4j.appender;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import lucee.commons.io.res.Resource;
import lucee.commons.io.retirement.RetireListener;

public class RollingResourceAppender extends ResourceAppender implements Appender {

	public static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;
	public static final int DEFAULT_MAX_BACKUP_INDEX = 10;

	protected final long maxFileSize;
	protected int maxBackupIndex;

	private long nextRollover = 0;

	/**
	 * Instantiate a FileAppender and open the file designated by <code>filename</code>. The opened
	 * filename will become the output destination for this appender.
	 * 
	 * <p>
	 * The file will be appended to.
	 */
	public RollingResourceAppender(Layout layout, Resource res, Charset charset, RetireListener listener) throws IOException {
		this(layout, res, charset, true, DEFAULT_MAX_FILE_SIZE, DEFAULT_MAX_BACKUP_INDEX, 60, listener);
	}

	/**
	 * Instantiate a RollingFileAppender and open the file designated by <code>filename</code>. The
	 * opened filename will become the output destination for this appender.
	 * 
	 * <p>
	 * If the <code>append</code> parameter is true, the file will be appended to. Otherwise, the file
	 * desginated by <code>filename</code> will be truncated before being opened.
	 */
	public RollingResourceAppender(Layout layout, Resource res, Charset charset, boolean append, RetireListener listener) throws IOException {
		this(layout, res, charset, append, DEFAULT_MAX_FILE_SIZE, DEFAULT_MAX_BACKUP_INDEX, 60, listener);
	}

	/**
	 * Instantiate a RollingFileAppender and open the file designated by <code>filename</code>. The
	 * opened filename will become the output destination for this appender.
	 * 
	 * <p>
	 * If the <code>append</code> parameter is true, the file will be appended to. Otherwise, the file
	 * desginated by <code>filename</code> will be truncated before being opened.
	 */
	public RollingResourceAppender(Layout layout, Resource res, Charset charset, boolean append, long maxFileSize, int maxBackupIndex, int timeout, RetireListener listener)
			throws IOException {
		super(layout, res, charset, append, timeout, listener);
		this.maxFileSize = maxFileSize;
		this.maxBackupIndex = maxBackupIndex;
	}

	/**
	 * Returns the value of the <b>MaxBackupIndex</b> option.
	 */
	public int getMaxBackupIndex() {
		return maxBackupIndex;
	}

	/**
	 * Get the maximum size that the output file is allowed to reach before being rolled over to backup
	 * files.
	 * 
	 * @since 1.1
	 */
	public long getMaximumFileSize() {
		return maxFileSize;
	}

	/**
	 * Implements the usual roll over behavior.
	 * 
	 * <p>
	 * If <code>MaxBackupIndex</code> is positive, then files {<code>File.1</code>, ...,
	 * <code>File.MaxBackupIndex -1</code>} are renamed to {<code>File.2</code>, ...,
	 * <code>File.MaxBackupIndex</code>}. Moreover, <code>File</code> is renamed <code>File.1</code> and
	 * closed. A new <code>File</code> is created to receive further log output.
	 * 
	 * <p>
	 * If <code>MaxBackupIndex</code> is equal to zero, then the <code>File</code> is truncated with no
	 * backup files created.
	 * 
	 */
	public void rollOver() {
		Resource target;
		Resource file;

		if (qw != null) {
			long size = ((CountingQuietWriter) qw).getCount();
			LogLog.debug("rolling over count=" + size);
			// if operation fails, do not roll again until
			// maxFileSize more bytes are written
			nextRollover = size + maxFileSize;
		}
		LogLog.debug("maxBackupIndex=" + maxBackupIndex);

		boolean renameSucceeded = true;
		Resource parent = res.getParentResource();

		// If maxBackups <= 0, then there is no file renaming to be done.
		if (maxBackupIndex > 0) {
			// Delete the oldest file, to keep Windows happy.
			file = parent.getRealResource(res.getName() + "." + maxBackupIndex + ".bak");

			if (file.exists()) renameSucceeded = file.delete();

			// Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex, ..., 3, 2}
			for (int i = maxBackupIndex - 1; i >= 1 && renameSucceeded; i--) {
				file = parent.getRealResource(res.getName() + "." + i + ".bak");
				if (file.exists()) {
					target = parent.getRealResource(res.getName() + "." + (i + 1) + ".bak");
					LogLog.debug("Renaming file " + file + " to " + target);
					renameSucceeded = file.renameTo(target);
				}
			}

			if (renameSucceeded) {
				// Rename fileName to fileName.1
				target = parent.getRealResource(res.getName() + ".1.bak");

				this.closeFile(); // keep windows happy.

				file = res;
				LogLog.debug("Renaming file " + file + " to " + target);
				renameSucceeded = file.renameTo(target);

				// if file rename failed, reopen file with append = true

				if (!renameSucceeded) {
					try {
						this.setFile(true);
					}
					catch (IOException e) {
						LogLog.error("setFile(" + res + ", true) call failed.", e);
					}
				}
			}
		}

		// if all renames were successful, then

		if (renameSucceeded) {
			try {
				// This will also close the file. This is OK since multiple
				// close operations are safe.
				this.setFile(false);
				nextRollover = 0;
			}
			catch (IOException e) {
				LogLog.error("setFile(" + res + ", false) call failed.", e);
			}
		}
	}

	@Override
	public synchronized void setFile(boolean append) throws IOException {
		long len = res.length();// this is done here, because in the location used the file is already locked
		super.setFile(append);
		if (append) {
			((CountingQuietWriter) qw).setCount(len);
		}
	}

	/**
	 * Set the maximum number of backup files to keep around.
	 * 
	 * <p>
	 * The <b>MaxBackupIndex</b> option determines how many backup files are kept before the oldest is
	 * erased. This option takes a positive integer value. If set to zero, then there will be no backup
	 * files and the log file will be truncated when it reaches <code>MaxFileSize</code>.
	 */
	public void setMaxBackupIndex(int maxBackups) {
		this.maxBackupIndex = maxBackups;
	}

	@Override
	protected void setQWForFiles(Writer writer) {
		this.qw = new CountingQuietWriter(writer, errorHandler);
	}

	/**
	 * This method differentiates RollingFileAppender from its super class.
	 * 
	 * @since 0.9.0
	 */
	@Override
	protected void subAppend(LoggingEvent event) {
		super.subAppend(event);
		if (res != null && qw != null) {
			long size = ((CountingQuietWriter) qw).getCount();
			if (size >= maxFileSize && size >= nextRollover) {
				rollOver();
			}
		}
	}
}