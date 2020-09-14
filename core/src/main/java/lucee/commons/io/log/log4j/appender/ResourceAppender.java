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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.QuietWriter;

import lucee.commons.io.res.Resource;
import lucee.commons.io.retirement.RetireListener;
import lucee.commons.io.retirement.RetireOutputStream;
import lucee.commons.lang.SerializableObject;

public class ResourceAppender extends WriterAppender implements AppenderState, Appender {

	private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

	/**
	 * Controls file truncation. The default value for this variable is <code>true</code>, meaning that
	 * by default a <code>FileAppender</code> will append to an existing file and not truncate it.
	 *
	 * <p>
	 * This option is meaningful only if the FileAppender opens the file.
	 */
	protected final boolean fileAppend;

	/**
	 * The name of the log file.
	 */
	protected final Resource res;

	/**
	 * Do we do bufferedIO?
	 */
	protected final boolean bufferedIO;

	/**
	 * Determines the size of IO buffer be. Default is 8K.
	 */
	protected final int bufferSize;

	private final int timeout;

	private final RetireListener listener;

	private Object sync = new SerializableObject();

	/**
	 * Instantiate a FileAppender and open the file designated by <code>filename</code>. The opened
	 * filename will become the output destination for this appender.
	 * 
	 * <p>
	 * The file will be appended to.
	 */
	public ResourceAppender(Layout layout, Resource res, Charset charset, RetireListener listener) throws IOException {
		this(layout, res, charset, true, true, 60/* a minute */, DEFAULT_BUFFER_SIZE, listener);
	}

	/**
	 * Instantiate a FileAppender and open the file designated by <code>filename</code>. The opened
	 * filename will become the output destination for this appender.
	 * 
	 * <p>
	 * If the <code>append</code> parameter is true, the file will be appended to. Otherwise, the file
	 * designated by <code>filename</code> will be truncated before being opened.
	 */
	public ResourceAppender(Layout layout, Resource res, Charset charset, boolean append, RetireListener listener) throws IOException {
		this(layout, res, charset, append, true, 60/* a minute */, DEFAULT_BUFFER_SIZE, listener);
	}

	public ResourceAppender(Layout layout, Resource res, Charset charset, boolean append, int timeout, RetireListener listener) throws IOException {
		this(layout, res, charset, append, true, timeout, DEFAULT_BUFFER_SIZE, listener);
	}

	/**
	 * Instantiate a <code>FileAppender</code> and open the file designated by <code>filename</code>.
	 * The opened filename will become the output destination for this appender.
	 * 
	 * <p>
	 * If the <code>append</code> parameter is true, the file will be appended to. Otherwise, the file
	 * designated by <code>filename</code> will be truncated before being opened.
	 * 
	 * <p>
	 * If the <code>bufferedIO</code> parameter is <code>true</code>, then buffered IO will be used to
	 * write to the output file.
	 * 
	 */
	public ResourceAppender(Layout layout, Resource res, Charset charset, boolean append, boolean bufferedIO, int timeout, int bufferSize, RetireListener listener)
			throws IOException {
		this.layout = layout;
		this.bufferedIO = bufferedIO;
		this.bufferSize = bufferSize;
		this.timeout = timeout;
		this.fileAppend = append;
		this.res = res;
		this.listener = listener;
		setEncoding(charset.name());
		this.setFile(append);
	}

	/**
	 * Returns the value of the <b>Append</b> option.
	 */
	public boolean getAppend() {
		return fileAppend;
	}

	/** Returns the value of the <b>File</b> option. */
	public Resource getResource() {
		return res;
	}

	/**
	 * If the value of <b>File</b> is not <code>null</code>, then {@link #setFile} is called with the
	 * values of <b>File</b> and <b>Append</b> properties.
	 * 
	 * @since 0.8.1
	 */

	/**
	 * Closes the previously opened file.
	 */
	protected void closeFile() {
		if (this.qw != null) {
			try {
				this.qw.close();
			}
			catch (java.io.IOException e) {
				// Exceptionally, it does not make sense to delegate to an
				// ErrorHandler. Since a closed appender is basically dead.
				LogLog.error("Could not close " + qw, e);
			}
		}
	}

	/**
	 * Get the value of the <b>BufferedIO</b> option.
	 * 
	 * <p>
	 * BufferedIO will significantly increase performance on heavily loaded systems.
	 * 
	 */
	public boolean getBufferedIO() {
		return this.bufferedIO;
	}

	/**
	 * Get the size of the IO buffer.
	 */
	public int getBufferSize() {
		return this.bufferSize;
	}

	/**
	 * <p>
	 * Sets and <i>opens</i> the file where the log output will go. The specified file must be writable.
	 * 
	 * <p>
	 * If there was already an opened file, then the previous file is closed first.
	 * 
	 * <p>
	 * <b>Do not use this method directly. To configure a FileAppender or one of its subclasses, set its
	 * properties one by one and then call activateOptions.</b>
	 * 
	 * @param fileName The path to the log file.
	 * @param append If true will append to fileName. Otherwise will truncate fileName.
	 */
	protected void setFile(boolean append) throws IOException {
		synchronized (sync) {
			LogLog.debug("setFile called: " + res + ", " + append);

			// It does not make sense to have immediate flush and bufferedIO.
			if (bufferedIO) {
				setImmediateFlush(false);
			}

			reset();
			Resource parent = res.getParentResource();
			if (!parent.exists()) parent.createDirectory(true);
			boolean writeHeader = !append || res.length() == 0;// this must happen before we open the stream
			Writer fw = createWriter(new RetireOutputStream(res, append, timeout, listener));
			if (bufferedIO) {
				fw = new BufferedWriter(fw, bufferSize);
			}
			this.setQWForFiles(fw);
			if (writeHeader) writeHeader();
			LogLog.debug("setFile ended");
		}
	}

	/**
	 * Sets the quiet writer being used.
	 * 
	 * This method is overridden by {@link RollingFileAppender}.
	 */
	protected void setQWForFiles(Writer writer) {
		this.qw = new QuietWriter(writer, errorHandler);
	}

	/**
	 * Close any previously opened file and call the parent's <code>reset</code>.
	 */
	@Override
	protected void reset() {
		closeFile();
		super.reset();
	}

	@Override
	public boolean isClosed() {
		return closed;
	}
}