package lucee.runtime.functions.other;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;

public class ApplicationPathCacheClear {
	public static String call(PageContext pc) {
		ConfigPro config = (ConfigPro) pc.getConfig();
		config.clearApplicationCache();
		return null;
	}
}