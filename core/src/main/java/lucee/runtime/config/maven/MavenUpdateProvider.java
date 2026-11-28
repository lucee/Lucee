package lucee.runtime.config.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
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

import org.xml.sax.SAXException;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPDownloader;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.Config;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.CastImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.util.ListUtil;

public final class MavenUpdateProvider {

	public static final int CONNECTION_TIMEOUT = 10000; // 10 seconds - for establishing connection
	public static final int READ_TIMEOUT = 60000; // 60 seconds - for reading response data

	// new last 90 days
	private static final Repository DEFAULT_REPOSITORY_SONATYPE_LAST90 = new Repository("Sonatype Repositry for Snapshots (last 90 days)",
			"https://central.sonatype.com/repository/maven-snapshots/", Repository.TIMEOUT_1HOUR, Repository.TIMEOUT_NEVER);

	private static final Repository[] DEFAULT_REPOSITORY_SNAPSHOTS = new Repository[] { DEFAULT_REPOSITORY_SONATYPE_LAST90 };

	private static final Repository[] DEFAULT_REPOSITORY_RELEASES = new Repository[] {
			new Repository("Maven Release Repository", "https://repo1.maven.org/maven2/", Repository.TIMEOUT_1HOUR, Repository.TIMEOUT_NEVER) };

	private static final Repository[] DEFAULT_REPOSITORY_MIXED = new Repository[] {
			// versions provided by Lucee (RC/BETA/final releases not published to Maven Central)
			new Repository("Lucee Maven repository", "https://maven.lucee-services.com/", Repository.TIMEOUT_1HOUR, Repository.TIMEOUT_NEVER) };

	public static final String DEFAULT_GROUP = "org.lucee";
	public static final String DEFAULT_ARTIFACT = "lucee";

	private static Repository[] defaultRepositoryReleases;
	private static Repository[] defaultRepositorySnapshots;
	private static Repository[] defaultRepositoryMixed;

	private final String group;
	private final String artifact;
	private final Repository[] repoSnapshots;
	private final Repository[] repoReleases;
	private final Repository[] repoMixed;
	private final List<Repository> repos;

	public static Repository[] getDefaultRepositoryReleases() {
		if (defaultRepositoryReleases == null) {
			defaultRepositoryReleases = readReposFromEnvVar("lucee.mvn.repo.releases", DEFAULT_REPOSITORY_RELEASES);
		}
		return defaultRepositoryReleases;
	}

	public static Repository[] getDefaultRepositorySnapshots() {
		if (defaultRepositorySnapshots == null) {
			defaultRepositorySnapshots = readReposFromEnvVar("lucee.mvn.repo.snapshots", DEFAULT_REPOSITORY_SNAPSHOTS);
		}
		return defaultRepositorySnapshots;
	}

	public static Repository[] getDefaultRepositoryMixed() {
		if (defaultRepositoryMixed == null) {
			defaultRepositoryMixed = readReposFromEnvVar("lucee.mvn.repo.mixed", DEFAULT_REPOSITORY_MIXED);
		}
		return defaultRepositoryMixed;
	}

	/**
	 * LDEV-6705: the one repository list used for all Maven downloads (core update, extensions, OSGi
	 * bundles, POM dependencies), the release (lucee.mvn.repo.releases) or snapshot
	 * (lucee.mvn.repo.snapshots) repositories followed by the mixed repositories (lucee.mvn.repo.mixed)
	 */
	public static List<String> getDefaultRepositoryURLs(boolean snapshot) {
		List<String> urls = new ArrayList<>();
		for (Repository repo: merge(snapshot ? getDefaultRepositorySnapshots() : getDefaultRepositoryReleases(), getDefaultRepositoryMixed())) {
			if (!urls.contains(repo.url)) urls.add(repo.url);
		}
		return urls;
	}

