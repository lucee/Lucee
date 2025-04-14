package lucee.runtime.process;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.UDF;

public class UDFProcessListener implements ProcessListener {

	private UDF listener;
	private PageContext pc;

	public UDFProcessListener(PageContext pc, UDF listener) {
		this.pc = pc;
		this.listener = listener;
	}

	@Override
	public void listen(String response) throws PageException {
		listener.call(pc, new Object[] { response }, true);
	}
}