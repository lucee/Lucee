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
 * Implements the CFML Function structdelete
 */
package lucee.runtime.functions.struct;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;

public final class StructDelete extends BIF {

	private static final long serialVersionUID = 6670961245029356618L;

	public static boolean call(PageContext pc, Struct struct, String key) throws TemplateException {
		return call(pc, struct, key, false);
	}

	public static boolean call(PageContext pc, Struct struct, String key, boolean indicatenotexisting) throws TemplateException {
		if(indicatenotexisting && !struct.containsKey(key)) throw new TemplateException("Cannot delete item with key " + key, "The key doesn't exist.");
		return struct.removeEL(KeyImpl.init(key)) != null || !indicatenotexisting;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]));
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]));
		throw new FunctionException(pc, "StructDelete", 2, 3, args.length);
	}
}
