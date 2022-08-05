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
package lucee.runtime.functions.closure;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lucee.commons.lang.Pair;
import lucee.runtime.PageContext;
import lucee.runtime.concurrency.Data;
import lucee.runtime.concurrency.UDFCaller2;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.ArrayPro;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Iteratorable;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.it.ForEachQueryIterator;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.scope.ArgumentIntKey;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.StringListData;

public class Filter extends BIF implements ClosureFunc {

	private static final long serialVersionUID = -5940580562772523622L;

	public static Object call(PageContext pc, Object obj, UDF udf) throws PageException {
		return _call(pc, obj, udf, false, 20, TYPE_UNDEFINED);
	}

	public static Object call(PageContext pc, Object obj, UDF udf, boolean parallel) throws PageException {
		return _call(pc, obj, udf, parallel, 20, TYPE_UNDEFINED);
	}

	public static Object call(PageContext pc, Object obj, UDF udf, boolean parallel, double maxThreads) throws PageException {
		return _call(pc, obj, udf, parallel, (int) maxThreads, TYPE_UNDEFINED);
	}

	public static Object call(PageContext pc, Object obj, UDF udf, boolean parallel, int maxThreads, short type) throws PageException {
		return _call(pc, obj, udf, parallel, maxThreads, type);
	}

	private static Collection _call(PageContext pc, Object obj, UDF udf, boolean parallel, int maxThreads, short type) throws PageException {

		ExecutorService execute = null;
		List<Future<Data<Pair<Object, Object>>>> futures = null;
		if (parallel) {
			execute = Executors.newFixedThreadPool(maxThreads);
			futures = new ArrayList<Future<Data<Pair<Object, Object>>>>();
		}

		Collection coll;
		// !!!! Don't combine the first 3 ifs with the ifs below, type overrules instanceof check
		// Array
		if (type == TYPE_ARRAY) {
			coll = invoke(pc, (Array) obj, udf, execute, futures);
		}
		// Query
		else if (type == TYPE_QUERY) {
			coll = invoke(pc, (Query) obj, udf, execute, futures);
		}
		// Struct
		else if (type == TYPE_STRUCT) {
			coll = invoke(pc, (Struct) obj, udf, execute, futures);
		}

		// Array
		else if (obj instanceof Array && !(obj instanceof Argument)) {
			coll = invoke(pc, (Array) obj, udf, execute, futures);
		}
		// Query
		else if (obj instanceof Query) {
			coll = invoke(pc, (Query) obj, udf, execute, futures);
		}
		// Struct
		else if (obj instanceof Struct) {
			coll = invoke(pc, (Struct) obj, udf, execute, futures);
		}
		// other Iteratorable
		else if (obj instanceof Iteratorable) {
			coll = invoke(pc, (Iteratorable) obj, udf, execute, futures);
		}
		// Map
		else if (obj instanceof java.util.Map) {
			coll = invoke(pc, (java.util.Map) obj, udf, execute, futures);
		}
		// List
		else if (obj instanceof List) {
			coll = invoke(pc, (List) obj, udf, execute, futures);
		}
		// Iterator
		else if (obj instanceof Iterator) {
			coll = invoke(pc, (Iterator) obj, udf, execute, futures);
		}
		// Enumeration
		else if (obj instanceof Enumeration) {
			coll = invoke(pc, (Enumeration) obj, udf, execute, futures);
		}
		// String List
		else if (obj instanceof StringListData) {
			coll = invoke(pc, (StringListData) obj, udf, execute, futures);
		}
		// char[]
		else if (obj instanceof char[]) {
			coll = invoke(pc, (char[]) obj, udf, execute, futures);
		}

		else throw new FunctionException(pc, "Filter", 1, "data", "cannot iterate througth this type " + Caster.toTypeName(obj.getClass()));

		if (parallel) afterCall(pc, coll, futures, execute);

		return coll;
	}

	private static Collection invoke(PageContext pc, Array arr, UDF udf, ExecutorService es, List<Future<Data<Pair<Object, Object>>>> futures)
			throws CasterException, PageException {
		Array rtn = new ArrayImpl();
		boolean async = es != null;

		Iterator it = (arr instanceof ArrayPro ? ((ArrayPro) arr).entryArrayIterator() : arr.entryIterator());
		Entry e;
		Object res;
		while (it.hasNext()) {
			e = (Entry) it.next();
			res = _inv(pc, udf, new Object[] { e.getValue(), Caster.toDoubleValue(e.getKey()), arr }, e.getKey(), e.getValue(), es, futures);
			if (!async && Caster.toBooleanValue(res)) {
				rtn.append(e.getValue());
			}
		}
		return rtn;

	}

