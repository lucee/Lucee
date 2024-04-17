package lucee.runtime.util;

import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.HARD;
import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.SOFT;

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.map.ReferenceMap;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;

public class CFIDUtil {
	public static Boolean IDENTIFY_CLIENT_DEFAULT = Boolean.FALSE;
	private static Boolean identifyClient;

	private static Map<String, String> clients = new ReferenceMap<String, String>(HARD, SOFT, 100, 0.75f);

	public static boolean isCFID(PageContext pc, Object obj) {
		String str = Caster.toString(obj, null);
		if (str == null) return false;

		if (str.length() != 36) return false;
		if (str.charAt(8) != '-' || str.charAt(13) != '-' || str.charAt(18) != '-' || str.charAt(23) != '-') return false;

		// client type id start with "z"
		String last = str.substring(24);
		if (last.length() != 12) return false;
		if (last.charAt(0) != 'x') return true;
		String cp = clientPart(pc);
		if (cp == null) return true;

		return cp.equals(last);
	}

	public static String createCFID(PageContext pc) {

		if (identifyClient == null) identifyClient = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.identify.client", null), IDENTIFY_CLIENT_DEFAULT);

		if (identifyClient.equals(Boolean.FALSE)) return UUID.randomUUID().toString();
		String cp = clientPart(pc);
		if (cp == null) return UUID.randomUUID().toString();

		String cfid = UUID.randomUUID().toString();
		return cfid.substring(0, cfid.length() - 12) + cp;
	}

	private static String clientPart(PageContext pc) {
		if (pc == null) pc = ThreadLocalPageContext.get();
		String str = null;
		if (pc != null) {
			HttpServletRequest req = pc.getHttpServletRequest();
			if (req != null) {
				str = req.getHeader("User-Agent");
				if (str == null) str = req.getHeader("user-agent");
				if (str == null) str = req.getHeader("accept");
			}
		}
		// str = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko)
		// Chrome/123.0.0.0 Safari/537.36";

		if (StringUtil.isEmpty(str, true)) return null;
		String val = clients.get(str);
		if (val != null) return val;

		val = HashUtil.create64BitHashAsString(str, Character.MAX_RADIX);
		if (val.length() > 11) {
			val = val.substring(0, 11);
		}
		while (val.length() < 11) {
			val = val + "0";
		}
		clients.put(str, val = "x" + val);
		return val;
	}

}
