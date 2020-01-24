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
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceSupport;
import lucee.runtime.db.ProcMetaCollection;

public class DatasourceFlushMetaCache {

	public static boolean call(PageContext pc) {
		return call(pc, null);
	}

	public synchronized static boolean call(PageContext pc, String datasource) {

		DataSource[] sources = pc.getConfig().getDataSources();
		DataSourceSupport ds;
		boolean has = false;
		for (int i = 0; i < sources.length; i++) {
			ds = (DataSourceSupport) sources[i];
			if (StringUtil.isEmpty(datasource) || ds.getName().equalsIgnoreCase(datasource.trim())) {
				Map<String, SoftReference<ProcMetaCollection>> cache = ds.getProcedureColumnCache();
				if (cache != null) cache.clear();
				if (!StringUtil.isEmpty(datasource)) return true;
				has = true;
			}
		}
		return has;
	}

}