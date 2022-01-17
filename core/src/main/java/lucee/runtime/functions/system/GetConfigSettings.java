package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class GetConfigSettings extends BIF {

	private static final long serialVersionUID = 267379882500267699L;

	public static Struct call(PageContext pc) throws PageException {

		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		{
			Struct log4j = new StructImpl(Struct.TYPE_LINKED);
			log4j.setEL(KeyConstants._version, ((ConfigPro) pc.getConfig()).getLogEngine().getVersion());

			sct.setEL("log4j", log4j);
		}
		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		throw new FunctionException(pc, "GetConfigSettings", 0, 0, args.length);
	}
}