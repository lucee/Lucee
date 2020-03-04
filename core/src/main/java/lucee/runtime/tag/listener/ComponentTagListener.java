package lucee.runtime.tag.listener;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;

public class ComponentTagListener extends TagListenerSupport {

	private Component component;

	public ComponentTagListener(Component component) {
		this.component = component;
	}

	@Override
	public Struct before(PageContext pc, Struct args) throws PageException {
		if (component.get("before", null) instanceof UDF) return Caster.toStruct(component.callWithNamedValues(pc, "before", args), null);
		return null;
	}

	@Override
	public Struct after(PageContext pc, Struct args) throws PageException {
		if (component.get("after", null) instanceof UDF) return Caster.toStruct(component.callWithNamedValues(pc, "after", args), null);
		else if (component.get("listen", null) instanceof UDF) return Caster.toStruct(component.callWithNamedValues(pc, "listen", args), null);
		return null;
	}

	@Override
	public boolean hasError() {
		return component.get("error", null) instanceof UDF;
	}

	@Override
	public Struct error(PageContext pc, Struct args) throws PageException {
		if (component.get("error", null) instanceof UDF) return Caster.toStruct(component.callWithNamedValues(pc, "error", args), null);
		return null;
	}
}
