package lucee.loader.engine.mvn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.osgi.framework.Version;
import org.xml.sax.SAXException;

import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;

public class MavenUpdateProvider {

	public static final int CONNECTION_TIMEOUT = 50000;
	public static final int READ_TIMEOUT_HEAD = 5000;
	public static final int READ_TIMEOUT_GET = 20000;

	private static final String DEFAULT_REPOSITORY_SNAPSHOT = "https://oss.sonatype.org/content/repositories/snapshots/";
	private static final String DEFAULT_REPOSITORY_RELEASES = "https://oss.sonatype.org/service/local/repositories/releases/content/";

	public static final String DEFAULT_GROUP = "org.lucee";
	public static final String DEFAULT_ARTIFACT = "lucee";

	private static String defaultRepositoryReleases;
	private static String defaultRepositorySnapshots;

	private String group;
	private String artifact;
	private String repoSnapshots;
	private String repoReleases;

	public static String getDefaultRepositoryReleases() {
		if (defaultRepositoryReleases == null) {
			String str = Util._getSystemPropOrEnvVar("lucee.mvn.repo.releases", null);
			if (!Util.isEmpty(str, true)) {
				try {
					new URL(str.trim());
					defaultRepositoryReleases = str.trim();
				}
				catch (Exception e) {
				}
			}
			if (defaultRepositoryReleases == null) defaultRepositoryReleases = DEFAULT_REPOSITORY_RELEASES;
		}
		return defaultRepositoryReleases;
	}

	public static String getDefaultRepositorySnapshots() {
		if (defaultRepositorySnapshots == null) {
			String str = Util._getSystemPropOrEnvVar("lucee.mvn.repo.snapshots", null);
			if (!Util.isEmpty(str, true)) {
				try {
					new URL(str.trim());
					defaultRepositorySnapshots = str.trim();
				}
				catch (Exception e) {
				}
			}
			if (defaultRepositorySnapshots == null) defaultRepositorySnapshots = DEFAULT_REPOSITORY_SNAPSHOT;
		}
		return defaultRepositorySnapshots;
	}

	public MavenUpdateProvider() {
		this.repoSnapshots = getDefaultRepositorySnapshots();
		this.repoReleases = getDefaultRepositoryReleases();
		this.group = DEFAULT_GROUP;
		this.artifact = DEFAULT_ARTIFACT;
	}

	public MavenUpdateProvider(String listProvider, String repoSnapshots, String repoReleases, String group, String artifact) {
		this.group = group;
		this.repoSnapshots = repoSnapshots;
		this.repoReleases = repoReleases;
		this.artifact = artifact;
	}

	public InputStream getCore(Version version) throws IOException, GeneralSecurityException, PageException, SAXException {
		URL urlLco = null;
		URL urljar = null;
		// SNAPSHOT
		if (version.getQualifier().endsWith("-SNAPSHOT")) {
			// so first we the location of the pom

			RepoReader repoReader = new RepoReader(repoSnapshots, group, artifact, version);
			Map<String, Map<String, Object>> map = repoReader.read();
			Map<String, Object> lco = map.get("lco");
			// if there is no lco (was in older version), extract from loader (slower)
			if (lco != null) urlLco = new URL(toString(lco.get("url")));
			urljar = new URL(toString(map.get("jar").get("url")));

		}
		// RELEASE
		else {
			String g = group.replace('.', '/');
			String a = artifact.replace('.', '/');
			String v = version.toString();
			String repo = repoReleases;
			urlLco = new URL(repo + "/" + g + "/" + a + "/" + v + "/" + a + "-" + v + ".lco");
			urljar = new URL(repo + "/" + g + "/" + a + "/" + v + "/" + a + "-" + v + ".jar");
		}
		// LCO
		if (urlLco != null) {
			HttpURLConnection conn = get(urlLco);
			if (conn != null) {
				int sc = conn.getResponseCode();
				if (sc >= 200 && sc < 300) return conn.getInputStream();
			}
		}
		// JAR
		HttpURLConnection conn = get(urljar);
		if (conn == null) {
			throw new IOException("jar [" + urljar + "] and core [" + urlLco + "] do not exist");
		}

		return getFileStreamFromZipStream(conn.getInputStream());
	}

	public static InputStream getFileStreamFromZipStream(InputStream zipStream) throws IOException {
		ZipInputStream zis = new ZipInputStream(zipStream);
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			if (entry.getName().equals("core/core.lco")) {
				// Return an InputStream which is limited to the current zip entry's data
				Enumeration<InputStream> singleStreamEnum = Collections.enumeration(Collections.singletonList(zis));
				return new SequenceInputStream(singleStreamEnum);
			}
		}
		throw new FileNotFoundException("core/core.lco not found in zip");
	}

	public static HttpURLConnection get(URL url) throws IOException {
		// TODO set timeout
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Lucee");

			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			connection.setReadTimeout(READ_TIMEOUT_GET);
			connection.connect();
			return connection;

		}
		catch (Exception e) {
			return null;
			// TODO log
		}
	}

	public static boolean exist(URL url) throws IOException {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			connection.setReadTimeout(READ_TIMEOUT_HEAD);
			connection.setRequestMethod("HEAD");
			connection.connect();

			int responseCode = connection.getResponseCode();
			return responseCode >= 200 && responseCode < 300;

		}
		finally {
			if (connection != null) connection.disconnect();
		}
	}

	public static String toString(Object obj) {
		if (obj == null) return "";
		return obj.toString();
	}

	public static String toString(Object obj, String defaultValue) {
		if (obj == null) return "";
		try {
			return obj.toString();
		}
		catch (Exception e) {
			return defaultValue;
		}
	}
}
