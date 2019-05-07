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
/**
 * Implements the CFML Function isuserinrole
 */
package lucee.runtime.functions.decision;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.security.Credential;
import lucee.runtime.type.util.ListUtil;

public final class IsUserInAnyRole implements Function {
	public static boolean call(PageContext pc) throws PageException {
		return call(pc, null);
	}

	public static boolean call(PageContext pc, String strRoles) throws PageException {
		if (StringUtil.isEmpty(strRoles)) {
			Credential ru = pc.getRemoteUser();
			if (ru == null) return false;
			return ru.getRoles().length > 0;
		}

		String[] roles = ListUtil.trimItems(ListUtil.listToStringArray(strRoles, ','));
		for (int i = 0; i < roles.length; i++) {
			if (IsUserInRole.call(pc, roles[i])) return true;
		}
		return false;
	}
}