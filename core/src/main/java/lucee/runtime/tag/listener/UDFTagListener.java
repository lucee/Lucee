package lucee.runtime.tag.listener;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;

public class UDFTagListener extends TagListenerSupport {// UDF before, UDF after

    private UDF before;
    private UDF after;

    public UDFTagListener(UDF before, UDF after) {
	this.before = before;
	this.after = after;
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
}