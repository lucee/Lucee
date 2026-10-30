package lucee.runtime.mvn;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.loader.util.OpenPGPVerifier;
import lucee.loader.util.OpenPGPVerifier.PublicKey;
import lucee.loader.util.OpenPGPVerifier.VerificationException;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.maven.MavenUpdateProvider;

/**
 * Verifies the detached OpenPGP signatures (.asc) of released Lucee artifacts (group org.lucee)
 * against the Lucee release key that is bundled with Lucee. Maven Central requires such a signature
 * for every artifact, so the signature sits next to the artifact in the repository.
 */
public final class ArtifactSignature {

	public static final String SYSTEM_PROP = "lucee.maven.signature.verification";

	private static final String KEY_RESOURCE = "/resource/security/lucee-release-key.asc";
	private static final int MAX_SIGNATURE_SIZE = 64 * 1024;

	private static List<PublicKey> keys;

	private ArtifactSignature() {
	}

	/** the artifact is not verified (other groups, snapshots) */
	public static final int NOT_SIGNED = 0;
	/** a missing signature is tolerated, an invalid one is not (pre-releases) */
	public static final int SIGNATURE_OPTIONAL = 1;
	/** the artifact must have a valid signature (releases) */
	public static final int SIGNATURE_REQUIRED = 2;

	/**
	 * Released artifacts of org.lucee are signed by Lucee and must have a valid signature. Pre-releases
	 * (-ALPHA, -BETA, -RC) are published to the Lucee repository before they go to Maven Central, which
	 * does not have signatures, so a missing signature is accepted for them, but a signature that is
	 * there must be valid. Snapshots and artifacts of other groups are not signed by Lucee.
	 */
	public static int requirement(String groupId, String version) {
		if (!MavenUpdateProvider.DEFAULT_GROUP.equals(groupId) || version == null) return NOT_SIGNED;
		String v = version.trim().toUpperCase();
		if (v.endsWith("SNAPSHOT")) return NOT_SIGNED;
		if (v.indexOf('-') != -1) return SIGNATURE_OPTIONAL;
		return SIGNATURE_REQUIRED;
	}

