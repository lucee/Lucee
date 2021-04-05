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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lucee.commons.cli.Command;
import lucee.commons.cli.CommandResult;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.type.util.ListUtil;

/**
 * Enables CFML developers to execute a process on a server computer.
 *
 *
 *
 **/
public final class Execute extends BodyTagImpl {

	/** Command-line arguments passed to the application. */
	private List<String> arguments;

	/**
	 * Indicates how long, in seconds, the CFML executing thread waits for the spawned process. A
	 * timeout of 0 is equivalent to the non-blocking mode of executing. A very high timeout value is
	 ** equivalent to a blocking mode of execution. The default is 0; therefore, the CFML thread spawns a
	 * process and returns without waiting for the process to terminate.If no output file is specified,
	 ** and the timeout value is 0, the program output is discarded.
	 */
	private long timeout;

	/**
	 * The full pathname of the application to execute. Note: On Windows, you must specify the extension
	 * as part of the application's name. For example, myapp.exe,
	 */
	private String name = null;

	/**
	 * The file to which to direct the output of the program. If not specified, the output is displayed
	 * on the page from which it was called.
	 */
	private Resource outputfile;
	private Resource errorFile;

	private String variable;
	private String errorVariable;

	private String body;
	private String directory;

	private boolean terminateOnTimeout = false;

	@Override
	public void release() {
		super.release();
		arguments = null;
		timeout = 0L;
		name = null;
		outputfile = null;
		errorFile = null;
		variable = null;
		errorVariable = null;
		body = null;
		terminateOnTimeout = false;
		directory = null;
	}

	/**
	 * set the value arguments Command-line arguments passed to the application.
	 * 
	 * @param args value to set
	 * @throws PageException
	 **/
	public void setArguments(Object args) throws PageException {
		if (args instanceof lucee.runtime.type.Collection) {
			this.arguments = new ArrayList<String>();
			lucee.runtime.type.Collection coll = (lucee.runtime.type.Collection) args;
			Iterator<Object> it = coll.valueIterator();
			while (it.hasNext()) {
				arguments.add(Caster.toString(it.next()));
			}
		}
		else {
			arguments = Command.toList(Caster.toString(args));
		}
	}

	public static void main(String[] args) throws Exception {
		CommandResult cr = Command.execute("curl https://update.lucee.org/rest/update/provider/echoGet", true);
		_Execute e = new _Execute(null, null, new String[] { "curl", "https://update.lucee.org/rest/update/provider/echoGet" }, null, null, null, null, null);
		e._run(null);
	}

	/**
	 * set the value timeout Indicates how long, in seconds, the CFML executing thread waits for the
	 * spawned process. A timeout of 0 is equivalent to the non-blocking mode of executing. A very high
	 * timeout value is equivalent to a blocking mode of execution. The default is 0; therefore, the
	 * CFML thread spawns a process and returns without waiting for the process to terminate.If no
	 * output file is specified, and the timeout value is 0, the program output is discarded.
	 * 
	 * @param timeout value to set
	 * @throws ApplicationException
	 **/
	public void setTimeout(double timeout) throws ApplicationException {
		if (timeout < 0) throw new ApplicationException("Attribute [timeout] must be a positive number, was [" + Caster.toString(timeout) + "]");
		this.timeout = (long) (timeout * 1000L);
	}

	public void setTerminateontimeout(boolean terminateontimeout) {
		this.terminateOnTimeout = terminateontimeout;
	}

	/**
	 * set the value name The full pathname of the application to execute. Note: On Windows, you must
	 * specify the extension as part of the application's name. For example, myapp.exe,
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * define name of variable where output is written to
	 * 
	 * @param variable
	 * @throws PageException
	 */
	public void setVariable(String variable) throws PageException {
		this.variable = variable;
		pageContext.setVariable(variable, "");
	}

	public void setErrorvariable(String errorVariable) throws PageException {
		this.errorVariable = errorVariable;
		pageContext.setVariable(errorVariable, "");
	}

	/**
	 * set the value outputfile The file to which to direct the output of the program. If not specified,
	 * the output is displayed on the page from which it was called.
	 * 
	 * @param outputfile value to set
	 * @throws SecurityException
	 **/
	public void setOutputfile(String outputfile) {
		try {
			this.outputfile = ResourceUtil.toResourceExistingParent(pageContext, outputfile);
			pageContext.getConfig().getSecurityManager().checkFileLocation(this.outputfile);

		}
		catch (PageException e) {
			this.outputfile = pageContext.getConfig().getTempDirectory().getRealResource(outputfile);
			if (!this.outputfile.getParentResource().exists()) this.outputfile = null;
			else if (!this.outputfile.isFile()) this.outputfile = null;
			else if (!this.outputfile.exists()) {
				ResourceUtil.createFileEL(this.outputfile, false);
				// try {
				// this.outputfile.createNewFile();
				/*
				 * } catch (IOException e1) { this.outputfile=null; }
				 */
			}
		}
	}

	public void setErrorfile(String errorfile) {

		try {
			this.errorFile = ResourceUtil.toResourceExistingParent(pageContext, errorfile);
			pageContext.getConfig().getSecurityManager().checkFileLocation(this.errorFile);
		}
		catch (PageException e) {

			this.errorFile = pageContext.getConfig().getTempDirectory().getRealResource(errorfile);

			if (!this.errorFile.getParentResource().exists()) this.errorFile = null;
			else if (!this.errorFile.isFile()) this.errorFile = null;
			else if (!this.errorFile.exists()) {
				ResourceUtil.createFileEL(this.errorFile, false);
			}
		}
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	@Override
	public int doStartTag() throws PageException {
		return EVAL_BODY_BUFFERED;
	}

	private void _execute() throws Exception {
		Object monitor = new SerializableObject();
		if (name == null) {
			if (StringUtil.isEmpty(body)) {
				required("execute", "name", name);
				required("execute", "arguments", arguments);
			}
			else {
				name = body;
			}
		}
		if (arguments == null || arguments.size() == 0) {
			arguments = Command.toList(name);
		}
		else {
			arguments.add(0, name);
		}

		_Execute execute = new _Execute(pageContext, monitor, arguments.toArray(new String[arguments.size()]), outputfile, variable, errorFile, errorVariable, directory);

		// if(timeout<=0)execute._run();
		// else {
		execute.start();
		if (timeout > 0) execute.join(timeout);
		else execute.join();
		if (execute.hasException()) throw execute.getException();
		if (!execute.hasFinished()) {
			execute.abort(terminateOnTimeout);
			throw new ApplicationException("Timeout [" + (timeout) + " ms] expired while executing [" + ListUtil.listToList(arguments, " ") + "]");
		}

	}

	@Override
	public int doEndTag() throws PageException {
		if (pageContext.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_EXECUTE) == SecurityManager.VALUE_NO)
			throw new SecurityException("Can't access tag [execute]", "Access is prohibited by Security Manager");
		try {
			_execute();
		}
		catch (PageException pe) {
			throw pe;
		}
		catch (Exception e) {
			throw new ApplicationException("Error invoking external process", e.getMessage());
		}
		return EVAL_PAGE;
	}

	@Override
	public void doInitBody() {

	}

	@Override
	public int doAfterBody() {
		body = bodyContent.getString();
		if (!StringUtil.isEmpty(body)) body = body.trim();
		return SKIP_BODY;
	}
}
