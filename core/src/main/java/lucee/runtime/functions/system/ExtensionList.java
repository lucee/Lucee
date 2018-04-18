package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.type.Query;

public class ExtensionList extends BIF {
	
	private static final long serialVersionUID = 3853910569001016577L;

	public static Query call(PageContext pc) throws PageException {
		ConfigImpl config = (ConfigImpl) pc.getConfig();
		
		Query qry = RHExtension.toQuery(config, ((ConfigWebImpl)pc.getConfig()).getServerRHExtensions(),null);
		RHExtension.toQuery(config, ((ConfigWebImpl)pc.getConfig()).getRHExtensions(),qry);
		
		return qry;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==0) return call(pc);
		else throw new FunctionException(pc, "ExtensionList", 0, 0, args.length);
	}
}