	private static Collection invoke(PageContext pc, StringListData sld, UDF udf, ExecutorService es, List<Future<Data<Pair<Object, Object>>>> futures)
			throws CasterException, PageException {
		Array arr = ListUtil.listToArray(sld.list, sld.delimiter, sld.includeEmptyFieldsx, sld.multiCharacterDelimiter);

		Array rtn = new ArrayImpl();
		Iterator it = (arr instanceof ArrayPro ? ((ArrayPro) arr).entryArrayIterator() : arr.entryIterator());
		Entry e;
		KeyImpl k = null;

		boolean async = es != null;
		Object res;
		while (it.hasNext()) {
			e = (Entry) it.next();
			res = _inv(pc, udf, new Object[] { e.getValue(), Caster.toDoubleValue(e.getKey()), sld.list, sld.delimiter }, e.getKey(), e.getValue(), es, futures);
			if (!async && Caster.toBooleanValue(res)) {
				rtn.append(e.getValue());
			}
		}
		return rtn;
	}

	private static Collection invoke(PageContext pc, char[] chars, UDF udf, ExecutorService es, List<Future<Data<Pair<Object, Object>>>> futures)
			throws CasterException, PageException {
		Array rtn = new ArrayImpl();
		boolean async = es != null;
		Object res;
		for (int i = 0; i < chars.length; i++) {
			char val = chars[i];
			res = _inv(pc, udf, new Object[] { chars[i], Caster.toDoubleValue(i+1), Caster.toString(chars) }, i, val, es, futures);
			if (!async && Caster.toBooleanValue(res)) rtn.append(val);;
		}
		return rtn;
	}

	private static Collection invoke(PageContext pc, Query qry, UDF udf, ExecutorService es, List<Future<Data<Pair<Object, Object>>>> futures)
			throws CasterException, PageException {
		Key[] colNames = qry.getColumnNames();
		Query rtn = new QueryImpl(colNames, 0, qry.getName());
		final int pid = pc.getId();
		ForEachQueryIterator it = new ForEachQueryIterator(pc, qry, pid);
		int rowNbr;
		Object row;
		boolean async = es != null;
		Object res;
		while (it.hasNext()) {
			row = it.next();
			rowNbr = qry.getCurrentrow(pid);

			res = _inv(pc, udf, new Object[] { row, Caster.toDoubleValue(rowNbr), qry }, rowNbr, qry, es, futures);
			if (!async && Caster.toBooleanValue(res)) {
				addRow(qry, rtn, rowNbr);
			}
		}
		return rtn;
	}

	private static void addRow(Query src, Query trg, int srcRow) {
		Key[] colNames = src.getColumnNames();
		int trgRow = trg.addRow();
		for (int c = 0; c < colNames.length; c++) {
			trg.setAtEL(colNames[c], trgRow, src.getAt(colNames[c], srcRow, null));
		}
	}

	private static Collection invoke(PageContext pc, List list, UDF udf, ExecutorService es, List<Future<Data<Pair<Object, Object>>>> futures)
			throws CasterException, PageException {
		Array rtn = new ArrayImpl();
		ListIterator it = list.listIterator();
		boolean async = es != null;
		Object res, v;
		int index;
		ArgumentIntKey k;
		while (it.hasNext()) {
			index = it.nextIndex();
			k = ArgumentIntKey.init(index);
			v = it.next();
			res = _inv(pc, udf, new Object[] { v, Caster.toDoubleValue(k.getString()), list }, k, v, es, futures);
			if (!async && Caster.toBooleanValue(res)) rtn.append(v);
		}
		return rtn;
	}

	private static Struct invoke(PageContext pc, Struct sct, UDF udf, ExecutorService es, List<Future<Data<Pair<Object, Object>>>> futures) throws PageException {
		Struct rtn = sct instanceof StructImpl ? new StructImpl(((StructImpl) sct).getType()) : new StructImpl();
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		boolean async = es != null;
		Object res;
		while (it.hasNext()) {
			e = it.next();
			res = _inv(pc, udf, new Object[] { e.getKey().getString(), e.getValue(), sct }, e.getKey(), e.getValue(), es, futures);
			if (!async && Caster.toBooleanValue(res)) rtn.set(e.getKey(), e.getValue());
		}
		return rtn;
	}

