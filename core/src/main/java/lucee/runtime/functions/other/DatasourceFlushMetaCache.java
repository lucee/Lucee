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

import java.lang.ref.SoftReference;
import java.util.Map;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.ProcMetaCollection;

public final class DatasourceFlushMetaCache {

	public static boolean call(PageContext pc) {
		return call(pc, null);
	}

	public static boolean call(PageContext pc, String datasource) {
		ConfigPro config = (ConfigPro) pc.getConfig();
		boolean cleared = false;

		if (StringUtil.isEmpty(datasource)) {
			// no-arg: walk the canonical pool registry, clear every cache
			for (DatasourceConnPool pool: config.getDatasourceConnectionPools()) {
				if (clear(pool)) cleared = true;
			}
			return cleared;
		}

		// named: resolve via app→server precedence (mirrors cfstoredproc's resolver),
		// then clear every pool whose factory holds the same DS content-id
		DataSource ds = pc.getDataSource(datasource.trim(), null);
		if (ds == null) return false;
		String dsId = ds.id();
		for (DatasourceConnPool pool: config.getDatasourceConnectionPools()) {
			if (dsId.equals(pool.getFactory().getDatasource().id())) {
				if (clear(pool)) cleared = true;
			}
		}
		return cleared;
	}

	private static boolean clear(DatasourceConnPool pool) {
		Map<String, SoftReference<ProcMetaCollection>> cache = pool.getProcMetaCache();
		if (cache.isEmpty()) return false;
		cache.clear();
		return true;
	}

}
