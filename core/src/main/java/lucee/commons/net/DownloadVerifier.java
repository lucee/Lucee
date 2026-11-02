package lucee.commons.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.digest.Hash;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.header.HeadersCollection;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.HTTPResponse;

/**
 * Verifies the integrity of a downloaded file (LDEV-5864).
 * 
 * The expected hash is taken from the response headers of the download (X-Checksum-*, Content-MD5,
 * Digest) or, for a Maven style artifact URL, from the checksum files next to the artifact
 * (.sha512, .sha256, .sha1, .md5). The strongest available algorithm is used.
 * 
 * Only a mismatch is an error. When there is nothing to verify against, a warning is logged and the
 * download is accepted, so repositories that do not provide checksums keep working.
 */
public final class DownloadVerifier {

	// strongest first
	private static final String[] ALGORITHMS = new String[] { "sha512", "sha256", "sha1", "md5" };

	private static final int CONNECT_TIMEOUT = 10000;
	private static final int READ_TIMEOUT = 20000;
	private static final int MAX_CHECKSUM_FILE_SIZE = 1024;
	private static final int MAX_WARNED = 1000;

	private static final String[] ARTIFACT_EXTENSIONS = new String[] { ".jar", ".lco", ".lex", ".pom", ".war", ".zip" };

	private static final Set<String> warned = ConcurrentHashMap.newKeySet();

	private DownloadVerifier() {
	}

	public static final class Result {

		private final String algorithm;
		private final String source;

		private Result(String algorithm, String source) {
			this.algorithm = algorithm;
			this.source = source;
		}

		public boolean isVerified() {
			return algorithm != null;
		}

		/**
		 * @return the algorithm used (sha512, sha256, sha1, md5) or null when the download was not verified
		 */
		public String getAlgorithm() {
			return algorithm;
		}

		/**
		 * @return where the expected hash came from (a header name or a URL) or null when the download was
		 *         not verified
		 */
		public String getSource() {
			return source;
		}

		@Override
		public String toString() {
			return isVerified() ? "verified:" + algorithm + ":" + source : "unverified";
		}
	}

	private static final class Expected {
		private final String algorithm;
		private final String hash; // lower case hex
		private final String source;

		private Expected(String algorithm, String hash, String source) {
			this.algorithm = algorithm;
			this.hash = hash;
			this.source = source;
		}
	}

