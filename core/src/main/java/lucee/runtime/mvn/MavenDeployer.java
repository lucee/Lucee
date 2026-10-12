package lucee.runtime.mvn;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;

/**
 * Deploys Maven artifacts (repository layout) shipped with Lucee core or an extension into the local
 * Maven repository.
 */
public final class MavenDeployer {

	private static final String LOG_TYPE = "maven";
	private static final String INDEX = "maven/.index";

	private MavenDeployer() {
	}

	/**
	 * Writes a single file into the repository.
	 * 
	 * @return the file written, or null if the file exists and overwrite is false
	 */
	public static Resource deploy(Resource repository, InputStream is, String realpath, boolean closeStream, boolean overwrite) throws IOException {
		if (!repository.exists()) repository.createDirectory(true);
		Resource file = repository.getRealResource(realpath);
		Resource p = file.getParentResource();
		if (!p.exists()) p.createDirectory(true);
		if (overwrite || !file.exists()) {
			IOUtil.copy(is, file.getOutputStream(), closeStream, true);
			return file;
		}
		return null;
	}

	/**
	 * Deploys the artifacts listed in "maven/.index" (semicolon separated paths relative to the
	 * repository root) of the given class loader. Nothing is done if the index does not exist.
	 * 
	 * @return number of files written
	 */
	public static int deployBundled(Resource repository, ClassLoader cl, Log log) {
		InputStream is = getStream(cl, INDEX);
		if (is == null) return 0;

		String[] paths;
		try {
			paths = IOUtil.toString(is, StandardCharsets.UTF_8).split(";");
		}
		catch (IOException e) {
			log.error(LOG_TYPE, e);
			return 0;
		}
		finally {
			IOUtil.closeEL(is);
		}

		int count = 0;
		for (String path: paths) {
			if (StringUtil.isEmpty(path, true)) continue;
			path = path.trim().replace('\\', '/');
			if (path.endsWith(".sha1")) continue; // handled together with its artifact
			try {
				if (deployFile(repository, cl, path, log)) count++;
			}
			catch (Exception e) {
				log.error(LOG_TYPE, "failed to deploy bundled maven file [" + path + "]", e);
			}
		}
		if (count > 0) log.info(LOG_TYPE, "deployed [" + count + "] bundled maven file(s) to [" + repository + "]");
		return count;
	}

	private static boolean deployFile(Resource repository, ClassLoader cl, String path, Log log) throws IOException {
		if (path.startsWith("/") || path.contains("../")) {
			log.error(LOG_TYPE, "rejected bundled maven file [" + path + "], invalid path");
			return false;
		}

		byte[] data = read(cl, "maven/" + path);
		if (data == null) {
			log.error(LOG_TYPE, "bundled maven file [" + path + "] is listed in the index but missing");
			return false;
		}
		byte[] sha1File = read(cl, "maven/" + path + ".sha1");
		if (sha1File == null) {
			log.error(LOG_TYPE, "rejected bundled maven file [" + path + "], no .sha1 file available");
			return false;
		}
		String expected = new String(sha1File, StandardCharsets.UTF_8).trim().split("\\s+")[0].toLowerCase();
		String actual = sha1(data);
		if (!expected.equals(actual)) {
			log.error(LOG_TYPE, "rejected bundled maven file [" + path + "], sha1 mismatch (expected [" + expected + "], actual [" + actual + "])");
			return false;
		}

		Resource target = repository.getRealResource(path);
		if (target.isFile()) {
			if (Arrays.equals(IOUtil.toBytes(target), data)) {
				deployChecksum(repository, path, sha1File);
				return false;
			}
			log.info(LOG_TYPE, "replacing [" + target + "], checksum does not match the bundled file");
		}

		Resource parent = target.getParentResource();
		if (!parent.exists()) parent.createDirectory(true);
		IOUtil.copy(new java.io.ByteArrayInputStream(data), target.getOutputStream(), true, true);
		deployChecksum(repository, path, sha1File);
		return true;
	}

	private static void deployChecksum(Resource repository, String path, byte[] sha1File) throws IOException {
		Resource target = repository.getRealResource(path + ".sha1");
		if (target.isFile() && Arrays.equals(IOUtil.toBytes(target), sha1File)) return;
		IOUtil.copy(new java.io.ByteArrayInputStream(sha1File), target.getOutputStream(), true, true);
	}

	private static byte[] read(ClassLoader cl, String path) throws IOException {
		InputStream is = getStream(cl, path);
		if (is == null) return null;
		return IOUtil.toBytes(is, true);
	}

	private static InputStream getStream(ClassLoader cl, String path) {
		InputStream is = cl.getResourceAsStream(path);
		if (is == null) is = cl.getResourceAsStream("/" + path);
		return is;
	}

	private static String sha1(byte[] data) throws IOException {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-1").digest(data);
			StringBuilder sb = new StringBuilder(digest.length * 2);
			for (byte b: digest) {
				sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
			}
			return sb.toString();
		}
		catch (java.security.NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}
}
