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
 * Implements the CFML Function arrayavg
 */
package lucee.runtime.functions.closure;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lucee.runtime.PageContext;
import lucee.runtime.concurrency.Data;
import lucee.runtime.concurrency.UDFCaller2;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayPro;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Iteratorable;
import lucee.runtime.type.Query;
import lucee.runtime.type.UDF;
import lucee.runtime.type.it.ForEachQueryIterator;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.StringListData;

public final class Each extends BIF implements ClosureFunc {

	private static final long serialVersionUID = 1955185705863596525L;

	public static String call(PageContext pc, Object obj, UDF udf) throws PageException {
		return _call(pc, obj, udf, false, 20, TYPE_UNDEFINED);
	}

	public static String call(PageContext pc, Object obj, UDF udf, boolean parallel) throws PageException {
		return _call(pc, obj, udf, parallel, 20, TYPE_UNDEFINED);
	}

	public static String call(PageContext pc, Object obj, UDF udf, boolean parallel, double maxThreads) throws PageException {
		return _call(pc, obj, udf, parallel, (int) maxThreads, TYPE_UNDEFINED);
	}

	private static String _call(PageContext pc, Object obj, UDF udf, boolean parallel, int maxThreads, short type) throws PageException {
		ExecutorService execute = null;
		List<Future<Data<Object>>> futures = null;
		if (parallel) {
			execute = Executors.newFixedThreadPool(maxThreads);
			futures = new ArrayList<Future<Data<Object>>>();
		}

		// !!!! Don't combine the first 2 ifs with the ifs below, type overrules instanceof check
		// Array
		if (type == TYPE_ARRAY) {
			invoke(pc, (Array) obj, udf, execute, futures);
		}
		// Query
		else if (type == TYPE_QUERY) {
			invoke(pc, (Query) obj, udf, execute, futures);
		}

		// Array
		else if (obj instanceof Array && !(obj instanceof Argument)) {
			invoke(pc, (Array) obj, udf, execute, futures);
		}
		// Query
		else if (obj instanceof Query) {
			invoke(pc, (Query) obj, udf, execute, futures);
		}

		// other Iteratorable
		else if (obj instanceof Iteratorable) {
			invoke(pc, (Iteratorable) obj, udf, execute, futures);
		}
		// Map
		else if (obj instanceof Map) {
			Iterator it = ((Map) obj).entrySet().iterator();
			Entry e;
			while (it.hasNext()) {
				e = (Entry) it.next();
				_call(pc, udf, new Object[] { e.getKey(), e.getValue(), obj }, execute, futures);
				// udf.call(pc, new Object[]{e.getKey(),e.getValue()}, true);
			}
		}
		// List
		else if (obj instanceof List) {
			ListIterator it = ((List) obj).listIterator();
			int index;
			while (it.hasNext()) {
				index = it.nextIndex();
				_call(pc, udf, new Object[] { it.next(), new Double(index), obj }, execute, futures);
				// udf.call(pc, new Object[]{it.next()}, true);
			}
		}

		// Iterator
		else if (obj instanceof Iterator) {
			Iterator it = (Iterator) obj;
			while (it.hasNext()) {
				_call(pc, udf, new Object[] { it.next() }, execute, futures);
				// udf.call(pc, new Object[]{it.next()}, true);
			}
		}
		// Enumeration
		else if (obj instanceof Enumeration) {
			Enumeration e = (Enumeration) obj;
			while (e.hasMoreElements()) {
				_call(pc, udf, new Object[] { e.nextElement() }, execute, futures);
				// udf.call(pc, new Object[]{e.nextElement()}, true);
			}
		}
		// StringListData
		else if (obj instanceof StringListData) {
			invoke(pc, (StringListData) obj, udf, execute, futures);
		}
		// char[]
		else if (obj instanceof char[]) {
			invoke(pc, (char[]) obj, udf, execute, futures);
		}

		else throw new FunctionException(pc, "Each", 1, "data", "cannot iterate througth this type " + Caster.toTypeName(obj.getClass()));

		if (parallel) afterCall(pc, futures, execute);

		return null;
	}

	public static void afterCall(PageContext pc, List<Future<Data<Object>>> futures, ExecutorService es) throws PageException {
		try {
			Iterator<Future<Data<Object>>> it = futures.iterator();
			// Future<String> f;
			while (it.hasNext()) {
				pc.write(it.next().get().output);
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		finally {
			es.shutdown();
		}
	}

	public static void invoke(PageContext pc, Array array, UDF udf, ExecutorService execute, List<Future<Data<Object>>> futures) throws PageException {
		Iterator it = (array instanceof ArrayPro ? ((ArrayPro) array).entryArrayIterator() : array.entryIterator());
		Entry e;
		while (it.hasNext()) {
			e = (Entry) it.next();
			_call(pc, udf, new Object[] { e.getValue(), Caster.toDoubleValue(e.getKey()), array }, execute, futures);
		}
	}

	public static void invoke(PageContext pc, char[] chars, UDF udf, ExecutorService execute, List<Future<Data<Object>>> futures) throws PageException {
		for (int i = 0; i < chars.length; i++) {
			_call(pc, udf, new Object[] { chars[i], Caster.toDoubleValue(i+1), Caster.toString(chars) }, execute, futures);
		}
	}

	public static void invoke(PageContext pc, Query qry, UDF udf, ExecutorService execute, List<Future<Data<Object>>> futures) throws PageException {
		final int pid = pc.getId();
		ForEachQueryIterator it = new ForEachQueryIterator(pc, qry, pid);
		try {
			Object row;
			while (it.hasNext()) {
				row = it.next();
				_call(pc, udf, new Object[] { row, Caster.toDoubleValue(qry.getCurrentrow(pid)), qry }, execute, futures);
			}
		}
		finally {
			it.reset();
		}
	}

	public static void invoke(PageContext pc, Iteratorable coll, UDF udf, ExecutorService execute, List<Future<Data<Object>>> futures) throws PageException {
		Iterator<Entry<Key, Object>> it = coll.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			_call(pc, udf, new Object[] { e.getKey().getString(), e.getValue(), coll }, execute, futures);
			// udf.call(pc, new Object[]{e.getKey().getString(),e.getValue()}, true);
		}
	}

	private static void invoke(PageContext pc, StringListData sld, UDF udf, ExecutorService execute, List<Future<Data<Object>>> futures) throws PageException {
		Array arr = ListUtil.listToArray(sld.list, sld.delimiter, sld.includeEmptyFieldsx, sld.multiCharacterDelimiter);

		Iterator it = (arr instanceof ArrayPro ? ((ArrayPro) arr).entryArrayIterator() : arr.entryIterator());
		Entry e;

		while (it.hasNext()) {
			e = (Entry) it.next();
			_call(pc, udf, new Object[] { e.getValue(), Caster.toDoubleValue(e.getKey()), sld.list, sld.delimiter }, execute, futures);
		}

	}

	private static void _call(PageContext pc, UDF udf, Object[] args, ExecutorService es, List<Future<Data<Object>>> futures) throws PageException {
		if (es == null) {
			udf.call(pc, args, true);
			return;
		}
		futures.add(es.submit(new UDFCaller2<Object>(pc, udf, args, null, true)));
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {

		if (args.length == 2) return call(pc, args[0], Caster.toFunction(args[1]));
		if (args.length == 3) return call(pc, args[0], Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]));
		if (args.length == 4) return call(pc, args[0], Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]));

		throw new FunctionException(pc, "Each", 2, 4, args.length);

	}

}