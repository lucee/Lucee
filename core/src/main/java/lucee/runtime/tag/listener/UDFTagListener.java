package lucee.runtime.tag.listener;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;

public class UDFTagListener extends TagListenerSupport {// UDF before, UDF after

	private UDF before;
	private UDF after;
	private UDF error;

	public UDFTagListener(UDF before, UDF after, UDF error) {
		this.before = before;
		this.after = after;
		this.error = error;
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
	public boolean hasError() {
		return this.error != null;
	}

	@Override
	public Struct error(PageContext pc, Struct args) throws PageException {
		if (this.error != null) return Caster.toStruct(error.callWithNamedValues(pc, args, true), null);
		return null;
	}
}