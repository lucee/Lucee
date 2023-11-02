package lucee.runtime.type.scope;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.type.StructImpl;

public class CFThread extends StructImpl {

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

}