	private static Struct invoke(PageContext pc, java.util.Map map, UDF udf, ExecutorService es, List<Future<Data<Pair<Object, Object>>>> futures) throws PageException {
		Struct rtn = new StructImpl();
		Iterator<Entry> it = map.entrySet().iterator();
		Entry e;
		boolean async = es != null;
		Object res;
		while (it.hasNext()) {
			e = it.next();
			res = _inv(pc, udf, new Object[] { e.getKey(), e.getValue(), map }, e.getKey(), e.getValue(), es, futures);
			if (!async && Caster.toBooleanValue(res)) rtn.set(KeyImpl.toKey(e.getKey()), e.getValue());
		}
		return rtn;
	}

	private static Struct invoke(PageContext pc, Iteratorable i, UDF udf, ExecutorService es, List<Future<Data<Pair<Object, Object>>>> futures) throws PageException {
		Iterator<Entry<Key, Object>> it = i.entryIterator();

		Struct rtn = new StructImpl();
		Entry<Key, Object> e;
		boolean async = es != null;
		Object res;
		while (it.hasNext()) {
			e = it.next();
			res = _inv(pc, udf, new Object[] { e.getKey().getString(), e.getValue() }, e.getKey(), e.getValue(), es, futures);
			if (!async && Caster.toBooleanValue(res)) rtn.set(e.getKey(), e.getValue());
		}
		return rtn;
	}

	private static Array invoke(PageContext pc, Iterator it, UDF udf, ExecutorService es, List<Future<Data<Pair<Object, Object>>>> futures) throws PageException {

		Array rtn = new ArrayImpl();
		Object v;
		boolean async = es != null;
		Object res;
		int count = 0;
		ArgumentIntKey k;
		while (it.hasNext()) {
			v = it.next();
			k = ArgumentIntKey.init(++count);
			res = _inv(pc, udf, new Object[] { v }, k, v, es, futures);
			if (!async && Caster.toBooleanValue(res)) rtn.append(v);
		}
		return rtn;
	}

	private static Array invoke(PageContext pc, Enumeration e, UDF udf, ExecutorService es, List<Future<Data<Pair<Object, Object>>>> futures) throws PageException {

		Array rtn = new ArrayImpl();
		Object v;
		boolean async = es != null;
		Object res;
		int count = 0;
		ArgumentIntKey k;
		while (e.hasMoreElements()) {
			v = e.nextElement();
			k = ArgumentIntKey.init(++count);
			res = _inv(pc, udf, new Object[] { v }, k, v, es, futures);
			if (!async && Caster.toBooleanValue(res)) rtn.append(v);
		}
		return rtn;
	}

	private static Object _inv(PageContext pc, UDF udf, Object[] args, Object key, Object value, ExecutorService es, List<Future<Data<Pair<Object, Object>>>> futures)
			throws PageException {
		if (es == null) {
			return udf.call(pc, args, true);
		}
		futures.add(es.submit(new UDFCaller2<Pair<Object, Object>>(pc, udf, args, new Pair<Object, Object>(key, value), true)));
		return null;
	}

	public static void afterCall(PageContext pc, Collection coll, List<Future<Data<Pair<Object, Object>>>> futures, ExecutorService es) throws PageException {
		try {
			boolean isArray = false;
			boolean isQuery = false;
			if (coll instanceof Array) isArray = true;
			else if (coll instanceof Query) isQuery = true;

			Iterator<Future<Data<Pair<Object, Object>>>> it = futures.iterator();
			Data<Pair<Object, Object>> d;
			while (it.hasNext()) {
				d = it.next().get();

				if (Caster.toBooleanValue(d.result)) {
					if (isArray) ((Array) coll).append(d.passed.getValue());
					else if (isQuery) addRow((Query) d.passed.getValue(), (Query) coll, Caster.toIntValue(d.passed.getName()));
					else coll.set(KeyImpl.toKey(d.passed.getName()), d.passed.getValue());
				}

				pc.write(d.output);
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		finally {
			es.shutdown();
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {

		if (args.length == 2) return call(pc, (args[0]), Caster.toFunction(args[1]));
		if (args.length == 3) return call(pc, (args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]));
		if (args.length == 4) return call(pc, (args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]));

		throw new FunctionException(pc, "Filter", 2, 4, args.length);
	}
}
