/*
 * Fetches and parses maven-metadata.xml for the runtime maven resolver.
 *
 * Intentionally separate from lucee.runtime.config.maven.MetadataReader —
 * same XML format, same URL convention, but different ownership and
 * different caching story:
 *
 *   - config.maven.MetadataReader is for the EXTENSION UPDATE provider:
 *     it asks "which versions of lucee.jira:s3-extension exist?" so the
 *     admin UI can offer updates. It's tied to MavenUpdateProvider's
 *     Repository type (separate class) and its own cacheDirectory layout.
 *
 *   - This class (lucee.runtime.mvn.MavenMetadataReader) is for the
 *     POM RESOLVER: when a transitive POM declares a version RANGE like
 *     [2.2,3), we need to enumerate available versions of that coord to
 *     pick the highest in range. It uses this package's own Repository
 *     type and the standard mvn/{groupId}/{artifactId}/ cache layout,
 *     with a .lastUpdated marker that matches how POMs/JARs are cached.
 *
 * Deliberate duplication — ~80 LOC here is worth keeping the extension
 * update path and the dep resolver path decoupled.
 */
package lucee.runtime.mvn;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.net.http.HTTPDownloader;
import lucee.runtime.text.xml.XMLUtil;

/**
 * Reads available versions of a given {@code groupId:artifactId} from
 * the configured maven repositories' {@code maven-metadata.xml} files.
 *
 * Results are cached under {@code {mvnDir}/{group}/{artifact}/
 * maven-metadata.xml} with a sibling {@code .lastUpdated} marker.
 * Two TTLs apply:
 *
 * <ul>
 * <li>Cache file present and marker fresh (within
 * {@link MavenUtil#METADATA_CACHE_DURATION}, default 24h) → local parse
 * returned.</li>
 * <li>Cache file absent but marker fresh (within
 * {@link MavenUtil#ARTIFACT_UNAVAILABLE_CACHE_DURATION}, default 15min)
 * → negative hit, empty list returned (previous fetch failed; don't
 * re-hammer the repos).</li>
 * <li>Marker stale or absent → fetch each repo in order, write cache
 * on success, write marker-only on failure.</li>
 * <li>All repos fail but a stale cache exists → parse stale and reset
 * the marker (yesterday's version list beats a hard failure).</li>
 * </ul>
 *
 * <h3>Known limitation: first-successful-repo-wins</h3>
 *
 * The repo loop returns on the first parseable {@code maven-metadata.xml}.
 * For releases on Central this is correct (Central is authoritative).
 * For a coord served as a release by one repo and as a newer snapshot by
 * another (e.g. Sonatype snapshots), the snapshot is invisible because
 * the first repo's metadata is returned unmerged. A proper fix would
 * union version lists across repos and let {@link MavenVersionRange#pickHighest}
 * pick from the merged set; tracked under the snapshot work in the
 * maven-lite epic.
 */
public final class MavenMetadataReader {

	/** Connect + read timeouts for the metadata fetch. */
	private static final int CONNECTION_TIMEOUT = POM.CONNECTION_TIMEOUT;
	private static final int READ_TIMEOUT = POM.READ_TIMEOUT_GET;

	private static final String METADATA_FILENAME = "maven-metadata.xml";
	private static final String LAST_UPDATED_SUFFIX = ".lastUpdated";

	private MavenMetadataReader() {
		// static-only
	}

	/**
	 * Lists versions present under {@code {mvnDir}/{group}/{artifact}/}
	 * as subdirectories — each represents a version that has been
	 * successfully installed at some point (the directory only exists
	 * after a successful download). Used as a zero-HTTP candidate source
	 * by {@link MavenUtil#resolveVersionRange(Resource, String, String, String, Collection, Log)}:
	 * if a locally-cached version already satisfies the range, no metadata
	 * fetch is needed and resolution works offline.
	 *
	 * @return version strings (directory names) — may be empty, never null.
	 */
	public static List<String> listLocalVersions(Resource mvnDir, String groupId, String artifactId) {
		Resource artifactDir = mvnDir.getRealResource(groupId.replace('.', '/') + '/' + artifactId);
		List<String> versions = new ArrayList<>();
		if (!artifactDir.isDirectory()) return versions;
		Resource[] children = artifactDir.listResources();
		if (children == null) return versions;
		for (Resource child: children) {
			if (child.isDirectory()) versions.add(child.getName());
		}
		return versions;
	}

