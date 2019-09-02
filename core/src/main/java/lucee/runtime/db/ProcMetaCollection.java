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
package lucee.runtime.db;

import java.util.List;
import java.util.stream.Collectors;

public class ProcMetaCollection {

	public final String name;
	public final List<ProcMeta> metas;
	public long created = System.currentTimeMillis();

	public ProcMetaCollection(String name, List<ProcMeta> metas) {
		this.name  = name;
		this.metas = metas;
	}

	public static String getParamTypeList(List<ProcMeta> metas) {
		return metas.stream()
				.map(pm -> SQLCaster.toStringType(pm.dataType, "?"))
				.collect(Collectors.joining(", "));
	}
}