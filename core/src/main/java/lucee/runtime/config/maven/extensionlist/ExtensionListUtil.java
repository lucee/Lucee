package lucee.runtime.config.maven.extensionlist;

import java.io.IOException;
import java.util.regex.Pattern;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;

final class ExtensionListUtil {

	private static final Pattern COORDINATE_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
	private static final String LOG_NAME = "extension-provider";
	private static final String LOG_CATEGORY = "mvn";

	private ExtensionListUtil() {}

	static void validateGroupId(String groupId) throws IOException {
		if (StringUtil.isEmpty(groupId, true) || groupId.contains("..") || groupId.contains("/") || !COORDINATE_PATTERN.matcher(groupId).matches()) {
			throw new IOException("invalid groupId [" + groupId + "]");
		}
	}

	static boolean isValidArtifactId(String artifactId) {
		return !StringUtil.isEmpty(artifactId, true) && !artifactId.contains("..") && !artifactId.contains("/") && COORDINATE_PATTERN.matcher(artifactId).matches();
	}

	static void logDebug(String lister, String msg) {
		Log log = LogUtil.getLog(null, LOG_NAME, LOG_CATEGORY);
		if (LogUtil.doesDebug(log)) {
			log.debug(LOG_CATEGORY, "[" + lister + "] " + msg);
		}
	}

	static void logInfo(String lister, String msg) {
		Log log = LogUtil.getLog(null, LOG_NAME, LOG_CATEGORY);
		if (LogUtil.doesInfo(log)) {
			log.info(LOG_CATEGORY, "[" + lister + "] " + msg);
		}
	}

	static void logWarn(String lister, String msg, Throwable t) {
		LogUtil.log((Config) null, "[" + lister + "] " + msg, t, Log.LEVEL_WARN, LOG_NAME);
	}

}
