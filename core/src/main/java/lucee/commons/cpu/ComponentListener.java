package lucee.commons.cpu;

import java.util.List;

import lucee.commons.cpu.CPULogger.StaticData;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public class ComponentListener extends CFMLListener {

	private Component cfc;

	public ComponentListener(Component cfc) {
		this.cfc = cfc;
	}

	@Override
	public void _listen(PageContext pc, List<StaticData> sd) throws PageException {
		cfc.call(pc, "listen", new Object[] { toQuery(sd) });
	}
}