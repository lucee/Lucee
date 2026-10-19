package lucee.runtime.functions.ast;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.transformer.util.SourceCode;

public final class AstFromString extends BIF {

	private static final long serialVersionUID = 5520676005160311790L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1 || args.length > 2) throw new FunctionException(pc, "AstFromString", 1, 2, args.length);

		String content = Caster.toString(args[0]);
		if (content == null) content = "";

		// mode
		Boolean script = Boolean.FALSE;
		if (args.length == 2) {
			String mode = Caster.toString(args[1], null);
			if ("script".equalsIgnoreCase(mode)) {
				script = Boolean.TRUE;
			}
			else if (mode != null && !"tag".equalsIgnoreCase(mode)) {
				throw new FunctionException(pc, "AstFromString", 2, "mode", "invalid mode [" + mode + "], valid values are [tag, script]");
			}
		}

		return ((PageContextImpl) pc).transform(new SourceCode(null, content, false, 0), script);
	}
}