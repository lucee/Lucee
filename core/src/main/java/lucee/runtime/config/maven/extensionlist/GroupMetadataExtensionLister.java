package lucee.runtime.config.maven.extensionlist;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.io.IOUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.runtime.config.maven.MavenUpdateProvider;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;
import lucee.runtime.text.xml.XMLUtil;
import lucee.transformer.library.function.FunctionLibEntityResolver;

public final class GroupMetadataExtensionLister implements ExtensionLister {

	@Override
	public String getName() {
		return "group-metadata";
	}

	@Override
	public Set<String> list(Repository repo, String groupId) throws IOException {
		ExtensionListUtil.validateGroupId(groupId);
		String url = repo.getUrl() + groupId.replace('.', '/') + "/maven-metadata.xml";
		ExtensionListUtil.logInfo(getName(), "listing artifacts for group [" + groupId + "] from [" + url + "]");

		Reader r = null;
		try {
			r = IOUtil.getReader(HTTPEngine.get(new URL(url), null, null, MavenUpdateProvider.CONNECTION_TIMEOUT, MavenUpdateProvider.READ_TIMEOUT, null, null, true),
					(Charset) null);
			Set<String> artifacts = parse(r);
			ExtensionListUtil.logInfo(getName(), "found " + artifacts.size() + " extension artifacts for group [" + groupId + "]");
			return artifacts;
		}
		catch (IOException e) {
			ExtensionListUtil.logDebug(getName(), "no group metadata for group [" + groupId + "] at [" + url + "]: " + e.getMessage());
			return new HashSet<>();
		}
		catch (SAXException e) {
			ExtensionListUtil.logWarn(getName(), "invalid group metadata for group [" + groupId + "] at [" + url + "]", e);
			return new HashSet<>();
		}
		finally {
			IOUtil.close(r);
		}
	}

	public static Set<String> parse(String xml) throws IOException, SAXException {
		return parse(new StringReader(xml));
	}

	private static Set<String> parse(Reader reader) throws IOException, SAXException {
		GroupMetadataHandler handler = new GroupMetadataHandler();
		XMLReader xmlReader = XMLUtil.createXMLReader();
		xmlReader.setContentHandler(handler);
		xmlReader.setErrorHandler(handler);
		xmlReader.setEntityResolver(new FunctionLibEntityResolver());
		xmlReader.parse(new InputSource(reader));
		return handler.getArtifactIds();
	}

	private static final class GroupMetadataHandler extends DefaultHandler {

		private final Stack<String> tree = new Stack<>();
		private final Set<String> artifactIds = new HashSet<>();
		private final StringBuilder content = new StringBuilder();
		private boolean insideArtifactId;
		private boolean hasArtifacts;

		Set<String> getArtifactIds() {
			return hasArtifacts ? artifactIds : new HashSet<>();
		}

		@Override
		public void startElement(String uri, String name, String qName, Attributes atts) {
			if ("artifacts".equals(name)) hasArtifacts = true;
			if ("artifactId".equals(name) && isInsideArtifact()) insideArtifactId = true;
			tree.push(qName);
		}

		@Override
		public void endElement(String uri, String name, String qName) {
			if (insideArtifactId && "artifactId".equals(name)) {
				String artifactId = content.toString().trim();
				if (ExtensionListUtil.isValidArtifactId(artifactId) && artifactId.endsWith("-extension")) {
					artifactIds.add(artifactId);
				}
				insideArtifactId = false;
			}
			if (!tree.isEmpty()) tree.pop();
			content.setLength(0);
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			if (insideArtifactId) content.append(ch, start, length);
		}

		private boolean isInsideArtifact() {
			for (int i = tree.size() - 1; i >= 0; i--) {
				if ("artifact".equals(tree.get(i))) return true;
				if ("artifacts".equals(tree.get(i))) return false;
			}
			return false;
		}
	}

}
