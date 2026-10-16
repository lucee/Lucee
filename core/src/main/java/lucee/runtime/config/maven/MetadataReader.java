package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.net.http.HTTPEngine;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.library.function.FunctionLibEntityResolver;

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

	public List<Version> read() throws IOException, SAXException {
		Log log = LogUtil.getLog(null, "maven", "application");

		if (LogUtil.doesDebug(log)) {
			log.debug("maven", "reading metadata for " + group + ":" + artifact + " (timeout: " + repository.timeoutList + ")");
		}

		// 1. Check local cache first
		List<Version> versionsFromCache = readFromCache("");
		if (versionsFromCache != null) {
			if (LogUtil.doesDebug(log)) {
				log.debug("maven", "metadata for " + group + ":" + artifact + " loaded from cache");
			}
			return versionsFromCache;
		}

		if (LogUtil.doesInfo(log)) {
			log.info("maven", "fetching new metadata for " + group + ":" + artifact + " from repository");
		}

		this.versions = new ArrayList<>();

		// Updated URL with correct path structure
		URL url = new URL(repository.url + group.replace('.', '/') + '/' + artifact + "/maven-metadata.xml");

		Reader r = null;
		try {
			// Use HTTPDownloader for the actual network call
			r = IOUtil.getReader(HTTPEngine.get(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, MavenUpdateProvider.READ_TIMEOUT, null, null, true), (Charset) null);
			init(new InputSource(r));
		}
		catch (IOException ioe) {
			// If the file is missing (404) or server is down, we cache the empty result to prevent hammering
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
			Resource resLastmod = repository.cacheDirectory
					.getRealResource(HashUtil.create64BitHashAsString(repository.url + "_" + group + "_" + artifact + appendix + "_lastmod", Character.MAX_RADIX));
			Resource resVersions = repository.cacheDirectory
					.getRealResource(HashUtil.create64BitHashAsString(repository.url + "_" + group + "_" + artifact + appendix + "_versions", Character.MAX_RADIX));
			StringBuilder sb = new StringBuilder();
			for (Version v: versions) {
				sb.append(v.toString()).append(',');
			}

			IOUtil.write(resVersions, sb.length() == 0 ? "" : sb.toString().substring(0, sb.length() - 1), StandardCharsets.UTF_8, false);
			IOUtil.write(resLastmod, Caster.toString(System.currentTimeMillis()), StandardCharsets.UTF_8, false);
		}
		catch (Exception e) {
			LogUtil.log("MetadataReader", e);
		}
	}

	private List<Version> readFromCache(String appendix) {
		if (DEBUG) return null;
		try {
			Resource resLastmod = repository.cacheDirectory
					.getRealResource(HashUtil.create64BitHashAsString(repository.url + "_" + group + "_" + artifact + appendix + "_lastmod", Character.MAX_RADIX));
			if (resLastmod.isFile()) {
				long lastmod = repository.timeoutList == Repository.TIMEOUT_NEVER ? Repository.TIMEOUT_NEVER
						: Caster.toLongValue(IOUtil.toString(resLastmod, StandardCharsets.UTF_8), 0L);
				if (repository.timeoutList == Repository.TIMEOUT_NEVER || lastmod + repository.timeoutList > System.currentTimeMillis()) {
					Resource resVersions = repository.cacheDirectory
							.getRealResource(HashUtil.create64BitHashAsString(repository.url + "_" + group + "_" + artifact + appendix + "_versions", Character.MAX_RADIX));
					String content = IOUtil.toString(resVersions, StandardCharsets.UTF_8);
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
			catch (IOException e) {
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