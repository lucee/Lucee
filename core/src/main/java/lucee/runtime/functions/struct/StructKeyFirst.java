/**
 * Implements the CFML Function structKeyFirst
 */
package lucee.runtime.functions.struct;

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.wrap.MapAsStruct;

public final class StructKeyFirst extends BIF {

	private static final long serialVersionUID = 4617702927146035391L;

	public static String call(PageContext pc, Struct struct) throws PageException {
		return key(struct, true);
	}

	/**
	 * returns the first or the last key of an ordered struct, without copying all the keys if the struct
	 * type allows direct access
	 * 
	 * @param struct an ordered struct
	 * @param first true for the first, false for the last key
	 * @return the key
	 * @throws ExpressionException if the struct is not ordered or is empty
	 */
	static String key(Struct struct, boolean first) throws ExpressionException {
		String position = first ? "first" : "last";
		if (!StructIsOrdered.isOrdered(struct)) {
			throw new ExpressionException("Cannot return " + position + " key of struct; the struct is not ordered, so the order of its keys is undefined. Use an ordered struct (structNew(\"ordered\") or [:] syntax)");
		}

		Collection.Key key;
		if (struct instanceof StructImpl) key = first ? ((StructImpl) struct).getFirstKey() : ((StructImpl) struct).getLastKey();
		else if (struct instanceof MapAsStruct) key = first ? ((MapAsStruct) struct).getFirstKey() : ((MapAsStruct) struct).getLastKey();
		else if (first) {
			Iterator<Collection.Key> it = struct.keyIterator();
			key = it != null && it.hasNext() ? it.next() : null;
		}
		else {
			Collection.Key[] keys = CollectionUtil.keys(struct);
			key = keys.length == 0 ? null : keys[keys.length - 1];
		}

		if (key == null) throw new ExpressionException("Cannot return " + position + " key of struct; struct is empty");
		return key.getString();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toStruct(args[0]));
		throw new FunctionException(pc, "StructKeyFirst", 1, 1, args.length);
	}
}
