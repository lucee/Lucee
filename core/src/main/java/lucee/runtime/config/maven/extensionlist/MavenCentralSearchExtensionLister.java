package lucee.runtime.config.maven.extensionlist;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.httpclient.HTTPResponse4Impl;
import lucee.runtime.config.maven.MavenUpdateProvider;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;

public final class MavenCentralSearchExtensionLister implements ExtensionLister {

	private static final String SEARCH_URL = "https://central.sonatype.com/solrsearch/select";
	private static final int PAGE_SIZE = 200;

	@Override
	public String getName() {
		return "central-search";
	}

	@Override
	public Set<String> list(Repository repo, String groupId) throws IOException {
		ExtensionListUtil.validateGroupId(groupId);
		ExtensionListUtil.logInfo(getName(), "listing artifacts for group [" + groupId + "] via Maven Central Search API");
		Set<String> artifacts = new HashSet<>();
		int start = 0;
		int numFound = -1;

		try {
			while (numFound < 0 || start < numFound) {
				String url = SEARCH_URL + "?q=" + URLEncoder.encode("g:" + groupId, StandardCharsets.UTF_8) + "&rows=" + PAGE_SIZE + "&start=" + start + "&wt=json";
				ExtensionListUtil.logDebug(getName(), "fetching page from [" + url + "]");
				String json = fetch(url);
				if (StringUtil.isEmpty(json, true)) break;

				Page page = parse(json);
				if (numFound < 0) numFound = page.numFound;
				if (page.artifacts.isEmpty()) break;

				artifacts.addAll(page.artifacts);
				start += page.artifacts.size();
				if (page.artifacts.size() < PAGE_SIZE) break;
			}
			ExtensionListUtil.logInfo(getName(), "found " + artifacts.size() + " artifacts for group [" + groupId + "] (" + numFound + " total in index)");
			return artifacts;
		}
		catch (IOException e) {
			ExtensionListUtil.logWarn(getName(), "failed listing artifacts for group [" + groupId + "] via Maven Central Search API", e);
			throw e;
		}
	}

	public static Page parse(String json) throws IOException {
		try {
			Struct root = Caster.toStruct(new JSONExpressionInterpreter(false, JSONExpressionInterpreter.FORMAT_JSON).interpret(null, json));
			Struct response = Caster.toStruct(root.get("response", null), null);
			if (response == null) throw new IOException("invalid Maven Central Search response");

			int numFound = Caster.toIntValue(response.get("numFound", 0), 0);
			Array docs = Caster.toArray(response.get("docs", null), null);
			Set<String> artifacts = new HashSet<>();
			if (docs != null) {
				Iterator<Object> it = docs.valueIterator();
				while (it.hasNext()) {
					Struct doc = Caster.toStruct(it.next(), null);
					if (doc == null) continue;
					String artifactId = Caster.toString(doc.get("a", null), null);
					if (ExtensionListUtil.isValidArtifactId(artifactId)) artifacts.add(artifactId);
				}
			}
			return new Page(numFound, artifacts);
		}
		catch (PageException pe) {
			throw new IOException(pe);
		}
	}

	private static String fetch(String url) throws IOException {
		HTTPResponse4Impl response = HTTPEngine.get(new URL(url), null, null, 2000, MavenUpdateProvider.CONNECTION_TIMEOUT, MavenUpdateProvider.READ_TIMEOUT, true, null,
				"Maven-Central-Search/1.0", null, null, true);
		try {
			int sc = response.getStatusCode();
			if (sc != 200) throw new IOException("HTTP " + sc + " for URL: " + url);
			return IOUtil.toString(response.getContentAsStream(), StandardCharsets.UTF_8);
		}
		finally {
			response.close();
		}
	}

	public static final class Page {
		public final int numFound;
		public final Set<String> artifacts;

		public Page(int numFound, Set<String> artifacts) {
			this.numFound = numFound;
			this.artifacts = artifacts;
		}
	}

}
