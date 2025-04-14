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
import lucee.commons.lang.PageContextThread;
import lucee.runtime.PageContext;
import lucee.runtime.op.Caster;
import lucee.runtime.process.UDFProcessListener;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;

/*
 * Execute external processes
 */
public final class _Execute extends PageContextThread {

	private Resource outputfile;
	private Resource errorFile;
	private String variable;
	private String errorVariable;
	private String resultVariable;
	private String exitCodeVariable;

	private boolean aborted;
	private String[] commands;
	// private static final int BLOCK_SIZE=4096;
	private Object monitor;
	private Exception exception;
	// private String body;
	private boolean finished;
	private Process process;

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
	public _Execute(PageContext pageContext, Object monitor, String[] commands, Resource outputfile, String variable, Resource errorFile,
			String errorVariable, String directory, Struct environment, String resultVariable, 
			String exitCodeVariable, UDF onProgress, UDF onError, long timeout) {
		super(pageContext);
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

	@Override
	public void run(PageContext pc) {
		try {
			_run(pc);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	void _run(PageContext pc) {
		try {
			CommandResult result;
			boolean redirectErrorStream = false;
			if (onProgress != null ){
				UDFProcessListener progress = new UDFProcessListener(pc, onProgress);
				UDFProcessListener error = null;
				if (onError != null ) error = new UDFProcessListener(pc, onError);
				else redirectErrorStream = true;
				process = Command.createProcess(pc, commands, directory, environment, redirectErrorStream);
				result = Command.execute(pc, process, timeout, progress, error);
			} else {
				process = Command.createProcess(pc, commands, directory, environment, redirectErrorStream);
				result = Command.execute(process);
			}
			
			String rst = result.getOutput();
			finished = true;
			if (!aborted) {
				if (outputfile == null && variable == null
						&& resultVariable == null && exitCodeVariable == null) pc.write(rst);
				else if (resultVariable != null){
					Struct sct = new StructImpl();
					sct.setEL(Caster.toKey("output"), result.getOutput());
					sct.setEL(Caster.toKey("error"), result.getError());
					sct.setEL(Caster.toKey("exitCode"), result.getExitCode());
					pc.setVariable(resultVariable, sct);
				}
				else {
					if (outputfile != null) IOUtil.write(outputfile, rst, SystemUtil.getCharset(), false);
					if (variable != null) pc.setVariable(variable, rst);
				}
				if (exitCodeVariable != null) pc.setVariable(exitCodeVariable, result.getExitCode());

				if (errorFile != null) IOUtil.write(errorFile, result.getError(), SystemUtil.getCharset(), false);
				if (errorVariable != null) pc.setVariable(errorVariable, result.getError());
			}
		}
		catch (Exception ioe) {
			exception = ioe;
		}
		// }
	}

	/**
	 * define that execution is aborted
	 * @param terminateProcess
	 */
	public void abort(boolean terminateProcess) {
		aborted = true;
		if (terminateProcess) process.destroy();
	}
	/**
	 * has an exception occured
	 * @return exception statuss
	 */
	public boolean hasException() {
		return exception != null;
	}

	/**
	 * has the execution finished
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