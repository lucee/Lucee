/**
 * Implements the CFML Function structToSorted
 */
package lucee.runtime.functions.struct;

import java.util.Arrays;
import java.util.Comparator;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.comparator.ExceptionComparator;
import lucee.runtime.type.comparator.NumberSortRegisterComparator;
import lucee.runtime.type.comparator.SortRegister;
import lucee.runtime.type.comparator.SortRegisterComparator;
import lucee.runtime.type.comparator.StructSortRegister;
import lucee.runtime.type.util.CollectionUtil;

public final class StructToSorted extends BIF {
	private static final long serialVersionUID = -7945612992641626477L;

	public static Struct call(PageContext pc, Struct base) throws PageException {
		return call(pc, base, "text", "asc", false);
	}

	public static Struct call(PageContext pc, Struct base, Object sortTypeOrSortFunc) throws PageException {
		if (Decision.isSimpleValue(sortTypeOrSortFunc)) return call(pc, base, Caster.toString(sortTypeOrSortFunc), "asc", false);
		return _call(pc, base, Caster.toFunction(sortTypeOrSortFunc));
	}

	public static Struct call(PageContext pc, Struct base, Object sortType, String sortOrder) throws PageException {
		return call(pc, base, sortType, sortOrder, false);
	}

	public static Struct call(PageContext pc, Struct base, Object sortType, String sortOrder, boolean localeSensitive) throws PageException {
		return _call(pc, "struct", base, Caster.toString(sortType), sortOrder, localeSensitive);
	}

	public static Struct _call(PageContext pc, String type, Struct base, String sortType, String sortOrder, boolean localeSensitive) throws PageException {

		boolean isAsc = true;
		PageException ee = null;
		if (sortOrder.equalsIgnoreCase("asc")) isAsc = true;
		else if (sortOrder.equalsIgnoreCase("desc")) isAsc = false;
		else throw new ExpressionException("Invalid sort order type [" + sortOrder + "], sort order types are [asc and desc]");

		Collection.Key[] keys = CollectionUtil.keys(base);
		SortRegister[] arr = new SortRegister[keys.length];

		for (int i = 0; i < keys.length; i++) {
			arr[i] = new SortRegister(i, keys[i]);
		}

		ExceptionComparator comp = null;
		if (sortType.equalsIgnoreCase("text")) comp = new SortRegisterComparator(pc, isAsc, false, localeSensitive);
		else if (sortType.equalsIgnoreCase("textnocase")) comp = new SortRegisterComparator(pc, isAsc, true, localeSensitive);
		else if (sortType.equalsIgnoreCase("numeric")) comp = new NumberSortRegisterComparator(isAsc);
		else {
			throw new ExpressionException("Invalid sort type [" + sortType + "], sort types are [text, textNoCase, numeric]");
		}

		Arrays.sort(arr, 0, arr.length, comp);
		ee = comp.getPageException();

		if (ee != null) {
			throw ee;
		}

		StructImpl rtn = new StructImpl(StructImpl.TYPE_LINKED, arr.length);
		for (int i = 0; i < arr.length; i++) {
			String key = keys[arr[i].getOldPosition()].getString();
			rtn.set(key, base.get(Caster.toKey(key))); // TODO duplicate?
		}
		return rtn;
	}

	public static Struct _call(PageContext pc, Struct base, UDF sortFunc) throws PageException {
		Collection.Key[] keys = CollectionUtil.keys(base);
		StructSortRegister[] arr = new StructSortRegister[keys.length];

		for (int i = 0; i < keys.length; i++) {
			arr[i] = new StructSortRegister(i, keys[i].toString(), base.get(keys[i], null));
		}

		Arrays.sort(arr, new StructComparator(pc, sortFunc));

		StructImpl rtn = new StructImpl(StructImpl.TYPE_LINKED, arr.length);
		for (int i = 0; i < arr.length; i++) {
			String key = keys[arr[i].getOldPosition()].getString();
			rtn.set(key, base.get(Caster.toKey(key))); // TODO duplicate?
		}
		return rtn;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 4) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBoolean(args[3]));
		if (args.length == 3) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), args[1]);
		if (args.length == 1) return call(pc, Caster.toStruct(args[0]));
		throw new FunctionException(pc, "StructToSorted", 1, 4, args.length);
	}

	public static class StructComparator implements Comparator<StructSortRegister> {

		private PageContext pc;
		private final UDF udf;

		public StructComparator(PageContext pc, UDF udf) {
			this.pc = pc;
			this.udf = udf;
		}

		@Override
		public int compare(StructSortRegister left, StructSortRegister right) {
			try {
				return Caster.toIntValue(udf.call(pc, new Object[] { left.getValue(), right.getValue(), left.getKey(), right.getKey() }, true));
			}
			catch (PageException pe) {
				throw new PageRuntimeException(pe);
			}
		}
	}
}