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

package lucee.runtime.tag;

import lucee.commons.cli.Command;
import lucee.commons.cli.CommandResult;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;
import lucee.runtime.process.UDFProcessListener;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;

/*
 * Execute external processes. The worker runs on a virtual thread when virtual threads are allowed
 * (see ThreadUtil.ALLOW_VIRTUAL_THREADS), otherwise on a daemon platform thread.
 */
public final class _Execute implements Runnable {

	private final PageContext pageContext;
	private volatile Thread thread;
	// the thread that started the worker and that the PageContext belongs to
	private volatile Thread caller;

	private Resource outputfile;
	private Resource errorFile;
	private String variable;
	private String errorVariable;
	private String resultVariable;
	private String exitCodeVariable;

	private volatile boolean aborted;
	private String[] commands;
	// private static final int BLOCK_SIZE=4096;
	private Object monitor;
	private volatile Exception exception;
	// private String body;
	private volatile boolean finished;
	private volatile Process process;

	private String directory;
	private Struct environment;

	private UDF onError;
	private UDF onProgress;
	private long timeout;

	/**
	 * Constructor: Execute external processes
	 * 
	 * @param pageContext
	 * @param monitor
	 * @param commands
	 * @param outputfile
	 * @param variable
	 * @param errorFile
	 * @param errorVariable
	 * @param directory
	 * @param environment
	 * @param resultVariable
	 * @param exitCodeVariable
	 * @param timeout
	 */
	public _Execute(PageContext pageContext, Object monitor, String[] commands, Resource outputfile, String variable, Resource errorFile, String errorVariable, String directory,
			Struct environment, String resultVariable, String exitCodeVariable, UDF onProgress, UDF onError, long timeout) {
		this.pageContext = pageContext;
		this.monitor = monitor;
		this.commands = commands;
		this.outputfile = outputfile;
		this.variable = variable;
		this.resultVariable = resultVariable;
		this.exitCodeVariable = exitCodeVariable;

		this.errorFile = errorFile;
		this.errorVariable = errorVariable;
		// this.body=body;

		this.directory = directory;
		this.environment = environment;

		this.onProgress = onProgress;
		this.onError = onError;
		this.timeout = timeout;
	}

	/**
	 * Starts the worker, on a virtual thread when virtual threads are allowed, otherwise on a daemon
	 * platform thread.
	 */
	public void start() {
		Thread t = ThreadUtil.getThread(this);
		// virtual threads are always daemon threads and do not allow to change that
		if (!t.isVirtual()) t.setDaemon(true);
		thread = t;
		caller = Thread.currentThread();
		t.start();
	}

	/**
	 * waits until the worker has ended
	 */
	public void join() throws InterruptedException {
		thread.join();
	}

	/**
	 * waits at most the given milliseconds for the worker to end
	 */
	public void join(long millis) throws InterruptedException {
		thread.join(millis);
	}

	@Override
	public void run() {
		ThreadLocalPageContext.register(pageContext);// register the PageContext to this thread
		// the caller already gave up (timeout) before the worker got to run
		if (aborted) bindToCaller();
		try {
			_run(pageContext);
		}
		finally {
			ThreadLocalPageContext.release();
			bindToCaller();
		}
	}

	void _run(PageContext pc) {
		try {
			CommandResult result;
			boolean redirectErrorStream = false;
			if (onProgress != null || onError != null) {
				UDFProcessListener progress = onProgress == null ? null : new UDFProcessListener(pc, onProgress);
				UDFProcessListener error = null;
				if (onError != null) error = new UDFProcessListener(pc, onError);
				else redirectErrorStream = true;
				process = Command.createProcess(pc, commands, directory, environment, redirectErrorStream);
				result = Command.execute(pc, process, timeout, progress, error);
			}
			else {
				process = Command.createProcess(pc, commands, directory, environment, redirectErrorStream);
				result = Command.execute(process);
			}

			String rst = result.getOutput();
			finished = true;
			if (!aborted) {
				if (outputfile == null && variable == null && resultVariable == null && exitCodeVariable == null) pc.write(rst);
				else if (resultVariable != null) {
					Struct sct = new StructImpl();
					sct.setEL(Caster.toKey("output"), result.getOutput());
					sct.setEL(Caster.toKey("error"), Caster.toString(result.getError()));
					sct.setEL(Caster.toKey("exitCode"), result.getExitCode());
					pc.setVariable(resultVariable, sct);
				}
				else {
					if (outputfile != null) IOUtil.write(outputfile, rst, SystemUtil.getCharset(), false);
					if (variable != null) pc.setVariable(variable, rst);
				}
				if (exitCodeVariable != null) pc.setVariable(exitCodeVariable, result.getExitCode());

				if (errorFile != null) IOUtil.write(errorFile, result.getError(), SystemUtil.getCharset(), false);
				if (errorVariable != null) pc.setVariable(errorVariable, Caster.toString(result.getError()));
			}
		}
		catch (Exception ioe) {
			exception = ioe;
		}
		// }
	}

	/**
	 * define that execution is aborted
	 * 
	 * @param terminateProcess
	 */
	public void abort(boolean terminateProcess) {
		aborted = true;
		Process p = process;
		if (terminateProcess && p != null) p.destroy();
		// the worker can still be unwinding, but the caller is going on with the PageContext, so it must be bound to the caller again right away
		bindToCaller();
	}

	private void bindToCaller() {
		Thread c = caller;
		if (c != null) ((PageContextImpl) pageContext).setThread(c);
	}

	/**
	 * has an exception occured
	 * 
	 * @return exception statuss
	 */
	public boolean hasException() {
		return exception != null;
	}

	/**
	 * has the execution finished
	 * 
	 * @return has finished
	 */

	public boolean hasFinished() {
		return finished;
	}

	/**
	 * @return the exception
	 */
	public Exception getException() {
		return exception;
	}

}