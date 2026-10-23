package lucee.loader.engine.mvn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
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

	private static final String DEFAULT_REPOSITORY_SNAPSHOT = "https://central.sonatype.com/repository/maven-snapshots/";
	private static final String DEFAULT_REPOSITORY_RELEASES = "https://repo1.maven.org/maven2/";
	// same default as lucee.runtime.config.maven.MavenUpdateProvider (core)
	private static final String DEFAULT_REPOSITORY_MIXED = "https://maven.lucee-services.com/";

	public static final String DEFAULT_GROUP = "org.lucee";
	public static final String DEFAULT_ARTIFACT = "lucee";

	private static String[] defaultRepositoryReleases;
	private static String[] defaultRepositorySnapshots;
	private static String[] defaultRepositoryMixed;

	private String group;
	private String artifact;
	private String[] repoSnapshots;
	private String[] repoReleases;

	public static String getDefaultRepositoryReleases() {
		return getDefaultRepositoriesReleases()[0];
	}

	public static String getDefaultRepositorySnapshots() {
		return getDefaultRepositoriesSnapshots()[0];
	}

	/**
	 * repositories used for release versions, the release repositories (lucee.mvn.repo.releases) followed
	 * by the mixed repositories (lucee.mvn.repo.mixed), same as the core
	 */
	public static String[] getDefaultRepositoriesReleases() {
		if (defaultRepositoryReleases == null) {
			defaultRepositoryReleases = readReposFromEnvVar("lucee.mvn.repo.releases", DEFAULT_REPOSITORY_RELEASES);
		}
		return merge(defaultRepositoryReleases, getDefaultRepositoriesMixed());
	}

	/**
	 * repositories used for snapshot versions, the snapshot repositories (lucee.mvn.repo.snapshots)
	 * followed by the mixed repositories (lucee.mvn.repo.mixed), same as the core
	 */
	public static String[] getDefaultRepositoriesSnapshots() {
		if (defaultRepositorySnapshots == null) {
			defaultRepositorySnapshots = readReposFromEnvVar("lucee.mvn.repo.snapshots", DEFAULT_REPOSITORY_SNAPSHOT);
		}
		return merge(defaultRepositorySnapshots, getDefaultRepositoriesMixed());
	}

	private static String[] getDefaultRepositoriesMixed() {
		if (defaultRepositoryMixed == null) {
			defaultRepositoryMixed = readReposFromEnvVar("lucee.mvn.repo.mixed", DEFAULT_REPOSITORY_MIXED);
		}
		return defaultRepositoryMixed;
	}

	private static String[] readReposFromEnvVar(String name, String defaultValue) {
		String str = Util.getSystemPropOrEnvVar(name, null);
		List<String> list = new ArrayList<>();
		if (!Util.isEmpty(str, true)) {
			for (String s: str.split(",")) {
				s = s.trim();
				if (s.isEmpty()) continue;
				try {
					new URL(s);
					list.add(s.endsWith("/") ? s : (s + "/"));
				}
				catch (Exception e) {
				}
			}
		}
		if (list.isEmpty()) list.add(defaultValue);
		return list.toArray(new String[list.size()]);
	}

	private static String[] merge(String[] left, String[] right) {
		List<String> list = new ArrayList<>();
		for (String s: left) {
			if (!list.contains(s)) list.add(s);
		}
		for (String s: right) {
			if (!list.contains(s)) list.add(s);
		}
		return list.toArray(new String[list.size()]);
	}

	public MavenUpdateProvider() {
		this.repoSnapshots = getDefaultRepositoriesSnapshots();
		this.repoReleases = getDefaultRepositoriesReleases();
		this.group = DEFAULT_GROUP;
		this.artifact = DEFAULT_ARTIFACT;
	}

	public MavenUpdateProvider(String listProvider, String repoSnapshots, String repoReleases, String group, String artifact) {
		this(new String[] { repoSnapshots }, new String[] { repoReleases }, group, artifact);
	}

	public MavenUpdateProvider(String[] repoSnapshots, String[] repoReleases, String group, String artifact) {
		this.group = group;
		this.repoSnapshots = repoSnapshots;
		this.repoReleases = repoReleases;
		this.artifact = artifact;
	}

	public InputStream getCore(Version version) throws IOException, GeneralSecurityException, PageException, SAXException {
		boolean isSnapshot = version.getQualifier().endsWith("-SNAPSHOT");
		String[] repos = isSnapshot ? repoSnapshots : repoReleases;
		StringBuilder checked = new StringBuilder();
		for (String repo: repos) {
			URL urlLco = null;
			URL urljar = null;
			// SNAPSHOT
			if (isSnapshot) {
				// so first we the location of the pom
				Map<String, Map<String, Object>> map;
				try {
					map = new RepoReader(repo, group, artifact, version).read();
				}
				catch (IOException e) {
					checked.append(checked.length() > 0 ? ", " : "").append(repo);
					continue;
				}
				Map<String, Object> lco = map.get("lco");
				// if there is no lco (was in older version), extract from loader (slower)
				if (lco != null) urlLco = new URL(toString(lco.get("url")));
				Map<String, Object> jar = map.get("jar");
				if (jar != null) urljar = new URL(toString(jar.get("url")));
			}
			// RELEASE
			else {
				String g = group.replace('.', '/');
				String a = artifact.replace('.', '/');
				String v = version.toString();
				if (!repo.endsWith("/")) repo += "/";
				urlLco = new URL(repo + g + "/" + a + "/" + v + "/" + a + "-" + v + ".lco");
				urljar = new URL(repo + g + "/" + a + "/" + v + "/" + a + "-" + v + ".jar");
			}
			// LCO
			if (urlLco != null) {
				HttpURLConnection conn = get(urlLco);
				if (conn != null) {
					int sc = conn.getResponseCode();
					if (sc >= 200 && sc < 300) return conn.getInputStream();
					conn.disconnect();
				}
			}
			// JAR
			if (urljar != null) {
				HttpURLConnection conn = get(urljar);
				if (conn != null) {
					int sc = conn.getResponseCode();
					if (sc >= 200 && sc < 300) return getFileStreamFromZipStream(conn.getInputStream());
					conn.disconnect();
				}
			}
			checked.append(checked.length() > 0 ? ", " : "").append(repo);
		}
		throw new IOException("neither the core (.lco) nor the jar for [" + group + ":" + artifact + ":" + version + "] exist in the repositories [" + checked + "]");
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
