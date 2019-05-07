package lucee.runtime.esapi;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.util.ClassUtilImpl;

public class ESAPIUtil {

	private static BIF esapi;

	public static String esapiEncode(PageContext pc, String encodeFor, String string) throws PageException {
		// we need to get the BIF ESAPIEncode
		try {
			if (esapi == null) esapi = new ClassUtilImpl().loadBIF(pc, "org.lucee.extension.esapi.functions.ESAPIEncode", "esapi.extension", null);
			return (String) esapi.invoke(pc, new Object[] { encodeFor, string });
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
}
