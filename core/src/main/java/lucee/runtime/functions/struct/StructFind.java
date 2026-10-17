/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 */
package lucee.runtime.functions.struct;

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Closure;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;

public final class StructFind extends BIF {

	private static final long serialVersionUID = 6251275814429295997L;

	public static Object call(PageContext pc, Struct struct, String key) throws PageException {
		return struct.get(KeyImpl.init(key));
	}

	public static Object call(PageContext pc, Struct struct, Key key) throws PageException {
		return struct.get(key);
	}

	public static Object call(PageContext pc, Struct struct, String key, Object defaultValue) throws PageException {
		return struct.get(Caster.toKey(key), defaultValue);
	}

	public static Object call(PageContext pc, Struct struct, Key key, Object defaultValue) {
		return struct.get(key, defaultValue);
	}

	/** LDEV-4495: Object overload so a callback is not cast to String */
	public static Object call(PageContext pc, Struct struct, Object keyOrUdf) throws PageException {
		if (keyOrUdf instanceof UDF) return find(pc, struct, (UDF) keyOrUdf);
		return struct.get(Caster.toKey(keyOrUdf));
	}

	public static Object call(PageContext pc, Struct struct, Object keyOrUdf, Object defaultValue) throws PageException {
		if (keyOrUdf instanceof UDF) return find(pc, struct, (UDF) keyOrUdf);
		return struct.get(Caster.toKey(keyOrUdf), defaultValue);
	}

	public static Object find(PageContext pc, Struct struct, UDF udf) throws PageException {
		Iterator<Entry<Key, Object>> it = struct.entryIterator();
		Object[] args = new Object[3];
		args[2] = struct;
		while (it.hasNext()) {
			Entry<Key, Object> e = it.next();
			args[0] = e.getKey().getString();
			args[1] = e.getValue();
			Object res = udf.call(pc, args, false);
			Boolean b = Caster.toBoolean(res, null);
			if (b == null) throw new FunctionException(pc, "StructFind", 2, "function",
					"return value of the " + (udf instanceof Closure ? "closure" : "function [" + udf.getFunctionName() + "]") + " cannot be casted to a boolean value.",
					CasterException.createMessage(res, "boolean"));
			if (b.booleanValue()) return e.getValue();
		}
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toStruct(args[0]), args[1], args[2]);
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), args[1]);
		throw new FunctionException(pc, "StructFind", 2, 3, args.length);
	}
}
