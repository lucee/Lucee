package lucee.runtime.mvn;

import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import lucee.commons.digest.Hash;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.loader.util.OpenPGPVerifier.VerificationException;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.maven.MavenUpdateProvider;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;
import lucee.runtime.config.maven.RepoReader;
import lucee.runtime.config.maven.Version;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.mvn.POMReader.Dependency;
import lucee.runtime.op.Caster;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public final class MavenUtil {
	private static Map<String, String> sysprops;
	private static Object token = new SerializableObject();

	public static final int CONNECTION_TIMEOUT = 5000;
	public static final int READ_TIMEOUT_HEAD = 5000;
	public static final int READ_TIMEOUT_GET = 5000;
	private static final int MAX_DOWNLOAD_ATTEMPTS = 2;
	// socket timeout for artifact downloads (pom, jar, lex), max inactivity between data packets
	public static final int READ_TIMEOUT_DOWNLOAD = 10000;
	public static final long ARTIFACT_UNAVAILABLE_CACHE_DURATION = Caster.toLongValue(SystemUtil.getSystemPropOrEnvVar("lucee.maven.negative.cache.duration", null), 60000L * 15L);

	private static final DateTimeFormatter MAVEN_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

	private static Map<Repository, AtomicInteger> ranking = new ConcurrentHashMap<>();
	private static boolean initLocalRepository;
	private static Resource localRepository;

	public static Map<String, String> getProperties(Map<String, String> rawProperties, POM parent) throws IOException {
		Map<String, String> properties = parent != null ? parent.getProperties() : new LinkedHashMap<>();

		int size = properties == null ? 0 : properties.size();
		if (rawProperties != null) size += rawProperties.size();

		Map<String, String> newProperties = new HashMap<>(size);

		// copy data from parent
		if (properties != null) {
			for (Entry<String, String> e: properties.entrySet()) {
				newProperties.put(e.getKey(), e.getValue());
			}
		}

		// add new data
		if (rawProperties != null) {
			for (Entry<String, String> e: rawProperties.entrySet()) {
				newProperties.put(e.getKey(), e.getValue());
			}
		}
		return newProperties;
	}

	public static Map<String, String> getSystemProperties() {
		if (sysprops == null) {
			synchronized (token) {
				if (sysprops == null) {
					Properties props = System.getProperties();
					sysprops = new HashMap<>(props.size());
					for (String name: props.stringPropertyNames()) {
						sysprops.put(name, props.getProperty(name));
					}
				}
			}
		}
		return sysprops;
	}

	public static Collection<Repository> getRepositories(List<POMReader.Repository> rawRepositories, POM current, POM parent, Map<String, String> properties,
			Collection<Repository> initRepositories) throws IOException {

		Map<String, Repository> repositories = new LinkedHashMap<>();// linked so we keep the order

		// add default repos
		if (initRepositories != null) {
			for (Repository r: initRepositories) {
				repositories.put(r.getUrl(), r);
			}
		}

		// get parent repos
		if (parent != null) {
			Collection<Repository> reps = parent.getRepositories();
			if (reps != null) {
				for (Repository r: reps) {
					repositories.put(r.getUrl(), r); // TODO clone?
				}
			}
		}
		// repos defined in POM
		if (rawRepositories != null) {
			for (POMReader.Repository rep: rawRepositories) {
				Repository r = new Repository(

						resolvePlaceholders(current, rep.name, properties),

						resolvePlaceholders(current, rep.url, properties),

						MavenUpdateProvider.TYPE_ALL,

						Repository.TIMEOUT_1HOUR,

						Repository.TIMEOUT_NEVER

				);
				repositories.put(r.getUrl(), r);
			}
		}
		return repositories.values();

	}

	public static List<POM> getDependencies(List<POMReader.Dependency> rawDependencies, POM current, POM parent, Map<String, String> properties, Resource localDirectory,
			final boolean management, Log log) throws IOException {
		List<POM> dependencies = new ArrayList<>();

		if (parent != null) {
			List<POM> tmp = parent.getDependencies();
			if (tmp != null) {
				for (POM pom: tmp) {
					dependencies.add(pom); // TODO clone?
				}
			}
		}
		if (rawDependencies != null) {
			// List<Future<POM>> futures = new ArrayList<>();
			for (POMReader.Dependency rd: rawDependencies) {
				final POM pom = getDependency(localDirectory, rd, current, properties, management, log);
				if (pom != null) dependencies.add(pom);
				// Future<POM> future = executor.submit(() -> {
				// if (!management) pom.initXML();
				// return pom;
				// });
				// futures.add(future);
			}

		}
		return dependencies;
	}

	public static POM getDependency(Resource localDirectory, POMReader.Dependency rd, POM current, Map<String, String> properties, boolean management, Log log) throws IOException {
		POM pdm = null;// TODO move out of here so multiple loop elements can profit
		boolean loadedPDM = false;
		String g = resolvePlaceholders(current, rd.groupId, properties);
		String a = resolvePlaceholders(current, rd.artifactId, properties);
		// scope
		String s = rd.scope;
		if (s != null) {
			s = resolvePlaceholders(current, s, properties);
		}

		// scope allowed?
		if (!allowed(management ? current.getDependencyScopeManagement() : current.getDependencyScope(), toScope(s, POM.SCOPE_COMPILE))) {
			return null;
		}

		// version
		String v = rd.version;
		if (v != null) {
			v = resolvePlaceholders(current, v, properties);
		}
		else {
			if (!loadedPDM) {
				pdm = getDependency(current.getDependencyManagement(), g, a);
				loadedPDM = true;
			}
			if (pdm != null) {
				v = pdm.getVersion();
				// v = resolvePlaceholders(pdm, v, pdm.getProperties());
			}
			if (v == null) {
				throw new IOException("No version defined for dependency [" + g + ":" + a + "] in POM [" + current + "]. "
						+ "Specify a version directly or ensure it is declared in <dependencyManagement>.");
			}
		}

		// PATCH TODO better solution for this
		if (v != null && v.startsWith("[")) {
			v = v.substring(1, v.indexOf(','));
		}

		// optional
		String o = rd.optional;
		if (o != null) {
			o = resolvePlaceholders(current, o, properties);
		}

		String classifier = rd.classifier;
		if (classifier != null) {
			classifier = resolvePlaceholders(current, classifier, properties);
		}

		// type: test-jar is the jar with the classifier "tests", pom/zip/... have an extension of their own
		String extension = null;
		String type = rd.type == null ? null : resolvePlaceholders(current, rd.type, properties);
		if (!StringUtil.isEmpty(type, true)) {
			type = type.trim().toLowerCase();
			if ("test-jar".equals(type)) {
				if (StringUtil.isEmpty(classifier, true)) classifier = "tests";
			}
			else if ("javadoc".equals(type)) {
				if (StringUtil.isEmpty(classifier, true)) classifier = "javadoc";
			}
			else if ("java-source".equals(type)) {
				if (StringUtil.isEmpty(classifier, true)) classifier = "sources";
			}
			else if (!"jar".equals(type) && !"bundle".equals(type) && !"maven-plugin".equals(type) && !"ejb".equals(type) && !"ejb-client".equals(type)) {
				extension = type;
			}
		}

		return POM.getInstance(localDirectory, current.getRepositories(), g, a, v, classifier, extension, s, o, null, current.getDependencyScope(),
				current.getDependencyScopeManagement(), false, log);
	}

	public static class MavenCoordinate implements Serializable {

		public static int ORIGIN_DEPENDENCY = 1;

		public final String groupId;// groupId
		public final String artifactId;// artifactId
		public final String version;// version
		public final String classifier;
		public final String extension;// packaging, null when implied by the context
		public final String scope;// scope
		public final String optional;// optional
		public final String checksum;
		private int d;// dependency

		public MavenCoordinate(String groupId, String artifactId, String version) {
			this(groupId, artifactId, version, null, null, null, null, null);
		}

		public MavenCoordinate(String groupId, String artifactId, String version, String classifier) {
			this(groupId, artifactId, version, classifier, null, null, null, null);
		}

		public MavenCoordinate(String groupId, String artifactId, String version, String scope, String optional, String checksum) {
			this(groupId, artifactId, version, null, null, scope, optional, checksum);
		}

		public MavenCoordinate(String groupId, String artifactId, String version, String classifier, String extension, String scope, String optional, String checksum) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
			this.classifier = StringUtil.isEmpty(classifier, true) ? null : classifier.trim();
			this.extension = StringUtil.isEmpty(extension, true) ? null : extension.trim();
			this.scope = scope;
			this.optional = optional;
			this.checksum = checksum;
		}

		public MavenCoordinate setDependency(int d) {
			this.d = d;
			return this;
		}

		public int getDependency() {
			return d;
		}

		/**
		 * Gradle style: g:a:v[:classifier][@extension]
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(toGAVC());
			if (extension != null) sb.append('@').append(extension);
			return sb.toString();
		}

		/**
		 * g:a:v, the classifier is not part of it (a classified artifact shares the POM of the main
		 * artifact)
		 */
		public String toGAV() {
			StringBuilder sb = new StringBuilder();

			if (!StringUtil.isEmpty(groupId, true)) sb.append(groupId);
			if (!StringUtil.isEmpty(artifactId, true)) {
				if (sb.length() > 0) sb.append(':');
				sb.append(artifactId);
			}
			if (!StringUtil.isEmpty(version, true)) {
				if (sb.length() > 0) sb.append(':');
				sb.append(version);
			}
			return sb.toString();
		}

		/**
		 * g:a:v[:classifier]
		 */
		public String toGAVC() {
			String gav = toGAV();
			if (classifier == null) return gav;
			return gav + ":" + classifier;
		}

		private String toGA() {
			StringBuilder sb = new StringBuilder();

			if (!StringUtil.isEmpty(groupId, true)) sb.append(groupId);
			if (!StringUtil.isEmpty(artifactId, true)) {
				if (sb.length() > 0) sb.append(':');
				sb.append(artifactId);
			}
			return sb.toString();
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof MavenCoordinate)) return false;
			return toGAVC().equals(((MavenCoordinate) other).toGAVC());
		}

		@Override
		public int hashCode() {
			return toGAVC().hashCode();
		}

		/**
		 * same endpoint with different versions
		 * 
		 * @param other
		 * @return
		 */
		public boolean same(MavenCoordinate other) {
			return toGA().equals(other.toGA());
		}

		public Struct populate(Struct sct) {
			if (!StringUtil.isEmpty(groupId, true)) sct.setEL(KeyConstants._groupId, groupId);
			if (!StringUtil.isEmpty(artifactId, true)) sct.setEL(KeyConstants._artifactId, artifactId);
			if (!StringUtil.isEmpty(version, true)) sct.setEL(KeyConstants._version, version);
			if (classifier != null) sct.setEL(KeyConstants._classifier, classifier);
			if (extension != null) sct.setEL(KeyConstants._extension, extension);
			if (!StringUtil.isEmpty(scope, true)) sct.setEL(KeyConstants._scope, scope);
			if (!StringUtil.isEmpty(optional, true)) sct.setEL(KeyConstants._optional, optional);
			if (!StringUtil.isEmpty(checksum, true)) sct.setEL(KeyConstants._checksum, checksum);
			return sct;
		}

		public static Struct toStruct(MavenCoordinate coordinate) {
			return coordinate.populate(new StructImpl(Struct.TYPE_LINKED));
		}

		public static Array toArray(List<MavenCoordinate> list) {
			ArrayImpl arr = new ArrayImpl();
			if (list == null || list.isEmpty()) return arr;
			for (MavenCoordinate coordinate: list) {
				arr.appendEL(coordinate.populate(new StructImpl(Struct.TYPE_LINKED)));
			}
			return arr;
		}

		public static Array toArray(MavenCoordinate[] list) {
			ArrayImpl arr = new ArrayImpl();
			if (list == null || list.length == 0) return arr;
			for (MavenCoordinate coordinate: list) {
				arr.appendEL(coordinate.populate(new StructImpl(Struct.TYPE_LINKED)));
			}
			return arr;
		}

		/**
		 * same group and artifact id, but version MAY differ
		 * 
		 * @param coordinate
		 * @return
		 */
		public boolean equalID(MavenCoordinate other) {
			if (!groupId.equalsIgnoreCase(other.groupId)) return false;
			if (!artifactId.equalsIgnoreCase(other.artifactId)) return false;
			return true;
		}

		public boolean equalIDAndVersion(MavenCoordinate other) {
			if (!version.equalsIgnoreCase(other.version)) return false;
			return equalID(other);
		}

		public boolean isValid() {
			return (!StringUtil.isEmpty(groupId, true) && !StringUtil.isEmpty(artifactId, true) && !StringUtil.isEmpty(version, true));
		}
	}

	public static boolean allowed(int allowedScopes, int scope) {
		return (allowedScopes & scope) != 0;
	}

	private static POM getDependency(List<POM> dependencies, String groupId, String artifactId) {
		if (dependencies != null) {

			for (POM pom: dependencies) {
				if (pom.getGroupId().equals(groupId) && pom.getArtifactId().equals(artifactId)) {
					return pom;
				}
			}
			for (POM pom: dependencies) {
				try {
					List<POM> dependencyManagement = pom.getDependencyManagement();
					if (dependencyManagement != null) {
						for (POM p: dependencyManagement) {
							if (p.getGroupId().equals(groupId) && p.getArtifactId().equals(artifactId)) {
								return p;
							}
						}
					}
				}
				catch (IOException ioe) {
					LogUtil.log((Config) null, "mvn", ioe, Log.LEVEL_WARN, "application");
				}
			}
		}
		return null;
	}

	public static List<POM> getDependencyManagement(List<POMReader.Dependency> rawDependencies, POM current, POM parent, Map<String, String> properties, Resource localDirectory,
			Log log) throws IOException {

		List<POM> dependencies = new ArrayList<>();

		if (parent != null) {
			List<POM> deps = parent.getDependencyManagement();
			if (deps != null) {
				for (POM pom: deps) {
					dependencies.add(pom); // TODO clone?
				}
			}
		}

		if (rawDependencies != null) {
			for (Dependency rd: rawDependencies) {
				POM pom = null;
				try {
					pom = getDependency(localDirectory, rd, current, properties, true, log);
				}
				catch (IOException ioe) {
					// LDEV-6321: benign upstream-POM pattern (e.g. Google's dangling ${jackson-core-asl.version}).
					// Apache Maven core does the same (MNG-5982): warn and skip. Resolution continues without this entry.
					if (log != null) log.trace("mvn", "Dropping <dependencyManagement> entry: " + ioe.getMessage());
				}
				if (pom == null) continue;

				dependencies.add(pom);
			}
		}
		return dependencies;
	}

	public static String resolvePlaceholders(POM pom, String value, Map<String, String> properties) throws IOException {
		boolean modifed;
		while (value != null && value.contains("${")) {
			modifed = false;
			if (pom != null && value != null && value.contains("${project.")) {
				String placeholder = value.substring(value.indexOf("${project.") + 10, value.indexOf("}"));
				POM p = pom;
				// parent?
				if (placeholder.startsWith("parent.")) {
					placeholder = placeholder.substring(7);
					if (p.getParent() != null) p = p.getParent();
				}

				if ("groupId".equals(placeholder)) {
					value = p.getGroupId();
					modifed = true;
				}
				else if ("artifactId".equals(placeholder)) {
					value = p.getArtifactId();
					modifed = true;
				}
				else if ("version".equals(placeholder)) {
					value = p.getVersion();
					modifed = true;
				}
				else if ("scope".equals(placeholder) && p.getScopeAsString() != null) {
					value = p.getScopeAsString();
					modifed = true;
				}
				else if ("optional".equals(placeholder)) {
					value = p.getOptionalAsString();
					modifed = true;
				}
				// TODO is there more?
			}

			// Resolve placeholders using properties
			if (value != null && value.contains("${")) {
				String placeholder = value.substring(value.indexOf("${") + 2, value.indexOf("}"));
				String val = properties.get(placeholder);
				if (val != null && !val.equals(value)) {
					modifed = true;
					value = val;
				}
			}

			if (value != null && value.contains("${")) {
				String placeholder = value.substring(value.indexOf("${") + 2, value.indexOf("}"));
				String resolvedValue = MavenUtil.getSystemProperties().get(placeholder);
				if (resolvedValue != null && !resolvedValue.equals(value)) {
					modifed = true;
					value = resolvedValue;
				}
			}
			if (!modifed) break;
		}
		if (value != null && value.indexOf("${") != -1) {
			throw new IOException("Cannot resolve [" + value + "] in POM [" + pom + "]");
		}
		return value;
	}

	public static boolean hasPlaceholders(String str) {
		return str != null && str.indexOf("${") != -1;
	}

	public static void downloadAsync(POM pom, Collection<Repository> repositories, String type, Log log) {
		// LDEV-6715: the download uses the proxy of the calling request
		ThreadUtil.getThread(HTTPEngine.withRequestProxy(() -> {
			try {
				download(pom, repositories, type, log);
			}
			catch (IOException e) {}
		}), true).start();
	}

	public static Resource download(POM pom, Collection<Repository> repositories, String type, Log log) throws IOException {
		Resource res = pom.local(type);

		// file is empty or does not exist
		if (!res.isFile()) {

			synchronized (SystemUtil.createToken("mvn", res.getAbsolutePath())) {
				// file is empty or does not exist
				if (!res.isFile()) {

					Resource localRepo = getLocalRepository();
					if (localRepo != null) {
						Resource tmp = pom.local(localRepo, type);
						// found one in local maven
						if (tmp.isFile()) {
							res.getParentResource().mkdirs();
							tmp.copyTo(res, false);
							return res;
						}
					}

					// it did already check for file, but it was not found
					Resource lastUpdated = res.getParentResource().getRealResource(res.getName() + ".lastUpdated");
					if (lastUpdated.isFile()) {

						// try again? TODO read lastModified from file, there is an entry for every single endpoint
						if ((lastUpdated.lastModified() + ARTIFACT_UNAVAILABLE_CACHE_DURATION) < System.currentTimeMillis()) {
							lastUpdated.remove(true);
							download(pom, repositories, type, log);
							return res;
						}

						throw new IOException("Maven artifact [" + pom.getGroupId() + ":" + pom.getArtifactId() + ":" + pom.getVersion() + "] "
								+ "is not available. A previous download attempt failed and is cached for " + (ARTIFACT_UNAVAILABLE_CACHE_DURATION / 60000) + " minutes. "
								+ "Delete the '.lastUpdated' file in the local cache to retry immediately.");
					}

					int policy = ConfigUtil.getMavenDownloadPolicy();
					if (policy != ConfigPro.MAVEN_DOWNLOAD_POLICY_IGNORE) {
						LogUtil.log(ConfigUtil.getConfigServerImpl().getMavenDownloadPolicyLogLevel(), "maven",
								"Downloading Maven artifact [" + pom.getGroupId() + ":" + pom.getArtifactId() + ":" + pom.getVersion() + "] " + "(type: " + type
										+ "). Maven download policy is set to '" + (policy == ConfigPro.MAVEN_DOWNLOAD_POLICY_ERROR ? "error" : "warn") + "'. "
										+ "To change this behavior, update your .CFConfig.json: "
										+ "{\"maven\":{\"downloadPolicyStartup\":\"ignore\",\"downloadPolicyRuntime\":\"ignore\"}} "
										+ "or set the system properties 'lucee.maven.download.policy.startup' / 'lucee.maven.download.policy.runtime' "
										+ "(environment variables: LUCEE_MAVEN_DOWNLOAD_POLICY_STARTUP / LUCEE_MAVEN_DOWNLOAD_POLICY_RUNTIME) to "
										+ "'error' to block downloads or 'ignore' to suppress this message.");
					}
					if (policy == ConfigPro.MAVEN_DOWNLOAD_POLICY_ERROR) {
						throw new IOException("Maven download is blocked by policy. " + "Attempted to download [" + pom.getGroupId() + ":" + pom.getArtifactId() + ":"
								+ pom.getVersion() + "] " + "(type: " + type + "). " + "To allow downloads, update your .CFConfig.json: "
								+ "{\"maven\":{\"downloadPolicyStartup\":\"warn\",\"downloadPolicyRuntime\":\"warn\"}} "
								+ "or set the system properties 'lucee.maven.download.policy.startup' / 'lucee.maven.download.policy.runtime' "
								+ "(environment variables: LUCEE_MAVEN_DOWNLOAD_POLICY_STARTUP / LUCEE_MAVEN_DOWNLOAD_POLICY_RUNTIME) to 'warn' or 'ignore'.");
					}

					String scriptName = pom.toScriptName(type);
					StringBuilder info = null;
					StringBuilder failureSummary = null;
					Exception lastException = null;
					boolean transientFailure = false;
					try {
						if (repositories == null || repositories.isEmpty()) repositories = pom.getRepositories();

						if (repositories == null || repositories.size() == 0) {
							throw new IOException("Failed to download Maven artifact [" + pom.getGroupId() + ":" + pom.getArtifactId() + ":" + pom.getVersion() + "] " + "(type: "
									+ type + "). No repositories are configured. " + "Ensure at least one repository is defined in the POM or Lucee configuration.");
						}
						// url = pom.getArtifact(type, repositories);
						//////// if (log != null) log.info("maven", "download [" + url + "]");
						URL url;
						HTTPResponse response;
						Version version = Version.parseVersion(pom.getVersion());
						boolean isSnap = version.is(Version.SNAPSHOT);

						// LDEV-6322: snapshots are only resolved against snapshot-capable repositories (and releases
						// against release-capable ones). If none of the configured repositories can serve this version
						// type, fail fast with a clear, actionable message instead of pointlessly probing every repo.
						int eligibleRepoCount = 0;
						for (Repository r: repositories) {
							if (r.handle(version)) eligibleRepoCount++;
						}
						if (eligibleRepoCount == 0) {
							throw new IOException("Failed to download Maven artifact [" + pom.getGroupId() + ":" + pom.getArtifactId() + ":" + pom.getVersion() + "] " + "(type: "
									+ type + "). No " + (isSnap ? "snapshot-capable" : "release-capable") + " repository is configured among the " + repositories.size()
									+ " available. " + "Configure a repository that serves " + (isSnap ? "snapshot" : "release") + " artifacts.");
						}

						for (Repository r: sort(repositories)) {
							if (!r.handle(version)) continue;
							// LDEV-6663: retry once on a network exception (e.g. read timeout) before moving on
							for (int attempt = 1;; attempt++) {
								url = null;
								response = null;

								try {
									if (isSnap) {
										RepoReader repoReader = new RepoReader(r.url, pom.getGroupId(), pom.getArtifactId(), version);
										Map<String, Object> result = repoReader.read(type, "pom".equalsIgnoreCase(type) ? null : pom.getClassifier());
										// result is null when the repo has no snapshot metadata for this artifact (404/not found);
										// leave url null so we fall through to the direct artifact URL (yielding a clean 404, not an NPE)
										String strUrl = result == null ? null : Caster.toString(result.get(type));
										if (!StringUtil.isEmpty(strUrl)) url = new URL(strUrl);
									}

									if (url == null) url = new URL(r.getUrl() + scriptName);
									// common downloader (LDEV-6579): shared pooled client, server proxy, engine cookie spec
									long start = System.currentTimeMillis();
									response = HTTPEngine.getResponse(url, null, null, CONNECTION_TIMEOUT, READ_TIMEOUT_DOWNLOAD, null);
									int sc = response.getStatusCode();
									if (log != null) log.debug("maven", "download [" + url + "] from repository [" + repoLabel(r) + "] - HTTP " + sc + " in " + (System.currentTimeMillis() - start) + "ms");
									// print.e(type + " >> " + url + " >> " + sc);
									if (sc == 200) {
										InputStream is = response.getContentAsStream();
										if (is != null) {
											Exception ex = null;
											Resource tmp = SystemUtil.getTempFile(type, false);
											try {
												IOUtil.copy(is, tmp, false);
											}
											catch (IOException e) {
												ex = e;
											}
											finally {
												IOUtil.closeEL(is);
												HTTPUtil.validateDownload(url, response, tmp, pom.getChecksum(), true, ex);
												verifySignature(tmp, url, pom, type);
												res.getParentResource().mkdirs();
												tmp.moveTo(res);
											}
											deleteLastUpdated(res);
											AtomicInteger rank = ranking.get(r);
											if (rank != null) rank.incrementAndGet();
											else {
												rank = new AtomicInteger(1);
												ranking.put(r, rank);
											}
											return res;
										}
									}
									else {
										// LDEV-6663: only 404/410 are definitive, anything else is transient and must not be negative-cached
										if (sc != 404 && sc != 410) transientFailure = true;
										String retryAfter = null;
										if (sc == 429 || sc == 503) {
											Header h = response.getLastHeaderIgnoreCase("Retry-After");
											if (h != null) retryAfter = h.getValue();
										}
										if (info == null) info = createInfo();
										// match Apache Maven Resolver convention: empty .error= for 404 (NOT_FOUND), status code otherwise
										info.append(r).append(".error=").append(sc == 404 ? "" : String.valueOf(sc)).append('\n');
										info.append(r).append(".lastUpdated=").append(System.currentTimeMillis()).append('\n');
										String detail = String.valueOf(sc) + (retryAfter != null ? " (Retry-After: " + retryAfter + ")" : "");
										failureSummary = appendFailure(failureSummary, repoLabel(r), detail);
										if (log != null) {
											// 404 is "not on this repo" — expected during fallback flows, demote to INFO
											// other status codes (429 rate-limit, 5xx, 4xx-non-404) are actionable failures
											String logMsg = "download failed for [" + pom + ":" + type + "] from [" + url + "] - HTTP " + sc
													+ (retryAfter != null ? ", Retry-After: " + retryAfter : "");
											if (sc == 404) log.info("maven", logMsg);
											else log.error("maven", logMsg);
										}
									}
									break;
								}
								catch (VerificationException ve) {
									// an invalid or missing signature is not transient and must not fall back to another repository
									if (log != null) log.error("maven", "download of [" + pom + ":" + type + "] from [" + (url != null ? url : r.getUrl()) + "] rejected - " + ve.getMessage());
									throw ve;
								}
								catch (Exception e) {
									String safeMsg = e.getMessage() == null ? "" : e.getMessage().replace('\n', ' ').replace('\r', ' ');
									if (attempt < MAX_DOWNLOAD_ATTEMPTS && e instanceof IOException) {
										if (log != null) log.warn("maven", "download attempt " + attempt + " for [" + pom + ":" + type + "] from ["
												+ (url != null ? url : r.getUrl()) + "] failed, retrying - " + e.getClass().getName() + ": " + safeMsg);
										continue;
									}
									transientFailure = true;
									String exSummary = e.getClass().getSimpleName() + (safeMsg.isEmpty() ? "" : ": " + safeMsg);
									if (info == null) info = createInfo();
									info.append(r).append(".error=").append(exSummary).append('\n');
									info.append(r).append(".lastUpdated=").append(System.currentTimeMillis()).append('\n');
									lastException = e;
									failureSummary = appendFailure(failureSummary, repoLabel(r), exSummary);
									if (log != null) {
										log.error("maven", "download failed for [" + pom + ":" + type + "] from [" + (url != null ? url : r.getUrl()) + "] - "
												+ e.getClass().getName() + ": " + safeMsg);
									}
									break;
								}
								finally {
									// releases the pooled connection (the body is drained or already read)
									HTTPEngine.releaseEL(response);
								}
							}
						}
					}
					catch (IOException ioe) {
						if (info != null && !transientFailure) createLastUpdated(res, info);
						if (failureSummary == null && lastException == null) throw ioe;
						StringBuilder m = new StringBuilder("Failed to download Maven artifact [").append(pom.getGroupId()).append(":").append(pom.getArtifactId()).append(":")
								.append(pom.getVersion()).append("] (type: ").append(type).append("). Check network connectivity and repository availability.");
						if (failureSummary != null) m.append(" - ").append(failureSummary);
						IOException ex = new IOException(m.toString());
						ExceptionUtil.initCauseEL(ex, lastException != null ? lastException : ioe);
						// MUST add again ResourceUtil.deleteEmptyFoldersInside(pom.getLocalDirectory());
						throw ex;
					}
					// LDEV-6663: do not negative-cache transient failures (timeouts, 5xx, 429), only definitive "not found"
					if (info != null && !transientFailure) createLastUpdated(res, info);
					StringBuilder m = new StringBuilder("Failed to download Maven artifact [").append(pom.getGroupId()).append(":").append(pom.getArtifactId()).append(":")
							.append(pom.getVersion()).append("] (type: ").append(type).append(") after checking all ").append(repositories.size())
							.append(" configured repositories. Verify the artifact coordinates are correct and the repositories are accessible.");
					if (failureSummary != null) m.append(" - ").append(failureSummary);
					IOException ex = new IOException(m.toString());
					if (lastException != null) ExceptionUtil.initCauseEL(ex, lastException);
					throw ex;
				}
			}
		}
		return res;
	}

	private static StringBuilder createInfo() {
		return new StringBuilder("#NOTE: This is a Maven Resolver internal implementation file (created by Lucee), its format can be changed without prior notice.\n#")
				.append(ZonedDateTime.now().format(MAVEN_DATE_FORMATTER)).append('\n');
	}

	private static StringBuilder appendFailure(StringBuilder sb, String label, String detail) {
		if (sb == null) sb = new StringBuilder();
		else sb.append("; ");
		sb.append(label).append(": ").append(detail);
		return sb;
	}

	private static String repoLabel(Repository r) {
		if (!StringUtil.isEmpty(r.label)) return r.label;
		return r.getUrl();
	}

	private static void createLastUpdated(Resource res, StringBuilder info) throws IOException {
		Resource lastUpdated = res.getParentResource().getRealResource(res.getName() + ".lastUpdated");
		lastUpdated.getParentResource().mkdirs();
		// print.e(lastUpdated);
		// print.e(info);
		IOUtil.write(lastUpdated, info.toString(), StandardCharsets.UTF_8, false);
	}

	private static void deleteLastUpdated(Resource res) {
		Resource lastUpdated = res.getParentResource().getRealResource(res.getName() + ".lastUpdated");
		lastUpdated.delete();
	}

	public static Repository[] sort(Collection<Repository> repositories) {
		// Convert collection to array for sorting
		Repository[] result = repositories.toArray(new Repository[0]);
		if (ranking.size() > 0) {
			AtomicInteger defaultValue = new AtomicInteger(0);
			// Sort the array based on the ranking map
			Arrays.sort(result, (repo1, repo2) -> {
				// Get the count for each repository, defaulting to 0 if not in the map
				int count1 = ranking.getOrDefault(repo1, defaultValue).get();
				int count2 = ranking.getOrDefault(repo2, defaultValue).get();

				// Sort in descending order (highest count first)
				return Integer.compare(count2, count1);
			});
		}
		return result;
	}

	public static POM toPOM(Resource localDirectory, Collection<Repository> repositories, POMReader.Dependency dependency, Map<String, String> properties, int dependencyScope,
			int dependencyScopeManagement, Log log) throws IOException {

		return POM.getInstance(localDirectory, repositories,

				resolvePlaceholders(null, dependency.groupId, properties),

				resolvePlaceholders(null, dependency.artifactId, properties),

				resolvePlaceholders(null, dependency.version, properties),

				null, null, null,

				dependencyScope, dependencyScopeManagement,

				log

		);
	}

	public static int toScopes(String scopes) throws IOException {
		if (StringUtil.isEmpty(scopes, true))
			throw new IOException("No Maven dependency scope defined. " + "Specify at least one scope from: compile, test, provided, runtime, system, import.");
		return toScopes(ListUtil.listToStringArray(scopes, ','));
	}

	public static int toScopes(String[] scopes) throws IOException {
		if (scopes.length == 0) throw new IOException("No Maven dependency scope defined. " + "Specify at least one scope from: compile, test, provided, runtime, system, import.");

		int rtn = 0, tmp;
		for (String scope: scopes) {
			tmp = toScope(scope, 0);
			if (tmp == 0) {
				throw new IOException("Invalid Maven scope [" + scope.trim() + "]. " + "Supported values are: compile, test, provided, runtime, system, import.");
			}
			rtn += tmp;
		}
		return rtn;
	}

	public static int toScopes(String scopes, int defaultValue) {
		if (StringUtil.isEmpty(scopes, true)) return defaultValue;

		int rtn = 0;
		for (String scope: ListUtil.listToStringArray(scopes, ',')) {
			rtn += toScope(scope, 0);
		}
		if (rtn > 0) return rtn;

		return defaultValue;
	}

	public static int toScope(String scope, int defaultValue) {
		if ("compile".equals(scope)) return POM.SCOPE_COMPILE;
		if ("test".equals(scope)) return POM.SCOPE_TEST;
		if ("provided".equals(scope)) return POM.SCOPE_PROVIDED;
		if ("runtime".equals(scope)) return POM.SCOPE_RUNTIME;
		if ("system".equals(scope)) return POM.SCOPE_SYSTEM;
		if ("import".equals(scope)) return POM.SCOPE_IMPORT;
		return defaultValue;
	}

	public static String toScope(int scope, String defaultValue) {
		switch (scope) {
		case POM.SCOPE_COMPILE:
			return "compile";
		case POM.SCOPE_TEST:
			return "test";
		case POM.SCOPE_PROVIDED:
			return "provided";
		case POM.SCOPE_RUNTIME:
			return "runtime";
		case POM.SCOPE_SYSTEM:
			return "system";
		case POM.SCOPE_IMPORT:
			return "import";
		default:
			return defaultValue;
		}
	}

	public static List<MavenCoordinate> toCoordinates(Object obj, List<MavenCoordinate> defaultValue) {
		// array
		List<MavenCoordinate> list = new ArrayList<>();
		Object[] arr = Caster.toNativeArray(obj, null);
		if (arr != null) {
			MavenCoordinate tmp;
			for (Object o: arr) {
				tmp = toCoordinate(o, null);
				if (tmp != null) list.add(tmp);
			}
			return list;
		}
		// struct
		Struct el = Caster.toStruct(obj, null);
		if (el != null) {
			MavenCoordinate tmp = toCoordinate(el, null);
			if (tmp != null) list.add(tmp);
			return list;
		}

		// gradle style?
		String str = Caster.toString(obj, null);
		if (!StringUtil.isEmpty(str)) {
			MavenCoordinate tmp;
			for (String s: ListUtil.listToStringArray(str, ',')) {
				tmp = toCoordinate(s, null);
				if (tmp != null) list.add(tmp);
			}
			return list;
		}
		return list;
	}

	public static List<MavenCoordinate> toCoordinates(Object obj) throws ApplicationException {
		// array
		List<MavenCoordinate> list = new ArrayList<>();
		Object[] arr = Caster.toNativeArray(obj, null);
		if (arr != null) {
			for (Object o: arr) {
				list.add(toCoordinate(o));
			}
			return list;
		}
		// struct
		Struct el = Caster.toStruct(obj, null);
		if (el != null) {
			list.add(toCoordinate(el));
			return list;
		}

		// gradle style?
		String str = Caster.toString(obj, null);
		if (!StringUtil.isEmpty(str)) {
			for (String s: ListUtil.listToStringArray(str, ',')) {
				list.add(toCoordinate(s));
			}
			return list;
		}
		return list;
	}

	public static MavenCoordinate toCoordinate(Object obj, MavenCoordinate defaultValue) {
		try {
			MavenCoordinate coordinate = parseCoordinate(obj, false);
			return coordinate == null ? defaultValue : coordinate;
		}
		catch (ApplicationException ae) {
			return defaultValue;
		}
	}

	public static MavenCoordinate toCoordinate(Object obj) throws ApplicationException {
		return parseCoordinate(obj, true);
	}

	/**
	 * String format (Gradle style): groupId:artifactId[:version[:classifier]][@extension]. A known
	 * scope (compile, provided, runtime, test, system, import) in 4th position is the legacy form
	 * groupId:artifactId:version:scope[:optional[:checksum]], otherwise the 4th part is the classifier
	 * and further parts are scope[:optional[:checksum]]. A classifier that equals a scope name (for
	 * example "test") cannot be expressed in the string form, use the struct form for that.
	 */
	private static MavenCoordinate parseCoordinate(Object obj, boolean strict) throws ApplicationException {
		if (obj instanceof MavenCoordinate) return (MavenCoordinate) obj;

		Struct el = Caster.toStruct(obj, null, false);
		if (el != null) {
			String g = Caster.toString(el.get(KeyConstants._groupId, null), null);
			if (StringUtil.isEmpty(g)) g = Caster.toString(el.get(KeyConstants._g, null), null);
			String a = Caster.toString(el.get(KeyConstants._artifactId, null), null);
			if (StringUtil.isEmpty(a)) a = Caster.toString(el.get(KeyConstants._a, null), null);

			if (!StringUtil.isEmpty(g) && !StringUtil.isEmpty(a)) {
				String v = Caster.toString(el.get(KeyConstants._version, null), null);
				if (StringUtil.isEmpty(v, true)) v = Caster.toString(el.get(KeyConstants._v, null), null);
				if (StringUtil.isEmpty(v, true)) v = null;

				return new MavenCoordinate(g, a, v, Caster.toString(el.get(KeyConstants._classifier, null), null), Caster.toString(el.get(KeyConstants._extension, null), null),
						Caster.toString(el.get(KeyConstants._scope, null), null), Caster.toString(el.get(KeyConstants._optional, null), null),
						Caster.toString(el.get(KeyConstants._checksum, null), null));
			}

			// single entry with gradle style?
			String m = Caster.toString(el.get(KeyConstants._maven, null), null);
			if (!StringUtil.isEmpty(m, true)) return parseCoordinate(m, strict);

			if (!strict) return null;
			if (StringUtil.isEmpty(g)) throw new ApplicationException("Missing required field: groupId. Ensure that the 'groupId' key is present and not empty.");
			throw new ApplicationException("Missing required field: artifactId. Ensure that the 'artifactId' key is present and not empty.");
		}

		// gradle style?
		String str = Caster.toString(obj, null);
		if (!StringUtil.isEmpty(str)) {
			str = str.trim();
			String extension = null;
			int at = str.lastIndexOf('@');
			if (at != -1) {
				extension = str.substring(at + 1).trim();
				str = str.substring(0, at);
			}
			String[] arr = ListUtil.listToStringArray(str, ':');
			int index = 3;
			String classifier = null;
			// a known scope in 4th position keeps its legacy meaning (scope[:optional[:checksum]]), anything else is the classifier
			if (arr.length > index && toScope(arr[index].trim(), -1) == -1) classifier = arr[index++].trim();
			boolean validScope = arr.length <= index || toScope(arr[index].trim(), -1) != -1;
			if (arr.length > 1 && arr.length < 8 && validScope) {
				return new MavenCoordinate(

						arr[0].trim(), // group

						arr[1].trim(), // artifact

						arr.length > 2 ? arr[2].trim() : null, // version

						classifier,

						extension,

						arr.length > index ? arr[index].trim() : null, // scope

						arr.length > index + 1 ? arr[index + 1].trim() : null, // optional

						arr.length > index + 2 ? arr[index + 2].trim() : null // checksum

				);
			}
			if (!strict) return null;
			throw new ApplicationException("Invalid Maven data in string [" + str + "]. Expected format: '<group>:<artifact>:<version>[:<classifier>][@<extension>]'.");
		}
		if (!strict) return null;
		throw new ApplicationException("Unable to parse Maven data from the provided input of type [" + Caster.toTypeName(obj) + "]. Supported formats are: "
				+ "1) A Struct with keys 'groupId', 'artifactId', and 'version' (optionally 'classifier', 'extension', 'scope', 'optional' and 'checksum'), or "
				+ "2) A String in the format 'group:artifact:version[:classifier][@extension]'. Ensure the input conforms to one of these formats.");
	}

	// LDEV-6250: not called — Maven does not restrict version format (see
	// maven.apache.org/pom.html#version-order-specification).
	// Versions like "v4-rev20260213-2.0.0" are valid. Downstream code (POM constructor, download)
	// already handles null/bad versions.
	public static boolean isValidVersion(String version) {
		if (StringUtil.isEmpty(version)) return false;

		String versionPattern = "^(\\d+)" + // Major (required)
				"(?:\\.(\\d+))?" + // Minor (optional)
				"(?:\\.(\\d+))?" + // Micro (optional)
				"(?:[-.]?" + // Separator for qualifier (optional)
				"([A-Za-z0-9_-]+(?:[-._][A-Za-z0-9_-]+)*))?" + // Qualifier
				"$";

		return version.matches(versionPattern);
	}

	public static String toString(MavenCoordinate[] coordinates) {
		StringBuilder sb = new StringBuilder();
		for (MavenCoordinate g: coordinates) {
			if (sb.length() > 0) sb.append(',');
			sb.append(g.toString());
		}
		return sb.toString();
	}

	public static Resource getLocalRepository() {
		if (!initLocalRepository) {
			String userHome = System.getProperty("user.home");

			if (!StringUtil.isEmpty(userHome, true)) {
				ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
				Resource rep = frp.getResource(userHome).getRealResource(".m2/repository");
				if (rep.isDirectory()) localRepository = rep;
			}
			initLocalRepository = true;
		}
		return localRepository;
	}

	public static String createChecksum(Resource res, String algorithm) throws IOException {
		if ("md5".equalsIgnoreCase(algorithm)) return "md5:" + Hash.md5(res);
		else if ("sha1".equalsIgnoreCase(algorithm)) return "sha1:" + Hash.sha1(res);
		else if ("sha256".equalsIgnoreCase(algorithm)) return "sha256:" + Hash.sha256(res);
		else if ("sha512".equalsIgnoreCase(algorithm)) return "sha512:" + Hash.sha512(res);

		throw new IOException("Invalid checksum algorithm '" + algorithm + "'. Only the following algorithms are supported: md5, sha1, sha256, sha512");
	}

	public static Struct getMetaData(Config config, Class clazz, Struct defaultValue) {
		String path = ClassUtil.getSourcePathForClass(clazz, null);
		if (path != null) {
			Resource res = ResourceUtil.toResourceExisting(config, path, null);
			if (res != null) {
				// is Maven?
				Resource mvnDir = ((ConfigPro) config).getMavenDir();
				if (ResourceUtil.isChildOf(res, mvnDir)) {
					String name = res.getName();
					if (name.endsWith(".jar")) {

						String pomName = name.substring(0, name.length() - 4) + ".pom";
						Resource pom = res.getParentResource().getRealResource(pomName);
						if (pom.isFile()) {
							try {

								Resource parent = res.getParentResource();
								String v = parent.getName();

								parent = parent.getParentResource();
								String a = parent.getName();

								parent = parent.getParentResource();
								String g = parent.getName();
								while (!mvnDir.equals(parent = parent.getParentResource())) {
									g = parent.getName() + "." + g;

								}
								Struct data = new StructImpl(Struct.TYPE_LINKED);
								data.setEL(KeyConstants._groupId, g);
								data.setEL(KeyConstants._artifactId, a);
								data.setEL(KeyConstants._version, v);
								data.setEL(KeyConstants._location, res.getAbsolutePath());
								return data;
							}
							catch (Exception e) {}
						}
					}
				}
			}
		}
		return defaultValue;
	}

	/**
	 * org.lucee artifacts must have a valid signature of the Lucee release key (see
	 * {@link ArtifactSignature}), the downloaded file is deleted otherwise
	 */
	private static void verifySignature(Resource tmp, URL url, POM pom, String type) throws IOException {
		int requirement = ArtifactSignature.requirement(pom.getGroupId(), pom.getVersion());
		if (requirement == ArtifactSignature.NOT_SIGNED) return;
		try {
			ArtifactSignature.verify(tmp, url, pom.getGroupId() + ":" + pom.getArtifactId() + ":" + pom.getVersion() + ":" + type, requirement);
		}
		catch (IOException | RuntimeException e) {
			tmp.delete();
			throw e;
		}
	}
}
