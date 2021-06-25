package lucee.runtime.functions.system;

import java.lang.reflect.Method;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.image.ImageUtil;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;

public class ExtensionList extends BIF {

	private static final long serialVersionUID = 3853910569001016577L;

	public static Query call(PageContext pc) throws PageException {
		return call(pc, false);
	}
	
	public static Query call(PageContext pc, boolean imageObject) throws PageException {
		ConfigPro config = (ConfigPro) pc.getConfig();

		Query qry = RHExtension.toQuery(config, ((ConfigWebPro) pc.getConfig()).getServerRHExtensions(), null);
		RHExtension.toQuery(config, ((ConfigWebPro) pc.getConfig()).getRHExtensions(), qry);
		if(imageObject) {
			try {
				for(int i=1; i <= qry.getRecordcount(); i++){
					Object image = qry.getAt("image", i, null);
					if( !StringUtil.isEmpty(image, true) ) {
						byte[] imgByte = Caster.toBinary(image);
						ImageUtil img = new ImageUtil();
						Method m = img.getClass().getDeclaredMethod("toImage",new Class[] { PageContext.class, Object.class, boolean.class });
						m.setAccessible(true);
						qry.setAt("image", i, m.invoke(null, new Object[] { pc, imgByte, false }));
					}
				}
			}
			catch (Exception e) {
			}
		}
		return qry;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		if (args.length == 1) return call(pc, Caster.toBoolean(args[0]));
		else throw new FunctionException(pc, "ExtensionList", 0, 1, args.length);
	}
}
