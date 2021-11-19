/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
package lucee.runtime.functions.cache;

import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheUtil;
//import lucee.runtime.cache.eh.EHCache;
import lucee.runtime.config.Config;
import lucee.runtime.config.Password;
import lucee.runtime.config.ConfigAdmin;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.transformer.library.ClassDefinitionImpl;

/**
 * implements BIF CacheRegionNew. This function only exists for compatibility with other CFML
 * Engines and should be avoided where possible. The preferred method to manipulate Cache
 * connections is via the Administrator interface or in Application.
 */
public class CacheRegionNew extends BIF {

	public static String call(PageContext pc, String cacheName, Object arg2, Object arg3, String arg4) throws PageException { // used Object for args 2 & 3 to match fld
		return _call(pc, cacheName, (Struct) arg2, (Boolean) arg3, arg4);
	}

	public static String call(PageContext pc, String cacheName, Object properties, Object arg3) throws PageException {
		if (arg3 instanceof Boolean) // name, properties, throwOnError
			return _call(pc, cacheName, (Struct) properties, (Boolean) arg3, null);
		if (arg3 instanceof String) // name, properties, password
			return _call(pc, cacheName, (Struct) properties, true, (String) arg3);
		throw new FunctionException(pc, "CacheRegionNew", 3, "throwOnError",
				"when calling this function with 3 arguments the 3rd argument must be either throwOnError (Boolean), or webAdminPassword (String)");
	}

	public static String call(PageContext pc, String cacheName, Object arg2) throws PageException {
		if (arg2 instanceof Struct) // name, properties
			return _call(pc, cacheName, (Struct) arg2, true, null);
		if (arg2 instanceof String) // name, password
			return _call(pc, cacheName, new StructImpl(), true, (String) arg2);
		throw new FunctionException(pc, "CacheRegionNew", 2, "properties",
				"when calling this function with 2 arguments the 2nd argument must be either properties (Struct), or webAdminPassword (String)");
	}

	public static String call(PageContext pc, String cacheName) throws PageException {
		return _call(pc, cacheName, new StructImpl(), true, null); // name
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), args[1]);
		if (args.length == 3) return call(pc, Caster.toString(args[0]), args[1], args[2]);
		if (args.length == 4) return call(pc, Caster.toString(args[0]), args[1], args[2], Caster.toString(args[3]));
		throw new FunctionException(pc, "CacheRegionNew", 1, 4, args.length);
	}

	static String _call(PageContext pc, String cacheName, Struct properties, Boolean throwOnError, String strWebAdminPassword) throws PageException {
		Password webAdminPassword = CacheUtil.getPassword(pc, strWebAdminPassword, false);
		try {
			ConfigAdmin adminConfig = ConfigAdmin.newInstance(pc.getConfig(), webAdminPassword);// TODO why we have here EHCache?
			adminConfig.updateCacheConnection(cacheName, new ClassDefinitionImpl("org.lucee.extension.cache.eh.EHCache", null, null, pc.getConfig().getIdentification()),
					Config.CACHE_TYPE_NONE, properties, false, false);
			adminConfig.storeAndReload();
		}
		catch (Exception e) {
			if (throwOnError) throw Caster.toPageException(e);
		}
		return null;
	}
}