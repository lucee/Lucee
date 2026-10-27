package lucee.runtime.config;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.engine.ThreadLocalPageContext;

/**
 * Deploys the trust store bundled with Lucee to lucee-server/context/security/cacerts and keeps
 * existing copies up to date, as long as they were never modified (LDEV-6741).
 */
public final class BundledTrustStore {

	public static final String RESOURCE = "/resource/security/cacerts";
	public static final String PREVIOUS = "/resource/security/cacerts.previous.txt";

	private static Set<String> previous;

	private BundledTrustStore() {
	}

	public static Resource deploy(Resource secDir) {
		if (!secDir.exists()) secDir.mkdirs();
		Resource file = secDir.getRealResource("cacerts");
		if (file.length() == 0L) ConfigFactory.createFileFromResourceEL(RESOURCE, file);
		else {
			try {
				updateIfUnmodified(file);
			}
			catch (IOException e) {
				LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), BundledTrustStore.class.getName(), e);
			}
		}
		return file;
	}

	/**
	 * replaces the given trust store with the currently bundled one, but only when it is byte
	 * identical to a version Lucee bundled in the past.
	 * 
	 * @return true when the file got replaced
	 */
	public static boolean updateIfUnmodified(Resource file) throws IOException {
		if (!file.isFile()) return false;
		if (!isPreviousBundledVersion(file)) return false;

		byte[] current = getBundled();
		if (current == null) return false;

		Resource tmp = file.getParentResource().getRealResource(file.getName() + ".tmp");
		try {
			IOUtil.copy(new ByteArrayInputStream(current), tmp, true);
			ResourceUtil.moveTo(tmp, file, true);
		}
		finally {
			if (tmp.exists()) tmp.delete();
		}
		LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, BundledTrustStore.class.getName(),
				"Updated unmodified trust store [" + file + "] to the version bundled with this Lucee release");
		return true;
	}

	public static boolean isPreviousBundledVersion(Resource file) throws IOException {
		return getPreviousHashes().contains(sha256(file));
	}

	public static byte[] getBundled() throws IOException {
		InputStream is = getResourceAsStream(RESOURCE);
		if (is == null) return null;
		return IOUtil.toBytes(is, true);
	}

	public static String getBundledHash() throws IOException {
		byte[] bundled = getBundled();
		return bundled == null ? null : DigestUtils.sha256Hex(bundled);
	}

	public static Set<String> getPreviousHashes() throws IOException {
		if (previous == null) {
			Set<String> hashes = new HashSet<>();
			InputStream is = getResourceAsStream(PREVIOUS);
			if (is != null) {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, CharsetUtil.UTF8))) {
					String line;
					while ((line = reader.readLine()) != null) {
						line = line.trim();
						if (line.isEmpty() || line.startsWith("#")) continue;
						hashes.add(line.toLowerCase());
					}
				}
			}
			// never replace a file with itself
			String current = getBundledHash();
			if (current != null) hashes.remove(current);
			previous = Collections.unmodifiableSet(hashes);
		}
		return previous;
	}

	private static String sha256(Resource file) throws IOException {
		InputStream is = file.getInputStream();
		try {
			return DigestUtils.sha256Hex(is);
		}
		finally {
			IOUtil.close(is);
		}
	}

	private static InputStream getResourceAsStream(String path) {
		InputStream is = InfoImpl.class.getResourceAsStream(path);
		if (is == null) is = SystemUtil.getResourceAsStream(null, path);
		return is;
	}
}
