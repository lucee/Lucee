package lucee.runtime.config.s3;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.runtime.config.maven.MavenUpdateProvider;
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

	private static URL DEFAULT_PROVIDER_LIST = null;
	private static URL DEFAULT_PROVIDER_DETAIL = null;

	private static URL defaultProviderList;
	private static URL defaultProviderDetail;

	static {
		try {
			DEFAULT_PROVIDER_LIST = new URL("https://lucee-downloads.s3.amazonaws.com/");
			DEFAULT_PROVIDER_DETAIL = new URL("https://maven.lucee-services.com/");
		}
		catch (Exception e) {}
	}

	public static URL getDefaultProviderList() {
		if (defaultProviderList == null) {
			String str = SystemUtil.getSystemPropOrEnvVar("lucee.s3.provider.list", null);
			if (!StringUtil.isEmpty(str, true)) {
				try {
					defaultProviderList = new URL(str.trim());
				}
				catch (Exception e) {}
			}
			if (defaultProviderList == null) defaultProviderList = DEFAULT_PROVIDER_LIST;
		}
		return defaultProviderList;
	}

	public static URL getDefaultProviderDetail() {
		if (defaultProviderDetail == null) {
			String str = SystemUtil.getSystemPropOrEnvVar("lucee.s3.provider.detail", null);
			if (!StringUtil.isEmpty(str, true)) {
				try {
					defaultProviderDetail = new URL(str.trim());
				}
				catch (Exception e) {}
			}
			if (defaultProviderDetail == null) defaultProviderDetail = DEFAULT_PROVIDER_DETAIL;
		}
		return defaultProviderDetail;
	}

	private XMLReader xmlReader;
	private Stack<String> tree = new Stack<>();
	private StringBuilder content = new StringBuilder();
	private final URL url;
	private boolean insideContents;
	private Map<Version, Element> elements = new LinkedHashMap<>();
	private Artifact artifact;
	private boolean isTruncated;
	private String lastKey;
	// private String last3PKey;
	private final URL detail;

	private static Map<String, Pair<Long, S3UpdateProvider>> readers = new HashMap<>();

	private S3UpdateProvider(URL list, URL detail) throws MalformedURLException {

		if (!list.toExternalForm().endsWith("/")) this.url = new URL(list.toExternalForm() + "/");
		else this.url = list;

		if (!detail.toExternalForm().endsWith("/")) this.detail = new URL(detail.toExternalForm() + "/");
		else this.detail = detail;
	}

	public static S3UpdateProvider getInstance() throws MalformedURLException {
		return getInstance(getDefaultProviderList(), getDefaultProviderDetail());
	}

	public static S3UpdateProvider getInstance(URL list, URL detail) throws MalformedURLException {
		String key = toKey(list, detail);
		Pair<Long, S3UpdateProvider> pair = readers.get(key);
		if (pair != null && pair.getName().longValue() + MAX_AGE > System.currentTimeMillis()) {
			return pair.getValue();
		}
		S3UpdateProvider reader = new S3UpdateProvider(list, detail);
		readers.put(key, new Pair<Long, S3UpdateProvider>(System.currentTimeMillis(), reader));
		return reader;
	}

	private static String toKey(URL list, URL detail) {
		return new StringBuilder().append(list.toExternalForm()).append(';').append(detail.toExternalForm()).toString();
	}

	public InputStream getCore(Version version) throws MalformedURLException, IOException, SAXException {
		for (Element e: read()) {
			if (version.equals(e.getVersion())) {
				Artifact art = e.getLCO();
				if (art != null) {
					try {
						// Use HTTPDownloader with DEBUG logging for S3 downloads
						return HTTPEngine.get(art.getURL(), null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, MavenUpdateProvider.CONNECTION_TIMEOUT, null, null, true);
					}
					catch (IOException ioe) {
						// Try JAR fallback
					}
				}

				art = e.getJAR();
				if (art != null) {
					return MavenUpdateProvider.getFileStreamFromZipStream(
							HTTPEngine.get(art.getURL(), null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, MavenUpdateProvider.CONNECTION_TIMEOUT, null, null, true));
				}
			}
		}
		throw new IOException("no core file found for version [" + version + "]");
	}

	public List<Element> read() throws IOException, SAXException {
		int count = 100;
		URL url = null;

		if (lastKey != null) url = new URL(this.url.toExternalForm() + "?marker=" + lastKey);

		do {
			if (url == null) url = isTruncated ? new URL(this.url.toExternalForm() + "?marker=" + this.lastKey) : this.url;
			url = addPrefix(url);
			// Use HTTPDownloader with DEBUG logging for S3 update provider list reads
			Reader r = null;
			try {
				r = IOUtil.getReader(HTTPEngine.get(url, null, null, S3UpdateProvider.CONNECTION_TIMEOUT, S3UpdateProvider.CONNECTION_TIMEOUT, null, null, true), (Charset) null);
				init(new InputSource(r));
			}
			finally {
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

	private static URL addPrefix(URL url) throws MalformedURLException {
		String strURL = url.toExternalForm();
		if (url.getQuery() != null) return new URL(strURL + "&prefix=org/lucee/lucee/");
		return new URL(strURL + "?prefix=org/lucee/lucee/");

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
			artifact = new Artifact(detail);
		}

	}

	@Override
	public void endElement(String uri, String name, String qName) {
		// print.e(tree.size() + ":" + name + ":" + content.toString().trim());

		if (tree.size() == 2 && "Contents".equals(name)) {
			insideContents = false;
			if (artifact.type != null) {
				Element existing = elements.get(artifact.getVersion());
				if (existing == null) {
					elements.put(artifact.getVersion(), existing = new Element());
				}
				existing.add(artifact);
			}
		}
		else if (insideContents) {
			if ("Key".equals(name)) {
				artifact.init(content.toString().trim());
				lastKey = artifact.raw;
			}
			else if ("LastModified".equals(name)) artifact.lastModified = content.toString().trim();
			else if ("ETag".equals(name)) artifact.ETag = content.toString().trim();
			else if ("Size".equals(name)) artifact.size = content.toString().trim();
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
		private List<Artifact> artifacts = new ArrayList<>();
		private Version version;

		public Element() {}

		public void add(Artifact artifact) {
			artifacts.add(artifact);
			version = artifact.getVersion();
		}

		public Artifact getJAR() {
			for (Artifact a: artifacts) {
				if ("jar".equalsIgnoreCase(a.type) && a.classifier == null) {
					return a;
				}
			}
			return null;
		}

		public Artifact getLCO() {
			for (Artifact a: artifacts) {
				if ("lco".equalsIgnoreCase(a.type) && a.classifier == null) {
					return a;
				}
			}
			return null;
		}

		public String getETag() {
			Artifact art = getJAR();
			if (art != null) return art.ETag;
			for (Artifact a: artifacts) {
				return a.ETag;
			}
			return null;
		}

		public DateTime getLastModifed() {
			Artifact art = getJAR();
			if (art != null) return art.getLastModifed();
			for (Artifact a: artifacts) {
				return a.getLastModifed();
			}
			return null;
		}

		public String getSize() {
			Artifact art = getJAR();
			if (art != null) return art.size;
			for (Artifact a: artifacts) {
				return a.size;
			}
			return null;
		}

		public Version getVersion() {
			return version;
		}

		public List<Artifact> getArtifacts() {
			return artifacts;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("version:").append(getVersion()).append(';');

			for (Artifact a: artifacts) {
				sb.append(a.name).append(';');
			}

			return sb.toString();
		}

	}

	public static class Artifact {
		public String raw;
		public Version version;
		public String name;
		public String size;
		public String ETag;
		public String lastModified;
		public String type;
		public String classifier;
		private URL root;
		private DateTime lastMod;

		public Artifact(URL root) {
			this.root = root;
		}

		public URL getURL() throws MalformedURLException {
			return new URL(root + raw);
		}

		public Artifact init(String raw) {
			this.raw = raw;
			if (raw.startsWith("org/lucee/lucee/")) {
				int index = raw.indexOf('/', 17);
				if (index == -1) return null;
				try {
					this.version = OSGiUtil.toVersion(raw.substring(16, index));
				}
				catch (BundleException e) {
					LogUtil.log("s3-update-provider", e);
				}
				this.name = raw.substring(index + 1);

				if (name.startsWith("lucee-" + version)) {
					index = name.lastIndexOf('.');
					this.type = name.substring(index + 1);
					int i;
					if (index > (i = ("lucee-" + version).length())) {
						classifier = name.substring(i + 1, index);
					}
				}
			}
			return this;
		}

		public Version getVersion() {
			return version;
		}

		@Override
		public String toString() {
			return new StringBuilder().append("size:").append(size).append(";version:").append(getVersion()).append(";last-mod:").append(lastModified).toString();
		}

		public DateTime getLastModifed() {
			if (lastMod == null) {
				this.lastMod = DateCaster.toDateAdvanced(lastModified, (TimeZone) null, null);
			}
			return lastMod;
		}
	}
}