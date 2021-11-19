/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.KeyConstants;

/**
 * A Page that can produce Components
 */
public abstract class InterfacePageImpl extends InterfacePage implements PagePro {

	public int getHash() {
		return 0;
	}

	public long getSourceLength() {
		return 0;
	}

	@Override
	public Object call(PageContext pc) throws PageException {
		try {
			pc.setSilent();
			InterfaceImpl interf = null;
			try {
				interf = newInstance(pc, getPageSource().getComponentName(), false);// TODO was only getComponentName before, is that change ok?
			}
			finally {
				pc.unsetSilent();
			}

			String qs = ReqRspUtil.getQueryString(pc.getHttpServletRequest());
			if (pc.getBasePageSource() == this.getPageSource() && pc.getConfig().debug()) pc.getDebugger().setOutput(false);
			boolean isPost = pc.getHttpServletRequest().getMethod().equalsIgnoreCase("POST");

			// POST
			if (isPost) {
				// Soap
				if (ComponentPageImpl.isSoap(pc)) throw new ApplicationException("can not instantiate interface [" + getPageSource().getComponentName() + "] as a component");
			}
			// GET
			else if (qs != null && qs.trim().equalsIgnoreCase("wsdl"))
				throw new ApplicationException("can not instantiate interface [" + getPageSource().getComponentName() + "] as a component");

			// WDDX
			if (pc.urlFormScope().containsKey(KeyConstants._method))
				throw new ApplicationException("can not instantiate interface [" + getPageSource().getComponentName() + "] as a component");

			// invoking via include
			if (pc.getTemplatePath().size() > 1) {
				throw new ApplicationException("can not invoke interface [" + getPageSource().getComponentName() + "] as a page");
			}

			// DUMP
			// TODO component.setAccess(pc,Component.ACCESS_PUBLIC);
			String cdf = pc.getConfig().getComponentDumpTemplate();
			if (cdf != null && cdf.trim().length() > 0) {
				pc.variablesScope().set(KeyConstants._component, interf);
				pc.doInclude(cdf, false);
			}
			else pc.write(pc.getConfig().getDefaultDumpWriter(DumpWriter.DEFAULT_RICH).toString(pc, interf.toDumpData(pc, 9999, DumpUtil.toDumpProperties()), true));

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);// Exception Handler.castAnd Stack(t, this, pc);
		}
		return null;
	}

	/**
	 * default implementation of the static constructor, that does nothing
	 */
	public void staticConstructor(PageContext pagecontext, ComponentImpl cfc) {
		// do nothing
	}

	public abstract void initInterface(InterfaceImpl i) throws PageException;

	public abstract InterfaceImpl newInstance(PageContext pc, String callPath, boolean isRealPath) throws lucee.runtime.exp.PageException;

}