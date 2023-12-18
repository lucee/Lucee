package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
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

import lucee.commons.date.DateTimeUtil;
import lucee.commons.date.TimeZoneConstants;
import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.dt.DateTime;
import lucee.transformer.library.function.FunctionLibEntityResolver;
import lucee.transformer.library.function.FunctionLibException;

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

	public Map<String, Map<String, Object>> read() throws IOException, GeneralSecurityException, SAXException, PageException {

		String g = group.replace('.', '/');
		String a = artifact.replace('.', '/');
		String v = version.toString();

		base = repo + (repo.endsWith("/") ? "" : "/") + g + "/" + a + "/" + v + "/";
		URL url = new URL(base + "maven-metadata.xml");
		HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
		if (rsp != null) {
			int sc = rsp.getStatusCode();
			if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + url + "], status code [" + sc + "]");
		}
		else {
			throw new IOException("unable to invoke [" + repo + "], no response.");
		}

		Reader r = null;
		try {
			init(new InputSource(r = IOUtil.getReader(rsp.getContentAsStream(), (Charset) null)));
		}
		finally {
			IOUtil.close(r);
		}

		/*
		 * Header[] headers = rsp.getAllHeaders(); for (Header h: headers) { if
		 * ("Last-Modified".equals(h.getName()) || "Date".equals(h.getName())) print.e(h.getName() + ":" +
		 * DateCaster.toDateAdvanced(h.getValue(), null)); // tmpMeta.put(h.getName(), //
		 * DateCaster.toDateAdvanced(h.getValue(), // null)); else print.e(h.getName() + ":" +
		 * h.getValue()); // tmpMeta.put(h.getName(), h.getValue()); } print.e(snapshots);
		 */
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
		xmlReader = XMLUtil.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.setErrorHandler(this);
		xmlReader.setEntityResolver(new FunctionLibEntityResolver());
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

	/*
	 * ,"modelVersion":xml.XmlRoot.modelVersion.XmlText ,"groupId":xml.XmlRoot.groupId.XmlText
	 * ,"artifactId":xml.XmlRoot.artifactId.XmlText ,"version":xml.XmlRoot.version.XmlText
	 * ,"name":xml.XmlRoot.name.XmlText ,"description":xml.XmlRoot.description.XmlText
	 * ,"groupId":xml.XmlRoot.groupId.XmlText
	 */
	@Override
	public void endElement(String uri, String name, String qName) {
		if (insideSnapshotVersion) {
			String tmp = content.toString();
			if (!StringUtil.isEmpty(tmp, true)) snapshot.put(name, tmp.trim());
		}

		if ("snapshotVersion".equals(name)) {
			insideSnapshotVersion = false;

			String classifier = Caster.toString(snapshot.get("classifier"), "");
			String extension = Caster.toString(snapshot.get("extension"), "");
			String value = Caster.toString(snapshot.get("value"), "");
			String updated = Caster.toString(snapshot.get("updated"), "");
			try {
				int year = Caster.toIntValue(updated.substring(0, 4));
				int month = Caster.toIntValue(updated.substring(4, 6));
				int day = Caster.toIntValue(updated.substring(6, 8));
				int hour = Caster.toIntValue(updated.substring(8, 10));
				int minute = Caster.toIntValue(updated.substring(10, 12));
				int second = Caster.toIntValue(updated.substring(12, 14));

				DateTime dt = DateTimeUtil.getInstance().toDateTime(TimeZoneConstants.UTC, year, month, day, hour, minute, second, 0);
				snapshot.put("lastModified", dt);

			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String key = StringUtil.isEmpty(classifier) ? extension : classifier + "." + extension;
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