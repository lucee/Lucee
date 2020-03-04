package lucee.runtime.tag.listener;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;

public class UDFTagListener extends TagListenerSupport {// UDF before, UDF after

	private UDF before;
	private UDF after;
	private UDF fail;

	public UDFTagListener(UDF before, UDF after, UDF fail) {
		this.before = before;
		this.after = after;
		this.fail = fail;
	}

	@Override
	public Struct before(PageContext pc, Struct args) throws PageException {
		if (before != null) return Caster.toStruct(before.callWithNamedValues(pc, args, true), null);
		return null;
	}

	@Override
	public Struct after(PageContext pc, Struct args) throws PageException {
		if (this.after != null) return Caster.toStruct(after.callWithNamedValues(pc, args, true), null);
		return null;
	}

	@Override
	public Struct fail(PageContext pc, Struct args) throws PageException {
		if (this.fail != null) return Caster.toStruct(fail.callWithNamedValues(pc, args, true), null);
		return null;
	}
}