	/**
	 * Returns the available versions of {@code groupId:artifactId}, using
	 * a local {@code maven-metadata.xml} cache when fresh and falling
	 * through to HTTP when not.
	 *
	 * @param mvnDir {@code mvn/} root where cache files live
	 * @return list of version strings in document order, or an empty list
	 *         if no repository returned a parseable metadata file.
	 */
	public static List<String> fetchAvailableVersions(Resource mvnDir, String groupId, String artifactId, Collection<Repository> repositories, Log log) throws IOException {
		Resource artifactDir = mvnDir.getRealResource(groupId.replace('.', '/') + '/' + artifactId);
		Resource cache = artifactDir.getRealResource(METADATA_FILENAME);
		Resource marker = artifactDir.getRealResource(METADATA_FILENAME + LAST_UPDATED_SUFFIX);

		if (marker.isFile()) {
			long age = System.currentTimeMillis() - marker.lastModified();
			if (cache.isFile()) {
				// positive cache — metadata churn is slow, use the longer TTL
				if (age < MavenUtil.METADATA_CACHE_DURATION) {
					try {
						return parse(cache);
					}
					catch (Exception e) {
						if (log != null) log.debug("maven", "local maven-metadata.xml unreadable for " + groupId + ":" + artifactId + ", refetching: " + e.getMessage());
						// fall through to HTTP
					}
				}
			}
			else if (age < MavenUtil.ARTIFACT_UNAVAILABLE_CACHE_DURATION) {
				// negative cache hit — recent fetch failed for all repos, don't retry yet
				return new ArrayList<>();
			}
		}

		MavenUtil.assertDownloadAllowed("maven-metadata.xml for " + groupId + ":" + artifactId);

		String path = groupId.replace('.', '/') + '/' + artifactId + "/" + METADATA_FILENAME;
		for (Repository repo: repositories) {
			try {
				URL url = new URL(joinUrl(repo.getUrl(), path));
				downloadToCache(url, cache, log);
				touchMarker(marker);
				return parse(cache);
			}
			catch (Exception e) {
				if (log != null) log.debug("maven", "metadata fetch failed for " + groupId + ":" + artifactId + " at " + repo.getUrl() + ": " + e.getMessage());
			}
		}

		// All repos failed. Prefer a stale-but-present cache over nothing:
		// yesterday's version list beats "cannot resolve" for an install
		// that just needs *a* satisfying version. Reset the marker so we
		// don't hammer the repos again for the duration of the TTL.
		if (cache.isFile()) {
			try {
				List<String> stale = parse(cache);
				if (log != null) log.info("maven", "all repos failed for " + groupId + ":" + artifactId + ", using stale local cache (" + stale.size() + " versions)");
				touchMarker(marker);
				return stale;
			}
			catch (Exception e) {
				if (log != null) log.debug("maven", "stale cache unreadable for " + groupId + ":" + artifactId + ": " + e.getMessage());
			}
		}

		// No cache, no network — negative cache so we don't retry for TTL.
		touchMarker(marker);
		return new ArrayList<>();
	}

	private static void touchMarker(Resource marker) {
		try {
			Resource parent = marker.getParentResource();
			if (!parent.isDirectory()) parent.createDirectory(true);
			IOUtil.write(marker, String.valueOf(System.currentTimeMillis()), lucee.commons.io.CharsetUtil.UTF8, false);
		}
		catch (IOException ignore) {
			// best effort — next call will simply refetch
		}
	}

	private static void downloadToCache(URL url, Resource cache, Log log) throws IOException, java.security.GeneralSecurityException {
		InputStream is = null;
		Resource tmp = SystemUtil.getTempFile("xml", false);
		try {
			is = HTTPDownloader.get(url, null, null, CONNECTION_TIMEOUT, READ_TIMEOUT, null, log == null ? Log.LEVEL_INFO : Log.LEVEL_TRACE);
			IOUtil.copy(is, tmp, false);
			Resource parent = cache.getParentResource();
			if (!parent.isDirectory()) parent.createDirectory(true);
			if (cache.isFile()) cache.remove(true);
			tmp.moveTo(cache);
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	private static List<String> parse(Resource cache) throws IOException, SAXException {
		Reader r = null;
		try {
			r = IOUtil.getReader(cache.getInputStream(), (Charset) null);
			Handler h = new Handler();
			XMLReader xr = XMLUtil.createXMLReader();
			xr.setContentHandler(h);
			xr.setErrorHandler(h);
			xr.parse(new InputSource(r));
			return h.versions;
		}
		finally {
			IOUtil.closeEL(r);
		}
	}

	private static String joinUrl(String base, String path) {
		if (base.endsWith("/")) return base + path;
		return base + "/" + path;
	}

	/**
	 * SAX handler extracting {@code /metadata/versioning/versions/version} text.
	 */
	private static final class Handler extends DefaultHandler {
		private final List<String> versions = new ArrayList<>();
		private final StringBuilder content = new StringBuilder();
		private int depth = 0;
		private boolean insideVersions = false;
		private boolean insideVersion = false;

		@Override
		public void startElement(String uri, String name, String qName, Attributes atts) {
			depth++;
			// path: metadata(1) > versioning(2) > versions(3) > version(4)
			if (depth == 3 && "versions".equalsIgnoreCase(qName)) insideVersions = true;
			else if (depth == 4 && insideVersions && "version".equalsIgnoreCase(qName)) {
				insideVersion = true;
				content.setLength(0);
			}
		}

		@Override
		public void endElement(String uri, String name, String qName) {
			if (insideVersion && depth == 4 && "version".equalsIgnoreCase(qName)) {
				String v = content.toString().trim();
				if (!v.isEmpty()) versions.add(v);
				insideVersion = false;
			}
			else if (depth == 3 && "versions".equalsIgnoreCase(qName)) insideVersions = false;
			depth--;
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			if (insideVersion) content.append(ch, start, length);
		}
	}
}
