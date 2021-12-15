package lucee.commons.cpu;

import java.util.List;

import lucee.commons.cpu.CPULogger.StaticData;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.UDF;

public class UDFListener extends CFMLListener {

	private UDF udf;

	public UDFListener(UDF udf) {
		this.udf = udf;
	}

	@Override
	public void _listen(PageContext pc, List<StaticData> sd) throws PageException {
		udf.call(pc, new Object[] { toQuery(sd) }, true);
	}
}
