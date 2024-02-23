package lucee.runtime.config.s3;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.CodeSource;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.felix.framework.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.print;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.osgi.BundleLoader;
import lucee.loader.util.Util;
import lucee.runtime.config.maven.MavenUpdateProvider;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.Pack200Util;
import lucee.transformer.library.function.FunctionLibEntityResolver;
import lucee.transformer.library.function.FunctionLibException;

public final class BundleProvider extends DefaultHandler {
	public static final int CONNECTION_TIMEOUT = 1000;
	private static final long MAX_AGE = 10000;
	private static final int MAX_REDIRECTS = 10;

	private static URL DEFAULT_PROVIDER_LIST = null;
	private static URL[] DEFAULT_PROVIDER_DETAILS = null;
	private static URL DEFAULT_PROVIDER_DETAIL_MVN = null;

	private static URL defaultProviderList;
	private static URL[] defaultProviderDetail;
	private static URL defaultProviderDetailMvn;
	private final Map<String, List<Info>> mappings;

	static {
		try {
			DEFAULT_PROVIDER_LIST = new URL("https://bundle-download.s3.amazonaws.com/");
			DEFAULT_PROVIDER_DETAILS = new URL[] { DEFAULT_PROVIDER_LIST };
			DEFAULT_PROVIDER_DETAIL_MVN = new URL("https://repo1.maven.org/maven2/");

		}
		catch (Exception e) {
		}

	}

	public static URL getDefaultProviderList() {
		if (defaultProviderList == null) {
			String str = SystemUtil.getSystemPropOrEnvVar("lucee.s3.bundle.list", null);
			if (!StringUtil.isEmpty(str, true)) {
				try {
					defaultProviderList = new URL(str.trim());
				}
				catch (Exception e) {
				}
			}
			if (defaultProviderList == null) defaultProviderList = DEFAULT_PROVIDER_LIST;
		}
		return defaultProviderList;
	}

	private static void put(Map<String, List<Info>> mappings, String name, Info... values) {
		if (mappings.containsKey(name)) throw new RuntimeException(name + " already set");
		List<Info> list = new ArrayList<>();
		for (Info i: values) {
			list.add(i);
		}
		mappings.put(name, list);
	}

	private static void put(Map<String, List<Info>> mappings, String name, List<Info> values) {
		if (mappings.containsKey(name)) throw new RuntimeException(name + " already set");

		mappings.put(name, values);
	}

	public static URL[] getDefaultProviderDetail() {
		if (defaultProviderDetail == null) {
			String str = SystemUtil.getSystemPropOrEnvVar("lucee.s3.bundle.detail", null);
			if (!StringUtil.isEmpty(str, true)) {
				try {
					defaultProviderDetail = new URL[] { new URL(str.trim()) };
				}
				catch (Exception e) {
				}
			}
			if (defaultProviderDetail == null) defaultProviderDetail = DEFAULT_PROVIDER_DETAILS;
		}
		return defaultProviderDetail;
	}

