package lucee.runtime.config.s3;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;

import org.osgi.framework.Version;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.config.maven.MavenUpdateProvider;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.dt.DateTime;
import lucee.transformer.library.function.FunctionLibEntityResolver;
import lucee.transformer.library.function.FunctionLibException;

public final class S3UpdateProvider extends DefaultHandler {
	public static final int CONNECTION_TIMEOUT = 1000;
	private static final long MAX_AGE = 10000;
	// public static URL DEFAULT_PROVIDER = null;
	public static URL DEFAULT_PROVIDER_LIST = null;
	public static URL[] DEFAULT_PROVIDER_DETAILS = null;

	static {
		try {
			DEFAULT_PROVIDER_LIST = new URL("https://s3.us-west-1.wasabisys.com/lucee-downloads/");
			DEFAULT_PROVIDER_DETAILS = new URL[] { new URL("https://cdn.lucee.org/"), DEFAULT_PROVIDER_LIST };
		}
		catch (MalformedURLException e) {
		}
	}

	private XMLReader xmlReader;
	private Stack<String> tree = new Stack<>();
	private StringBuilder content = new StringBuilder();
	private final URL url;
	private boolean insideContents;
	private Map<String, Element> elements = new LinkedHashMap<>();
	private Element element;
	private boolean isTruncated;
	private String lastKey;
	private String last3;
	private String last3Key;
	private String last3P;
	private String last3PKey;
	private URL[] details;

	private static Map<String, Pair<Long, S3UpdateProvider>> readers = new HashMap<>();

	public S3UpdateProvider(URL list, URL[] details) throws MalformedURLException {

		if (!list.toExternalForm().endsWith("/")) this.url = new URL(list.toExternalForm() + "/");
		else this.url = list;

		for (int i = 0; i < details.length; i++) {
			if (!details[i].toExternalForm().endsWith("/")) details[i] = new URL(details[i].toExternalForm() + "/");
		}
		this.details = details;
	}

	public static S3UpdateProvider getInstance(URL list, URL[] details) throws MalformedURLException {
		String key = toKey(list, details);
		Pair<Long, S3UpdateProvider> pair = readers.get(key);
		if (pair != null && pair.getName().longValue() + MAX_AGE > System.currentTimeMillis()) {
			return pair.getValue();
		}
		S3UpdateProvider reader = new S3UpdateProvider(list, details);
		readers.put(key, new Pair<Long, S3UpdateProvider>(System.currentTimeMillis(), reader));
		return reader;
	}

	private static String toKey(URL list, URL[] details) {
		StringBuilder sb = new StringBuilder().append(list.toExternalForm());
		for (URL d: details) {
			sb.append(';').append(d.toExternalForm());
		}
		return sb.toString();
	}

	/*
	 * public static void main(String[] args) throws Exception {
	 * print.e(ListBucketReader.getInstance(DEFAULT_PROVIDER).read().size());
	 * print.e("-------------------");
	 * print.e(ListBucketReader.getInstance(DEFAULT_PROVIDER).read().size());
	 * print.e("-------------------");
	 * print.e(ListBucketReader.getInstance(DEFAULT_PROVIDER).read().size());
	 * 
	 * }
	 */

	public static void main(String[] args) throws Exception {
		String path = "/Users/mic/tmp8/eeee/tmp.jar";
		IOUtil.copy(S3UpdateProvider.getInstance(DEFAULT_PROVIDER_LIST, DEFAULT_PROVIDER_DETAILS).getCore(OSGiUtil.toVersion("5.4.1.8")), new FileOutputStream(path), true, true);
		String path2 = "/Users/mic/tmp8/eeee/tmp2.jar";
		IOUtil.copy(S3UpdateProvider.getInstance(DEFAULT_PROVIDER_LIST, DEFAULT_PROVIDER_DETAILS).getCore(OSGiUtil.toVersion("6.0.1.2-SNAPSHOT")), new FileOutputStream(path2),
				true, true);

		// lucee-6.0.1.2-SNAPSHOT
	}

