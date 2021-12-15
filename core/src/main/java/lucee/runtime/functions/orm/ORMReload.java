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
package lucee.runtime.functions.orm;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.orm.ORMUtil;

public class ORMReload {
	public static String call(PageContext pc) throws PageException {

		// flush and close session
		ORMSession session = ORMUtil.getSession(pc, false);
		if (session != null) {// MUST do the same with all sesson using the same engine
			ORMConfiguration config = session.getEngine().getConfiguration(pc);
			if (config.autoManageSession()) {
				session.flushAll(pc);
				session.closeAll(pc);
			}
		}
		pc.getApplicationContext().reinitORM(pc);
		ORMUtil.resetEngine(pc, true);
		return null;
	}
}