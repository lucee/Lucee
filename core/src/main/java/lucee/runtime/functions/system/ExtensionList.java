package lucee.runtime.functions.system;

import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;
import lucee.runtime.type.util.KeyConstants;

public class ExtensionList extends BIF {

	private static final long serialVersionUID = 3853910569001016577L;

	public static Query call(PageContext pc) throws PageException {
		return call(pc, false);
	}

	public static Query call(PageContext pc, boolean imageObject) throws PageException {
		ConfigPro config = (ConfigPro) pc.getConfig();
		BIF bif = null;
		boolean bifLoaded = false;

		Query qry = RHExtension.toQuery(config, ((ConfigWebPro) pc.getConfig()).getServerRHExtensions(), null);
		RHExtension.toQuery(config, ((ConfigWebPro) pc.getConfig()).getRHExtensions(), qry);
		if (imageObject) {
			try {
				for (int i = 1; i <= qry.getRecordcount(); i++) {
					Object image = qry.getAt("image", i, null);
					if (!StringUtil.isEmpty(image, true)) {
						// image stuff is in a OPTIONAL extension
						if (!bifLoaded) {
							bif = getBIF(pc);
							bifLoaded = true;
						}
						if (bif != null) {
							Object res = bif.invoke(pc, new Object[] { image });
							if (res != null) {
								qry.setAt(KeyConstants._image, i, res);
							}
						}
					}
				}
			}
			catch (Exception e) {
			}
		}
		return qry;
	}

	private static BIF getBIF(PageContext pc) {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		try {
			return eng.getClassUtil().loadBIF(pc, "org.lucee.extension.image.functions.ImageRead");
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		if (args.length == 1) return call(pc, Caster.toBoolean(args[0]));
		else throw new FunctionException(pc, "ExtensionList", 0, 1, args.length);
	}
}
