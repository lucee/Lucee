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

import java.io.IOException;

import lucee.runtime.CFMLFactory;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.MissingIncludeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;
import lucee.runtime.type.scope.Application;
import lucee.runtime.type.scope.Session;

/*
 * only use by CFML dialect so checking for dialect is not necessary
 */
public final class ClassicAppListener extends AppListenerSupport {

	private int mode = MODE_CURRENT2ROOT;

	@Override
	public void onRequest(PageContext pc, PageSource requestedPage, RequestListener rl) throws PageException {

		Page application = AppListenerUtil.getApplicationPage(pc, requestedPage, Constants.CFML_CLASSIC_APPLICATION_EVENT_HANDLER, mode, ApplicationListener.TYPE_CLASSIC);

		_onRequest(pc, requestedPage, application, rl);
	}

	static void _onRequest(PageContext pc, PageSource requestedPage, Page application, RequestListener rl) throws PageException {
		PageContextImpl pci = (PageContextImpl) pc;
		pci.setAppListenerType(ApplicationListener.TYPE_CLASSIC);

		// on requestStart
		if (application != null) pci._doInclude(new PageSource[] { application.getPageSource() }, false, null);

		if (rl != null) {
			requestedPage = rl.execute(pc, requestedPage);
			if (requestedPage == null) return;
		}

		// request
		try {
			pci._doInclude(new PageSource[] { requestedPage }, false, null);
		}
		catch (MissingIncludeException mie) {
			ApplicationContext ac = pc.getApplicationContext();
			boolean rethrow = true;
			if (ac instanceof ClassicApplicationContext) {
				ClassicApplicationContext cfc = (ClassicApplicationContext) ac;
				UDF udf = cfc.getOnMissingTemplate();
				if (udf != null) {
					String targetPage = requestedPage.getRealpathWithVirtual();
					rethrow = (!Caster.toBooleanValue(udf.call(pc, new Object[] { targetPage }, true), true));
				}
			}
			if (rethrow) throw mie;
		}

		// on Request End
		if (application != null) {
			PageSource onReqEnd = application.getPageSource().getRealPage(Constants.CFML_CLASSIC_APPLICATION_END_EVENT_HANDLER);
			if (onReqEnd.exists()) pci._doInclude(new PageSource[] { onReqEnd }, false, null);
		}
	}

	@Override
	public boolean onApplicationStart(PageContext pc) throws PageException {
		// do nothing
		return true;
	}

	@Override
	public boolean onApplicationStart(PageContext pc, Application application) throws PageException {
		// do nothing
		return true;
	}

	@Override
	public void onSessionStart(PageContext pc) throws PageException {
		// do nothing
	}

	@Override
	public void onSessionStart(PageContext pc, Session session) throws PageException {
		// do nothing
	}

	@Override
	public void onApplicationEnd(CFMLFactory factory, String applicationName) throws PageException {
		// do nothing
	}

	@Override
	public void onSessionEnd(CFMLFactory cfmlFactory, String applicationName, String cfid) throws PageException {
		// do nothing
	}

	@Override
	public void onDebug(PageContext pc) throws PageException {
		_onDebug(pc);
	}

	public static void _onDebug(PageContext pc) throws PageException {
		try {
			if (pc.getConfig().debug()) pc.getDebugger().writeOut(pc);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public void onError(PageContext pc, PageException pe) {
		_onError(pc, pe);
	}

	public static void _onError(PageContext pc, PageException pe) {
		pc.handlePageException(pe);
	}

	@Override
	public void onTimeout(PageContext pc) {
		_onTimeout(pc);
	}

	public static void _onTimeout(PageContext pc) {
	}

	@Override
	public boolean hasOnApplicationStart() {
		return false;
	}

	public static boolean _hasOnApplicationStart() {
		return false;
	}

	@Override
	public boolean hasOnSessionStart(PageContext pc) {
		return false;
	}

	public static boolean _hasOnSessionStart(PageContext pc) {
		return false;
	}

	@Override
	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public int getMode() {
		return mode;
	}

	@Override
	public String getType() {
		return "classic";
	}
}