	/**
	 * Downloads the whole stream to a temp file, verifies it against the signature at the url + ".asc"
	 * and returns a stream of the verified content. The temp file is deleted when the stream is closed.
	 * Nothing is returned for an artifact that fails the verification, so a caller can not use
	 * unverified bytes.
	 */
	public static InputStream verifiedStream(InputStream is, URL artifactUrl, String label, int requirement) throws IOException {
		if (getPolicy() == ConfigPro.MAVEN_SIGNATURE_VERIFICATION_OFF) return is;
		java.nio.file.Path tmp = java.nio.file.Files.createTempFile("lucee-artifact", ".tmp");
		try {
			try {
				java.nio.file.Files.copy(is, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			}
			finally {
				IOUtil.closeEL(is);
			}
			verify(tmp.toFile(), artifactUrl, label, requirement);
			return java.nio.file.Files.newInputStream(tmp, java.nio.file.StandardOpenOption.DELETE_ON_CLOSE);
		}
		catch (IOException | RuntimeException e) {
			java.nio.file.Files.deleteIfExists(tmp);
			throw e;
		}
	}

	public static int getPolicy() {
		ConfigServerImpl cs = ConfigServerImpl.instance;
		if (cs != null) return cs.getMavenSignatureVerification();
		// the config is not available yet, only the system property and the environment variable can be used
		String val = lucee.commons.io.SystemUtil.getSystemPropOrEnvVar(SYSTEM_PROP, null);
		if (val == null) return ConfigPro.MAVEN_SIGNATURE_VERIFICATION_VERIFY;
		val = val.trim();
		if ("off".equalsIgnoreCase(val)) return ConfigPro.MAVEN_SIGNATURE_VERIFICATION_OFF;
		if ("warn".equalsIgnoreCase(val) || "warning".equalsIgnoreCase(val)) return ConfigPro.MAVEN_SIGNATURE_VERIFICATION_WARN;
		if ("strict".equalsIgnoreCase(val)) return ConfigPro.MAVEN_SIGNATURE_VERIFICATION_STRICT;
		return ConfigPro.MAVEN_SIGNATURE_VERIFICATION_VERIFY;
	}

	public static synchronized List<PublicKey> getKeys() throws IOException {
		if (keys == null) {
			InputStream is = ArtifactSignature.class.getResourceAsStream(KEY_RESOURCE);
			if (is == null) throw new IOException("the Lucee release key [" + KEY_RESOURCE + "] is missing in this Lucee installation");
			keys = OpenPGPVerifier.readKeys(is);
			if (keys.isEmpty()) throw new IOException("the Lucee release key [" + KEY_RESOURCE + "] does not contain a usable key");
		}
		return keys;
	}

	public static void verify(Resource file, URL artifactUrl, String label, int requirement) throws IOException {
		verify(new OpenPGPVerifier.Source() {
			@Override
			public InputStream open() throws IOException {
				return file.getInputStream();
			}
		}, artifactUrl, label, requirement);
	}

	public static void verify(File file, URL artifactUrl, String label, int requirement) throws IOException {
		verify(new OpenPGPVerifier.Source() {
			@Override
			public InputStream open() throws IOException {
				return new FileInputStream(file);
			}
		}, artifactUrl, label, requirement);
	}

	/**
	 * loads the signature that belongs to the artifact (url of the artifact + ".asc") and verifies the
	 * already downloaded file with it according to the configured policy.
	 * 
	 * @throws VerificationException when the policy is "verify" and the signature is missing or invalid
	 */
	public static void verify(OpenPGPVerifier.Source data, URL artifactUrl, String label, int requirement) throws IOException {
		int policy = getPolicy();
		if (policy == ConfigPro.MAVEN_SIGNATURE_VERIFICATION_OFF) return;

		URL sigUrl = new URL(artifactUrl.toExternalForm() + ".asc");
		byte[] signature = null;
		String problem = null;
		try {
			signature = fetch(sigUrl);
		}
		catch (IOException e) {
			problem = "failed to load the signature from [" + sigUrl + "]: " + e.getMessage();
		}
		if (signature == null && problem == null) {
			if (requirement == SIGNATURE_OPTIONAL && policy != ConfigPro.MAVEN_SIGNATURE_VERIFICATION_STRICT) {
				LogUtil.log(Log.LEVEL_INFO, "maven", "[" + label + "] is a pre-release without a signature at [" + sigUrl + "], it is not verified");
				return;
			}
			problem = "there is no signature at [" + sigUrl + "]";
		}
		verify(data, signature, problem, label, policy);
	}

	public static void verify(File file, byte[] signature, String problem, String label, int policy) throws IOException {
		verify(new OpenPGPVerifier.Source() {
			@Override
			public InputStream open() throws IOException {
				return new FileInputStream(file);
			}
		}, signature, problem, label, policy);
	}

	/**
	 * @param signature the content of the .asc file, null if it could not be loaded
	 * @param problem why the signature could not be loaded, used when signature is null
	 */
	public static void verify(OpenPGPVerifier.Source data, byte[] signature, String problem, String label, int policy) throws IOException {
		if (policy == ConfigPro.MAVEN_SIGNATURE_VERIFICATION_OFF) return;
		String error;
		if (signature == null) error = problem;
		else {
			try {
				PublicKey key = OpenPGPVerifier.verify(data, signature, getKeys());
				if (LogUtil.doesDebug(null)) LogUtil.log(Log.LEVEL_DEBUG, "maven", "verified the signature of [" + label + "] (key " + key.getFingerprint() + ")");
				return;
			}
			catch (VerificationException e) {
				error = e.getMessage();
			}
		}

		String msg = "The signature of [" + label + "] could not be verified: " + error + ". Lucee only installs org.lucee artifacts that are signed with the Lucee release key. "
				+ "To change this, set the setting 'maven.signatureVerification' in .CFConfig.json, the system property '" + SYSTEM_PROP + "' or the environment variable '"
				+ SYSTEM_PROP.toUpperCase().replace('.', '_') + "' to 'warn' or 'off'.";
		if (policy == ConfigPro.MAVEN_SIGNATURE_VERIFICATION_WARN) {
			LogUtil.log(Log.LEVEL_WARN, "maven", msg);
			return;
		}
		throw new VerificationException(msg);
	}

	private static byte[] fetch(URL url) throws IOException {
		HTTPResponse rsp = HTTPEngine.getResponse(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, MavenUpdateProvider.READ_TIMEOUT, null);
		try {
			int sc = rsp.getStatusCode();
			if (sc == 404 || sc == 410) return null;
			if (sc != 200) throw new IOException("HTTP " + sc);
			InputStream is = rsp.getContentAsStream();
			if (is == null) return null;
			try {
				java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
				byte[] buffer = new byte[4096];
				int len;
				while ((len = is.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
					if (baos.size() > MAX_SIGNATURE_SIZE) throw new IOException("the signature is too big");
				}
				return baos.toByteArray();
			}
			finally {
				IOUtil.closeEL(is);
			}
		}
		finally {
			HTTPEngine.releaseEL(rsp);
		}
	}
}
