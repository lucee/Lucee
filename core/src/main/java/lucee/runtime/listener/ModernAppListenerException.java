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
package lucee.runtime.listener;

import java.io.PrintStream;
import java.io.PrintWriter;

import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.err.ErrorPage;
import lucee.runtime.exp.CatchBlock;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageExceptionImpl;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public final class ModernAppListenerException extends PageException {

	private static final Collection.Key ROOT_CAUSE = KeyConstants._rootCause;
	private static final Collection.Key CAUSE = KeyConstants._cause;
	private PageException rootCause;
	private String eventName;

	/**
	 * Constructor of the class
	 * 
	 * @param pe
	 * @param eventName
	 */
	public ModernAppListenerException(PageException pe, String eventName) {
		super(pe.getMessage());
		setStackTrace(pe.getStackTrace());
		this.rootCause = pe;
		this.eventName = eventName;
	}

	@Override
	public void addContext(PageSource pageSource, int line, int column, StackTraceElement ste) {
		rootCause.addContext(pageSource, line, column, ste);
	}

	@Override
	public Struct getAdditional() {
		return rootCause.getAdditional();
	}

	@Override
	public Struct getAddional() {
		return rootCause.getAdditional();
	}

	public Struct getCatchBlock() {
		return getCatchBlock(ThreadLocalPageContext.getConfig());
	}

	@Override
	public Struct getCatchBlock(PageContext pc) {
		return getCatchBlock(pc.getConfig());
	}

	@Override
	public CatchBlock getCatchBlock(Config config) {
		CatchBlock cb = rootCause.getCatchBlock(config);
		Collection cause = (Collection) Duplicator.duplicate(cb, false);
		// rtn.setEL("message", getMessage());
		if (!cb.containsKey(KeyConstants._detail)) cb.setEL(KeyConstants._detail, "Exception thrown while invoking function [" + eventName + "] in application event handler ");
		cb.setEL(ROOT_CAUSE, cause);
		cb.setEL(CAUSE, cause);
		// cb.setEL("stacktrace", getStackTraceAsString());
		// rtn.setEL("tagcontext", new ArrayImpl());
		// rtn.setEL("type", getTypeAsString());
		cb.setEL(KeyConstants._name, eventName);
		return cb;
	}

	@Override
	public String getCustomTypeAsString() {
		return rootCause.getCustomTypeAsString();
	}

	@Override
	public String getDetail() {
		return rootCause.getDetail();
	}

	@Override
	public Struct getErrorBlock(PageContext pc, ErrorPage ep) {
		return rootCause.getErrorBlock(pc, ep);
	}

	@Override
	public String getErrorCode() {
		return rootCause.getErrorCode();
	}

	@Override
	public String getExtendedInfo() {
		return rootCause.getExtendedInfo();
	}

	@Override
	public String getStackTraceAsString() {
		return rootCause.getStackTraceAsString();
	}

	@Override
	public int getTracePointer() {
		return rootCause.getTracePointer();
	}

	@Override
	public String getTypeAsString() {
		return rootCause.getTypeAsString();
	}

	@Override
	public void setDetail(String detail) {
		rootCause.setDetail(detail);
	}

	@Override
	public void setErrorCode(String errorCode) {
		rootCause.setErrorCode(errorCode);
	}

	@Override
	public void setExtendedInfo(String extendedInfo) {
		rootCause.setExtendedInfo(extendedInfo);
	}

	@Override
	public void setTracePointer(int tracePointer) {
		rootCause.setTracePointer(tracePointer);
	}

	@Override
	public boolean typeEqual(String type) {
		return rootCause.equals(type);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return rootCause.toDumpData(pageContext, maxlevel, dp);
	}

	/**
	 * @return the eventName
	 */
	public String getEventName() {
		return eventName;
	}

	public String getLine(Config config) {
		return ((PageExceptionImpl) rootCause).getLine(config);
	}

	@Override
	public Throwable getRootCause() {
		return rootCause.getRootCause();
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		return rootCause.getStackTrace();
	}

	@Override
	public void printStackTrace() {
		rootCause.printStackTrace();
	}

	@Override
	public void printStackTrace(PrintStream s) {
		rootCause.printStackTrace(s);
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		rootCause.printStackTrace(s);
	}

	public PageException getPageException() {
		return rootCause;
	}

	@Override
	public void setExposeMessage(boolean exposeMessage) {
		rootCause.setExposeMessage(exposeMessage);
	}

	@Override
	public boolean getExposeMessage() {
		return rootCause.getExposeMessage();
	}

}