	public InputStream getCore(Version version) throws PageException, MalformedURLException, IOException, GeneralSecurityException, SAXException {
		for (Element e: read()) {
			if (version.equals(e.getVersion())) {
				URL url = e.getLCO();
				if (url != null) {
					HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
					if (rsp != null) {
						int sc = rsp.getStatusCode();
						if (sc >= 200 && sc < 300) return rsp.getContentAsStream();
					}
				}

				url = e.getJAR();
				if (url != null) {
					HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
					if (rsp != null) {
						int sc = rsp.getStatusCode();
						if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + url + "], status code [" + sc + "]");
					}
					else {
						throw new IOException("unable to invoke [" + url + "], no response.");
					}
					return MavenUpdateProvider.getFileStreamFromZipStream(rsp.getContentAsStream());
				}
			}
		}
		throw new IOException("no core file found for version [" + version + "]");
	}

	public List<Element> read() throws IOException, GeneralSecurityException, SAXException, PageException {
		int count = 100;
		URL url = null;

		if (last3PKey != null) url = new URL(this.url.toExternalForm() + "?marker=" + last3PKey);

		do {
			if (url == null) url = isTruncated ? new URL(this.url.toExternalForm() + "?marker=" + this.lastKey) : this.url;
			HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, S3UpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
			if (rsp != null) {
				int sc = rsp.getStatusCode();
				if (sc < 200 || sc >= 300) throw new IOException("unable to invoke [" + url + "], status code [" + sc + "]");
			}
			else {
				throw new IOException("unable to invoke [" + url + "], no response.");
			}

			Reader r = null;
			try {
				init(new InputSource(r = IOUtil.getReader(rsp.getContentAsStream(), (Charset) null)));
			}
			finally {
				last3P = null;
				last3 = null;
				url = null;
				IOUtil.close(r);
			}

		}
		while (isTruncated || --count == 0);

		List<Element> list = new ArrayList<>();
		for (Element e: elements.values()) {
			list.add(e);
		}

		Collections.sort(list, new Comparator<Element>() {
			@Override
			public int compare(Element l, Element r) {
				return OSGiUtil.compare(l.getVersion(), r.getVersion());
			}
		});

		return list;
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
		if (tree.size() == 2 && "Contents".equals(name)) {
			insideContents = true;
			element = new Element(details);
		}

	}

	@Override
	public void endElement(String uri, String name, String qName) {
		// print.e(tree.size() + ":" + name + ":" + content.toString().trim());

		if (tree.size() == 2 && "Contents".equals(name)) {
			insideContents = false;
			if (element.validExtension()) {
				Version v = element.getVersion();
				if (v != null) {
					Element existing = elements.get(v.toString());
					if (existing != null) existing.addKey(element.keys.get(0));
					else {
						element.version = v;
						elements.put(v.toString(), element);
					}
				}
			}
		}
		else if (insideContents) {
			if ("Key".equals(name)) {
				lastKey = content.toString().trim();

				element.addKey(lastKey);

				if (last3 == null || !last3.equals(element.getVersion().toString().substring(0, 6))) {
					last3P = last3;
					last3PKey = last3Key;
					last3 = element.getVersion().toString().substring(0, 6);
					last3Key = lastKey;
					// print.e(last3 + "->" + last3P);
				}
			}
			else if ("LastModified".equals(name)) element.setLastModified(content.toString().trim());
			else if ("ETag".equals(name)) element.setETag(content.toString().trim());
			else if ("Size".equals(name)) element.setSize(content.toString().trim());
		}

		// meta data
		if (tree.size() == 2 && "IsTruncated".equals(name)) {
			isTruncated = Caster.toBooleanValue(content.toString().trim(), false);
			// String tmp = content.toString();
			// if (!StringUtil.isEmpty(tmp, true)) tmpMeta.put(name, tmp.trim());
		}

		content.delete(0, content.length());
		tree.pop();

	}

	@Override
	public void characters(char ch[], int start, int length) {
		content.append(ch, start, length);
	}

	public static class Element {
		private List<String> keys = new ArrayList<>();
		private long size;
		private String etag;
		private DateTime lastMod;
		private Version version;
		private URL[] details;
		private URL lco;
		private URL jar;

		public Element(URL[] details) {
			this.details = details;
		}

		public boolean validExtension() {
			for (String k: keys) {
				if (k.endsWith(".lco") || k.endsWith(".jar")) return true;
			}
			return false;
		}

		/*
		 * <>4.5.3.020.lco</Key> <LastModified>2018-05-28T20:10:55.000Z</LastModified>
		 * <ETag>"08e36e85b35f6f41380b6b013fc68b97"</ETag> <Size>9718200</Size>
		 */
		public void addKey(String key) {
			keys.add(key);

		}

		public void setSize(String size) {
			this.size = Caster.toLongValue(size, 0L);
		}

		public long getSize() {
			return this.size;
		}

		public URL getJAR() throws MalformedURLException {
			if (jar == null) {
				initFiles();
			}
			return jar;
		}

		public URL getLCO() throws MalformedURLException {
			if (lco == null) {
				initFiles();
			}
			return lco;
		}

		private void initFiles() throws MalformedURLException {
			for (String k: keys) {
				if (k.endsWith(".lco")) lco = validate(k);
				else if (k.endsWith(".jar")) jar = validate(k);
			}
		}

		private URL validate(String k) {
			for (URL d: details) {
				try {
					URL url = new URL(d.toExternalForm() + k);

					HTTPResponse rsp = HTTPEngine4Impl.head(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
					if (rsp != null) {
						int sc = rsp.getStatusCode();
						if (sc >= 200 && sc < 300) return url;
					}
				}
				catch (Exception e) {

				}
			}
			return null;
		}

		public void setETag(String etag) {
			this.etag = StringUtil.unwrap(etag);
		}

		public String getETag() {
			return etag;
		}

		public void setLastModified(String lm) {
			this.lastMod = DateCaster.toDateAdvanced(lm, (TimeZone) null, null);
			// 2023-10-20T10:03:41.000Z
			// TODO
		}

		public DateTime getLastModifed() {
			return lastMod;
		}

		public static Version toVersion(String version, Version defaultValue) {
			String v;
			if (version.startsWith("lucee-")) v = version.substring(6);
			else v = version;

			if (!v.endsWith(".jar") && !v.endsWith(".lco")) return defaultValue;
			v = v.substring(0, v.length() - 4);
			return OSGiUtil.toVersion(v, defaultValue);
		}

		public Version getVersion() {
			if (version == null) {
				Version tmp;
				for (String k: keys) {
					tmp = toVersion(k, null);
					if (tmp != null) {
						version = tmp;
						break;
					}
				}
			}
			return version;
		}

		@Override
		public String toString() {
			return new StringBuilder().append("size:").append(size).append(";version:").append(getVersion()).append(";last-mod:").append(lastMod).toString();
		}
	}
}