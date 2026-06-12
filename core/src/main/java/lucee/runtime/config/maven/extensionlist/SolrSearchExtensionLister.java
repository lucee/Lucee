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

public final class SolrSearchExtensionLister implements ExtensionLister {

	private static final String SEARCH_PATH = "solrsearch/select";
	private static final int PAGE_SIZE = 200;

	@Override
	public String getName() {
		return "solr-search";
	}

	public static String buildSearchUrl(String repoUrl, String groupId, int start) {
		String base = ExtensionListUtil.normalizeBaseUrl(repoUrl);
		return base + SEARCH_PATH + "?q=" + URLEncoder.encode("g:" + groupId, StandardCharsets.UTF_8) + "&rows=" + PAGE_SIZE + "&start=" + start + "&wt=json";
	}

	@Override
	public Set<String> list(Repository repo, String groupId) throws IOException {
		ExtensionListUtil.validateGroupId(groupId);
		ExtensionListUtil.logInfo(getName(), "listing artifacts for group [" + groupId + "] via Solr search API at [" + repo.getUrl() + "]");
		Set<String> artifacts = new HashSet<>();
		String baseUrl = ExtensionListUtil.normalizeBaseUrl(repo.getUrl());
		int start = 0;
		int numFound = -1;

		try {
			while (numFound < 0 || start < numFound) {
				PageFetch fetch = fetchPage(baseUrl, groupId, start);
				baseUrl = fetch.baseUrl;
				if (StringUtil.isEmpty(fetch.body, true)) break;

				Page page = parse(fetch.body);
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
			if (!artifacts.isEmpty()) {
				ExtensionListUtil.logWarn(getName(), "failed listing remaining artifacts for group [" + groupId + "], returning " + artifacts.size() + " partial result(s)", e);
				return artifacts;
			}
			if (isUnavailable(e)) {
				ExtensionListUtil.logDebug(getName(), "no Solr search API for group [" + groupId + "] at [" + repo.getUrl() + "]: " + e.getMessage());
				return new HashSet<>();
			}
			ExtensionListUtil.logWarn(getName(), "failed listing artifacts for group [" + groupId + "] via Solr search API at [" + repo.getUrl() + "]", e);
			throw e;
		}
	}

	private PageFetch fetchPage(String baseUrl, String groupId, int start) throws IOException {
		String url = buildSearchUrl(baseUrl, groupId, start);
		ExtensionListUtil.logDebug(getName(), "fetching page from [" + url + "]");
		try {
			return new PageFetch(baseUrl, fetchSingle(url));
		}
		catch (IOException e) {
			String altBase = ExtensionListUtil.flipProtocol(baseUrl);
			if (altBase == null || !ExtensionListUtil.isProtocolMismatch(e)) throw e;
			String altUrl = buildSearchUrl(altBase, groupId, start);
			ExtensionListUtil.logDebug(getName(), "retrying with alternate protocol [" + altUrl + "]");
			return new PageFetch(altBase, fetchSingle(altUrl));
		}
	}

	private static boolean isUnavailable(IOException e) {
		String msg = e.getMessage();
		if (msg == null) return false;
		return msg.contains("HTTP 404") || msg.contains("HTTP 403") || msg.contains("HTTP 501");
	}

	public static Page parse(String json) throws IOException {
		try {
			Struct root = Caster.toStruct(new JSONExpressionInterpreter(false, JSONExpressionInterpreter.FORMAT_JSON).interpret(null, json));
			Struct response = Caster.toStruct(root.get("response", null), null);
			if (response == null) throw new IOException("invalid Solr search response");

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

	private static String fetchSingle(String url) throws IOException {
		HTTPResponse4Impl response = HTTPEngine.get(new URL(url), null, null, 2000, MavenUpdateProvider.CONNECTION_TIMEOUT, MavenUpdateProvider.READ_TIMEOUT, true, null,
				"Solr-Search/1.0", null, null, true);
		try {
			int sc = response.getStatusCode();
			if (sc != 200) throw new IOException("HTTP " + sc + " for URL: " + url);
			return IOUtil.toString(response.getContentAsStream(), StandardCharsets.UTF_8);
		}
		finally {
			response.close();
		}
	}

	private static final class PageFetch {
		private final String baseUrl;
		private final String body;

		private PageFetch(String baseUrl, String body) {
			this.baseUrl = baseUrl;
			this.body = body;
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
