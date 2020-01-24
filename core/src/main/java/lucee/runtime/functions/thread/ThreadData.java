package lucee.runtime.functions.thread;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Struct;

public class ThreadData extends BIF {

	/**
	 * Verify if in thread or not
	 * 
	 * @param pc
	 * @return
	 * @throws PageException
	 */
	public static Struct call(PageContext pc) throws PageException {
		PageContextImpl pci = (PageContextImpl) pc;
		PageContextImpl root = (PageContextImpl) pci.getRootPageContext();
		if (root == null) root = pci;
		return root.getCFThreadScope();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		else throw new FunctionException(pc, "ThreadData", 0, 0, args.length);
	}

}
