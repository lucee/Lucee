package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.net.http.HTTPDownloader;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.library.function.FunctionLibEntityResolver;
import lucee.transformer.library.function.FunctionLibException;

public final class MetadataReader extends DefaultHandler {

	private static final boolean DEBUG = false;
	private XMLReader xmlReader;
	private Stack<String> tree = new Stack<>();
	private StringBuilder content = new StringBuilder();
	private boolean insideVersion;

	private Repository repository;
	private String group;
	private String artifact;
	private List<Version> versions;

	MetadataReader(Repository repository, String group, String artifact) {
		this.repository = repository;
		this.group = group;
		this.artifact = artifact;
	}

	/*
	 * public List<Version> read(String extensionFilter) throws IOException, GeneralSecurityException,
	 * SAXException { if (StringUtil.isEmpty(extensionFilter, true)) return read(); // cache read
	 * List<Version> versionsFromCache = readFromCache(extensionFilter); if (versionsFromCache != null)
	 * { return versionsFromCache; }
	 * 
	 * List<Version> versions = new ArrayList<>(); URL url; int count = 2; for (Version v: read()) {
	 * 
	 * url = new URL(repository.url + group.replace('.', '/') + '/' + artifact + "/" + v + "/" +
	 * artifact + "-" + v + "." + extensionFilter);
	 * 
	 * HTTPResponse rsp = HTTPEngine4Impl.head(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT,
	 * true, null, null, null, null); if (rsp != null) { int sc = rsp.getStatusCode(); if (sc >= 200 &&
	 * sc < 300) { versions.add(v); } } // if at least count have no lex, we assume there is none if
	 * (--count == 0) break; } storeToCache(versions, extensionFilter); return versions; }
	 */

	public List<Version> read() throws IOException, GeneralSecurityException, SAXException {
		// cache read
		List<Version> versionsFromCache = readFromCache("");
		if (versionsFromCache != null) {
			return versionsFromCache;
		}

		this.versions = new ArrayList<>();

		// Updated URL with correct parameter names and no classifier filter
		URL url = new URL(repository.url + group.replace('.', '/') + '/' + artifact + "/maven-metadata.xml");

		// Use HTTPDownloader with DEBUG logging for Maven metadata lookups
		Reader r = null;
		try {
			r = IOUtil.getReader(HTTPDownloader.get(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, MavenUpdateProvider.READ_TIMEOUT, null, Log.LEVEL_TRACE),
					(Charset) null);
			init(new InputSource(r));
		}
		catch (IOException ioe) {
			// 404 or other errors - return empty list
			storeToCache(versions, "");
			return versions;
		}
		finally {
			IOUtil.close(r);
		}
		storeToCache(versions, "");
		return versions;

	}

	private void storeToCache(List<Version> versions, String appendix) {
		try {
			Resource resLastmod = repository.cacheDirectory.getRealResource(HashUtil.create64BitHashAsString(group + "_" + artifact + appendix + "_lastmod", Character.MAX_RADIX));
			Resource resVersions = repository.cacheDirectory
					.getRealResource(HashUtil.create64BitHashAsString(group + "_" + artifact + appendix + "_versions", Character.MAX_RADIX));
			StringBuilder sb = new StringBuilder();
			for (Version v: versions) {
				sb.append(v.toString()).append(',');
			}

			IOUtil.write(resVersions, sb.length() == 0 ? "" : sb.toString().substring(0, sb.length() - 1), CharsetUtil.UTF8, false);
			IOUtil.write(resLastmod, Caster.toString(System.currentTimeMillis()), CharsetUtil.UTF8, false);
		}
		catch (Exception e) {
			LogUtil.log("MetadataReader", e);
		}
	}

	private List<Version> readFromCache(String appendix) {
		if (DEBUG) return null;
		try {
			Resource resLastmod = repository.cacheDirectory.getRealResource(HashUtil.create64BitHashAsString(group + "_" + artifact + appendix + "_lastmod", Character.MAX_RADIX));
			if (resLastmod.isFile()) {
				long lastmod = repository.timeoutList == Repository.TIMEOUT_NEVER ? Repository.TIMEOUT_NEVER
						: Caster.toLongValue(IOUtil.toString(resLastmod, CharsetUtil.UTF8), 0L);
				if (repository.timeoutList == Repository.TIMEOUT_NEVER || lastmod + repository.timeoutList > System.currentTimeMillis()) {
					Resource resVersions = repository.cacheDirectory
							.getRealResource(HashUtil.create64BitHashAsString(group + "_" + artifact + appendix + "_versions", Character.MAX_RADIX));
					String content = IOUtil.toString(resVersions, CharsetUtil.UTF8);
					List<Version> versions = new ArrayList<>();
					if (content.length() > 0) {
						List<String> list = ListUtil.listToList(content, ',', true);
						for (String v: list) {
							versions.add(Version.parseVersion(v.trim()));
						}
					}
					return versions;
				}
			}
		}
		catch (Exception e) {
			LogUtil.log("MetadataReader", e);
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
		if (tree.size() == 3 && "versions".equals(tree.peek()) && "version".equals(name)) {
			insideVersion = true;
		}
		tree.add(qName);
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		if (insideVersion) {
			insideVersion = false;
			try {
				versions.add(Version.parseVersion(content.toString().trim()));
			}
			catch (Exception e) {
				LogUtil.log("MavenReader", e);
			}
		}
		tree.pop();
		content = new StringBuilder();
	}

	@Override
	public void characters(char ch[], int start, int length) {
		content.append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}
}