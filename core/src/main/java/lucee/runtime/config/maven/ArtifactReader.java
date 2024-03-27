package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

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
	private Map<String, Repository> tmpNexuss = new HashMap<>();
	private Map<String, String> tmpMeta = new HashMap<>();

	private String listProvider;
	private String group;
	private String artifact;
	private String key;
	private boolean insideNexus;
	private Repository nexus;

	private static Map<String, Integer> totalCounts = new ConcurrentHashMap<>();
	private static Map<String, List<Version>> versionss = new ConcurrentHashMap<>();
	private static Map<String, Map<String, String>> metas = new ConcurrentHashMap<>();

	ArtifactReader(String listProvider, String group, String artifact) {
		this.listProvider = listProvider;
		this.group = group;
		this.artifact = artifact;
		this.key = listProvider + ":" + group + ":" + artifact;
	}

	public void read() throws IOException, GeneralSecurityException, SAXException {

		Integer tc = totalCounts.get(key);

		// first we check the size
		if (tc != null) {
			// first we see if there is a change
			read(1, 1);
			// if it matches we take the cached data
			int tmpTotalCount = Caster.toIntValue(tmpMeta.get("totalCount"), 0);
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

		// Sort using streams with custom comparator
		List<Version> sorted = tmpArtifacts.stream().sorted(new Comparator<Version>() {
			@Override
			public int compare(Version l, Version r) {
				return OSGiUtil.compare(l, r);
			}
		}).collect(Collectors.toList());

		versionss.put(key, sorted);
		metas.put(key, tmpMeta);

		clear();
	}

	private void read(int from, int count) throws IOException, GeneralSecurityException, SAXException {

		URL url = new URL(listProvider + "?g=" + group + "&a=" + artifact + "&c=sources&from=" + from + "&count=" + count);
		HTTPResponse rsp = HTTPEngine4Impl.get(url, null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, true, null, null, null, null);
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
		tmpNexuss = new HashMap<>();
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
		// enter nexus?
		if (tree.size() == 2 && "repoDetails".equals(tree.peek()) && "org.sonatype.nexus.rest.model.NexusNGRepositoryDetail".equals(name)) {
			insideNexus = true;
			nexus = new Repository();
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
		if (insideNexus) {
			String val = content.toString().trim();
			if ("repositoryId".equals(name)) nexus.id = val;
			else if ("repositoryId".equals(name)) nexus.id = val;
			else if ("repositoryName".equals(name)) nexus.name = val;
			else if ("repositoryContentClass".equals(name)) nexus.contentClass = val;
			else if ("repositoryKind".equals(name)) nexus.kind = val;
			else if ("repositoryPolicy".equals(name)) nexus.policy = val;
			else if ("repositoryURL".equals(name)) nexus.URL = val;

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

		// exit nexus?
		if (tree.size() == 2 && "repoDetails".equals(tree.peek()) && "org.sonatype.nexus.rest.model.NexusNGRepositoryDetail".equals(name)) {
			insideNexus = false;
			tmpNexuss.put(nexus.policy, nexus);
			nexus = null;
		}
	}

	@Override
	public void characters(char ch[], int start, int length) {
		content.append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {

		super.endDocument();
	}

	public Map<String, String> getMeta() {
		return metas.get(key);
	}

	public List<Version> getVersions() {
		return versionss.get(key);
	}

	private static class Repository {
		private String id;
		private String name;
		private String contentClass;
		private String kind;
		private String policy;
		private String URL;

		@Override
		public String toString() {
			return "id:" + id + ";name:" + name + ";contentClass:" + contentClass + ";kind:" + kind + ";policy:" + policy + ";URL:" + URL;
		}
	}
}