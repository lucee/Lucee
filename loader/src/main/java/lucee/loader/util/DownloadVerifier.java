package lucee.loader.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Verifies the integrity of a file downloaded by the loader (LDEV-5864). The loader has no
 * dependencies, so this only uses the JDK.
 * 
 * The expected hash is taken from the response headers (X-Checksum-*, Content-MD5, Digest) or, for a
 * Maven style artifact URL, from the checksum files next to the artifact (.sha512, .sha256, .sha1,
 * .md5). The strongest algorithm available is used. Only a mismatch is an error, if there is nothing
 * to compare with, a warning is logged.
 */
public final class DownloadVerifier {

	private static final Logger LOG = Logger.getLogger("lucee.loader");

	// strongest first
	private static final String[] ALGORITHMS = new String[] { "sha512", "sha256", "sha1", "md5" };
	private static final String[] JAVA_ALGORITHMS = new String[] { "SHA-512", "SHA-256", "SHA-1", "MD5" };
	private static final int[] HEX_LENGTHS = new int[] { 128, 64, 40, 32 };
	private static final String[][] HEADERS = new String[][] { { "X-Checksum-SHA512", "X-Checksum-SHA-512" }, { "X-Checksum-SHA256", "X-Checksum-SHA-256" },
			{ "X-Checksum-SHA1", "X-Checksum-SHA-1" }, { "X-Checksum-MD5", "Content-MD5" } };

	private static final String[] ARTIFACT_EXTENSIONS = new String[] { ".jar", ".lco", ".lex", ".pom", ".war", ".zip" };
	private static final int CONNECT_TIMEOUT = 10000;
	private static final int READ_TIMEOUT = 20000;
	private static final int MAX_CHECKSUM_FILE_SIZE = 1024;

	private DownloadVerifier() {
	}

	/**
	 * Verify a downloaded file.
	 * 
	 * @param url the URL of the download (the final URL in case of a redirect)
	 * @param conn the connection of the download, can be null
	 * @param file the downloaded file
	 * @param siblings if the response headers have no hash, look for checksum files next to an artifact
	 *            URL
	 * @return the algorithm used or null if the download could not be verified
	 * @throws IOException in case of a mismatch, the file is deleted in that case
	 */
	public static String verify(URL url, HttpURLConnection conn, File file, boolean siblings) throws IOException {
		String algorithm = null;
		String expected = null;
		String source = null;
		StringBuilder unavailable = new StringBuilder();

		if (conn != null) {
			for (int i = 0; i < ALGORITHMS.length && expected == null; i++) {
				for (String name: HEADERS[i]) {
					expected = normalize(i, conn.getHeaderField(name));
					if (expected != null) {
						algorithm = ALGORITHMS[i];
						source = "header " + name;
						break;
					}
				}
				if (expected == null) {
					expected = digestHeader(i, conn.getHeaderField("Digest"));
					if (expected != null) {
						algorithm = ALGORITHMS[i];
						source = "header Digest";
					}
				}
			}
		}

		boolean triedSiblings = false;
		if (expected == null && siblings && isArtifactURL(url)) {
			triedSiblings = true;
			for (int i = 0; i < ALGORITHMS.length; i++) {
				URL checksumUrl = null;
				try {
					checksumUrl = new URL(url.toExternalForm() + "." + ALGORITHMS[i]);
					expected = readChecksumFile(i, checksumUrl);
					if (expected != null) {
						algorithm = ALGORITHMS[i];
						source = checksumUrl.toExternalForm();
						break;
					}
				}
				catch (IOException e) {
					unavailable.append(unavailable.length() > 0 ? ", " : "").append(checksumUrl != null ? checksumUrl : url).append(" (").append(e.getClass().getSimpleName())
							.append(": ").append(e.getMessage()).append(")");
				}
			}
		}

		if (expected == null) {
			LOG.log(Level.WARNING,
					"Could not verify the integrity of the download [" + url + "], neither the response headers nor "
							+ (triedSiblings ? "checksum files next to the artifact [.sha512, .sha256, .sha1, .md5]" : "a checksum file") + " provide a hash to compare with"
							+ (unavailable.length() > 0 ? ", failed to read: " + unavailable : "") + ".");
			return null;
		}

		int index = indexOf(algorithm);
		String actual = hash(JAVA_ALGORITHMS[index], file);
		if (!actual.equalsIgnoreCase(expected)) {
			file.delete();
			String label = algorithm.toUpperCase();
			throw new IOException(label + " validation failed: The " + label + " hash [" + expected + "] provided by [" + source + "] for the download [" + url
					+ "] does not match the computed " + label + " hash [" + actual + "] of the downloaded file [" + file + "]. File integrity compromised and file got deleted.");
		}
		return algorithm;
	}

