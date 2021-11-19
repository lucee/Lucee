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

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.tag.TagImpl;

/**
 * Executes a Java servlet on a JRun engine. This tag is used in conjunction with the cfserletparam
 * tag, which passes data to the servlet.
 *
 *
 *
 **/
public final class Servlet extends TagImpl {

	private boolean debug;
	private String code;
	private boolean writeoutput;
	private double timeout;
	private String jrunproxy;

	/**
	 * constructor for the tag class
	 * 
	 * @throws ExpressionException
	 **/
	public Servlet() throws ExpressionException {
		throw new ExpressionException("tag cfservlet is deprecated");
	}

	/**
	 * set the value debug Boolean specifying whether additional information about the JRun connection
	 * status and activity is to be written to the JRun error log
	 * 
	 * @param debug value to set
	 **/
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * set the value code The class name of the Java servlet to execute.
	 * 
	 * @param code value to set
	 **/
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * set the value writeoutput
	 * 
	 * @param writeoutput value to set
	 **/
	public void setWriteoutput(boolean writeoutput) {
		this.writeoutput = writeoutput;
	}

	/**
	 * set the value timeout Specifies how many seconds JRun waits for the servlet to complete before
	 * timing out.
	 * 
	 * @param timeout value to set
	 **/
	public void setTimeout(double timeout) {
		this.timeout = timeout;
	}

	/**
	 * set the value jrunproxy
	 * 
	 * @param jrunproxy value to set
	 **/
	public void setJrunproxy(String jrunproxy) {
		this.jrunproxy = jrunproxy;
	}

	@Override
	public int doStartTag() {
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		super.release();
		debug = false;
		code = "";
		writeoutput = false;
		timeout = 0d;
		jrunproxy = "";
	}
}