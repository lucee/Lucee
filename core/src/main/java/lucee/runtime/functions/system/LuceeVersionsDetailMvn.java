
package lucee.runtime.functions.system;

import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.config.maven.MavenUpdateProvider;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public final class LuceeVersionsDetailMvn extends BIF {

	private static final long serialVersionUID = 1009881259163647851L;

	public static Struct call(PageContext pc, String version) throws PageException {

		Struct sct = new StructImpl();

		try {
			MavenUpdateProvider mup = new MavenUpdateProvider();
			for (Entry<String, Object> e: mup.detail(OSGiUtil.toVersion(version)).entrySet()) {
				sct.set(e.getKey(), e.getValue());
			}

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));

		throw new FunctionException(pc, "LuceeVersionsDetailMvn", 1, 1, args.length);
	}
}