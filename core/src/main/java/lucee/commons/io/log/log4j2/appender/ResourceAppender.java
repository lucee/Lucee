package lucee.commons.io.log.log4j2.appender;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.retirement.RetireListener;
import lucee.commons.io.retirement.RetireOutputStream;
import lucee.commons.lang.StringUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;

public final class ResourceAppender extends AbstractAppender {

	public static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;
	public static final int DEFAULT_MAX_BACKUP_INDEX = 10;

	private final long maxFileSize;
	private final int maxfiles;

	private final Resource res;
	private final Charset charset;
	private final boolean append;
	private final int timeout;
	private final RetireListener listener;
	private OutputStreamWriter writer = null;
	private String token;

	private long size = 0;

	public ResourceAppender(String name, Filter filter, Layout layout, Resource res, Charset charset, boolean append, int timeout, long maxFileSize, int maxfiles,
			RetireListener listener) {
		super(name, filter, layout);
		this.res = res;
		this.charset = charset;
		this.append = append;
		this.timeout = timeout;
		this.listener = listener;
		this.maxFileSize = maxFileSize;
		this.maxfiles = maxfiles;
		this.token = SystemUtil.createToken("ResourceAppender", res.getAbsolutePath());

	}

	@Override
	public void append(LogEvent event) {
		try {
			setFile(append, true);
		}
		catch (IOException ioe) {
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "Unable to write to" + res, ioe);
		}

		start();
		// check file length
		if (size > maxFileSize) {
			synchronized (token) {
				if (size > maxFileSize) { // we do not trust size to much because of multi threading issues we do not avoid setting this var
					try {
						rollOver();
					}
					catch (IOException e) {
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "Log rollover failed for [" + res + "]", e);
					}
				}
			}
		}

		try {
			final String str = Caster.toString(getLayout().toSerializable(event));
			if (!StringUtil.isEmpty(str)) {
				synchronized (token) {
					if (!StringUtil.isEmpty(str)) {
						try {
							if (writer == null) setFile(append, true);
							writer.write(str);
							writer.flush();
							size += str.length();
						}
						catch (IOException ioe) {
							LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "Unable to write to" + res, ioe);
							closeFile();
							setFile(append, true);
							writer.write(str);
							writer.flush();
							size += str.length();
						}
					}
				}
			}
		}
		catch (Exception e) {
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "Unable to write to log [" + res + "]", e);
			synchronized (token) {
				closeFile();
			}
		}
	}

	@Override
	public void stop() {
		closeFile();
		super.stop();
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
	 * @param event
	 * 
	 * @param fileName The path to the log file.
	 * @param append If true will append to fileName. Otherwise will truncate fileName.
	 */
	private void setFile(boolean append, boolean onlyWhenNull) throws IOException {
		if (!onlyWhenNull || writer == null) {
			synchronized (token) {
				if (!onlyWhenNull || writer == null) {
					closeFile();
					Resource parent = res.getParentResource();
					if (!parent.exists()) parent.createDirectory(true);
					boolean writeHeader = !append || res.length() == 0;// this must happen before we open the stream
					size = res.length();
					writer = new OutputStreamWriter(new RetireOutputStream(res, append, timeout, listener), charset);
					if (writeHeader) {
						byte[] layoutHeader = getLayout().getHeader();
						if (layoutHeader != null){
							String header = new String(layoutHeader, charset);
							size += header.length();
							writer.write(header);
							writer.flush();
						}
						// TODO new line?
					}
				}
			}
		}
	}

	private void rollOver() throws IOException {
		synchronized (token) {
			setFile(append, true);
			String footer = new String(getLayout().getFooter(), charset);
			size += footer.length();
			writer.write(footer);
			closeFile();

			Resource target;
			Resource file;

			boolean renameSucceeded = true;
			Resource parent = res.getParentResource();

			// If maxBackups <= 0, then there is no file renaming to be done.
			if (maxfiles > 0) {
				// Delete the oldest file, to keep Windows happy.
				file = parent.getRealResource(res.getName() + "." + maxfiles + ".bak");

				if (file.exists()) renameSucceeded = file.delete();

				// Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex, ..., 3, 2}
				for (int i = maxfiles - 1; i >= 1 && renameSucceeded; i--) {
					file = parent.getRealResource(res.getName() + "." + i + ".bak");
					if (file.exists()) {
						target = parent.getRealResource(res.getName() + "." + (i + 1) + ".bak");
						renameSucceeded = file.renameTo(target);
					}
				}

				if (renameSucceeded) {
					// Rename fileName to fileName.1
					target = parent.getRealResource(res.getName() + ".1.bak");

					file = res;
					renameSucceeded = file.renameTo(target);

					// if file rename failed, reopen file with append = true

					if (!renameSucceeded) {
						try {
							this.setFile(true, false);
						}
						catch (IOException e) {
							LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "setFile([" + res + "], true) call failed.", e);
						}
					}
				}
			}

			// if all renames were successful, then

			if (renameSucceeded) {
				try {
					// This will also close the file. This is OK since multiple
					// close operations are safe.
					this.setFile(false, false);
				}
				catch (IOException e) {
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "setFile([" + res + "], false) call failed.", e);
				}
			}
		}
	}

	private void closeFile() {
		size = 0;
		if (writer != null) {
			synchronized (token) {
				if (writer != null) {

					try {
						writer.flush();
						writer.close();
						writer = null;
					}
					catch (java.io.IOException e) {
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), "log-loading", "Could not close [" + res + "]", e);
					}
				}
			}
		}
	}
}
