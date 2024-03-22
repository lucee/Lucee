package lucee.runtime.config.maven;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.osgi.framework.Version;
import org.xml.sax.SAXException;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;

public class MavenUpdateProvider {

	private static final String[] LCOS = new String[] { "lco", "lcojvm11", "lcojvm17", "lcojvm21" };

	public static final int CONNECTION_TIMEOUT = 10000;

	private static final String DEFAULT_LIST_PROVIDER = "https://oss.sonatype.org/service/local/lucene/search";
	// public static final String DEFAULT_REPOSITORY = "https://repo1.maven.org/maven2";
	// public static final String DEFAULT_REPOSITORY_SNAPSHOT =
	// "https://oss.sonatype.org/service/local/repositories/snapshots/content";
	// public static final String DEFAULT_REPOSITORY_RELEASES =
	// "https://oss.sonatype.org/service/local/repositories/releases/content";

	private static final String DEFAULT_REPOSITORY_SNAPSHOT = "https://oss.sonatype.org/content/repositories/snapshots/";
	// public static final String DEFAULT_REPOSITORY_RELEASES =
	// "https://oss.sonatype.org/content/repositories/releases/";
	private static final String DEFAULT_REPOSITORY_RELEASES = "https://oss.sonatype.org/service/local/repositories/releases/content/";

	public static final String DEFAULT_GROUP = "org.lucee";
	public static final String DEFAULT_ARTIFACT = "lucee";

	private static String defaultListProvider;
	private static String defaultRepositoryReleases;
	private static String defaultRepositorySnapshots;

	private String listProvider;
	private String group;
	private String artifact;
	private String repoSnapshots;
	private String repoReleases;

	public static String getDefaultListProvider() {
		if (defaultListProvider == null) {
			String str = SystemUtil.getSystemPropOrEnvVar("lucee.mvn.provider.list", null);
			if (!StringUtil.isEmpty(str, true)) {
				try {
					new URL(str.trim());
					defaultListProvider = str.trim();
				}
				catch (Exception e) {
				}
			}
			if (defaultListProvider == null) defaultListProvider = DEFAULT_LIST_PROVIDER;
		}
		return defaultListProvider;
	}

