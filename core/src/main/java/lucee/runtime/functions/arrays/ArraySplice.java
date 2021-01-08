package lucee.runtime.functions.arrays;

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

public class ArraySplice extends BIF implements Function {

	private static final long serialVersionUID = -8604228677976070247L;

	public static Array call(PageContext pc, Array arr, double index) throws PageException {
		return call(pc, arr, index, -1, null);
	}

	public static Array call(PageContext pc, Array arr, double index, double len) throws PageException {
		return call(pc, arr, index, len, null);
	}

	public static Array call(PageContext pc, Array arr, double index, double length, Array replacements) throws PageException {
		Array removed = new ArrayImpl();
		// check index
		if (index < 1) index = arr.size()+index+1;
		else if (index > arr.size()) index = arr.size()+1;
		int idx = (int) index;

		// check len
		int len = (int) length;
		if (len == -1) len = (int) arr.size()-idx+1;
		else if (len < -1) len = 0; // stupid ut how acf works
		else {
			int size = arr.size();
			if (len - 1 > size - idx) len = size - idx + 1;
		}

		// first we remove what is not needed
		while (len > 0) {
			removed.append(arr.removeE(idx));
			len--;
		}
		// insert data
		if (replacements != null) {
			Iterator<Object> it = replacements.valueIterator();
			while (it.hasNext()) {
				arr.insert(idx++, it.next());
			}

		}
		return removed;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]));
		if (args.length == 3) return call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]));
		if (args.length == 4) return call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]), Caster.toArray(args[3]));

		throw new FunctionException(pc, "ArraySplice", 2, 4, args.length);
	}

	/*
	 * public static void main(String[] args2) throws PageException { ArrayImpl arr = new ArrayImpl();
	 * arr.add("a"); arr.add("b"); arr.add("c"); arr.add("d"); ArrayImpl rep = new ArrayImpl();
	 * rep.add("111"); rep.add("222"); print.e(arr); Object[] args = new Object[] { arr, 2, 1, rep };
	 * Object res = new ArraySplice().invoke(null, args); print.e(arr); print.e(res); }
	 */
}
