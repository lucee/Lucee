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

import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;

/**
 * this class is only used by the CFML dialect, so checking for the dialect is not necessary
 */
public final class MixedAppListener extends ModernAppListener {

	@Override
	public void onRequest(PageContext pc, PageSource requestedPage, RequestListener rl) throws PageException {

		RefBoolean isCFC = new RefBooleanImpl(false);
		Page appP = getApplicationPage(pc, requestedPage, mode, isCFC);

		if (isCFC.toBooleanValue()) _onRequest(pc, requestedPage, appP, rl);
		else ClassicAppListener._onRequest(pc, requestedPage, appP, rl);
	}

	@Override
	public final String getType() {
		return "mixed";
	}

	private static Page getApplicationPage(PageContext pc, PageSource requestedPage, int mode, RefBoolean isCFC) throws PageException {
		PageSource ps = ((ConfigPro) pc.getConfig()).getApplicationPageSource(pc, requestedPage.getPhyscalFile().getParent(), "Application.[cfc|cfm]", mode, isCFC);
		if (ps != null) {
			Page p = ps.loadPage(pc, false, null);
			if (p != null) return p;
		}

		Page p;
		if (mode == ApplicationListener.MODE_CURRENT2ROOT) p = getApplicationPageCurrToRoot(pc, requestedPage, isCFC);
		else if (mode == ApplicationListener.MODE_CURRENT_OR_ROOT) p = getApplicationPageCurrOrRoot(pc, requestedPage, isCFC);
		else if (mode == ApplicationListener.MODE_CURRENT) p = getApplicationPageCurrent(pc, requestedPage, isCFC);
		else p = getApplicationPageRoot(pc, isCFC);
		if (p != null) ((ConfigPro) pc.getConfig()).putApplicationPageSource(requestedPage.getPhyscalFile().getParent(), p.getPageSource(), "Application.[cfc|cfm]", mode,
				isCFC.toBooleanValue());

		return p;
	}

	private static Page getApplicationPageCurrent(PageContext pc, PageSource requestedPage, RefBoolean isCFC) throws PageException {
		PageSource ps = requestedPage.getRealPage(Constants.CFML_APPLICATION_EVENT_HANDLER);
		if (ps != null) {
			Page p = ps.loadPage(pc, false, null);
			if (p != null) {
				isCFC.setValue(true);
				return p;
			}
		}
		ps = requestedPage.getRealPage(Constants.CFML_CLASSIC_APPLICATION_EVENT_HANDLER);
		if (ps != null) {
			Page p = ps.loadPage(pc, false, null);
			if (p != null) {
				return p;
			}
		}
		return null;
	}

	private static Page getApplicationPageCurrToRoot(PageContext pc, PageSource requestedPage, RefBoolean isCFC) throws PageException {
		Page p = getApplicationPageCurrent(pc, requestedPage, isCFC);
		if (p != null) return p;

		Array arr = lucee.runtime.type.util.ListUtil.listToArrayRemoveEmpty(requestedPage.getRealpathWithVirtual(), "/");
		// Config config = pc.getConfig();
		String path;
		PageSource ps;
		for (int i = arr.size() - 1; i > 0; i--) {
			StringBuilder sb = new StringBuilder("/");
			for (int y = 1; y < i; y++) {
				sb.append((String) arr.get(y, ""));
				sb.append('/');
			}
			path = sb.toString();
			ps = ((PageContextImpl) pc).getPageSource(path.concat(Constants.CFML_APPLICATION_EVENT_HANDLER));
			if (ps != null) {
				p = ps.loadPage(pc, false, null);
				if (p != null) {
					isCFC.setValue(true);
					return p;
				}
			}
			ps = ((PageContextImpl) pc).getPageSource(path.concat(Constants.CFML_CLASSIC_APPLICATION_EVENT_HANDLER));
			if (ps != null) {
				p = ps.loadPage(pc, false, null);
				if (p != null) {
					return p;
				}
			}
		}
		return null;
	}

	private static Page getApplicationPageCurrOrRoot(PageContext pc, PageSource requestedPage, RefBoolean isCFC) throws PageException {
		// current
		Page p = getApplicationPageCurrent(pc, requestedPage, isCFC);
		if (p != null) return p;

		// root
		return getApplicationPageRoot(pc, isCFC);
	}

	private static Page getApplicationPageRoot(PageContext pc, RefBoolean isCFC) throws PageException {
		PageSource ps = ((PageContextImpl) pc).getPageSource("/" + Constants.CFML_APPLICATION_EVENT_HANDLER);
		if (ps != null) {
			Page p = ps.loadPage(pc, false, null);
			if (p != null) {
				isCFC.setValue(true);
				return p;
			}

		}
		ps = ((PageContextImpl) pc).getPageSource("/" + Constants.CFML_CLASSIC_APPLICATION_EVENT_HANDLER);
		if (ps != null) {
			Page p = ps.loadPage(pc, false, null);
			if (p != null) {
				return p;
			}
		}
		return null;
	}

	@Override
	public void onDebug(PageContext pc) throws PageException {
		if (((PageContextImpl) pc).getAppListenerType() == ApplicationListener.TYPE_CLASSIC) ClassicAppListener._onDebug(pc);
		else super.onDebug(pc);
	}

	@Override
	public void onError(PageContext pc, PageException pe) {
		if (((PageContextImpl) pc).getAppListenerType() == ApplicationListener.TYPE_CLASSIC) ClassicAppListener._onError(pc, pe);
		else super.onError(pc, pe);
	}

	@Override
	public boolean hasOnSessionStart(PageContext pc) {
		if (((PageContextImpl) pc).getAppListenerType() == ApplicationListener.TYPE_CLASSIC) return ClassicAppListener._hasOnSessionStart(pc);
		return super.hasOnSessionStart(pc);
	}

	@Override
	public boolean hasOnApplicationStart() {
		PageContext pc = ThreadLocalPageContext.get();
		if (pc != null && ((PageContextImpl) pc).getAppListenerType() == ApplicationListener.TYPE_CLASSIC) return ClassicAppListener._hasOnApplicationStart();
		return super.hasOnApplicationStart();
	}

	@Override
	public void onTimeout(PageContext pc) {
		if (((PageContextImpl) pc).getAppListenerType() == ApplicationListener.TYPE_CLASSIC) ClassicAppListener._onTimeout(pc);
		else super.onTimeout(pc);
	}
}