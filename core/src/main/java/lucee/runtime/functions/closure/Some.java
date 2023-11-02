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

import lucee.runtime.PageContext;
import lucee.runtime.concurrency.Data;
import lucee.runtime.concurrency.UDFCaller2;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayPro;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Iteratorable;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.it.ForEachQueryIterator;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.scope.ArgumentIntKey;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.StringListData;

public class Some extends BIF implements ClosureFunc {

	private static final long serialVersionUID = -5940580562772523622L;

	public static boolean call(PageContext pc, Object obj, UDF udf) throws PageException {
		return _call(pc, obj, udf, false, 20, TYPE_UNDEFINED);
	}

	public static boolean call(PageContext pc, Object obj, UDF udf, boolean parallel) throws PageException {
		return _call(pc, obj, udf, parallel, 20, TYPE_UNDEFINED);
	}

	public static boolean call(PageContext pc, Object obj, UDF udf, boolean parallel, double maxThreads) throws PageException {
		return _call(pc, obj, udf, parallel, (int) maxThreads, TYPE_UNDEFINED);
	}

	public static boolean _call(PageContext pc, Object obj, UDF udf, boolean parallel, int maxThreads, short type) throws PageException {

		ExecutorService execute = null;
		List<Future<Data<Object>>> futures = null;
		if (parallel) {
			execute = Executors.newFixedThreadPool(maxThreads);
			futures = new ArrayList<Future<Data<Object>>>();
		}

		boolean res;

		// Array
		if (type == TYPE_ARRAY) {
			res = invoke(pc, (Array) obj, udf, execute, futures);
		}
		// Query
		else if (type == TYPE_QUERY) {
			res = invoke(pc, (Query) obj, udf, execute, futures);
		}
		// Struct
		else if (type == TYPE_STRUCT) {
			res = invoke(pc, (Struct) obj, udf, execute, futures);
		}
		// Array
		else if (obj instanceof Array && !(obj instanceof Argument)) {
			res = invoke(pc, (Array) obj, udf, execute, futures);
		}
		// Query
		else if (obj instanceof Query) {
			res = invoke(pc, (Query) obj, udf, execute, futures);
		}
		// Struct
		else if (obj instanceof Struct) {
			res = invoke(pc, (Struct) obj, udf, execute, futures);
		}
		// other Iteratorable
		else if (obj instanceof Iteratorable) {
			res = invoke(pc, (Iteratorable) obj, udf, execute, futures);
		}
		// Map
		else if (obj instanceof java.util.Map) {
			res = invoke(pc, (java.util.Map) obj, udf, execute, futures);
		}
		// List
		else if (obj instanceof List) {
			res = invoke(pc, (List) obj, udf, execute, futures);
		}
		// Iterator
		else if (obj instanceof Iterator) {
			res = invoke(pc, (Iterator) obj, udf, execute, futures);
		}
		// Enumeration
		else if (obj instanceof Enumeration) {
			res = invoke(pc, (Enumeration) obj, udf, execute, futures);
		}
		// String List
		else if (obj instanceof StringListData) {
			res = invoke(pc, (StringListData) obj, udf, execute, futures);
		}
		else throw new FunctionException(pc, "Some", 1, "data", "cannot iterate througth this type " + Caster.toTypeName(obj.getClass()));

		if (parallel) res = afterCall(pc, futures, execute);

		return res;
	}

	private static boolean invoke(PageContext pc, Query qry, UDF udf, ExecutorService es, List<Future<Data<Object>>> futures) throws CasterException, PageException {
		final int pid = pc.getId();
		ForEachQueryIterator it = new ForEachQueryIterator(pc, qry, pid);
		boolean async = es != null;
		double r;
		Object res, row;
		try {
			while (it.hasNext()) {
				row = it.next();
				r = Caster.toDoubleValue(qry.getCurrentrow(pid));
				res = _inv(pc, udf, new Object[] { row, r, qry }, r, row, es, futures);
				if (!async && Caster.toBooleanValue(res)) {
					return true;
				}
			}
		}
		finally {
			it.reset();
		}
		return false;
	}

	private static boolean invoke(PageContext pc, Array arr, UDF udf, ExecutorService es, List<Future<Data<Object>>> futures) throws CasterException, PageException {
		Iterator it = (arr instanceof ArrayPro ? ((ArrayPro) arr).entryArrayIterator() : arr.entryIterator());
		Entry e;
		boolean async = es != null;
		Object res;
		while (it.hasNext()) {
			e = (Entry) it.next();
			res = _inv(pc, udf, new Object[] { e.getValue(), Caster.toDoubleValue(e.getKey()), arr }, e.getKey(), e.getValue(), es, futures);
			if (!async && Caster.toBooleanValue(res)) {
				return true;
			}
		}
		return false;
	}

