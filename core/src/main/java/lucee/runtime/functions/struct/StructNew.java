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
 * Implements the CFML Function structnew
 */
package lucee.runtime.functions.struct;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.StructListenerImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.wrap.MapAsStruct;

public final class StructNew extends BIF {

	private static final long serialVersionUID = 2439168907287957648L;

	public static Struct call(PageContext pc) {
		return new StructImpl();
	}

	public static Struct call(PageContext pc, String type) throws PageException {
		return call(pc, type, null);
	}

	public static Struct call(PageContext pc, String type, UDF onMissingKey) throws PageException {
		int t = toType(type);
		if (t == StructImpl.TYPE_LINKED_CASESENSITIVE || t == StructImpl.TYPE_CASESENSITIVE) {
			if (onMissingKey != null) throw new ApplicationException("type [" + type + "] is not supported in combination with onMissingKey listener");
			return MapAsStruct.toStruct(t == StructImpl.TYPE_LINKED_CASESENSITIVE ? Collections.synchronizedMap(new LinkedHashMap<>()) : new ConcurrentHashMap<>(), true);
		}
		if (t == StructImpl.TYPE_MAX) {
			int max = Caster.toIntValue(type.substring(type.indexOf(':') + 1));
			if (max < 1) throw new ApplicationException("Invalid size for [max] type: [" + max + "]. The size must be at least 1.");
			return new StructImpl(t, StructImpl.DEFAULT_INITIAL_CAPACITY, max);
		}

		if (onMissingKey != null) {
			return new StructListenerImpl(t, onMissingKey);
		}
		return new StructImpl(t);
	}

	public static int toType(String type) throws ApplicationException {
		type = type.toLowerCase();
		if (type.equals("linked")) return Struct.TYPE_LINKED;
		else if (type.equals("ordered")) return Struct.TYPE_LINKED;
		else if (type.equals("weaked")) return Struct.TYPE_WEAKED;
		else if (type.equals("weak")) return Struct.TYPE_WEAKED;
		else if (type.equals("syncronized")) return Struct.TYPE_SYNC;
		else if (type.equals("synchronized")) return Struct.TYPE_SYNC;
		else if (type.equals("sync")) return Struct.TYPE_SYNC;
		else if (type.equals("soft")) return Struct.TYPE_SOFT;
		else if (type.equals("normal")) return Struct.TYPE_REGULAR;
		else if (type.equals("regular")) return Struct.TYPE_REGULAR;
		else if (type.equals("ordered-casesensitive")) return StructImpl.TYPE_LINKED_CASESENSITIVE;
		else if (type.equals("casesensitive")) return StructImpl.TYPE_CASESENSITIVE;
		else if (type.startsWith("max:")) return StructImpl.TYPE_MAX;
		else throw new ApplicationException("valid struct types are [normal, weak, linked, soft, synchronized,ordered-casesensitive,casesensitive,max:<number>]");

	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 0) return call(pc);
		throw new FunctionException(pc, "StructNew", 0, 1, args.length);

	}
}