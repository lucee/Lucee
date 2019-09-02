package lucee.runtime.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.UDFUtil;

public class Closure extends EnvUDF {

	public Closure() {// used for externalize
		super();
	}

	public Closure(UDFProperties properties) {
		super(properties);
	}

	private Closure(UDFProperties properties, Variables variables) { // used for duplicate
		super(properties, variables);
	}

	@Override
	public DumpData _toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return UDFUtil.toDumpData(pageContext, maxlevel, dp, this, UDFUtil.TYPE_CLOSURE);
	}

	@Override
	public Struct _getMetaData(PageContext pc) throws PageException {
		Struct meta = ComponentUtil.getMetaData(pc, properties);
		meta.setEL(KeyConstants._closure, Boolean.TRUE);// MUST move this to class UDFProperties
		meta.setEL("ANONYMOUSCLOSURE", Boolean.TRUE);// MUST move this to class UDFProperties
		return meta;
	}

	@Override
	public UDF _duplicate(Component c) {
		Closure clo = new Closure(properties, variables);// TODO duplicate variables as well?
		clo.ownerComponent = c;
		clo.setAccess(getAccess());
		return clo;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
	}
}