	private static boolean invoke(PageContext pc, StringListData sld, UDF udf, ExecutorService es, List<Future<Data<Object>>> futures) throws CasterException, PageException {
		Array arr = ListUtil.listToArray(sld.list, sld.delimiter, sld.includeEmptyFieldsx, sld.multiCharacterDelimiter);
		Iterator it = (arr instanceof ArrayPro ? ((ArrayPro) arr).entryArrayIterator() : arr.entryIterator());
		Entry e;
		boolean async = es != null;
		Object res;
		while (it.hasNext()) {
			e = (Entry) it.next();
			res = _inv(pc, udf, new Object[] { e.getValue(), Caster.toDoubleValue(e.getKey()), sld.list, sld.delimiter }, e.getKey(), e.getValue(), es, futures);
			if (!async && Caster.toBooleanValue(res)) {
				return true;
			}
		}
		return false;
	}

	private static boolean invoke(PageContext pc, List list, UDF udf, ExecutorService es, List<Future<Data<Object>>> futures) throws CasterException, PageException {
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
			if (!async && Caster.toBooleanValue(res)) return true;
		}
		return false;
	}

	private static boolean invoke(PageContext pc, Struct sct, UDF udf, ExecutorService es, List<Future<Data<Object>>> futures) throws PageException {
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		boolean async = es != null;
		Object res;
		while (it.hasNext()) {
			e = it.next();
			res = _inv(pc, udf, new Object[] { e.getKey().getString(), e.getValue(), sct }, e.getKey(), e.getValue(), es, futures);
			if (!async && Caster.toBooleanValue(res)) return true;
		}
		return false;
	}

	private static boolean invoke(PageContext pc, java.util.Map map, UDF udf, ExecutorService es, List<Future<Data<Object>>> futures) throws PageException {
		Iterator<Entry> it = map.entrySet().iterator();
		Entry e;
		boolean async = es != null;
		Object res;
		while (it.hasNext()) {
			e = it.next();
			res = _inv(pc, udf, new Object[] { e.getKey(), e.getValue(), map }, e.getKey(), e.getValue(), es, futures);
			if (!async && Caster.toBooleanValue(res)) return true;
		}
		return false;
	}

	private static boolean invoke(PageContext pc, Iteratorable i, UDF udf, ExecutorService es, List<Future<Data<Object>>> futures) throws PageException {
		Iterator<Entry<Key, Object>> it = i.entryIterator();

		Entry<Key, Object> e;
		boolean async = es != null;
		Object res;
		while (it.hasNext()) {
			e = it.next();
			res = _inv(pc, udf, new Object[] { e.getKey().getString(), e.getValue() }, e.getKey(), e.getValue(), es, futures);
			if (!async && Caster.toBooleanValue(res)) return true;
		}
		return false;
	}

	private static boolean invoke(PageContext pc, Iterator it, UDF udf, ExecutorService es, List<Future<Data<Object>>> futures) throws PageException {

		Object v;
		boolean async = es != null;
		Object res;
		int count = 0;
		ArgumentIntKey k;
		while (it.hasNext()) {
			v = it.next();
			k = ArgumentIntKey.init(++count);
			res = _inv(pc, udf, new Object[] { v }, k, v, es, futures);
			if (!async && Caster.toBooleanValue(res)) return true;
		}
		return false;
	}

	private static boolean invoke(PageContext pc, Enumeration e, UDF udf, ExecutorService es, List<Future<Data<Object>>> futures) throws PageException {

		Object v;
		boolean async = es != null;
		Object res;
		int count = 0;
		ArgumentIntKey k;
		while (e.hasMoreElements()) {
			v = e.nextElement();
			k = ArgumentIntKey.init(++count);
			res = _inv(pc, udf, new Object[] { v }, k, v, es, futures);
			if (!async && Caster.toBooleanValue(res)) return true;
		}
		return false;
	}

	private static Object _inv(PageContext pc, UDF udf, Object[] args, Object key, Object value, ExecutorService es, List<Future<Data<Object>>> futures) throws PageException {
		if (es == null) {
			return udf.call(pc, args, true);
		}
		futures.add(es.submit(new UDFCaller2<Object>(pc, udf, args, null, true)));
		return null;
	}

	public static boolean afterCall(PageContext pc, List<Future<Data<Object>>> futures, ExecutorService es) throws PageException {
		try {
			Iterator<Future<Data<Object>>> it = futures.iterator();
			Data<Object> d;
			while (it.hasNext()) {
				d = it.next().get();
				if (Caster.toBooleanValue(d.result)) return true;
				pc.write(d.output);
			}
			return false;
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
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]));
		if (args.length == 3) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]));
		if (args.length == 4) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]));

		throw new FunctionException(pc, "Some", 2, 4, args.length);

	}

}