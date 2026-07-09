package lucee.runtime.util;

import java.lang.ref.Reference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;
import lucee.commons.collection.RefMap;
import lucee.commons.collection.RefMap.ReferenceType;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

public final class CFIDUtil {
	public static Boolean IDENTIFY_CLIENT_DEFAULT = Boolean.FALSE;
	private static Boolean identifyClient;

	private static Map<String, String> clients = new RefMap<>(ReferenceType.SOFT, new ConcurrentHashMap<String, Reference<String>>(100));

	public static boolean isCFID(PageContext pc, Object obj) {
		return Decision.isGUIdSimple(obj);
	}

	public static boolean isCFToken(PageContext pc, Object obj) {
		if (obj == null) return false;
		String str = obj.toString().trim();
		return str.equals("0") || str.length() == 12;
	}

	public static String createCFID(PageContext pc) {
		return UUID.randomUUID().toString();
	}

	public static String createCFToken(PageContext pc) {

		if (identifyClient == null) identifyClient = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.identify.client", null), IDENTIFY_CLIENT_DEFAULT);
		if (identifyClient.equals(Boolean.FALSE)) return "0";

		return createClientBasedCFToken(pc);
	}

	private static String createClientBasedCFToken(PageContext pc) {
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

		if (StringUtil.isEmpty(str, true)) return "0";
		String val = clients.get(str);
		if (val != null) return val;

		val = HashUtil.create64BitHashAsString(str, Character.MAX_RADIX);
		if (val.length() > 12) {
			val = val.substring(0, 12);
		}
		while (val.length() < 12) {
			val = val + "0";
		}
		clients.put(str, val);
		return val;
	}

}
