package lucee.runtime.type.scope;

import java.util.Iterator;
import java.util.Map;

import lucee.commons.io.SystemUtil;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.op.Caster;
import lucee.runtime.type.StructImpl;

public class CFThread extends StructImpl {

	private static final int MAX_THREADS_STORED_DEFAULT = 10000;
	private static int maxThreadStored = -1;

	static {
		maxThreadStored = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.tasks.limit", null), MAX_THREADS_STORED_DEFAULT);
		if (maxThreadStored <= 0) maxThreadStored = MAX_THREADS_STORED_DEFAULT;
	}

	public CFThread() {
		super(TYPE_LINKED);
	}

	public static int getThreadLimit() {
		return maxThreadStored;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		DumpData dd = super.toDumpData(pageContext, maxlevel, properties);
		if (dd instanceof DumpTable) { // always the case ...
			DumpTable dt = (DumpTable) dd;
			dt.setTitle("Scope CFThread");
			dt.setComment(
					"CFthread only provides the direct children of the current thread, to get all threads (parent and sister threads) use the function [threadData] instead.");
		}
		return dd;
	}

	public void removeOldest() {
		Iterator<Entry<Key, Object>> it = entryIterator();
		if (it.hasNext()) {
			it.next();
			it.remove();
		}
	}

	public static void removeOldest(Map map) {
		Iterator<Entry<Key, Object>> it = map.entrySet().iterator();
		if (it.hasNext()) {
			it.next();
			it.remove();
		}
	}
}
