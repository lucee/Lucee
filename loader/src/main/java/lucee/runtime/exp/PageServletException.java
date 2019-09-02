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

import javax.servlet.ServletException;

import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.err.ErrorPage;
import lucee.runtime.type.Struct;

/**
 * by definition a JSP Tag can only throw JSPExceptions, for that case the PageException is a
 * Subclass of the JSPExceptions, but when a PageException, is escaleted to a parent page, this goes
 * over the include method of the PageContext Object, but this can only throw ServletException. For
 * that this class can Box a JSPException (PageException) in a ServletException
 * (PageServletException)
 */
public final class PageServletException extends ServletException implements IPageException, PageExceptionBox {

	private static final long serialVersionUID = -3654238294705464067L;

	private final PageException pe;

	/**
	 * constructor of the class
	 * 
	 * @param pe page exception to hold
	 */
	public PageServletException(final PageException pe) {
		super(pe.getMessage());
		this.pe = pe;
	}

	/**
	 * @see lucee.runtime.exp.PageExceptionBox#getPageException()
	 */
	@Override
	public PageException getPageException() {
		return pe;
	}

	/**
	 * @see lucee.runtime.exp.IPageException#getDetail()
	 */
	@Override
	public String getDetail() {
		return pe.getDetail();
	}

	/**
	 * @see lucee.runtime.exp.IPageException#getErrorCode()
	 */
	@Override
	public String getErrorCode() {
		return pe.getErrorCode();
	}

	/**
	 * @see lucee.runtime.exp.IPageException#getExtendedInfo()
	 */
	@Override
	public String getExtendedInfo() {
		return pe.getExtendedInfo();
	}

	/**
	 * 
	 * @see lucee.runtime.exp.IPageException#getCatchBlock(lucee.runtime.PageContext)
	 */
	@Override
	public Struct getCatchBlock(final PageContext pc) {
		return pe.getCatchBlock(pc.getConfig());
	}

	/**
	 * 
	 * @see lucee.runtime.exp.IPageException#getCatchBlock(lucee.runtime.PageContext)
	 */
	@Override
	public CatchBlock getCatchBlock(final Config config) {
		return pe.getCatchBlock(config);
	}

	/**
	 * @see lucee.runtime.exp.IPageException#getErrorBlock(PageContext pc,ErrorPage ep)
	 */
	@Override
	public Struct getErrorBlock(final PageContext pc, final ErrorPage ep) {
		return pe.getErrorBlock(pc, ep);
	}

	/**
	 * @see lucee.runtime.exp.IPageException#addContext(lucee.runtime.PageSource, int, int,
	 *      java.lang.StackTraceElement)
	 */
	@Override
	public void addContext(final PageSource template, final int line, final int column, final StackTraceElement ste) {
		pe.addContext(template, line, column, ste);
	}

	/**
	 * @see lucee.runtime.dump.Dumpable#toDumpData(lucee.runtime.PageContext, int,
	 *      lucee.runtime.dump.DumpProperties)
	 */
	@Override
	public DumpData toDumpData(final PageContext pageContext, final int maxlevel, final DumpProperties dp) {
		return pe.toDumpData(pageContext, maxlevel, dp);
	}

	/**
	 * @see lucee.runtime.exp.IPageException#setDetail(java.lang.String)
	 */
	@Override
	public void setDetail(final String detail) {
		pe.setDetail(detail);
	}

	/**
	 * @see lucee.runtime.exp.IPageException#setErrorCode(java.lang.String)
	 */
	@Override
	public void setErrorCode(final String errorCode) {
		pe.setErrorCode(errorCode);
	}

	/**
	 * @see lucee.runtime.exp.IPageException#setExtendedInfo(java.lang.String)
	 */
	@Override
	public void setExtendedInfo(final String extendedInfo) {
		pe.setExtendedInfo(extendedInfo);
	}

	/**
	 * @see lucee.runtime.exp.IPageException#getTypeAsString()
	 */
	@Override
	public String getTypeAsString() {
		return pe.getTypeAsString();
	}

	/**
	 * @see lucee.runtime.exp.IPageException#typeEqual(java.lang.String)
	 */
	@Override
	public boolean typeEqual(final String type) {
		return pe.typeEqual(type);
	}

	/**
	 * @see lucee.runtime.exp.IPageException#getCustomTypeAsString()
	 */
	@Override
	public String getCustomTypeAsString() {
		return pe.getCustomTypeAsString();
	}

	/*
	 * *
	 * 
	 * @see lucee.runtime.exp.IPageException#getLine() / public String getLine() { return pe.getLine();
	 * }
	 */

	/**
	 * @see lucee.runtime.exp.IPageException#getTracePointer()
	 */
	@Override
	public int getTracePointer() {
		return pe.getTracePointer();
	}

	/**
	 * @see lucee.runtime.exp.IPageException#setTracePointer(int)
	 */
	@Override
	public void setTracePointer(final int tracePointer) {
		pe.setTracePointer(tracePointer);
	}

	/**
	 * @see lucee.runtime.exp.IPageException#getAdditional()
	 */
	@Override
	public Struct getAdditional() {
		return pe.getAdditional();
	}

	@Override
	public Struct getAddional() {
		return pe.getAdditional();
	}

	/**
	 * @see lucee.runtime.exp.IPageException#getStackTraceAsString()
	 */
	@Override
	public String getStackTraceAsString() {
		return pe.getStackTraceAsString();
	}
}