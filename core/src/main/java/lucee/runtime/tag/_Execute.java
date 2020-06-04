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

/**
 * 
 */
public final class _Execute extends PageContextThread {

	private Resource outputfile;
	private Resource errorFile;
	private String variable;
	private String errorVariable;
	private boolean aborted;
	private String[] commands;
	// private static final int BLOCK_SIZE=4096;
	private Object monitor;
	private Exception exception;
	// private String body;
	private boolean finished;
	private Process process;

	private String directory;

	/**
	 * @param pageContext
	 * @param monitor
	 * @param process
	 * @param outputfile
	 * @param variable
	 * @param body
	 * @param terminateOnTimeout
	 */
	public _Execute(PageContext pageContext, Object monitor, String[] commands, Resource outputfile, String variable, Resource errorFile, String errorVariable, String directory) {
		super(pageContext);
		this.monitor = monitor;
		this.commands = commands;
		this.outputfile = outputfile;
		this.variable = variable;

		this.errorFile = errorFile;
		this.errorVariable = errorVariable;
		// this.body=body;

		this.directory = directory;
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

			process = Command.createProcess(pc, commands, directory);

			CommandResult result = Command.execute(process);
			String rst = result.getOutput();
			finished = true;
			if (!aborted) {
				if (outputfile == null && variable == null) pc.write(rst);
				else {
					if (outputfile != null) IOUtil.write(outputfile, rst, SystemUtil.getCharset(), false);
					if (variable != null) pc.setVariable(variable, rst);
				}

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
	 */
	public void abort(boolean terminateProcess) {
		aborted = true;
		if (terminateProcess) process.destroy();
	}

	public boolean hasException() {
		return exception != null;
	}

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