	public static String getDefaultRepositoryReleases() {
		if (defaultRepositoryReleases == null) {
			String str = SystemUtil.getSystemPropOrEnvVar("lucee.mvn.repo.releases", null);
			if (!StringUtil.isEmpty(str, true)) {
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
			String str = SystemUtil.getSystemPropOrEnvVar("lucee.mvn.repo.snapshots", null);
			if (!StringUtil.isEmpty(str, true)) {
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
		this.listProvider = getDefaultListProvider();
		this.repoSnapshots = getDefaultRepositorySnapshots();
		this.repoReleases = getDefaultRepositoryReleases();
		this.group = DEFAULT_GROUP;
		this.artifact = DEFAULT_ARTIFACT;
	}

	public MavenUpdateProvider(String listProvider, String repoSnapshots, String repoReleases, String group, String artifact) {
		this.listProvider = listProvider;
		this.group = group;
		this.repoSnapshots = repoSnapshots;
		this.repoReleases = repoReleases;
		this.artifact = artifact;
	}

	public List<Version> list() throws IOException, GeneralSecurityException, SAXException {
		try {
			ArtifactReader reader = new ArtifactReader(listProvider, group, artifact);
			reader.read();
			return reader.getVersions();
		}
		catch (UnknownHostException uhe) {
			throw new IOException("cannot reach maven server", uhe);
		}
	}

	/*
	 * public static void main(String[] args) throws PageException, IOException,
	 * GeneralSecurityException, SAXException, BundleException {
	 * 
	 * MavenUpdateProvider mup = new MavenUpdateProvider(); Map<String, Object> map =
	 * mup.detail(OSGiUtil.toVersion("6.1.0.719-SNAPSHOT")); print.e(map); }
	 */

	public Map<String, Object> detail(Version version) throws IOException, GeneralSecurityException, SAXException, PageException {
		// SNAPSHOT - snapshot have a more complicated structure, ebcause there can be udaptes/multiple
		// versions
		try {
			if (version.getQualifier().endsWith("-SNAPSHOT")) {
				// so first we the location of the pom
				RepoReader repoReader = new RepoReader(repoSnapshots, group, artifact, version);
				Map<String, Map<String, Object>> result = repoReader.read();
				String urlPom = Caster.toString(result.get("pom").get("url"));
				String urlJar = Caster.toString(result.get("jar").get("url"));

				Object lastModified = result.get("jar").get("lastModified");

				// PomReader pomRreader = new PomReader(new URL(urlPom));
				// Map<String, Object> res = pomRreader.read();
				Map<String, Object> res = new LinkedHashMap<>();
				res.put("pom", urlPom);
				res.put("jar", urlJar);
				res.put("lastModified", lastModified);

				// LCOS
				for (String lcoName: LCOS) {
					Map<String, Object> lco = result.get(lcoName);
					String urlLco = null;
					if (lco != null) {
						urlLco = Caster.toString(lco.get("url"), null);
					}
					if (!StringUtil.isEmpty(urlLco)) res.put(lcoName, urlLco);
				}

				return res;
			}

			// Release
			Map<String, Object> res = new LinkedHashMap<>();
			String g = group.replace('.', '/');
			String a = artifact.replace('.', '/');
			String v = version.toString();
			URL urlPom = new URL(repoReleases + "/" + g + "/" + a + "/" + v + "/" + a + "-" + v + ".pom");
			{
				HTTPResponse rsp = HTTPEngine4Impl.head(urlPom, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
				if (rsp != null) {
					int sc = rsp.getStatusCode();
					if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + urlPom + "], status code [" + sc + "]");
				}
				else {
					throw new IOException("unable to invoke [" + urlPom + "], no response.");
				}
				Header[] headers = rsp.getAllHeaders();
				for (Header h: headers) {
					if ("Last-Modified".equals(h.getName())) res.put("lastModified", DateCaster.toDateAdvanced(h.getValue(), null));
				}
			}
			URL urlLco = new URL(repoReleases + "/" + g + "/" + a + "/" + v + "/" + a + "-" + v + ".lco");
			{
				HTTPResponse rsp = HTTPEngine4Impl.head(urlLco, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
				if (rsp != null) {
					int sc = rsp.getStatusCode();
					if (sc >= 200 && sc < 300) {
						res.put("lco", urlLco.toExternalForm());
					}
				}
			}
			// PomReader pomRreader = new PomReader(url);
			// Map<String, Object> res = pomRreader.read();

			res.put("pom", urlPom.toExternalForm());
			res.put("jar", repoReleases + "/" + g + "/" + a + "/" + v + "/" + a + "-" + v + ".jar");
			return res;
		}
		catch (UnknownHostException uhe) {
			throw new IOException("cannot reach maven server", uhe);
		}
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
			if (lco != null) urlLco = new URL(Caster.toString(lco.get("url")));
			urljar = new URL(Caster.toString(map.get("jar").get("url")));

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
			HTTPResponse rsp = HTTPEngine4Impl.get(urlLco, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
			if (rsp != null) {
				int sc = rsp.getStatusCode();
				if (sc >= 200 && sc < 300) return rsp.getContentAsStream();
			}
		}
		// JAR
		HTTPResponse rsp = HTTPEngine4Impl.get(urljar, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
		if (rsp != null) {
			int sc = rsp.getStatusCode();
			if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + urljar + "], status code [" + sc + "]");
		}
		else {
			throw new IOException("unable to invoke [" + urljar + "], no response.");
		}
		return getFileStreamFromZipStream(rsp.getContentAsStream());
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

	public InputStream getLoader(Version version) throws IOException, GeneralSecurityException, PageException, SAXException {
		URL url;
		// SNAPSHOT
		if (version.getQualifier().endsWith("-SNAPSHOT")) {
			// so first we the location of the pom
			RepoReader repoReader = new RepoReader(repoSnapshots, group, artifact, version);
			url = new URL(Caster.toString(repoReader.read().get("jar").get("url")));

		}
		// RELEASE
		else {
			String g = group.replace('.', '/');
			String a = artifact.replace('.', '/');
			String v = version.toString();
			String repo = repoReleases;
			url = new URL(repo + "/" + g + "/" + a + "/" + v + "/" + a + "-" + v + ".jar");
		}

		HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
		if (rsp != null) {
			int sc = rsp.getStatusCode();
			if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + url + "], status code [" + sc + "]");
		}
		else {
			throw new IOException("unable to invoke [" + url + "], no response.");
		}
		return rsp.getContentAsStream();
	}
}