	/**
	 * is this the URL of an artifact in a Maven style repository, so that we can expect checksum files
	 * next to it?
	 */
	public static boolean isArtifactURL(URL url) {
		if (url == null || url.getQuery() != null) return false;
		String protocol = url.getProtocol();
		if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) return false;
		String path = url.getPath();
		if (path == null) return false;
		path = path.toLowerCase();
		for (String ext: ARTIFACT_EXTENSIONS) {
			if (path.endsWith(ext)) return true;
		}
		return false;
	}

	/**
	 * the URL the content was delivered from, which is not the requested URL when the request was
	 * redirected (the checksum files are next to the final artifact)
	 */
	public static URL finalURL(HTTPResponse rsp, URL requested) {
		URL target = rsp == null ? null : rsp.getTargetURL();
		if (target != null) return target;
		return requested;
	}

	/**
	 * Verify a downloaded file.
	 * 
	 * @param url the URL the file was downloaded from
	 * @param headers the headers of the download response, can be null
	 * @param file the downloaded file
	 * @param pinnedChecksum a checksum defined by the caller in the form algorithm:hash (for example from
	 *            a dependency definition), can be null
	 * @param siblings if no header provides a hash, look for the checksum files next to an artifact URL
	 * @param deleteFileWhenInvalid delete the file in case of a mismatch
	 * @param log log to write the warning to, when null the log of the current context is used
	 * @return the result of the verification, never null
	 * @throws IOException in case of a mismatch
	 */
	public static Result verify(URL url, HeadersCollection headers, Resource file, String pinnedChecksum, boolean siblings, boolean deleteFileWhenInvalid, Log log)
			throws IOException {

		// the checksum defined by the caller has to match whatever the server tells us
		if (!StringUtil.isEmpty(pinnedChecksum, true)) {
			Expected pinned = parsePinned(pinnedChecksum);
			if (pinned != null) compare(url, pinned, file, deleteFileWhenInvalid);
		}

		Expected expected = headers == null ? null : fromHeaders(headers);
		StringBuilder unavailable = new StringBuilder();
		if (expected == null && siblings && isArtifactURL(url)) expected = fromSiblings(url, unavailable);

		if (expected == null) {
			warnOnce(url, siblings && isArtifactURL(url), unavailable, log);
			return new Result(null, null);
		}
		compare(url, expected, file, deleteFileWhenInvalid);
		return new Result(expected.algorithm, expected.source);
	}

	private static void compare(URL url, Expected expected, Resource file, boolean deleteFileWhenInvalid) throws IOException {
		String actual = hash(expected.algorithm, file);
		if (actual.equalsIgnoreCase(expected.hash)) return;

		if (deleteFileWhenInvalid) file.remove(true);
		String label = expected.algorithm.toUpperCase();
		throw new IOException(label + " validation failed: The " + label + " hash [" + expected.hash + "] provided by [" + expected.source + "] for the download [" + url
				+ "] does not match the computed " + label + " hash [" + actual + "] of the downloaded file [" + file + "]. File integrity compromised"
				+ (deleteFileWhenInvalid ? " and file got deleted" : "") + ".");
	}

	private static String hash(String algorithm, Resource file) throws IOException {
		if ("sha512".equals(algorithm)) return Hash.sha512(file);
		if ("sha256".equals(algorithm)) return Hash.sha256(file);
		if ("sha1".equals(algorithm)) return Hash.sha1(file);
		if ("md5".equals(algorithm)) return Hash.md5(file);
		throw new IOException("unsupported hash algorithm [" + algorithm + "]");
	}

	private static Expected fromHeaders(HeadersCollection headers) {
		for (String algorithm: ALGORITHMS) {
			for (String name: headerNames(algorithm)) {
				Expected e = fromHeader(headers, name, algorithm);
				if (e != null) return e;
			}
			// the Digest header (RFC 3230), a list of algorithm=value
			Expected e = fromDigestHeader(headers, algorithm);
			if (e != null) return e;
		}
		return null;
	}

	private static String[] headerNames(String algorithm) {
		switch (algorithm) {
		case "sha512":
			return new String[] { "X-Checksum-SHA512", "X-Checksum-SHA-512" };
		case "sha256":
			return new String[] { "X-Checksum-SHA256", "X-Checksum-SHA-256" };
		case "sha1":
			return new String[] { "X-Checksum-SHA1", "X-Checksum-SHA-1" };
		default:
			return new String[] { "X-Checksum-MD5", "Content-MD5" };
		}
	}

	private static Expected fromHeader(HeadersCollection headers, String name, String algorithm) {
		Header[] hs = headers.getHeaders(name);
		if (hs == null) return null;
		for (Header h: hs) {
			String hash = normalize(algorithm, h.getValue());
			if (hash != null) return new Expected(algorithm, hash, "header " + name);
		}
		return null;
	}

	private static Expected fromDigestHeader(HeadersCollection headers, String algorithm) {
		Header[] hs = headers.getHeaders("Digest");
		if (hs == null) return null;
		for (Header h: hs) {
			String raw = h.getValue();
			if (StringUtil.isEmpty(raw, true)) continue;
			for (String entry: raw.split(",")) {
				// the value can end with = (base64), so we only split at the first
				int index = entry.indexOf('=');
				if (index == -1) continue;
				if (!algorithm.equals(digestAlgorithm(entry.substring(0, index)))) continue;
				String hash = normalize(algorithm, entry.substring(index + 1));
				if (hash != null) return new Expected(algorithm, hash, "header Digest");
			}
		}
		return null;
	}

	private static String digestAlgorithm(String name) {
		name = name.trim().toLowerCase();
		if (name.startsWith("id-")) name = name.substring(3);
		switch (name) {
		case "sha-512":
		case "sha512":
			return "sha512";
		case "sha-256":
		case "sha256":
			return "sha256";
		case "sha":
		case "sha-1":
		case "sha1":
			return "sha1";
		case "md5":
		case "md-5":
			return "md5";
		default:
			return null;
		}
	}

	private static int hexLength(String algorithm) {
		switch (algorithm) {
		case "sha512":
			return 128;
		case "sha256":
			return 64;
		case "sha1":
			return 40;
		default:
			return 32;
		}
	}

	/**
	 * @return the value as lower case hex or null if this is not a valid hash for the algorithm. Some
	 *         headers (Content-MD5, Digest) are base64 encoded.
	 */
	private static String normalize(String algorithm, String value) {
		if (value == null) return null;
		value = value.trim();
		int hexLength = hexLength(algorithm);
		if (value.length() == hexLength && isHex(value)) return value.toLowerCase();
		try {
			byte[] bytes = Base64.getDecoder().decode(value);
			if (bytes.length * 2 == hexLength) return Hash.toHexString(bytes, false);
		}
		catch (IllegalArgumentException e) {
			// not base64, so not a hash for this algorithm
		}
		return null;
	}

	private static boolean isHex(String str) {
		char c;
		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) return false;
		}
		return true;
	}

	/**
	 * a checksum defined by the caller, algorithm:hash or algorithm-hash
	 */
	private static Expected parsePinned(String checksum) {
		checksum = checksum.trim();
		int index = checksum.indexOf(':');
		if (index == -1) index = checksum.indexOf('-');
		if (index == -1) return null;
		String algorithm = digestAlgorithm(checksum.substring(0, index));
		if (algorithm == null) return null;
		String hash = normalize(algorithm, checksum.substring(index + 1));
		if (hash == null) return null;
		return new Expected(algorithm, hash, "the defined checksum");
	}

	private static Expected fromSiblings(URL url, StringBuilder unavailable) {
		for (String algorithm: ALGORITHMS) {
			URL checksumUrl = null;
			HTTPResponse rsp = null;
			try {
				checksumUrl = new URL(url.toExternalForm() + "." + algorithm);
				rsp = HTTPEngine.getResponse(checksumUrl, null, null, CONNECT_TIMEOUT, READ_TIMEOUT, null);
				if (rsp.getStatusCode() != 200) continue;
				String hash = parseChecksumFile(algorithm, rsp.getContentAsStream());
				if (hash != null) return new Expected(algorithm, hash, checksumUrl.toExternalForm());
			}
			catch (IOException e) {
				// we try the next algorithm, the problem ends up in the warning if nothing else is found
				unavailable.append(unavailable.length() > 0 ? ", " : "").append(checksumUrl != null ? checksumUrl : url).append(" (").append(e.getClass().getSimpleName()).append(": ")
						.append(e.getMessage()).append(")");
			}
			finally {
				HTTPEngine.releaseEL(rsp);
			}
		}
		return null;
	}

	/**
	 * a checksum file contains the hash, optionally followed by the name of the file. Anything else
	 * (for example an HTML error page delivered with a 200) is not a checksum.
	 */
	private static String parseChecksumFile(String algorithm, InputStream is) throws IOException {
		if (is == null) return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[256];
		int len;
		while (baos.size() < MAX_CHECKSUM_FILE_SIZE && (len = is.read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		String str = baos.toString("UTF-8").trim();
		if (str.isEmpty()) return null;
		int end = 0;
		while (end < str.length() && !Character.isWhitespace(str.charAt(end))) end++;
		String token = str.substring(0, end);
		if (token.length() == hexLength(algorithm) && isHex(token)) return token.toLowerCase();
		return null;
	}

	private static void warnOnce(URL url, boolean triedSiblings, StringBuilder unavailable, Log log) {
		String key = url.toExternalForm();
		if (warned.size() > MAX_WARNED) warned.clear();
		if (!warned.add(key)) return;
		String msg = "Could not verify the integrity of the download [" + key + "], neither the response headers nor "
				+ (triedSiblings ? "checksum files next to the artifact [.sha512, .sha256, .sha1, .md5]" : "a checksum file") + " provide a hash to compare with"
				+ (unavailable.length() > 0 ? ", failed to read: " + unavailable : "") + ".";
		if (log != null) log.log(Log.LEVEL_WARN, "download", msg);
		else LogUtil.log(Log.LEVEL_WARN, "download", "verify", msg);
	}

	// for testing, so a warning shows up again
	public static void resetWarned() {
		warned.clear();
	}
}
