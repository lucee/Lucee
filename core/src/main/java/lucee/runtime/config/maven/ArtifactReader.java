package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.print;
import lucee.commons.io.IOUtil;
import lucee.commons.io.sax.SaxUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.text.xml.XMLUtil;
import lucee.transformer.library.function.FunctionLibEntityResolver;
import lucee.transformer.library.function.FunctionLibException;

public final class ArtifactReader extends DefaultHandler {

	private XMLReader xmlReader;
	private String name;
	private Map<String, String> attributes;
	private Stack<String> tree = new Stack<>();
	private StringBuilder content = new StringBuilder();
	private boolean insideArtifact;
	private List<Version> tmpArtifacts = new ArrayList<>();
	private Map<String, String> tmpMeta = new HashMap<>();

	private String listProvider;
	private String group;
	private String artifact;
	private String key;

	private static Map<String, Integer> totalCounts = new ConcurrentHashMap<>();
	private static Map<String, Version[]> artifactss = new ConcurrentHashMap<>();
	private static Map<String, Map<String, String>> metas = new ConcurrentHashMap<>();

	/*
	 * public static void main(String[] args) throws Exception { ArtifactReader reader = new
	 * ArtifactReader(MavenUpdateProvider.DEFAULT_LIST_PROVIDER, MavenUpdateProvider.DEFAULT_GROUP,
	 * MavenUpdateProvider.DEFAULT_ARTIFACT); long start = System.currentTimeMillis(); reader.read();
	 * print.e(System.currentTimeMillis() - start); start = System.currentTimeMillis(); reader.read();
	 * print.e(System.currentTimeMillis() - start); print.e(reader.getMeta()); }
	 */

	ArtifactReader(String listProvider, String group, String artifact) {
		this.listProvider = listProvider;
		this.group = group;
		this.artifact = artifact;
		this.key = listProvider + ":" + group + ":" + artifact;
	}

	public void read() throws IOException, GeneralSecurityException, SAXException {

		Integer tc = totalCounts.get(key);

		print.e("totalCount:" + tc);
		// first we check the size
		if (tc != null) {
			// first we see if there is a change
			read(1, 1);
			// if it matches we take the cached data
			int tmpTotalCount = Caster.toIntValue(tmpMeta.get("totalCount"), 0);
			print.e("tmpTotalCount:" + tmpTotalCount);
			clear();
			if (tmpTotalCount == tc.intValue()) {
				return;
			}

		}

		int slotSize = 500;
		int count = 100;
		int start = 0;
		while (true) {
			read(start, slotSize);
			int tmpFrom = Caster.toIntValue(tmpMeta.get("from"), 0);
			int tmpCount = Caster.toIntValue(tmpMeta.get("count"), 0);
			int tmpTotalCount = Caster.toIntValue(tmpMeta.get("totalCount"), 0);

			start = tmpFrom + tmpCount;

			if (tmpTotalCount <= (tmpFrom + tmpCount)) {
				totalCounts.put(key, tmpTotalCount);
				break;
			}
			if (--count == 0) {
				totalCounts.put(key, tmpTotalCount);
				break; // just in case
			}
		}
		artifactss.put(key, toArray(tmpArtifacts));
		metas.put(key, tmpMeta);

		clear();
	}

	private void read(int from, int count) throws IOException, GeneralSecurityException, SAXException {

		URL url = new URL(listProvider + "?g=" + group + "&a=" + artifact + "&c=sources&from=" + from + "&count=" + count);

		HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, 0, true, null, null, null, null);
		if (rsp != null) {
			int sc = rsp.getStatusCode();
			if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + listProvider + "], status code [" + sc + "]");
		}
		else {
			throw new IOException("unable to invoke [" + listProvider + "], no response.");
		}

		Reader r = null;
		try {
			init(new InputSource(r = IOUtil.getReader(rsp.getContentAsStream(), (Charset) null)));
		}
		finally {
			IOUtil.close(r);
		}
	}

	private void clear() {
		tmpArtifacts = new ArrayList<>();
		tmpMeta = new HashMap<>();
		tree.clear();
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
		this.name = name;
		this.attributes = SaxUtil.toMap(atts);

		// enter artifact?
		if (tree.size() == 2 && "data".equals(tree.peek()) && "artifact".equals(name)) {
			insideArtifact = true;
		}
		tree.add(qName);

	}

	@Override
	public void endElement(String uri, String name, String qName) {
		if (insideArtifact && "version".equals(name)) {
			try {
				tmpArtifacts.add(OSGiUtil.toVersion(content.toString().trim()));
			}
			catch (BundleException e) {
				throw new RuntimeException(ExceptionUtil.toIOException(e));
			}
		}

		// meta data
		if (!insideArtifact && tree.size() == 2) {
			tmpMeta.put(name, content.toString().trim());
		}

		content.delete(0, content.length());
		tree.pop();

		// exit artifact?
		if (tree.size() == 2 && "data".equals(tree.peek()) && "artifact".equals(name)) {
			insideArtifact = false;
		}
	}

	@Override
	public void characters(char ch[], int start, int length) {
		content.append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		/*
		 * Collections.sort(artifacts, new Comparator<Version>() {
		 * 
		 * @Override public int compare(Version l, Version r) { return OSGiUtil.compare(l, r); } });
		 */
		super.endDocument();
	}

	public Map<String, String> getMeta() {
		return metas.get(key);
	}

	public Version[] getArtifacts() {
		return artifactss.get(key);
	}

	private static Version[] toArray(List<Version> data) {
		Version[] arr = new Version[data.size()];
		int index = 0;
		for (Version v: data) {
			arr[index++] = v;
		}
		return arr;
	}
}