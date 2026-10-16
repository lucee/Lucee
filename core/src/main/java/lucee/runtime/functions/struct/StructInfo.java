
package lucee.runtime.functions.struct;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class StructInfo extends BIF {

	private static final long serialVersionUID = 6837257606513875592L;

	public static Struct call(PageContext pc, Struct struct) {
		Struct sct = new StructImpl();
		sct.setEL(KeyConstants._hash, struct.hashCode());
		sct.setEL(KeyConstants._class, struct.getClass().getName());
		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toStruct(args[0]));
		throw new FunctionException(pc, "StructInfo", 1, 1, args.length);
	}
}