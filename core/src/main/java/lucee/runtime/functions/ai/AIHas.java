package lucee.runtime.functions.ai;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class AIHas extends BIF {

	private static final long serialVersionUID = 6532201888958323478L;

	public static boolean call(PageContext pc, String nameAI) {
		PageContextImpl pci = (PageContextImpl) pc;

		if (nameAI.startsWith("default:")) {
			nameAI = pci.getNameFromDefault(nameAI.substring(8), null);
			if (nameAI == null) return false;
		}
		return pci.getAIEngine(nameAI, null) != null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		throw new FunctionException(pc, "AIHas", 1, 1, args.length);
	}

}