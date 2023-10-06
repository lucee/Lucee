package lucee.runtime.config.maven;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Map;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.xml.sax.SAXException;

import lucee.commons.io.IOUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.osgi.OSGiUtil;

public class MavenUpdateProvider {

	public static final String DEFAULT_LIST_PROVIDER = "https://oss.sonatype.org/service/local/lucene/search";
	public static final String DEFAULT_REPOSITORY = "https://repo1.maven.org/maven2";
	public static final String DEFAULT_GROUP = "org.lucee";
	public static final String DEFAULT_ARTIFACT = "lucee";

	private String listProvider;
	private String group;
	private String artifact;
	private String repo;

	public MavenUpdateProvider(String listProvider, String repo, String group, String artifact) {
		this.listProvider = listProvider;
		this.group = group;
		this.repo = repo;
		this.artifact = artifact;
	}

	public Version[] list() throws IOException, GeneralSecurityException, SAXException {
		ArtifactReader reader = new ArtifactReader(listProvider, group, artifact);
		reader.read();
		return reader.getArtifacts();
	}

	public static void main(String[] args) throws IOException, GeneralSecurityException, SAXException, BundleException {
		MavenUpdateProvider provider = new MavenUpdateProvider(DEFAULT_LIST_PROVIDER, DEFAULT_REPOSITORY, DEFAULT_GROUP, DEFAULT_ARTIFACT);
		// provider.detail(OSGiUtil.toVersion("6.0.0.572-RC"));

		;
		IOUtil.copy(provider.getLoader(OSGiUtil.toVersion("6.0.0.572-RC")), new FileOutputStream("/Users/mic/tmp8/6.0.0.572-RC.zip"), true, true);

	}

	public Map<String, String> detail(Version version) throws IOException, GeneralSecurityException, SAXException {
		RepoReader reader = new RepoReader(repo, group, artifact, version);
		return reader.read();
	}

	public InputStream downloadCore(Version version) {
		return null;
	}

	public InputStream getLoader(Version version) throws IOException, GeneralSecurityException {
		String g = group.replace('.', '/');
		String a = artifact.replace('.', '/');
		String v = version.toString();

		URL url = new URL(repo + "/" + g + "/" + a + "/" + v + "/" + a + "-" + v + ".jar");
		HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, 0, true, null, null, null, null);
		if (rsp != null) {
			int sc = rsp.getStatusCode();
			if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + repo + "], status code [" + sc + "]");
		}
		else {
			throw new IOException("unable to invoke [" + repo + "], no response.");
		}

		return rsp.getContentAsStream();
	}

	public InputStream downloadBundle(String bundleName, Version bundleVersion) {
		return null;
	}

}