	public static URL getDefaultProviderDetailMvn() {
		if (defaultProviderDetailMvn == null) {
			String str = SystemUtil.getSystemPropOrEnvVar("lucee.mvn.bundle.detail", null);
			if (!StringUtil.isEmpty(str, true)) {
				try {
					defaultProviderDetailMvn = new URL(str.trim());
				}
				catch (Exception e) {
				}
			}
			if (defaultProviderDetailMvn == null) defaultProviderDetailMvn = DEFAULT_PROVIDER_DETAIL_MVN;
		}
		return defaultProviderDetailMvn;
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

	private URL[] details;

	private static Map<String, Pair<Long, BundleProvider>> readers = new HashMap<>();

	private BundleProvider(URL list, URL[] details) throws MalformedURLException {
		Bundle bundle = null;
		try {
			bundle = CFMLEngineFactory.getInstance().getBundleContext().getBundle();
		}
		catch (Exception e) {

		}

		mappings = readIniFile(SystemUtil.getResourceAsStream(bundle, "META-INF/osgi-maven-mapping.ini"));
		if (!list.toExternalForm().endsWith("/")) this.url = new URL(list.toExternalForm() + "/");
		else this.url = list;

		for (int i = 0; i < details.length; i++) {
			if (!details[i].toExternalForm().endsWith("/")) details[i] = new URL(details[i].toExternalForm() + "/");
		}
		this.details = details;
	}

	public static BundleProvider getInstance() throws MalformedURLException {
		return getInstance(getDefaultProviderList(), getDefaultProviderDetail());
	}

	public static BundleProvider getInstance(URL list, URL[] details) throws MalformedURLException {
		String key = toKey(list, details);
		Pair<Long, BundleProvider> pair = readers.get(key);
		if (pair != null && pair.getName().longValue() + MAX_AGE > System.currentTimeMillis()) {
			return pair.getValue();
		}
		BundleProvider reader = new BundleProvider(list, details);
		readers.put(key, new Pair<Long, BundleProvider>(System.currentTimeMillis(), reader));
		return reader;
	}

	private static String toKey(URL list, URL[] details) {
		StringBuilder sb = new StringBuilder().append(list.toExternalForm());
		for (URL d: details) {
			sb.append(';').append(d.toExternalForm());
		}
		return sb.toString();
	}

	public URL getBundleAsURL(BundleDefinition bd, boolean includeS3, URL defaultValue) {
		try {
			return getBundleAsURL(bd, includeS3);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public URL getBundleAsURL(BundleDefinition bd, boolean includeS3) throws PageException, MalformedURLException, IOException {
		URL url = null;

		// MAVEN: looking for a matching mapping, so we can get from maven
		List<Info> infos = mappings.get(bd.getName());
		if (infos != null && infos.size() > 0) {
			String v;
			for (Info info: infos) {
				if (!info.isMaven()) continue;
				v = bd.getVersionAsString();
				url = validate(createURL(getDefaultProviderDetailMvn(), info, v), null);
				if (url != null) return url;
				if (v != null && v.endsWith(".0")) {
					v = v.substring(0, v.length() - 2);
					url = validate(createURL(getDefaultProviderDetailMvn(), info, v), null);
					if (url != null) return url;
				}
			}

		}
		else {
			String last = ListUtil.last(bd.getName(), '.');
			url = validate(
					new URL(getDefaultProviderDetailMvn(), bd.getName().replace('.', '/') + "/" + bd.getVersionAsString() + "/" + last + "-" + bd.getVersionAsString() + ".jar"),
					null);
			if (url != null) return url;
		}
		if (!includeS3) throw new IOException("no URL found for bundle [" + bd + "]");
		// S3: we check for a direct match
		for (URL detail: details) {
			url = validate(new URL(detail, bd.getName() + "-" + bd.getVersionAsString() + ".jar"), null);
			if (url != null) return url;
		}

		// S3: we loop through all records and S3 and pick one
		if (url == null) {
			try {
				for (Element e: read()) {
					if (bd.equals(e.getBundleDefinition())) {
						url = e.getJAR();
						if (url != null) return url;
					}
				}
			}
			catch (Exception e) {
				throw ExceptionUtil.toIOException(e);
			}
		}

		throw new IOException("no URL found for bundle [" + bd + "]");
	}

	public InputStream getBundleAsStream(BundleDefinition bd) throws PageException, MalformedURLException, IOException, GeneralSecurityException, SAXException {
		URL url = getBundleAsURL(bd, true);

		if (url != null) {
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
		throw new IOException("no bundle found for bundle [" + bd + "]");

	}

	public File downloadBundle(BundleDefinition bd) throws IOException, PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		final File jarDir = eng.getCFMLEngineFactory().getBundleDirectory();

		// before we download we check if we have it bundled
		File jar = deployBundledBundle(jarDir, bd.getName(), bd.getVersionAsString());
		if (jar != null && jar.isFile()) return jar;
		if (jar != null) {
			LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download",
					jar + " should exist but does not (exist?" + jar.exists() + ";file?" + jar.isFile() + ";hidden?" + jar.isHidden() + ")");

		}

		String str = Util._getSystemPropOrEnvVar("lucee.enable.bundle.download", null);
		if (str != null && ("false".equalsIgnoreCase(str) || "no".equalsIgnoreCase(str))) { // we do not use CFMLEngine to cast, because the engine may not exist yet
			throw (new RuntimeException("Lucee is missing the Bundle jar, " + bd.getName() + ":" + bd.getVersionAsString()
					+ ", and has been prevented from downloading it. If this jar is not a core jar, it will need to be manually downloaded and placed in the {{lucee-server}}/context/bundles directory."));
		}

		jar = new File(jarDir, bd.getName() + "-" + bd.getVersionAsString() + (".jar"));

		final URL updateUrl = getBundleAsURL(bd, true);

		LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download",
				"Downloading bundle [" + bd.getName() + ":" + bd.getVersionAsString() + "] from " + updateUrl + " and copying to " + jar);

		int code;
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) updateUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10000);
			conn.connect();
			code = conn.getResponseCode();
		}
		catch (UnknownHostException e) {
			LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download",
					"Failed to download the bundle  [" + bd.getName() + ":" + bd.getVersionAsString() + "] from [" + updateUrl + "] and copy to [" + jar + "]");
			// remove
			throw new IOException("Failed to download the bundle  [" + bd.getName() + ":" + bd.getVersionAsString() + "] from [" + updateUrl + "] and copy to [" + jar + "]", e);
		}
		// the update provider is not providing a download for this
		if (code != 200) {

			// the update provider can also provide a different (final) location for this
			int count = 1;
			while ((code == 302 || code == 301) && count++ <= MAX_REDIRECTS) {
				String location = conn.getHeaderField("Location");
				// just in case we check invalid names
				if (location == null) location = conn.getHeaderField("location");
				if (location == null) location = conn.getHeaderField("LOCATION");
				LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download", "download redirected:" + location);

				conn.disconnect();
				URL url = new URL(location);
				try {
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(10000);
					conn.connect();
					code = conn.getResponseCode();
				}
				catch (final UnknownHostException e) {
					LogUtil.log("deploy", "bundle-download", e);
					throw new IOException("Failed to download the bundle  [" + bd.getName() + ":" + bd.getVersionAsString() + "] from [" + location + "] and copy to [" + jar + "]",
							e);
				}
			}

			// no download available!
			if (code != 200) {
				final String msg = "Failed to download the bundle for [" + bd.getName() + "] in version [" + bd.getVersionAsString() + "] from [" + updateUrl
						+ "], please download manually and copy to [" + jarDir + "]";
				LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download", msg);
				conn.disconnect();
				throw new IOException(msg);
			}

		}

		IOUtil.copy((InputStream) conn.getContent(), new FileOutputStream(jar), true, true);
		conn.disconnect();
		return jar;
		/*
		 * } else { throw new IOException("File ["+jar.getName()+"] already exists, won't copy new one"); }
		 */
	}

	private File deployBundledBundle(File bundleDirectory, String symbolicName, String symbolicVersion) {
		String sub = "bundles/";
		String nameAndVersion = symbolicName + "|" + symbolicVersion;
		String osgiFileName = symbolicName + "-" + symbolicVersion + ".jar";
		String pack20Ext = ".jar.pack.gz";
		boolean isPack200 = false;

		// first we look for an exact match
		InputStream is = getClass().getResourceAsStream("bundles/" + osgiFileName);
		if (is == null) is = getClass().getResourceAsStream("/bundles/" + osgiFileName);

		if (is != null) LogUtil.log(Logger.LOG_DEBUG, "deploy", "bundle-download", "Found ]/bundles/" + osgiFileName + "] in lucee.jar");
		else LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download", "Could not find [/bundles/" + osgiFileName + "] in lucee.jar");

		if (is == null) {
			is = getClass().getResourceAsStream("bundles/" + osgiFileName + pack20Ext);
			if (is == null) is = getClass().getResourceAsStream("/bundles/" + osgiFileName + pack20Ext);
			isPack200 = true;

			if (is != null) LogUtil.log(Logger.LOG_DEBUG, "deploy", "bundle-download", "Found [/bundles/" + osgiFileName + pack20Ext + "] in lucee.jar");
			else LogUtil.log(Logger.LOG_INFO, "deploy", "bundle-download", "Could not find [/bundles/" + osgiFileName + pack20Ext + "] in lucee.jar");
		}
		if (is != null) {
			File temp = null;
			try {
				// copy to temp file
				temp = File.createTempFile("bundle", ".tmp");
				LogUtil.log(Logger.LOG_DEBUG, "deploy", "bundle-download", "Copying [lucee.jar!/bundles/" + osgiFileName + pack20Ext + "] to [" + temp + "]");
				Util.copy(new BufferedInputStream(is), new FileOutputStream(temp), true, true);

				if (isPack200) {
					File temp2 = File.createTempFile("bundle", ".tmp2");
					Pack200Util.pack2Jar(temp, temp2);
					LogUtil.log(Logger.LOG_DEBUG, "deploy", "bundle-download", "Upack [" + temp + "] to [" + temp2 + "]");
					temp.delete();
					temp = temp2;
				}

				// adding bundle
				File trg = new File(bundleDirectory, osgiFileName);
				FileUtil.move(temp, trg);
				LogUtil.log(Logger.LOG_DEBUG, "deploy", "bundle-download", "Adding bundle [" + symbolicName + "] in version [" + symbolicVersion + "] to [" + trg + "]");
				return trg;
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
			finally {
				if (temp != null && temp.exists()) temp.delete();
			}
		}

		// now we search the current jar as an external zip what is slow (we do not support pack200 in this
		// case)
		// this also not works with windows
		if (SystemUtil.isWindows()) return null;
		ZipEntry entry;
		File temp;
		ZipInputStream zis = null;
		try {
			CodeSource src = CFMLEngineFactory.class.getProtectionDomain().getCodeSource();
			if (src == null) return null;
			URL loc = src.getLocation();

			zis = new ZipInputStream(loc.openStream());
			String path, name, bundleInfo;
			int index;
			while ((entry = zis.getNextEntry()) != null) {
				temp = null;
				path = entry.getName().replace('\\', '/');
				if (path.startsWith("/")) path = path.substring(1); // some zip path start with "/" some not
				isPack200 = false;
				if (path.startsWith(sub) && (path.endsWith(".jar") /* || (isPack200=path.endsWith(".jar.pack.gz")) */)) { // ignore non jar files or file from elsewhere
					index = path.lastIndexOf('/') + 1;
					if (index == sub.length()) { // ignore sub directories
						name = path.substring(index);
						temp = null;
						try {
							temp = File.createTempFile("bundle", ".tmp");
							Util.copy(zis, new FileOutputStream(temp), false, true);

							/*
							 * if(isPack200) { File temp2 = File.createTempFile("bundle", ".tmp2"); Pack200Util.pack2Jar(temp,
							 * temp2); temp.delete(); temp=temp2; name=name.substring(0,name.length()-".pack.gz".length()); }
							 */

							bundleInfo = BundleLoader.loadBundleInfo(temp);
							if (bundleInfo != null && nameAndVersion.equals(bundleInfo)) {
								File trg = new File(bundleDirectory, name);
								temp.renameTo(trg);
								LogUtil.log(Logger.LOG_DEBUG, "deploy", "bundle-download",
										"Adding bundle [" + symbolicName + "] in version [" + symbolicVersion + "] to [" + trg + "]");

								return trg;
							}
						}
						finally {
							if (temp != null && temp.exists()) temp.delete();
						}

					}
				}
				zis.closeEntry();
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		finally {
			Util.closeEL(zis);
		}
		return null;
	}

	// 1146:size:305198;bundle:name:xmlgraphics.batik.awt.util;version:version EQ 1.8.0;;last-mod:{ts
	// '2024-01-14 22:32:05'};

	public List<Element> read() throws IOException, GeneralSecurityException, SAXException, PageException {
		int count = 100;
		URL url = null;

		if (lastKey != null) url = new URL(this.url.toExternalForm() + "?marker=" + lastKey);

		do {
			if (url == null) url = isTruncated ? new URL(this.url.toExternalForm() + "?marker=" + this.lastKey) : this.url;
			HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, BundleProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
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
				int cmp = l.getBundleDefinition().getName().compareTo(r.getBundleDefinition().getName());
				if (cmp != 0) return cmp;
				return OSGiUtil.compare(l.getBundleDefinition().getVersion(), r.getBundleDefinition().getVersion());
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
		if (tree.size() == 2 && "Contents".equals(name)) {
			insideContents = false;
			if (element.validExtension()) {
				BundleDefinition bd = element.getBundleDefinition();
				if (bd != null) {
					Element existing = elements.get(bd.toString());
					if (existing != null) existing.addKey(element.keys.get(0));
					else {
						element.bd = bd;
						elements.put(bd.toString(), element);
					}
				}
			}
		}
		else if (insideContents) {
			if ("Key".equals(name)) {
				lastKey = content.toString().trim();

				element.addKey(lastKey);
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

	public static URL validate(URL url, URL defaultValue) {
		try {
			HTTPResponse rsp = HTTPEngine4Impl.head(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
			if (rsp != null) {
				int sc = rsp.getStatusCode();
				if (sc >= 200 && sc < 300) return url;
			}
		}
		catch (Exception e) {

		}
		return defaultValue;
	}

	public static class Element {
		private List<String> keys = new ArrayList<>();
		private long size;
		private String etag;
		private DateTime lastMod;
		private BundleDefinition bd;
		private URL[] details;
		private URL jar;

		public Element(URL[] details) {
			this.details = details;
		}

		public boolean validExtension() {
			for (String k: keys) {
				if (k.endsWith(".jar")) return true;
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

		private void initFiles() throws MalformedURLException {
			for (String k: keys) {
				if (k.endsWith(".jar")) jar = validate(k);
			}
		}

		private URL validate(String k) throws MalformedURLException {
			URL url;
			for (URL d: details) {
				url = BundleProvider.validate(new URL(d.toExternalForm() + k), null);
				if (url != null) return url;
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

		public static BundleDefinition toBundleDefinition(String version, BundleDefinition defaultValue) {
			if (!version.endsWith(".jar")) return defaultValue;
			version = version.substring(0, version.length() - 4);

			// an OSGi version number can have "-" ony in the last part, so to find the complete version number
			// we execlude the last part from the search
			int index = version.lastIndexOf('.');
			if (index == -1) return defaultValue;
			String v = version.substring(0, index);
			index = v.lastIndexOf('-');
			if (index == -1) return defaultValue;

			String symbolicName = version.substring(0, index);
			Version symbolicVersion = OSGiUtil.toVersion(version.substring(index + 1), null);
			if (symbolicVersion == null) return defaultValue;
			return new BundleDefinition(symbolicName, symbolicVersion);
		}

		public BundleDefinition getBundleDefinition() {
			if (bd == null) {
				BundleDefinition tmp;
				for (String k: keys) {
					tmp = toBundleDefinition(k, null);
					if (tmp != null) {
						bd = tmp;
						break;
					}
				}
			}
			return bd;
		}

		@Override
		public String toString() {
			return new StringBuilder().append("size:").append(size).append(";bundle:").append(getBundleDefinition()).append(";last-mod:").append(lastMod).toString();
		}
	}

	private static Map<String, List<Info>> readIniFile(InputStream is) {
		Map<String, List<Info>> mappings = new ConcurrentHashMap<>();
		String section = "", key, value;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String line;
			List<Info> infos = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				if (!line.isEmpty()) {
					if (line.startsWith("[") && line.endsWith("]")) {
						section = line.substring(1, line.length() - 1);
						infos = new ArrayList<>();
						mappings.put(section, infos);
					}
					else if (line.contains("=")) {
						int i = line.indexOf('=');
						key = line.substring(0, i).trim();
						value = line.substring(i + 1).trim();

						i = key.indexOf(':');
						int index;
						if (i != -1) {
							index = Integer.parseInt(key.substring(i + 1));
							key = key.substring(0, i);
						}
						else {
							index = 1;
						}

						Info info;
						// set new info
						if (infos.size() < index) {
							info = new Info();
							infos.add(index - 1, info);
						}
						else {
							info = infos.get(index - 1);
						}

						if ("groupId".equals(key)) info.setGroupId(value);
						else if ("artifactId".equals(key)) info.setArtifactId(value);
						else throw new IOException("key [" + key + "] is invalid, only valid keys are [groupId, artifactId]");
						// infos.add(new Info(key, value));
					}
					// Ignore lines that are not key-value pairs or section headers
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return mappings;
	}

	public static void main(String[] args) throws Exception {
		// memcached)(bundle-version>=3.0.2
		// print.e(getInstance().getBundleAsURL(new BundleDefinition("org.lucee.spymemcached",
		// "2.12.3.0001"), true));
		// getInstance().getBundle(new BundleDefinition("com.mysql.cj", "8.0.33"));
		// getInstance().getBundle(new BundleDefinition("com.mysql.cj", "8.0.27"));
		print.e("------- MAVEN -> OSGi Mappings -------");
		getInstance().createOSGiMavenMapping(); // create java code for OSGi to Maven mapping based on
		print.e("------- What can be removed from S3? -------");
		getInstance().whatcanBeRemovedFromS3(); // create java code for OSGi to Maven mapping based on
		// what is in the S3 bucket
		// getInstance().whatcanBeRemovedFromS3(); // show what records can be removed from S3 bucket
		// because we have a working link to Maven
	}

	public void createOSGiMavenMapping() throws PageException, IOException, GeneralSecurityException, SAXException {
		Struct sct = new StructImpl();
		Set<String> has = new HashSet<>();
		List<Info> infos;
		Info info;
		for (Element e: read()) {
			infos = mappings.get(e.bd.getName());
			if (infos != null) continue;

			try {
				info = extractMavenInfoFromZip(getBundleAsStream(e.bd));
				if (!info.isOSGi()) {
					info.setBundleSymbolicName(e.bd.getName());
				}
				else if (!e.bd.getName().equals(info.getBundleSymbolicName())) {
					print.e("!!! file name [" + e.bd.getName() + "-" + e.bd.getVersionAsString() + ".jar] differs from symbolic name [" + info.getBundleSymbolicName()
							+ "] in the Manifest.MF");

				}
				if (!has.contains(info.bundleSymbolicName) && info.isComplete()) {
					has.add(info.bundleSymbolicName);
					print.e("\nput(mappings,\"" + info.bundleSymbolicName + "\", new Info(\"" + info.groupId + "\", \"" + info.artifactId + "\"));");

					print.e("\n[" + info.bundleSymbolicName + "]");
					print.e("groupId=" + info.getGroupId());
					print.e("artifactId=" + info.getArtifactId());

					info.add(sct);
				}
				else if (!info.isComplete()) {
					// print.e("// put(mappings,\"" + info.bundleSymbolicName + "\", new Info(\"\", \"\"));");
				}
			}
			catch (Exception ex) {
				print.e(e.bd.getName() + ":" + e.bd.getVersionAsString());
				ex.printStackTrace();
			}
		}
	}

	public void whatcanBeRemovedFromS3() throws PageException, IOException, GeneralSecurityException, SAXException {
		URL url;
		for (Element e: read()) {
			url = getBundleAsURL(e.bd, false, null);

			if (url != null) {
				print.e("// " + (url.toExternalForm()));
				print.e("print.e(getInstance().getBundleAsURL(new BundleDefinition(\"" + e.bd.getName() + "\", \"" + e.bd.getVersion() + "\"),null,true));");

				getInstance().getBundleAsStream(new BundleDefinition(e.bd.getName(), e.bd.getVersion()));
			}

			// name:apache.http.components.mime;version:version EQ 4.4.1;
			// lucee.runtime.config.s3.S3BundleProvider$Info[]{groupId:org.apache.httpcomponents;artifactId:httpcomponents-client;bundleSymbolicName:null

			// name:apache.http.components.mime;version:version EQ 4.5.0;
			// lucee.runtime.config.s3.S3BundleProvider$Info[]{groupId:org.apache.httpcomponents;artifactId:httpcomponents-client;bundleSymbolicName:null

			// https://repo1.maven.org/maven2/org/apache/httpcomponents/httpmime/4.4.1/httpmime-4.4.1.jar
		}
	}

	private static URL createURL(URL base, Info info, String version) throws MalformedURLException {
		return new URL(base, info.groupId.replace('.', '/') + "/" + info.artifactId + "/" + version + "/" + info.artifactId + "-" + version + ".jar");
	}

	public static Info extractMavenInfoFromZip(InputStream is) throws IOException {
		Info info = new Info();

		try (ZipInputStream zipStream = new ZipInputStream(is)) {
			String name;
			ZipEntry entry;
			boolean hasMaven = false;
			String n, g, a, v, content;
			int start, end;
			while ((entry = zipStream.getNextEntry()) != null) {
				name = improve(entry.getName());
				// read Maven properties
				if (!hasMaven && (name).startsWith("META-INF/maven/") && (name).endsWith("/pom.properties")) {

					// Create a Properties object
					Properties prop = new Properties();

					// Load properties from the InputStream
					prop.load(new ByteArrayInputStream(readEntry(zipStream)));
					g = prop.getProperty("groupId");
					a = prop.getProperty("artifactId");
					v = prop.getProperty("version");

					if (!Util.isEmpty(g) && !Util.isEmpty(a) && !Util.isEmpty(v)) {
						info.setGroupId(g);
						info.setArtifactId(a);
						// info.setVersion(v);
						hasMaven = true;
					}
				}
				// read Maven xml
				if (!hasMaven && (name).startsWith("META-INF/maven/") && (name).endsWith("/pom.xml")) {
					content = new String(readEntry(zipStream));
					start = content.indexOf("</parent>");
					if (start == -1) start = 0;
					start = content.indexOf("<groupId>", start);
					end = content.indexOf("</groupId>");
					g = null;
					a = null;
					v = null;
					if (start != -1 && end > start) {
						g = content.substring(start + 9, end);
					}
					start = content.indexOf("<artifactId>", end);
					end = content.indexOf("</artifactId>", end);
					if (start != -1 && end > start) {
						a = content.substring(start + 12, end);
					}
					start = content.indexOf("<version>", end);
					end = content.indexOf("</version>", end);
					if (start != -1 && end > start) {
						v = content.substring(start + 9, end);
					}
					if (!Util.isEmpty(g) && !Util.isEmpty(a) && !Util.isEmpty(v)) {
						info.setGroupId(g);
						info.setArtifactId(a);
						// info.setVersion(v);
						hasMaven = true;
					}
				}

				// read Manifest
				if (entry.getName().equals("META-INF/MANIFEST.MF")) {

					Manifest manifest = new Manifest(new ByteArrayInputStream(readEntry(zipStream)));
					java.util.jar.Attributes attr = manifest.getMainAttributes();
					// id = unwrap(attr.getValue("mapping-id"));
					n = StringUtil.unwrap(attr.getValue("Bundle-SymbolicName"));
					v = StringUtil.unwrap(attr.getValue("Bundle-Version"));
					if (!Util.isEmpty(n, true) && !Util.isEmpty(v, true)) {
						info.setBundleSymbolicName(n);
					}
				}
			}
		}

		return info;
	}

	private static byte[] readEntry(ZipInputStream zipStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead;

		while ((bytesRead = zipStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}

		return outputStream.toByteArray();
	}

	private static String improve(String name) {
		if (name == null) return "";
		name = name.replace('\\', '/');
		if (name.startsWith("/")) name = name.substring(1);
		if (name.endsWith("/")) name = name.substring(0, name.length() - 1);
		return name;
	}

	private static class Info {
		private String groupId;
		private String artifactId;
		private String bundleSymbolicName;

		public Info() {

		}

		public Info(String groupId, String artifactId) {
			this.groupId = groupId;
			this.artifactId = artifactId;
		}

		public Info(String groupId, String artifactId, String version) {
			this.groupId = groupId;
			this.artifactId = artifactId;
		}

		public String getGroupId() {
			return groupId;
		}

		public void add(Struct sct) throws PageException {
			if (!isComplete()) return;
			Struct data = new StructImpl();
			data.set("groupid", getGroupId());
			data.set("artifactid", getArtifactId());
			data.set("bundleSymbolicName", getBundleSymbolicName());

			sct.set(getBundleSymbolicName(), data);
		}

		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}

		public String getArtifactId() {
			return artifactId;
		}

		public void setArtifactId(String artifactId) {
			this.artifactId = artifactId;
		}

		public boolean isComplete() {
			return isMaven() && isOSGi();
		}

		public boolean isMaven() {
			return groupId != null && artifactId != null;
		}

		public boolean isOSGi() {
			return bundleSymbolicName != null;
		}

		public String getBundleSymbolicName() {
			return bundleSymbolicName;
		}

		public void setBundleSymbolicName(String bundleSymbolicName) {
			this.bundleSymbolicName = bundleSymbolicName;
		}

		@Override
		public String toString() {
			return String.format("groupId:%s;artifactId:%s;bundleSymbolicName:%s", groupId, artifactId, bundleSymbolicName);
		}
	}
}