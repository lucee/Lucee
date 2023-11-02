/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.exp;

import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.err.ErrorPage;
import lucee.runtime.type.Struct;

/**
 * interface of the root business exception of Lucee
 */
public interface IPageException extends Dumpable {

	/**
	 * return detailed error message
	 * 
	 * @return detailed error message
	 */
	public String getDetail();

	/**
	 * Error Code
	 * 
	 * @return Error Code
	 */
	public String getErrorCode();

	/**
	 * return extended info to the error
	 * 
	 * @return extended info
	 */
	public String getExtendedInfo();

	/*
	 * *
	 * 
	 * @return returns the line where the failure occurred
	 */
	// public String getLine();

	/**
	 * @return Returns the tracePointer.
	 */
	public int getTracePointer();

	/**
	 * @param tracePointer The tracePointer to set.
	 */
	public void setTracePointer(int tracePointer);

	/**
	 * Error type as String
	 * 
	 * @return error type
	 */
	public String getTypeAsString();

	/**
	 * Error custom type as String
	 * 
	 * @return error type
	 */
	public String getCustomTypeAsString();

	/**
	 * return detailed catch block of the error
	 * 
	 * @return catch block
	 * @deprecated use instead <code>getCatchBlock(Config config);</code>
	 */
	@Deprecated
	public Struct getCatchBlock(PageContext pc);

	/**
	 * return detailed catch block of the error
	 * 
	 * @return catch block
	 */
	public CatchBlock getCatchBlock(Config config);

	/**
	 * return detailed error block of the error
	 * 
	 * @param pc page context of the request
	 * @param ep error page
	 * @return catch block
	 */
	public Struct getErrorBlock(PageContext pc, ErrorPage ep);

	/**
	 * add a template to the context of the error
	 * 
	 * @param pageSource new template context
	 * @param line line of the error
	 * @param column column of the error
	 */
	public void addContext(PageSource pageSource, int line, int column, StackTraceElement element);

	/**
	 * compare error type as String
	 * 
	 * @param type other error type
	 * @return is same error type
	 */
	public boolean typeEqual(String type);

	/**
	 * sets detailed error message
	 * 
	 * @param detail
	 */
	public void setDetail(String detail);

	/**
	 * sets the Error Code
	 * 
	 * @param errorCode
	 */
	public void setErrorCode(String errorCode);

	/**
	 * sets extended info to the error
	 * 
	 * @param extendedInfo
	 */
	public void setExtendedInfo(String extendedInfo);

	/**
	 * @return Returns the additional.
	 * @deprecated use instead <code>getAdditional();</code>
	 */
	@Deprecated
	public Struct getAddional();

	/**
	 * @return Returns the additional.
	 */
	public Struct getAdditional();

	/**
	 * returns the java stracktrace as a String
	 * 
	 * @return stack trace
	 */
	public String getStackTraceAsString();
}