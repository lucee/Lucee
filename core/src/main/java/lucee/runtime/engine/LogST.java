package lucee.runtime.engine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;

public class LogST extends Thread {

	private static final char NL = '\n';
	private Thread thread;
	private long size = 0;
	private long max = 1024 * 1024 * 100;
	private final File logDirectory;
	private final String logName;
	private final int timeRange;

	/*
	 * public static void main(String[] args) throws InterruptedException {
	 * 
	 * print.e("----------- start ------------"); LogST log = new LogST(Thread.currentThread());
	 * log.start(); log.join(); print.e("----------- stop ------------"); }
	 */

	public LogST(Thread thread, File logDirectory, String logName, int timeRange) {
		this.thread = thread;
		this.logDirectory = logDirectory;
		this.logName = logName;
		this.timeRange = timeRange;
		if (timeRange < 1) throw new RuntimeException("time range " + timeRange + " is invalid.");
	}

	@Override
	public void run() {
		PrintStream ps = null;
		try {
			ps = new PrintStream(createFile());
			while (true) {
				printStackTrace(ps, thread.getStackTrace());
				SystemUtil.wait(this, timeRange);
				if (size > max) {
					IOUtil.close(ps);
					ps = new PrintStream(createFile());
					size = 0;
				}
			}
		}
		catch (IOException e) {
			LogUtil.log(ThreadLocalPageContext.getConfig(), LogST.class.getName(), e);
		}
		finally {
			try {
				IOUtil.close(ps);
			}
			catch (IOException e) {
				LogUtil.log(ThreadLocalPageContext.getConfig(), LogST.class.getName(), e);
			}

		}
	}

	private File createFile() throws IOException {
		File f;
		int count = 0;
		while ((f = new File(logDirectory, logName + "-" + (++count) + ".log")).isFile()) {

		}
		return f;
	}

	private void printStackTrace(PrintStream ps, StackTraceElement[] trace) {

		{
			String line;
			// Print our stack trace
			String head = System.currentTimeMillis() + "\n";
			ps.print(head);
			size += head.length();
			for (StackTraceElement traceElement: trace) {
				line = "\tat " + traceElement + "\n";
				ps.print(line);
				size += line.length();
			}
			ps.print(NL);
			ps.flush();
			size += 1;

		}
	}

	public static void _do(File logDirectory) {
		_do(logDirectory, "stacktrace", 10);
	}

	public static void _do(File logDirectory, String logName) {
		_do(logDirectory, logName, 10);
	}

	public static void _do(File logDirectory, String logName, int timeRange) {

		LogST log = new LogST(Thread.currentThread(), logDirectory, logName, timeRange);
		log.start();
		// log.join();
	}
}
