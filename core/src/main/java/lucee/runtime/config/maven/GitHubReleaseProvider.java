package lucee.runtime.config.maven;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPEngineBasic.HTTPDownloaderHeadResponse;
import lucee.commons.net.http.Header;
import lucee.runtime.op.date.DateCaster;

/**
 * Resolves Lucee core artifacts (jar, light, zero, lco) attached to the GitHub Releases of Lucee.
 * Used as fallback for artifacts that are not available on Maven Central.
 * 
 * The base URL can be changed with the system property / environment variable
 * "lucee.github.releases.url".
 */
public final class GitHubReleaseProvider {

	public static final String DEFAULT_RELEASES_URL = "https://github.com/lucee/Lucee/releases/download/";
	public static final String[] ARTIFACT_KEYS = new String[] { "jar", "light", "zero", "lco" };

	private static final long CACHE_TIMEOUT = 60 * 60 * 1000; // 1 hour
	private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
	private static String releasesUrl;

	public static String getReleasesUrl() {
		if (releasesUrl == null) {
			String str = SystemUtil.getSystemPropOrEnvVar("lucee.github.releases.url", null);
			releasesUrl = toBaseUrl(StringUtil.isEmpty(str, true) ? DEFAULT_RELEASES_URL : str.trim());
		}
		return releasesUrl;
	}

	private static String toBaseUrl(String url) {
		return url.endsWith("/") ? url : url + "/";
	}

	/**
	 * the GitHub release tag of a version, the numeric version without qualifier (e.g. "7.1.2.184" for
	 * "7.1.2.184-RC"), the same tag the release build creates ("Tag published revision" in
	 * .github/workflows/main.yml)
	 */
	public static String getTag(String version) {
		int index = version.indexOf('-');
		return index > 0 ? version.substring(0, index) : version;
	}

	/**
	 * name of the release asset for a given artifact key (jar, light, zero, lco), or null if the key is
	 * unknown
	 */
	public static String getFileName(String version, String key) {
		if ("jar".equals(key)) return "lucee-" + version + ".jar";
		if ("light".equals(key)) return "lucee-light-" + version + ".jar";
		if ("zero".equals(key)) return "lucee-zero-" + version + ".jar";
		if ("lco".equals(key)) return version + ".lco";
		return null;
	}

	public static String toURL(String base, String tag, String version, String key) {
		String fileName = getFileName(version, key);
		if (fileName == null) return null;
		return toBaseUrl(base) + tag + "/" + fileName;
	}

	/**
	 * returns the URLs of all artifacts (jar, light, zero, lco) that exist as GitHub Release asset for
	 * the given version, plus "lastModified" when known. Snapshots are not released on GitHub, so for
	 * them the result is always empty.
	 */
	public static Map<String, Object> detail(Version version) {
		Map<String, Object> result = new LinkedHashMap<>();
		if (version == null || version.is(Version.SNAPSHOT)) return result;

		String v = version.toString();
		String base = getReleasesUrl();
		CacheEntry ce = cache.get(base + v);
		if (ce != null && ce.created + CACHE_TIMEOUT > System.currentTimeMillis()) return new LinkedHashMap<>(ce.data);

		String tag = getTag(v);
		for (String key: ARTIFACT_KEYS) {
			String strURL = toURL(base, tag, v, key);
			try {
				HTTPDownloaderHeadResponse rsp = HTTPEngine.head(new URL(strURL), MavenUpdateProvider.CONNECTION_TIMEOUT, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null);
				if (rsp != null && rsp.getStatusCode() >= 200 && rsp.getStatusCode() < 300) {
					if (!result.containsKey("lastModified")) {
						Header[] headers = rsp.getAllHeaders();
						if (headers != null) {
							for (Header h: headers) {
								if ("Last-Modified".equalsIgnoreCase(h.getName())) {
									Object lm = DateCaster.toDateAdvanced(h.getValue(), null);
									if (lm != null) result.put("lastModified", lm);
								}
							}
						}
					}
					result.put(key, strURL);
				}
			}
			catch (Exception e) {
				LogUtil.log(Log.LEVEL_DEBUG, "github-releases", "Failed to check [" + strURL + "]: " + e.getMessage());
			}
		}
		cache.put(base + v, new CacheEntry(result));
		return new LinkedHashMap<>(result);
	}

	/**
	 * adds the GitHub Release URL for every artifact key (jar, light, zero, lco) missing in the given
	 * detail data, existing entries are not changed
	 */
	public static void fillMissing(Version version, Map<String, Object> data) {
		if (data == null) return;
		boolean missing = false;
		for (String key: ARTIFACT_KEYS) {
			if (StringUtil.isEmpty(data.get(key))) missing = true;
		}
		if (!missing) return;
		for (Map.Entry<String, Object> e: detail(version).entrySet()) {
			if ("lastModified".equals(e.getKey())) continue;
			if (StringUtil.isEmpty(data.get(e.getKey()))) data.put(e.getKey(), e.getValue());
		}
	}

	private static final class CacheEntry {
		private final long created;
		private final Map<String, Object> data;

		private CacheEntry(Map<String, Object> data) {
			this.created = System.currentTimeMillis();
			this.data = data;
		}
	}
}
