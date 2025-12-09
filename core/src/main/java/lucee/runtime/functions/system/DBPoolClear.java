/**
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
 * Clears idle datasource connections from the pool.
 */
package lucee.runtime.functions.system;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.ext.function.Function;

public final class DBPoolClear implements Function {

	public static boolean call( PageContext pc ) {
		return call( pc, null, true );
	}

	public static boolean call( PageContext pc, String dataSourceName ) {
		return call( pc, dataSourceName, true );
	}

	public static boolean call( PageContext pc, String dataSourceName, boolean force ) {
		for ( DatasourceConnPool pool : ((ConfigPro) pc.getConfig()).getDatasourceConnectionPools() ) {
			if ( StringUtil.isEmpty( dataSourceName ) || dataSourceName.equalsIgnoreCase( pool.getFactory().getDatasource().getName() ) ) {
				if ( force ) {
					pool.clear();
				}
				else {
					try {
						pool.evict();
					}
					catch ( Exception e ) {
						// evict() can throw Exception, but we don't want to fail the whole operation
					}
				}
			}
		}
		return true;
	}
}