	private static Repository[] readReposFromEnvVar(String envVarName, Repository[] defaultValue) {
		String str = SystemUtil.getSystemPropOrEnvVar(envVarName, null);
		if (!StringUtil.isEmpty(str, true)) {

			List<String> raw = ListUtil.listToList(str.trim(), ',', true);
			List<Repository> repos = new ArrayList<>();
			for (String s: raw) {
				try {
					repos.add(new Repository(null, new URL(s).toExternalForm(), Repository.TIMEOUT_5MINUTES, Repository.TIMEOUT_NEVER));
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

	public MavenUpdateProvider() {
		this.repoSnapshots = getDefaultRepositorySnapshots();
		this.repoReleases = getDefaultRepositoryReleases();
		this.repoMixed = getDefaultRepositoryMixed();
		this.repos = merge(repoSnapshots, repoReleases, repoMixed);
		this.group = DEFAULT_GROUP;
		this.artifact = DEFAULT_ARTIFACT;
	}

	public MavenUpdateProvider(String group, String artifact) {
		this.repoSnapshots = getDefaultRepositorySnapshots();
		this.repoReleases = getDefaultRepositoryReleases();
		this.repoMixed = getDefaultRepositoryMixed();
		this.repos = merge(repoSnapshots, repoReleases, repoMixed);
		this.group = group;
		this.artifact = artifact;
	}

	public MavenUpdateProvider(Repository[] repoSnapshots, Repository[] repoReleases, Repository[] repoMixed, String group, String artifact) {
		this.repoSnapshots = repoSnapshots;
		this.repoReleases = repoReleases;
		this.repoMixed = repoMixed;
		this.repos = merge(repoSnapshots, repoReleases, repoMixed);
		this.group = group;
		this.artifact = artifact;
	}

	public MavenUpdateProvider(Collection<Repository> repositories, String group, String artifact) {
		this.repoSnapshots = new Repository[0];
		this.repoReleases = new Repository[0];
		this.repoMixed = repositories.toArray(new Repository[repositories.size()]);
		this.repos = new ArrayList<>(repositories);
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

	static List<Repository> merge(Repository[] left, Repository[] middle, Repository[] right) {
		List<Repository> list = new ArrayList<>();
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

	public List<Version> list() throws IOException, GeneralSecurityException, SAXException, InterruptedException {
		try {
			Set<Version> versions = Collections.synchronizedSet(new HashSet<>());
			List<Thread> threads = new ArrayList<>();
			Stack<Exception> exceptions = new Stack<>();
			for (Repository repo: repos) {
				Thread thread = ThreadUtil.getThread(() -> {
					try {
						MetadataReader mr = new MetadataReader(repo, group, artifact);
						for (Version v: mr.read()) {
							// print.e(repo.label + "(" + repo.url + "):" + v);
							versions.add(v);
						}
					}
					catch (Exception e) {
						exceptions.add(e);
					}
				}, true);
				thread.start();
				threads.add(thread);
			}

			// Join all threads
			for (Thread thread: threads) {
				thread.join();
			}

			// handle exceptions
			if (exceptions.size() > 0) {
				Exception e = exceptions.pop();
				if (e instanceof GeneralSecurityException) throw (GeneralSecurityException) e;
				else if (e instanceof SAXException) throw (SAXException) e;

				throw ExceptionUtil.toIOException(new IOException("Failed to list available versions from Maven repositories for [" + group + ":" + artifact + "]", e));
			}

			if (versions.size() > 0) {
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

	public InputStream getCore(Version version) throws IOException, GeneralSecurityException, SAXException, PageException {
		Map<String, Object> data = detail(version, "jar", true);
		String strURL = Caster.toString(data.get("lco"), null);
		assertDownloadAllowed(strURL);
		if (!StringUtil.isEmpty(strURL)) {
			// Use HTTPDownloader with DEBUG logging for Maven operations
			return HTTPDownloader.get(new URL(strURL), null, null, CONNECTION_TIMEOUT, READ_TIMEOUT, null, Log.LEVEL_TRACE);
		}
		return getFileStreamFromZipStream(getLoader(version));
	}

	/**
	 * LDEV-7027: the repositories to look for a core update, by the update type of the installed
	 * version: the release and mixed repositories for a release, the snapshot, release and mixed
	 * repositories for a SNAPSHOT, RC, BETA or ALPHA
	 */
	public static List<Repository> getCoreUpdateRepositories(Version current) {
		List<Repository> merged = isRelease(current) ? merge(getDefaultRepositoryReleases(), getDefaultRepositoryMixed())
				: merge(getDefaultRepositorySnapshots(), getDefaultRepositoryReleases(), getDefaultRepositoryMixed());
		// the same repository can be in more than one list, ask it only once
		List<Repository> list = new ArrayList<>();
		Set<String> urls = new HashSet<>();
		for (Repository r: merged) {
			if (urls.add(r.url)) list.add(r);
		}
		return list;
	}

	private static boolean isRelease(Version v) {
		return StringUtil.isEmpty(v.getQualifier(), true);
	}

	/**
	 * LDEV-7027: replacement for the update provider endpoint "update-for/{version}", the newest core
	 * version that is an update for the given version, or null if there is none. The versions come from
	 * the repositories for the update type ({@link #getCoreUpdateRepositories}) with {@link #list()}
	 * (maven-metadata.xml, cached per repository). In 7.1 the repositories are the defaults (system
	 * properties lucee.mvn.repo.*), the config is not used.
	 */
	public static Version updateFor(Config config, Version current) throws IOException, GeneralSecurityException, SAXException, InterruptedException {
		return new MavenUpdateProvider(getCoreUpdateRepositories(current), DEFAULT_GROUP, DEFAULT_ARTIFACT).updateFor(current);
	}

	/**
	 * same as {@link #updateFor(Config, Version)}, with the repositories of this provider
	 */
	public Version updateFor(Version current) throws IOException, GeneralSecurityException, SAXException, InterruptedException {
		return selectUpdate(list(), current);
	}

	// no updates for versions smaller than this (same as the update provider)
	private static final Version MIN_UPDATE_VERSION = Version.parseVersion("5.0.0.254", null);

	/**
	 * selects the update for the installed version, the rules of the update provider ("update-for",
	 * lucee-data-provider UpdateProvider.getInfo) unchanged:
	 * <ul>
	 * <li>no update for versions older than 5.0.0.254</li>
	 * <li>only versions the provider offers: 4 numbers, the last one optionally with SNAPSHOT, BETA, RC
	 * or ALPHA (7.0.0.202 is hidden)</li>
	 * <li>only versions of the same major version (default version filter of the provider)</li>
	 * <li>an installed pre-release (SNAPSHOT, BETA, RC, ALPHA) is offered any of them</li>
	 * <li>an installed stable version is offered stable versions, and pre-releases of the same
	 * major.minor, never an ALPHA</li>
	 * <li>the highest of them (SNAPSHOT &lt; BETA &lt; RC/ALPHA &lt; stable for the same numbers), if it
	 * is newer than the installed version</li>
	 * </ul>
	 * 
	 * @return the update or null
	 */
	public static Version selectUpdate(Collection<Version> available, Version current) {
		if (available == null || current == null) return null;
		int[] curr = providerNumbers(current);
		if (curr == null) return null;
		if (!isNewerForUpdate(current, MIN_UPDATE_VERSION)) return null;

		Version selected = null;
		int[] nbrs;
		for (Version v: available) {
			if (v == null || !isOffered(v)) continue;
			nbrs = providerNumbers(v);
			if (nbrs[0] != curr[0]) continue;
			if (!isOfferableUpdate(v, nbrs, current, curr)) continue;
			if (selected == null || isNewerForUpdate(v, selected)) selected = v;
		}
		if (selected == null || !isNewerForUpdate(selected, current)) return null;
		return selected;
	}

	private static boolean isOffered(Version v) {
		String str = v.toString();
		String[] parts = str.split("\\.", -1);
		if (parts.length != 4) return false;
		for (int i = 0; i < 3; i++) {
			if (!isDigits(parts[i])) return false;
		}
		if ("7.0.0.202".equals(str)) return false;
		String[] patch = parts[3].split("-", -1);
		if (patch.length > 2 || !isDigits(patch[0])) return false;
		return patch.length == 1 || "SNAPSHOT".equals(patch[1]) || "BETA".equals(patch[1]) || "RC".equals(patch[1]) || "ALPHA".equals(patch[1]);
	}

	private static boolean isOfferableUpdate(Version candidate, int[] candNbrs, Version installed, int[] instNbrs) {
		if (!StringUtil.isEmpty(installed.getQualifier())) return true;
		String q = candidate.getQualifier();
		if (StringUtil.isEmpty(q)) return true;
		if ("ALPHA".equals(q)) return false;
		return candNbrs[0] == instNbrs[0] && candNbrs[1] == instNbrs[1];
	}

	/**
	 * same order as the update provider (isNewer): the 4 numbers, then the qualifier (SNAPSHOT &lt;
	 * BETA &lt; any other &lt; stable), for two "other" qualifiers the text
	 */
	static boolean isNewerForUpdate(Version left, Version right) {
		int[] l = providerNumbers(left), r = providerNumbers(right);
		if (l == null || r == null) return false;
		for (int i = 0; i < 4; i++) {
			if (l[i] != r[i]) return l[i] > r[i];
		}
		int lr = providerRank(left), rr = providerRank(right);
		if (lr != rr) return lr > rr;
		if (lr == 75) return left.getQualifier().compareTo(right.getQualifier()) > 0;
		return false;
	}

	private static int providerRank(Version v) {
		String q = v.getQualifier();
		if (StringUtil.isEmpty(q)) return 100;
		if ("SNAPSHOT".equals(q)) return 0;
		if ("BETA".equals(q)) return 50;
		return 75;
	}

	// major, minor, micro, build like the update provider (a missing build is 0), null if invalid
	private static int[] providerNumbers(Version v) {
		String base = v.toString();
		int index = base.indexOf('-');
		if (index != -1) base = base.substring(0, index);
		String[] parts = base.split("\\.", -1);
		if (parts.length < 3 || parts.length > 4) return null;
		int[] arr = new int[4];
		for (int i = 0; i < parts.length; i++) {
			if (!isDigits(parts[i])) return null;
			arr[i] = Caster.toIntValue(parts[i], 0);
		}
		return arr;
	}

	private static boolean isDigits(String str) {
		if (str == null || str.isEmpty()) return false;
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) return false;
		}
		return true;
	}

	/**
	 * LDEV-7027: replacement for the update provider endpoint "download/{version}", downloads the core
	 * (.lco) with {@link #getCore(Version)} (checksum verified, Maven download policy) into the given
	 * directory as "{version}.lco". The file only gets its final name once the download is complete and
	 * verified.
	 */
	public File downloadCore(Version version, File directory) throws IOException, GeneralSecurityException, SAXException, PageException {
		File target = new File(directory, version + ".lco");
		File temp = new File(directory, version + ".lco.download");
		InputStream is = null;
		try {
			is = getCore(version);
			IOUtil.copy(is, new FileOutputStream(temp), false, true);
		}
		catch (IOException | GeneralSecurityException | SAXException | PageException | RuntimeException e) {
			temp.delete();
			throw e;
		}
		finally {
			IOUtil.closeEL(is);
		}
		if (target.exists() && !target.delete()) {
			temp.delete();
			throw new IOException("Could not replace the existing core file [" + target + "]");
		}
		if (!temp.renameTo(target)) {
			temp.delete();
			throw new IOException("Could not move the downloaded core to [" + target + "]");
		}
		return target;
	}

	public InputStream getLoader(Version version) throws IOException, GeneralSecurityException, SAXException, PageException {
		Map<String, Object> data = detail(version, "jar", true);
		String strURL = Caster.toString(data.get("jar"), null);
		// the repository found first may only have the .lco, look for the jar in the other repositories
		if (StringUtil.isEmpty(strURL)) strURL = findArtifact(version, "jar");
		if (StringUtil.isEmpty(strURL)) {
			throw new IOException("No JAR artifact found for [" + group + ":" + artifact + ":" + version + "]. " + "Verify the version exists in the configured repositories.");
		}

		// Use HTTPDownloader with DEBUG logging for Maven operations
		URL url = new URL(strURL);
		assertDownloadAllowed(strURL);
		return HTTPDownloader.get(url, null, null, CONNECTION_TIMEOUT, READ_TIMEOUT, null, Log.LEVEL_TRACE);
	}

	/*
	 * public static void main(String[] args) throws PageException, IOException,
	 * GeneralSecurityException, SAXException, BundleException {
	 * 
	 * MavenUpdateProvider mup = new MavenUpdateProvider(); Map<String, Object> map =
	 * mup.detail(OSGiUtil.toVersion("6.1.0.719-SNAPSHOT")); print.e(map); }
	 */

	public Map<String, Object> detail(Version version, String requiredArtifactExtension, boolean throwException)
			throws IOException, GeneralSecurityException, SAXException, PageException {
		// SNAPSHOT - snapshot have a more complicated structure, ebcause there can be udaptes/multiple
		// versions

		boolean isSnap = version.is(Version.SNAPSHOT);
		List<Repository> repos = isSnap ? merge(repoSnapshots, repoMixed) : merge(repoReleases, repoMixed);

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
						result = readFromCache(repo, artifact, v);
						if (result != null) {
							return result;
						}
					}
				}

				for (Repository repo: repos) {
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
						String base = repo.url + g + "/" + a + "/" + v + "/" + a + "-" + v;
						// LDEV-6704: for the core, look for the .lco first, a version with only .lco and pom is valid
						boolean lcoFirst = "jar".equals(requiredArtifactExtension);
						// LDEV-6523: only the core has a .lco, and the .pom is not needed to install an extension
						boolean isCore = DEFAULT_GROUP.equals(group) && DEFAULT_ARTIFACT.equals(artifact);
						HTTPResponse rspLco = null;
						URL urlLco = new URL(base + ".lco");
						if (lcoFirst) {
							rspLco = HTTPDownloader.head(urlLco, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT, Log.LEVEL_TRACE);
						}
						URL urlMain = new URL(base + "." + requiredArtifactExtension);
						HTTPResponse rspMain = HTTPDownloader.head(urlMain, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT, Log.LEVEL_TRACE);
						boolean hasMain = validSatusCode(rspMain);
						boolean hasLco = lcoFirst && validSatusCode(rspLco);
						if (hasMain || hasLco) {
							Map<String, Object> result = new LinkedHashMap<>();
							Header[] headers = (hasMain ? rspMain : rspLco).getAllHeaders();
							for (Header h: headers) {
								if ("Last-Modified".equals(h.getName())) result.put("lastModified", DateCaster.toDateAdvanced(h.getValue(), null));
							}

							if (hasMain) result.put(requiredArtifactExtension, urlMain.toExternalForm());

							// optional
							// pom
							if (isCore) {
								URL url = new URL(base + ".pom");
								HTTPResponse rsp = HTTPDownloader.head(url, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT, Log.LEVEL_TRACE);
								if (validSatusCode(rsp)) {
									result.put("pom", url.toExternalForm());
								}
							}
							// lco
							if (hasLco) {
								result.put("lco", urlLco.toExternalForm());
							}
							else if (!lcoFirst && isCore) {
								HTTPResponse rsp = HTTPDownloader.head(urlLco, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT, Log.LEVEL_TRACE);
								if (validSatusCode(rsp)) {
									result.put("lco", urlLco.toExternalForm());
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
		if (throwException) throw new IOException("Could not find the artifact [" + group + ":" + artifact + ":" + version + "] (type: "
				+ ("jar".equals(requiredArtifactExtension) ? "lco or jar" : requiredArtifactExtension) + ") in any of the configured repositories: [" + toList(repos) + "]. " + "Verify the artifact coordinates and version are correct.");
		return null;
	}

	private void storeToCache(Repository repository, String artifact, String version, Map<String, Object> detail) {
		try {
			Resource resLastmod = repository.cacheDirectory
					.getRealResource("detail_" + HashUtil.create64BitHashAsString(group + "_" + artifact + "_" + version + "_lastmod", Character.MAX_RADIX));
			Resource resVersions = repository.cacheDirectory
					.getRealResource("detail_" + HashUtil.create64BitHashAsString(group + "_" + artifact + "_" + version + "_versions", Character.MAX_RADIX));
			String content = fromMapToJsonString(detail, true);

			IOUtil.write(resVersions, StringUtil.isEmpty(content, true) ? "" : content.trim(), CharsetUtil.UTF8, false);
			IOUtil.write(resLastmod, Caster.toString(System.currentTimeMillis()), CharsetUtil.UTF8, false);
		}
		catch (Exception e) {
			LogUtil.log("MetadataReader", e);
		}
	}

	private String findArtifact(Version version, String extension) throws IOException, GeneralSecurityException {
		if (version.is(Version.SNAPSHOT)) return null;
		String g = group.replace('.', '/');
		String a = artifact.replace('.', '/');
		String v = version.toString();
		for (Repository repo: merge(repoReleases, repoMixed)) {
			URL url = new URL(repo.url + g + "/" + a + "/" + v + "/" + a + "-" + v + "." + extension);
			if (validSatusCode(HTTPDownloader.head(url, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT, Log.LEVEL_TRACE))) return url.toExternalForm();
		}
		return null;
	}

	private Map<String, Object> readFromCache(Repository repository, String artifact, String version) {
		try {
			Resource resLastmod = repository.cacheDirectory
					.getRealResource("detail_" + HashUtil.create64BitHashAsString(group + "_" + artifact + "_" + version + "_lastmod", Character.MAX_RADIX));
			if (resLastmod.isFile()) {
				long lastmod = repository.timeoutDetail == Repository.TIMEOUT_NEVER ? Repository.TIMEOUT_NEVER
						: Caster.toLongValue(IOUtil.toString(resLastmod, CharsetUtil.UTF8), 0L);

				if (repository.timeoutDetail == Repository.TIMEOUT_NEVER || lastmod + repository.timeoutDetail > System.currentTimeMillis()) {

					Resource resVersions = repository.cacheDirectory
							.getRealResource("detail_" + HashUtil.create64BitHashAsString(group + "_" + artifact + "_" + version + "_versions", Character.MAX_RADIX));
					String content = IOUtil.toString(resVersions, CharsetUtil.UTF8);
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

	private String toList(Repository[] repos) {
		StringBuilder sb = new StringBuilder();
		for (Repository r: repos) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(r.url);
		}
		return sb.toString();
	}

	private String toList(List<Repository> repos) {
		StringBuilder sb = new StringBuilder();
		for (Repository r: repos) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(r.url);
		}
		return sb.toString();
	}

	/**
	 * returns the url of the pom next to the given artifact url, or null if there is none
	 */
	public static String getPOMURL(String artifactUrl) throws IOException {
		if (artifactUrl == null) return null;
		int slash = artifactUrl.lastIndexOf('/');
		int dot = artifactUrl.lastIndexOf('.');
		if (dot <= slash) return null;
		URL url = new URL(artifactUrl.substring(0, dot) + ".pom");
		HTTPResponse rsp = HTTPDownloader.head(url, CONNECTION_TIMEOUT, CONNECTION_TIMEOUT, Log.LEVEL_TRACE);
		if (rsp != null && rsp.getStatusCode() >= 200 && rsp.getStatusCode() < 300) return url.toExternalForm();
		return null;
	}

	private boolean validSatusCode(HTTPResponse rsp) {
		if (rsp == null) return false;
		return rsp.getStatusCode() >= 200 && rsp.getStatusCode() < 300;
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
		JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, compact);
		try {
			return json.serialize(null, detail, SerializationSettings.SERIALIZE_AS_COLUMN, null);
		}
		catch (ConverterException e) {
			throw Caster.toPageException(e);
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

		private static Resource cacheRootDirectory;

		public final String label;
		public final String url;
		public final long timeoutList;
		public final long timeoutDetail;
		public final Resource cacheDirectory;

		static {
			try {
				cacheRootDirectory = CFMLEngineFactory.getInstance().getThreadConfig().getConfigDir();
			}
			catch (Exception e) {
				cacheRootDirectory = SystemUtil.getTempDirectory();
			}
		}

		public Repository(String label, String url, long timeoutList, long timeoutDetail) {
			this(label, url, timeoutList, timeoutDetail, getCacheDirectory(url));
		}

		public Repository(String label, String url, long timeoutList, long timeoutDetail, Resource cacheDirectory) {
			if (!url.endsWith("/")) url += "/";
			this.label = label;
			this.url = url;
			this.timeoutList = timeoutList;
			this.timeoutDetail = timeoutDetail;
			this.cacheDirectory = cacheDirectory;
		}

		private static Resource getCacheDirectory(String url) {
			Resource cacheDirectory = cacheRootDirectory.getRealResource("mvn/cache/" + HashUtil.create64BitHashAsString(url, Character.MAX_RADIX) + "/");
			cacheDirectory.mkdirs();
			return cacheDirectory;
		}

		@Override
		public Object clone() {
			return duplicate();
		}

		public Repository duplicate() {
			return new Repository(label, url, timeoutList, timeoutDetail, cacheDirectory);
		}

		@Override
		public String toString() {
			return "label:" + label + ";url:" + url + ";timeoutList:" + timeoutList + ";timeoutDetail:" + timeoutDetail;
		}
	}

	private static void assertDownloadAllowed(String url) throws IOException {
		int policy = CFMLEngineImpl.getActiveDownloadPolicy();
		if (policy == CFMLEngineImpl.MAVEN_DOWNLOAD_POLICY_ERROR) {
			throw new IOException("Maven download is blocked by policy. Attempted to download [" + url + "]. "
					+ "To allow downloads, set the " + CFMLEngineImpl.getActiveDownloadPolicySetting() + " to 'warn' or 'ignore'. "
					+ "Alternatively, place the artifact manually in your local Maven repository (~/.m2/repository).");
		}
		else if (policy == CFMLEngineImpl.MAVEN_DOWNLOAD_POLICY_WARN) {
			LogUtil.log(CFMLEngineImpl.MAVEN_DOWNLOAD_POLICY_LOG_LEVEL, "maven", "Downloading Maven artifact from [" + url + "]. Maven download policy is set to 'warn'. "
					+ "Set the " + CFMLEngineImpl.getActiveDownloadPolicySetting() + " to 'error' to block downloads or 'ignore' to suppress this warning.");
		}
	}
}
