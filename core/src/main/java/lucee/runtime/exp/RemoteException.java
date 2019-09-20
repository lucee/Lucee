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
package lucee.runtime.exp;

import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;

public class RemoteException extends PageExceptionImpl {

	public RemoteException(Throwable t) {
		super(createMessage(t), "remote");

	}

	private static String createMessage(Throwable t) {
		StringBuilder message = new StringBuilder(t.getMessage());
		if (t instanceof IPageException) {
			IPageException pe = (IPageException) t;
			String detail = pe.getDetail();
			if (!StringUtil.isEmpty(detail)) message.append("; ").append(detail);

		}
		message.append("; ");
		message.append(Caster.toClassName(t));

		return message.toString();
	}

}