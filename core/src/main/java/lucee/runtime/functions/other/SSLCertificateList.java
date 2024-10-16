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
package lucee.runtime.functions.other;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.tag.Admin;
import lucee.runtime.type.Query;

public final class SSLCertificateList implements Function {

	private static final long serialVersionUID = 1114950592159155566L;

	public static Query call(PageContext pc) throws PageException {
		return Admin.getAllSSLCertificate(pc.getConfig());
	}

	public static Query call(PageContext pc, String host) throws PageException {
		if (StringUtil.isEmpty(host, true)) return call(pc);
		return call(pc, host, 443);
	}

	public static Query call(PageContext pc, String host, double port) throws PageException {
		if (StringUtil.isEmpty(host, true)) return call(pc);
		return Admin.getSSLCertificate(pc.getConfig(), host, (int) port);
	}

	public static Query call(PageContext pc, String host, Number port) throws PageException {
		if (StringUtil.isEmpty(host, true)) return call(pc);
		return Admin.getSSLCertificate(pc.getConfig(), host, port.intValue());
	}
}