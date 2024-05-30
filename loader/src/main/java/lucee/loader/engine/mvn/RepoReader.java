package lucee.loader.engine.mvn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.osgi.framework.Version;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.loader.util.Util;
import lucee.runtime.util.XMLUtilImpl;

public final class RepoReader extends DefaultHandler {

	private XMLReader xmlReader;
	private Stack<String> tree = new Stack<>();
	private StringBuilder content = new StringBuilder();

	private String repo;
	private String group;
	private String artifact;
	private Version version;
	private String key;
	private boolean insideSnapshotVersion;
	private Map<String, Object> snapshot;
	private Map<String, Map<String, Object>> snapshots = new HashMap<>();
	private String base;

	RepoReader(String repo, String group, String artifact, Version version) {
		this.repo = repo;
		this.group = group;
		this.artifact = artifact;
		this.version = version;
		this.key = repo + ":" + group + ":" + artifact + ":" + version;
	}

	public Map<String, Map<String, Object>> read() throws IOException, GeneralSecurityException, SAXException {

		String g = group.replace('.', '/');
		String a = artifact.replace('.', '/');
		String v = version.toString();

		base = repo + (repo.endsWith("/") ? "" : "/") + g + "/" + a + "/" + v + "/";
		URL url = new URL(base + "maven-metadata.xml");

		HttpURLConnection conn = MavenUpdateProvider.get(url);
		if (conn != null) {
			int sc = conn.getResponseCode();
			if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + url + "], status code [" + sc + "]");
		}
		else {
			throw new IOException("unable to invoke [" + repo + "], no response.");
		}
		Reader r = null;
		try {
			init(new InputSource(r = new BufferedReader(new InputStreamReader(conn.getInputStream()))));
		}
		finally {
			Util.closeEL(r);
		}
		return snapshots;
	}

	/**
	 * Generelle Initialisierungsmetode der Konstruktoren.
	 * 
	 * @param saxParser String Klassenpfad zum Sax Parser.
	 * @param is InputStream auf die TLD.
	 * @throws SAXException
	 * @throws IOException
	 * @throws FunctionLibException
	 */
	private void init(InputSource is) throws SAXException, IOException {

		xmlReader = new XMLUtilImpl().createXMLReader("org.apache.xerces.parsers.SAXParser");
		xmlReader.setContentHandler(this);
		xmlReader.setErrorHandler(this);
		xmlReader.parse(is);

	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		tree.add(qName);
		if ("snapshotVersion".equals(name)) {
			insideSnapshotVersion = true;
			snapshot = new HashMap<>();

		}
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		if (insideSnapshotVersion) {
			String tmp = content.toString();
			if (!Util.isEmpty(tmp, true)) snapshot.put(name, tmp.trim());
		}

		if ("snapshotVersion".equals(name)) {
			insideSnapshotVersion = false;

			String classifier = MavenUpdateProvider.toString(snapshot.get("classifier"), "");
			String extension = MavenUpdateProvider.toString(snapshot.get("extension"), "");
			String value = MavenUpdateProvider.toString(snapshot.get("value"), "");
			String key = Util.isEmpty(classifier) ? extension : classifier + "." + extension;
			snapshot.put("url", base + artifact + "-" + value + "." + extension);
			snapshots.put(key, snapshot);
			snapshot = null;

		}

		content.delete(0, content.length());
		tree.pop();

	}

	@Override
	public void characters(char ch[], int start, int length) {
		content.append(ch, start, length);
	}

}