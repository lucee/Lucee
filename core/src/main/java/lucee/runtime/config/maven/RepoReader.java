package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.date.DateTimeUtil;
import lucee.commons.date.TimeZoneConstants;
import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.dt.DateTime;
import lucee.transformer.library.function.FunctionLibEntityResolver;
import lucee.transformer.library.function.FunctionLibException;

public final class RepoReader extends DefaultHandler {

	private static DateTimeFormatter MVN_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss");

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
	private Map<String, Map<String, Object>> artifacts = new HashMap<>();
	private String base;

	public RepoReader(String repo, String group, String artifact, Version version) {
		this.repo = repo;
		this.group = group;
		this.artifact = artifact;
		this.version = version;
		this.key = repo + ":" + group + ":" + artifact + ":" + version;
	}

	public Map<String, Object> read(String requiredArtifactExtension) throws IOException, SAXException {
		if (requiredArtifactExtension == null) requiredArtifactExtension = "jar";
		else requiredArtifactExtension = requiredArtifactExtension.toLowerCase();

		String g = group.replace('.', '/');
		String a = artifact.replace('.', '/');
		String v = version.toString();

		base = repo + (repo.endsWith("/") ? "" : "/") + g + "/" + a + "/" + v + "/";

		URL url = new URL(base + "maven-metadata.xml");

		// Use HTTPDownloader with DEBUG logging for Maven repo metadata lookups
		Reader r = null;
		try {
			r = IOUtil.getReader(HTTPEngine.get(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, MavenUpdateProvider.READ_TIMEOUT, null, null, true), (Charset) null);
			init(new InputSource(r));
		}
		catch (IOException ioe) {
			// 404 or other errors - return null
			return null;
		}
		finally {
			IOUtil.close(r);
		}
		Map<String, Object> res = new LinkedHashMap<>();
		String key;
		Object lastModified = null;
		for (Entry<String, Map<String, Object>> e: artifacts.entrySet()) {
			key = Caster.toString(e.getValue().get("classifier"), null);
			if (StringUtil.isEmpty(key, true)) key = Caster.toString(e.getValue().get("extension"), null);
			if (StringUtil.isEmpty(key, true)) key = e.getKey();
			res.put(key, e.getValue().get("url"));
			if (lastModified == null) {
				Object tmp = e.getValue().get("lastModified");
				if (tmp != null) {
					lastModified = tmp;
				}
			}
		}

		if (res.containsKey(requiredArtifactExtension)) {
			if (lastModified != null) res.put("lastModified", lastModified);
			return res;
		}
		return null;

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
			snapshot.put("url", base + artifact + "-" + value + (StringUtil.isEmpty(classifier) ? "" : "-" + classifier) + "." + extension);
			artifacts.put(key, snapshot);
			snapshot = null;

		}

		content.delete(0, content.length());
		tree.pop();

	}

	@Override
	public void characters(char ch[], int start, int length) {
		content.append(ch, start, length);
	}

	public static Date parseTimestamp(String timestamp) {
		LocalDateTime localDateTime = LocalDateTime.parse(timestamp, MVN_DATE_FORMATTER);
		return Date.from(localDateTime.atZone(ZoneOffset.UTC).toInstant());
	}
}