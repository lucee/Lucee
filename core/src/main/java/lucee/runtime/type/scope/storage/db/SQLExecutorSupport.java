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
package lucee.runtime.type.scope.storage.db;

import java.util.HashSet;
import java.util.Set;

import lucee.runtime.config.Config;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.util.KeyConstants;

public abstract class SQLExecutorSupport implements SQLExecutor {

	protected static final Set<Collection.Key> ignoreSet = new HashSet<Collection.Key>();
	static {
		ignoreSet.add(KeyConstants._cfid);
		ignoreSet.add(KeyConstants._cftoken);
		ignoreSet.add(KeyConstants._urltoken);
	}

	protected static String now(Config config) {
		return Caster.toString(new DateTimeImpl(config).getTime());
	}

	protected static String createExpires(Config config, long timespan) {
		return Caster.toString(timespan + new DateTimeImpl(config).getTime());
	}

}