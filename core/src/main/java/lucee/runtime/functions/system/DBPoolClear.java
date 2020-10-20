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
 * Implements the CFML Function gettemplatepath
 */
package lucee.runtime.functions.system;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.ext.function.Function;

public final class DBPoolClear implements Function {

	public static boolean call(PageContext pc) {
		return call(pc, null);
	}

	public static boolean call(PageContext pc, String dataSourceName) {
		if (StringUtil.isEmpty(dataSourceName)) ((ConfigPro) pc.getConfig()).getDatasourceConnectionPool().clear(true);
		else((ConfigPro) pc.getConfig()).getDatasourceConnectionPool().clear(dataSourceName, true);
		return true;
	}
}