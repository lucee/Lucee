package lucee.runtime.config.maven;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.StatusLine;
import org.xml.sax.SAXException;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPEngineBasic.HTTPDownloaderHeadResponse;
import lucee.commons.net.http.Header;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.config.maven.extensionlist.ExtensionLister;
import lucee.runtime.config.maven.extensionlist.ExtensionListers;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.CastImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public final class MavenUpdateProvider {

	public static final int TYPE_ALL = 0;
	public static final int TYPE_SNAPSHOT = 1;
	public static final int TYPE_RELEASE = 2;

	public static final int CONNECTION_TIMEOUT = 10000; // 10 seconds - for establishing connection
	public static final int READ_TIMEOUT = 60000; // 60 seconds - for reading response data

	// MAVEN
	public static final Repository REPOSITORY_MAVEN_CENTRAL_RELEASES = new Repository("Maven Release Repository", "https://repo1.maven.org/maven2/", TYPE_RELEASE,
			Repository.TIMEOUT_1HOUR, Repository.TIMEOUT_NEVER, null, ExtensionListers.CENTRAL);

	// GOOGLE
	public static final Repository REPOSITORY_GOOGLE_CENTRAL_MIRROR = new Repository("Google Cloud Maven Central mirror",
			"https://maven-central.storage-download.googleapis.com/maven2/", TYPE_RELEASE, Repository.TIMEOUT_1HOUR, Repository.TIMEOUT_NEVER, null, ExtensionListers.EMPTY);

	// SONATYPE
	public static final Repository REPOSITORY_SONATYPE_SNAPSHOTS = new Repository("Sonatype Repository for Snapshots (last 90 days)",
			"https://central.sonatype.com/repository/maven-snapshots/", TYPE_SNAPSHOT, Repository.TIMEOUT_1HOUR, Repository.TIMEOUT_NEVER, null, ExtensionListers.EMPTY);

	// LUCEE
	public static final Repository REPOSITORY_LUCEE = new Repository("Lucee Maven repository", "https://maven.lucee-services.com/", TYPE_ALL, Repository.TIMEOUT_1HOUR,
			Repository.TIMEOUT_NEVER, null, ExtensionListers.LUCEE);

	////////////////////////////

	public static final Repository[] DEFAULT_REPOSITORIES_SNAPSHOTS = new Repository[] { REPOSITORY_SONATYPE_SNAPSHOTS };
	public static final Repository[] DEFAULT_REPOSITORIES_RELEASES = new Repository[] { REPOSITORY_MAVEN_CENTRAL_RELEASES, REPOSITORY_GOOGLE_CENTRAL_MIRROR, REPOSITORY_LUCEE };
	public static final Repository[] DEFAULT_REPOSITORIES_ALL = new Repository[] { REPOSITORY_LUCEE };

	// private static final Repository[] DEFAULT_REPOSITORY_MIXED = new Repository[] {
	// DEFAULT_REPOSITORY_LUCEE };

	public static final String DEFAULT_GROUP = "org.lucee";
	public static final String DEFAULT_ARTIFACT = "lucee";

	private final String group;
	private final String artifact;
	private final Collection<Repository> repos;

	private static Repository[] readReposFromEnvVar(String envVarName, Repository[] defaultValue) {
		String str = SystemUtil.getSystemPropOrEnvVar(envVarName, null);
		if (!StringUtil.isEmpty(str, true)) {

			List<String> raw = ListUtil.listToList(str.trim(), ',', true);
			List<Repository> repos = new ArrayList<>();
			for (String s: raw) {
				try {
					repos.add(new Repository(null, new URL(s).toExternalForm(), TYPE_ALL, Repository.TIMEOUT_5MINUTES, Repository.TIMEOUT_NEVER));
				}
				catch (Exception e) {
					LogUtil.log(Log.LEVEL_WARN, "MavenUpdateProvider", "Invalid repository URL [" + s + "] in environment variable [" + envVarName + "]: " + e.getMessage());
				}
			}
			if (repos.size() > 0) {
				return repos.toArray(new Repository[repos.size()]);
			}

		}

		return defaultValue;
	}

	public MavenUpdateProvider(Config config) {
		this.repos = getRepositories(config);
		this.group = DEFAULT_GROUP;
		this.artifact = DEFAULT_ARTIFACT;
	}

	public MavenUpdateProvider(Config config, String group, String artifact) {
		this.repos = getRepositories(config);
		this.group = group;
		this.artifact = artifact;
	}

	public static Collection<Repository> getRepositories(Config config) {
		ConfigPro cp = ThreadLocalPageContext.getConfigServer(config);
		Repository[] repoSnapshots = cp == null ? DEFAULT_REPOSITORIES_SNAPSHOTS : cp.getMavenSnapshotRepository();
		Repository[] repoReleases = cp == null ? DEFAULT_REPOSITORIES_RELEASES : cp.getMavenRepository();
		return merge(repoSnapshots, repoReleases, DEFAULT_REPOSITORIES_ALL);
	}

	public static Collection<Repository> getReleaseRepositories(Config config) {
		ConfigPro cp = (ConfigPro) ThreadLocalPageContext.getConfig(config);
		Repository[] repoReleases = cp == null ? DEFAULT_REPOSITORIES_RELEASES : cp.getMavenRepository();
		return merge(repoReleases, DEFAULT_REPOSITORIES_ALL);
	}

	public MavenUpdateProvider(Repository[] repositories, String group, String artifact) {
		this(Arrays.asList(repositories), group, artifact);
	}

	public MavenUpdateProvider(Collection<Repository> repositories, String group, String artifact) {
		this.repos = repositories;
		this.group = group;
		this.artifact = artifact;
	}

	static List<Repository> merge(Repository[] left, Repository[] right) {
		List<Repository> list = new ArrayList<>();
		for (Repository repo: left) {
			list.add(repo);
		}
		for (Repository repo: right) {
			list.add(repo);
		}

		return list;
	}

	static Collection<Repository> merge(Repository[] left, Repository[] middle, Repository[] right) {
		Set<Repository> list = new HashSet<>();
		for (Repository repo: left) {
			list.add(repo);
		}
		for (Repository repo: middle) {
			list.add(repo);
		}
		for (Repository repo: right) {
			list.add(repo);
		}

		return list;
	}

	public static Version last(Collection<Repository> repos, String group, String artifact, int type)
			throws IOException, GeneralSecurityException, SAXException, InterruptedException {
		List<Version> list = list(repos, group, artifact);

		if (list == null || list.isEmpty()) {
			throw new IOException(
					"No versions found for artifact [" + group + ":" + artifact + "] " + "in " + repos.size() + " repositor" + (repos.size() == 1 ? "y" : "ies") + ".");
		}

		if (TYPE_ALL == type) {
			return list.get(list.size() - 1);
		}

		Version version;
		for (int i = list.size() - 1; i >= 0; i--) {
			version = list.get(i);
			boolean isSnapshot = version.is(Version.SNAPSHOT);
			if ((type == TYPE_SNAPSHOT && isSnapshot) || (type == TYPE_RELEASE && !isSnapshot)) {
				return version;
			}
		}

		throw new IOException("No " + (type == TYPE_SNAPSHOT ? "snapshot" : "release") + " version found for [" + group + ":" + artifact + "]. " + list.size() + " version"
				+ (list.size() == 1 ? "" : "s") + " exist but none matched the requested type. " + "Available versions: " + list);

	}

	public List<Version> list() throws IOException, GeneralSecurityException, SAXException, InterruptedException {
		return list(repos, group, artifact);
	}

	public static List<Version> list(Collection<Repository> repos, String group, String artifact, int type)
			throws IOException, GeneralSecurityException, SAXException, InterruptedException {
		List<Repository> filtered = new ArrayList<>();
		for (Repository repo: repos) {
			if (type == TYPE_ALL || repo.type == TYPE_ALL || repo.type == type) {
				filtered.add(repo);
			}
		}
		return list(filtered, group, artifact);
	}

	public static List<Version> list(Collection<Repository> repos, String group, String artifact) throws IOException, GeneralSecurityException, SAXException, InterruptedException {
		Log log = LogUtil.getLog(null, "maven", "application");
		try {
			Set<Version> versions = Collections.synchronizedSet(new HashSet<>());
			List<Thread> threads = new ArrayList<>();
			Stack<Exception> exceptions = new Stack<>();

			for (Repository repo: repos) {
				Thread thread = ThreadUtil.getThread(() -> {
					try {
						if (LogUtil.doesInfo(log)) {
							log.info("maven", "scanning metadata for " + group + ":" + artifact + " at " + repo.url);
						}

						MetadataReader mr = new MetadataReader(repo, group, artifact);
						List<Version> readVersions = mr.read();
						for (Version v: readVersions) {
							versions.add(v);
						}

						if (LogUtil.doesDebug(log)) {
							log.debug("maven", "found " + readVersions.size() + " versions at " + repo.url);
						}
					}
					catch (Exception e) {
						// Forwarded to parent thread; silent local catch
						exceptions.add(e);
					}
				}, false);
				thread.start();
				threads.add(thread);
			}

			// Wait for all repository scans to complete
			for (Thread thread: threads) {
				thread.join();
			}

			// Handle exceptions collected from threads
			if (!exceptions.isEmpty()) {
				Exception e = exceptions.pop();
				if (e instanceof GeneralSecurityException) throw (GeneralSecurityException) e;
				if (e instanceof SAXException) throw (SAXException) e;

				throw ExceptionUtil.toIOException(new IOException("Failed to list available versions from Maven repositories for [" + group + ":" + artifact + "]", e));
			}

			if (!versions.isEmpty()) {
				List<Version> sortedList = new ArrayList<>(versions);
				Collections.sort(sortedList, Version::compare);
				return sortedList;
			}

			return new ArrayList<>();
		}
		catch (UnknownHostException uhe) {
			throw new IOException("Cannot reach Maven server [" + uhe.getMessage() + "] " + "while resolving [" + group + ":" + artifact + "]. "
					+ "Check your network connectivity and DNS configuration.", uhe);
		}
	}

	public InputStream getCore(Version version) throws IOException, SAXException, PageException {
		Map<String, Object> data = detail(version, "jar", true);
		String strURL = Caster.toString(data.get("lco"), null);
		assertDownloadAllowed(strURL);
		if (!StringUtil.isEmpty(strURL)) {
			// Use HTTPDownloader with DEBUG logging for Maven operations
			return HTTPEngine.get(new URL(strURL), null, null, CONNECTION_TIMEOUT, READ_TIMEOUT, null, null, true);
		}
		return getFileStreamFromZipStream(getLoader(version));
	}

	public InputStream getLoader(Version version) throws IOException, SAXException, PageException {
		Map<String, Object> data = detail(version, "jar", true);
		String strURL = Caster.toString(data.get("jar"), null);
		if (StringUtil.isEmpty(strURL)) {
			throw new IOException("No JAR artifact found for [" + group + ":" + artifact + ":" + version + "]. " + "Verify the version exists in the configured repositories.");
		}

		// Use HTTPDownloader with DEBUG logging for Maven operations
		URL url = new URL(strURL);
		assertDownloadAllowed(strURL);
		return HTTPEngine.get(url, null, null, CONNECTION_TIMEOUT, READ_TIMEOUT, null, null, true);
	}

	/*
	 * public static void main(String[] args) throws PageException, IOException,
	 * GeneralSecurityException, SAXException, BundleException {
	 * 
	 * MavenUpdateProvider mup = new MavenUpdateProvider(); Map<String, Object> map =
	 * mup.detail(OSGiUtil.toVersion("6.1.0.719-SNAPSHOT")); print.e(map); }
	 */

	public Map<String, Object> detail(Version version, String requiredArtifactExtension, boolean throwException) throws IOException, SAXException, PageException {
		// SNAPSHOT - snapshot have a more complicated structure, ebcause there can be udaptes/multiple
		// versions
		boolean isSnap = version.is(Version.SNAPSHOT);

		if (requiredArtifactExtension == null) requiredArtifactExtension = "jar";
		else requiredArtifactExtension = requiredArtifactExtension.toLowerCase();
		try {
			// direct access
			{

				String g = group.replace('.', '/');
				String a = artifact.replace('.', '/');
				String v = version.toString();

				// check caches
				{
					Map<String, Object> result;
					for (Repository repo: repos) {
						if (!repo.handle(version)) continue;
						result = readFromCache(repo, artifact, v);
						if (result != null) {
							return result;
						}
					}
				}

				for (Repository repo: repos) {
					if (!repo.handle(version)) continue;

					// read from maven-metadata.xml, snapshots mostly use that pattern
					if (isSnap) {
						RepoReader repoReader = new RepoReader(repo.url, group, artifact, version);
						Map<String, Object> result = repoReader.read(requiredArtifactExtension);
						if (result != null) {
							storeToCache(repo, artifact, v, result);
							return result;
						}
					}
					// read main
					{
						String strURL = repo.url + g + "/" + a + "/" + v + "/" + a + "-" + v + "." + requiredArtifactExtension;
						URL urlMain = new URL(strURL);
						HTTPDownloaderHeadResponse rsp = HTTPEngine.head(urlMain, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT, true, null);
						if (rsp != null && validSatusCode(rsp.getStatusCode())) {
							Map<String, Object> result = new LinkedHashMap<>();

							Header[] headers = rsp.getAllHeaders();
							if (headers != null) {
								for (Header h: headers) {
									if ("Last-Modified".equals(h.getName())) result.put("lastModified", DateCaster.toDateAdvanced(h.getValue(), null));
								}
							}
							result.put(requiredArtifactExtension, urlMain.toExternalForm());

							// optional
							// pom
							{
								URL url = new URL(repo.url + g + "/" + a + "/" + v + "/" + a + "-" + v + ".pom");
								rsp = HTTPEngine.head(url, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT, true, null);
								if (rsp != null && validSatusCode(rsp.getStatusCode())) {
									result.put("pom", url.toExternalForm());
								}
							}
							// lco
							{
								URL url = new URL(repo.url + g + "/" + a + "/" + v + "/" + a + "-" + v + ".lco");
								rsp = HTTPEngine.head(url, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT, true, null);
								if (rsp != null && validSatusCode(rsp.getStatusCode())) {
									result.put("lco", url.toExternalForm());
								}
							}
							storeToCache(repo, artifact, v, result);
							return result;
						}
					}
				}
			}

		}
		catch (UnknownHostException uhe) {
			throw new IOException("cannot reach maven server", uhe);
		}
		if (throwException) throw new IOException("Could not find the artifact [" + group + ":" + artifact + ":" + version + "] (type: " + requiredArtifactExtension
				+ ") in any of the configured repositories: [" + toList(repos, version) + "]. " + "Verify the artifact coordinates and version are correct.");
		return null;
	}

	private void storeToCache(Repository repository, String artifact, String version, Map<String, Object> detail) {
		try {
			Resource resLastmod = repository.cacheDirectory
					.getRealResource("detail_" + HashUtil.create64BitHashAsString(group + "_" + artifact + "_" + version + "_lastmod", Character.MAX_RADIX));
			Resource resVersions = repository.cacheDirectory
					.getRealResource("detail_" + HashUtil.create64BitHashAsString(group + "_" + artifact + "_" + version + "_versions", Character.MAX_RADIX));
			String content = fromMapToJsonString(detail, true);

			IOUtil.write(resVersions, StringUtil.isEmpty(content, true) ? "" : content.trim(), StandardCharsets.UTF_8, false);
			IOUtil.write(resLastmod, Caster.toString(System.currentTimeMillis()), StandardCharsets.UTF_8, false);
		}
		catch (Exception e) {
			LogUtil.log("MetadataReader", e);
		}
	}

	private Map<String, Object> readFromCache(Repository repository, String artifact, String version) {
		try {
			Resource resLastmod = repository.cacheDirectory
					.getRealResource("detail_" + HashUtil.create64BitHashAsString(group + "_" + artifact + "_" + version + "_lastmod", Character.MAX_RADIX));
			if (resLastmod.isFile()) {
				long lastmod = repository.timeoutDetail == Repository.TIMEOUT_NEVER ? Repository.TIMEOUT_NEVER
						: Caster.toLongValue(IOUtil.toString(resLastmod, StandardCharsets.UTF_8), 0L);

				if (repository.timeoutDetail == Repository.TIMEOUT_NEVER || lastmod + repository.timeoutDetail > System.currentTimeMillis()) {

					Resource resVersions = repository.cacheDirectory
							.getRealResource("detail_" + HashUtil.create64BitHashAsString(group + "_" + artifact + "_" + version + "_versions", Character.MAX_RADIX));
					String content = IOUtil.toString(resVersions, StandardCharsets.UTF_8);
					if (content.length() > 0) {
						return new CastImpl().fromJsonStringToMap(content);
					}
					return null;
				}
			}
		}
		catch (Exception e) {
			LogUtil.log(Log.LEVEL_WARN, "MetadataReader", e);
		}
		return null;
	}

	private String toList(Collection<Repository> repos, Version filter) {
		StringBuilder sb = new StringBuilder();
		for (Repository r: repos) {
			if (filter != null && !r.handle(filter)) continue;
			if (sb.length() > 0) sb.append(", ");
			sb.append(r.url);
		}
		return sb.toString();
	}

	private boolean validSatusCode(StatusLine sl) {
		if (sl == null) return false;
		return sl.getStatusCode() >= 200 && sl.getStatusCode() < 300;
	}

	private boolean validSatusCode(int code) {
		return code >= 200 && code < 300;
	}

	public static InputStream getFileStreamFromZipStream(InputStream zipStream) throws IOException {
		ZipInputStream zis = new ZipInputStream(zipStream);
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			if (entry.getName().equals("core/core.lco")) {
				// Return an InputStream which is limited to the current zip entry's data
				Enumeration<InputStream> singleStreamEnum = Collections.enumeration(Collections.singletonList(zis));
				return new SequenceInputStream(singleStreamEnum);
			}
		}
		throw new FileNotFoundException("core/core.lco not found in zip");
	}

	private static String fromMapToJsonString(Map<String, Object> detail, boolean compact) throws PageException {
		JSONConverter json = new JSONConverter(true, StandardCharsets.UTF_8, JSONDateFormat.PATTERN_CF, compact);
		try {
			return json.serialize(null, detail, SerializationSettings.SERIALIZE_AS_COLUMN, null);
		}
		catch (ConverterException e) {
			throw Caster.toPageException(e);
		}
	}

	public final static class RepositoryFactory implements PropFactory<Repository> {

		private static RepositoryFactory instanceSnaps;
		private static RepositoryFactory instanceReleases;
		private int type;

		public RepositoryFactory(int type) {
			this.type = type;
		}

		public static RepositoryFactory getInstance(int type) {
			if (MavenUpdateProvider.TYPE_RELEASE == type) {
				if (instanceReleases == null) {
					instanceReleases = new RepositoryFactory(MavenUpdateProvider.TYPE_RELEASE);
				}
				return instanceReleases;
			}

			if (instanceSnaps == null) {
				instanceSnaps = new RepositoryFactory(MavenUpdateProvider.TYPE_SNAPSHOT);
			}
			return instanceSnaps;
		}

		@Override
		public Repository evaluate(Config config, String name, Object val, short source) throws PageException {
			Struct data = Caster.toStruct(val, null);
			if (data != null) {
				String url = Caster.toString(data.get(KeyConstants._url));
				if (StringUtil.isEmpty(url, true)) throw new ApplicationException("url cannot be an empty string");

				String label = Caster.toString(data.get(KeyConstants._label, null), null);
				int type = toType(Caster.toString(data.get(KeyConstants._label, null), null), this.type);
				TimeSpan tList = Caster.toTimespan(data.get("timeoutList", null), null);
				TimeSpan tDetail = Caster.toTimespan(data.get("timeoutDetail", null), null);
				String listingMode = Caster.toString(data.get("listingMode", null), null);

				return new Repository(StringUtil.isEmpty(label, true) ? null : label, url, type, tList != null ? tList.getMillis() : Repository.TIMEOUT_5MINUTES,
						tDetail != null ? tDetail.getMillis() : Repository.TIMEOUT_NEVER, null, ExtensionListers.resolve(listingMode, url));
			}
			// coming from env var/sys op
			String url = Caster.toString(val, null);
			if (!StringUtil.isEmpty(url, true)) {
				return new Repository(null, url, TYPE_ALL, Repository.TIMEOUT_5MINUTES, Repository.TIMEOUT_NEVER, null, ExtensionListers.resolve(null, url));
			}

			throw new ApplicationException("a repository need to be a URL string or a struct containing at least the key url");
		}

		private int toType(String type, int defaultValue) {
			if (StringUtil.isEmpty(type)) return defaultValue;
			if ("snapshot".equals(type)) return TYPE_SNAPSHOT;
			if ("release".equals(type)) return TYPE_RELEASE;
			if ("all".equals(type)) return TYPE_ALL;
			return defaultValue;
		}

		@Override
		public Struct schema(Prop<Repository> prop) {
			Struct sct = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._type, "object");
			sct.setEL(KeyConstants._description, "Configuration for an extension provider repository.");

			Struct properties = new StructImpl(Struct.TYPE_LINKED);
			sct.setEL(KeyConstants._properties, properties);

			// URL is mandatory in the struct
			addProp(properties, "url", "string", "The endpoint URL for the repository.");
			addProp(properties, "label", "string", "A human-readable name for the repository.");
			addProp(properties, "timeoutList", "string", "Caching duration for the extension list (e.g., '0,0,5,0').");
			addProp(properties, "timeoutDetail", "string", "Caching duration for specific extension details.");
			addProp(properties, "listingMode", "string",
					"Extension discovery lister name, e.g. scraping, group-metadata, solr-search, empty, or group-metadata-then-solr-search-then-scraping (default). central-search is accepted as an alias for solr-search.");

			Array required = new ArrayImpl();
			required.appendEL("url");
			sct.setEL(KeyConstants._required, required);

			return sct;
		}

		private void addProp(Struct props, String key, String type, String desc) {
			Struct p = new StructImpl(Struct.TYPE_LINKED);
			p.setEL(KeyConstants._type, type);
			p.setEL(KeyConstants._description, desc);
			props.setEL(key, p);
		}

		@Override
		public Object resolvedValue(Repository value) {
			return value;
		}

	}

	public final static class Repository implements Cloneable {

		public static final long TIMEOUT_1HOUR = 60 * 60 * 1000;
		public static final long TIMEOUT_NEVER = Long.MAX_VALUE;
		public static final long TIMEOUT_5MINUTES = 60 * 5 * 1000;
		public static final long TIMEOUT_10MINUTES = 60 * 10 * 1000;
		public static final long TIMEOUT_15MINUTES = 60 * 15 * 1000;
		public static final long TIMEOUT_5SECONDS = 5 * 1000;
		public static final long TIMEOUT_ZERO = 0;

		public static final int TYPE_ALL = 0;
		public static final int TYPE_SNAPSHOT = 1;
		public static final int TYPE_RELEASE = 2;

		private static Resource cacheRootDirectory;

		public final int type;
		public final String label;
		public final String url;
		public final long timeoutList;
		public final long timeoutDetail;
		public final Resource cacheDirectory;
		public final ExtensionLister extensionLister;

		static {
			try {
				cacheRootDirectory = ThreadLocalPageContext.getConfigServer().getConfigDir();
			}
			catch (Exception e) {
				cacheRootDirectory = SystemUtil.getTempDirectory();
			}
		}

		public Repository(String label, String url, int type, long timeoutList, long timeoutDetail) {
			this(label, url, type, timeoutList, timeoutDetail, getCacheDirectory(url), null);
		}

		public Repository(String label, String url, int type, long timeoutList, long timeoutDetail, Resource cacheDirectory) {
			this(label, url, type, timeoutList, timeoutDetail, cacheDirectory, null);
		}

		public Repository(String label, String url, int type, long timeoutList, long timeoutDetail, Resource cacheDirectory, ExtensionLister extensionLister) {
			if (!url.endsWith("/")) url += "/";
			this.label = label;
			this.url = url;
			this.type = type;
			this.timeoutList = timeoutList;
			this.timeoutDetail = timeoutDetail;
			this.cacheDirectory = cacheDirectory != null ? cacheDirectory : getCacheDirectory(url);
			this.extensionLister = extensionLister != null ? extensionLister : ExtensionListers.resolve(null, url);
			if (Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.repos.flush", null), false)) {
				ResourceUtil.deleteContent(this.cacheDirectory, null);
			}
		}

		private static Resource getCacheDirectory(String url) {
			Resource cacheDirectory = cacheRootDirectory.getRealResource("mvn/cache/" + HashUtil.create64BitHashAsString(url, Character.MAX_RADIX) + "/");
			cacheDirectory.mkdirs();
			return cacheDirectory;
		}

		public String getUrl() {
			return url;
		}

		@Override
		public Object clone() {
			return duplicate();
		}

		public Repository duplicate() {
			return new Repository(label, url, type, timeoutList, timeoutDetail, cacheDirectory, extensionLister);
		}

		@Override
		public String toString() {
			return "label:" + label + ";url:" + url + ";type:" + toType(type) + ";timeoutList:" + timeoutList + ";timeoutDetail:" + timeoutDetail;
		}

		public static String toType(int type) {
			if (TYPE_SNAPSHOT == type) return "snapshot";
			if (TYPE_RELEASE == type) return "release";
			return "all";
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		/**
		 * 
		 * @param version
		 * @return
		 */
		public boolean handle(Version version) {
			if (TYPE_ALL == type) return true;
			if (TYPE_RELEASE == type) return version.is(TYPE_RELEASE);
			return version.is(TYPE_SNAPSHOT);
		}
	}

	private static void assertDownloadAllowed(String url) throws IOException {
		int policy = ConfigUtil.getMavenDownloadPolicy();
		if (policy == ConfigPro.MAVEN_DOWNLOAD_POLICY_ERROR) {
			throw new IOException("Maven download is blocked by policy. Attempted to download [" + url + "]. "
					+ "To allow downloads, set the system property or environment variable " + "'lucee.maven.download.policy' to 'warn' or 'ignore'. "
					+ "Alternatively, place the artifact manually in your local Maven repository (~/.m2/repository).");
		}
		else if (policy == ConfigPro.MAVEN_DOWNLOAD_POLICY_WARN) {
			LogUtil.log(ConfigUtil.getConfigServerImpl().getMavenDownloadPolicyLogLevel(), "maven", "Downloading Maven artifact from [" + url
					+ "]. Maven download policy is set to 'warn'. " + "Set 'lucee.maven.download.policy' to 'error' to block downloads " + "or 'ignore' to suppress this warning.");
		}
	}
}
