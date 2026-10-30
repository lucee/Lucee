package lucee.runtime.esapi;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.util.ClassUtilImpl;

public final class ESAPIUtil {

	/**
	 * value of the encodeFor of cfoutput (and this.encodeFor) that means "no encoding"
	 */
	public static final String ENCODE_FOR_NONE = "none";

	private static BIF esapi;

	/**
	 * is the given encodeFor type the value that means no encoding, this is the case for "none" (case
	 * insensitive, trimmed) and for an empty value. This is handled by Lucee itself, the esapi extension
	 * does not need to know this value.
	 */
	public static boolean isNoEncoding(String encodeFor) {
		return StringUtil.isEmpty(encodeFor, true) || ENCODE_FOR_NONE.equalsIgnoreCase(encodeFor.trim());
	}

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
