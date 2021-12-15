package lucee.runtime.functions.system;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.debug.Debugger;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;

public class DebugAdd extends BIF {

	private static final long serialVersionUID = 3480038887443615199L;

	public static String call(PageContext pc, String category, Struct data) throws PageException {
		Debugger debugger = pc.getDebugger();

		debugger.addGenericData(category, toMapStrStr(data));
		return null;
	}

	private static Map<String, String> toMapStrStr(Struct struct) throws PageException {
		Iterator<Entry<Key, Object>> it = struct.entryIterator();
		Map<String, String> map = new HashMap<String, String>();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			map.put(e.getKey().getString(), Caster.toString(e.getValue()));
		}
		return map;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toStruct(args[1]));
		throw new FunctionException(pc, "DebugAdd", 2, 2, args.length);
	}
}