	/**
	 * Download the connection to the given file and verify it. The content is written to a temporary
	 * file first, so there is never an unverified file at the target. The temporary file is deleted when
	 * the download fails or the verification does not pass.
	 */
	public static void download(HttpURLConnection conn, File target) throws IOException {
		File tmp = new File(target.getPath() + ".download");
		boolean success = false;
		try {
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(tmp);
			Util.copy(is, os, true, true);
			verify(conn.getURL(), conn, tmp, true);
			if (!tmp.renameTo(target)) {
				Util.copy(new FileInputStream(tmp), new FileOutputStream(target), true, true);
			}
			success = true;
		}
		finally {
			conn.disconnect();
			if (tmp.exists()) tmp.delete();
		}
	}

	/**
	 * Same as {@link #download(HttpURLConnection, File)}, but returns a stream for the verified
	 * content. The temporary file behind the stream is deleted when the stream is closed.
	 */
	public static InputStream getVerifiedStream(HttpURLConnection conn) throws IOException {
		final File tmp = File.createTempFile("lucee-download", ".tmp");
		try {
			download(conn, tmp);
			return new FilterInputStream(new FileInputStream(tmp)) {
				@Override
				public void close() throws IOException {
					try {
						super.close();
					}
					finally {
						tmp.delete();
					}
				}
			};
		}
		catch (IOException | RuntimeException e) {
			tmp.delete();
			throw e;
		}
	}

	private static boolean isArtifactURL(URL url) {
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

	private static int indexOf(String algorithm) {
		for (int i = 0; i < ALGORITHMS.length; i++) {
			if (ALGORITHMS[i].equals(algorithm)) return i;
		}
		return -1;
	}

	private static String digestHeader(int index, String raw) {
		if (raw == null || raw.trim().length() == 0) return null;
		for (String entry: raw.split(",")) {
			// the value can end with = (base64), so we only split at the first
			int pos = entry.indexOf('=');
			if (pos == -1) continue;
			String name = entry.substring(0, pos).trim().toLowerCase();
			if (name.startsWith("id-")) name = name.substring(3);
			int algorithm;
			if ("sha-512".equals(name) || "sha512".equals(name)) algorithm = 0;
			else if ("sha-256".equals(name) || "sha256".equals(name)) algorithm = 1;
			else if ("sha".equals(name) || "sha-1".equals(name) || "sha1".equals(name)) algorithm = 2;
			else if ("md5".equals(name) || "md-5".equals(name)) algorithm = 3;
			else continue;
			if (algorithm != index) continue;
			String hash = normalize(index, entry.substring(pos + 1));
			if (hash != null) return hash;
		}
		return null;
	}

	/**
	 * @return the value as lower case hex or null if this is not a valid hash for the algorithm, some
	 *         headers (Content-MD5, Digest) are base64 encoded
	 */
	private static String normalize(int index, String value) {
		if (value == null) return null;
		value = value.trim();
		if (value.length() == HEX_LENGTHS[index] && isHex(value)) return value.toLowerCase();
		try {
			byte[] bytes = Base64.getDecoder().decode(value);
			if (bytes.length * 2 == HEX_LENGTHS[index]) return toHex(bytes);
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

	private static String toHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b: bytes) {
			sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
		}
		return sb.toString();
	}

	private static String hash(String algorithm, File file) throws IOException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(algorithm);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IOException("unsupported hash algorithm [" + algorithm + "]", e);
		}
		InputStream is = new FileInputStream(file);
		try {
			byte[] buffer = new byte[8192];
			int len;
			while ((len = is.read(buffer)) != -1) {
				md.update(buffer, 0, len);
			}
		}
		finally {
			Util.closeEL(is);
		}
		return toHex(md.digest());
	}

	/**
	 * @return the hash or null if there is no (valid) checksum file, anything that is not a hash (for
	 *         example an HTML error page delivered with a 200) is not a checksum
	 */
	private static String readChecksumFile(int index, URL checksumUrl) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) checksumUrl.openConnection();
		try {
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", "Lucee");
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
			conn.connect();
			if (conn.getResponseCode() != 200) return null;

			InputStream is = conn.getInputStream();
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[256];
				int len;
				while (baos.size() < MAX_CHECKSUM_FILE_SIZE && (len = is.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
				String str = baos.toString("UTF-8").trim();
				int end = 0;
				while (end < str.length() && !Character.isWhitespace(str.charAt(end)))
					end++;
				String token = str.substring(0, end);
				if (token.length() == HEX_LENGTHS[index] && isHex(token)) return token.toLowerCase();
				return null;
			}
			finally {
				Util.closeEL(is);
			}
		}
		finally {
			conn.disconnect();
		}
	}
}
