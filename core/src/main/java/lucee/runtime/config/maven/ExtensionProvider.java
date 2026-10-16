package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xml.sax.SAXException;

import lucee.aprint;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.http.HTTPDownloader;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.mvn.MavenUtil.GAVSO;
import lucee.runtime.mvn.POM;
import lucee.runtime.op.Caster;
import lucee.runtime.tag.Http;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

public class ExtensionProvider {

	private static final String EXTENSION_EXTENSION = "lex";

	private static final int DOWNLOAD_CONNECT_TIMEOUT = 5000; // 5 seconds
	private static final int DOWNLOAD_READ_TIMEOUT = 60000; // 60 seconds
	private static final String DOWNLOAD_USER_AGENT = "Lucee Extension Provider 1.0";

	private static final Map<String, String> uuidMapping = new HashMap<>();
	static {
		uuidMapping.put("CED6227E-0F49-6367-A68D21AACA6B07E8", "administrator-extension");
		uuidMapping.put("6E2CB28F-98FB-4B51-B6BE6C64ADF35473", "ajax-extension");
		uuidMapping.put("7891D723-8F78-45F5-B7E333A22F8467CA", "argon2-extension");
		uuidMapping.put("58110B5E-E7CB-47AF-8E80D70DDD80C46F", "argus-monitor-extension");
		uuidMapping.put("DF28D0A4-6748-44B9-A2FDC12E4E2E4D38", "axis-extension");
		uuidMapping.put("8D7FB0DF-08BB-1589-FE3975678F07DB17", "compress-extension");
		uuidMapping.put("0F6E1F35-32A0-4B8C-B5A4BBA87EE621A8", "couchbase-extension");
		uuidMapping.put("17AB52DE-B300-A94B-E058FC978BE4542D", "crypto-extension");
		uuidMapping.put("1E12B23C-5B38-4764-8FF41B7FD9428468", "distrokid-extension");
		uuidMapping.put("D46D49C3-EB85-8D97-30BEC2F38561E985", "documentation-extension");
		uuidMapping.put("261114AC-7372-4CA8-BA7090895E01682D", "ec2-extension");
		uuidMapping.put("87FE44E5-179C-43A3-A87B3D38BEF4652E", "ehcache-extension");
		uuidMapping.put("37C61C0A-5D7E-4256-8572639BE0CF5838", "esapi-extension");
		uuidMapping.put("B737ABC4-D43F-4D91-8E8E973E37C40D1B", "image-extension");
		uuidMapping.put("E6634E1A-4CC5-4839-A83C67549ECA8D5B", "mongodb-extension");
		uuidMapping.put("FAD67145-E3AE-30F8-1C11A6CCF544F0B7", "form-extension");
		uuidMapping.put("1A1FA05C-CF89-4834-9BC71D617046A6A8", "git-extension");
		uuidMapping.put("FAD1E8CB-4F45-4184-86359145767C29DE", "hibernate-extension");
		uuidMapping.put("71BF38A8-6AC8-4704-8BC02C29893F56B3", "infinispan-extension");
		uuidMapping.put("DFE10517-14CE-4D8B-89A68091D9A6C81E", "athena-jdbc-extension");
		uuidMapping.put("F81ADA62-BB10-552D-9ACEE5D43F3FFC46", "derby-jdbc-extension");
		uuidMapping.put("811918E2-796C-4354-8374B1F331118AEB", "duckdb-extension");
		uuidMapping.put("0F5DEC68-DB34-42BB-A1C1B609175D7C57", "exasol-jdbc-extension");
		uuidMapping.put("465E1E35-2425-4F4E-8B3FAB638BD7280A", "h2-jdbc-extension");
		uuidMapping.put("6DD4728A-AB0C-4F67-9DCE1A91A8ACD114", "hsqldb-jdbc-extension");
		uuidMapping.put("2BCD080F-4E1E-48F5-BEFE794232A21AF6", "jtds-jdbc-extension");
		uuidMapping.put("99A4EF8D-F2FD-40C8-8FB8C2E67A4EEEB6", "mssql-jdbc-extension");
		uuidMapping.put("7E673D15-D87C-41A6-8B5F1956528C605F", "mysql-jdbc-extension");
		uuidMapping.put("D4EDFDBD-A9A3-E9AF-597322D767E0C949", "oracle-jdbc-extension");
		uuidMapping.put("671B01B8-B3B3-42B9-AC055A356BED5281", "postgresql-jdbc-extension");
		uuidMapping.put("337A9955-C0FA-848F-0B3F0AEBA155CA9B", "teradata-jdbc-extension");
		uuidMapping.put("FA79A831-7D30-4D8A-B7F300DECEB00001", "debugger-extension");
		uuidMapping.put("D46B46A9-A0E3-44E1-D972A04AC3A8DC10", "chart-extension");
		uuidMapping.put("A03F4335-BDEF-44DE-946FB16C47802F96", "json-extension");
		uuidMapping.put("7FA79F92-0018-47F5-8C80E6C608C573BE", "jsonata-extension");
		uuidMapping.put("D6700FE4-E168-4512-9B95E1AE7784A3A5", "kinesis-extension");
		uuidMapping.put("EAF0AAF1-E068-4BA7-B72FF3D8E730696C", "lsp-extension");
		uuidMapping.put("1C9A7C34-2555-4AAA-92FBB7FC7111140C", "loganalyzer-extension");
		uuidMapping.put("EFDEB172-F52E-4D84-9CD1A1F561B3DFC8", "lucene-search-extension");
		uuidMapping.put("16FF9B13-C595-4FA7-B87DED467B7E61A0", "memcached-extension");
		uuidMapping.put("3A2EFA5E-94BA-CB7B-1D8DF106CBF81AE4", "monitor-runningrequests-extension");
		uuidMapping.put("08C17A44-1AAE-41B1-8E31D8B6E3F30A28", "oauth-extension");
		uuidMapping.put("66E312DD-D083-27C0-64189D16753FD6F0", "pdf-extension");
		uuidMapping.put("64B91581-2F6D-4316-8F21279369EB6F82", "poi-extension");
		uuidMapping.put("E99E43A5-C10E-41E9-878BFC82BAAD99CE", "quartz-extension");
		uuidMapping.put("99614730-61EC-4F65-B78229B9555CDEFE", "querybuilder-extension");
		uuidMapping.put("60772C12-F179-D555-8E2CD2B4F7428718", "redis-extension");
		uuidMapping.put("17AB52DE-B300-A94B-E058BD978511E39E", "s3-extension");
		// uuidMapping.put("0B018E86-DC57-ACA6-C3AB0FCE9A1F510A", "scheduledtask-extension");
		uuidMapping.put("97EB5427-F051-4684-91EBA6DBB5C5203F", "scheduler-classic-extension");
		uuidMapping.put("16953C9D-0A26-4283-904AD851B30506AF", "aws-sm-extension");
		uuidMapping.put("83062C18-FA1F-4647-815BB663BCF98AC0", "sentry-extension");
		uuidMapping.put("287B6309-9D31-8865-EA453D209B13882B", "socket-server-extension");
		uuidMapping.put("947C02B0-7AE4-4054-938A8E059DD7625A", "tasks-extension");
		uuidMapping.put("3F9DFF32-B555-449D-B0EB5DB723044045", "websocket-extension");
		uuidMapping.put("058215B3-5544-4392-A187A1649EB5CA90", "websocket-client-extension");
		uuidMapping.put("212BA548-F15A-4EBD-8B1EEDF8DD8A844D", "mail-extension");
		uuidMapping.put("C09479BE-3309-47AB-BD2D89F9973D8A03", "ftp-extension");

	}

	private Repository[] repoSnapshots;
	private Repository[] repoReleases;
	private Repository[] repoMixed;
	private String group;
	private List<Repository> repos;

	public ExtensionProvider(Repository[] repoSnapshots, Repository[] repoReleases, Repository[] repoMixed, String group) {
		this.repoSnapshots = repoSnapshots;
		this.repoReleases = repoReleases;
		this.repoMixed = repoMixed;
		this.repos = MavenUpdateProvider.merge(repoSnapshots, repoReleases, repoMixed);
		this.group = group;
	}

	public ExtensionProvider(String group) {
		this.repoSnapshots = MavenUpdateProvider.getDefaultRepositorySnapshots();
		this.repoReleases = MavenUpdateProvider.getDefaultRepositoryReleases();
		this.repoMixed = MavenUpdateProvider.getDefaultRepositoryMixed();
		this.repos = MavenUpdateProvider.merge(repoSnapshots, repoReleases, repoMixed);
		this.group = group;
	}

	public ExtensionProvider() {
		this.repoSnapshots = MavenUpdateProvider.getDefaultRepositorySnapshots();
		this.repoReleases = MavenUpdateProvider.getDefaultRepositoryReleases();
		this.repoMixed = MavenUpdateProvider.getDefaultRepositoryMixed();
		this.repos = MavenUpdateProvider.merge(repoSnapshots, repoReleases, repoMixed);
		this.group = MavenUpdateProvider.DEFAULT_GROUP;
	}

	private ExtensionProvider disableCache() {
		// snap
		List<Repository> snap = new ArrayList<>();
		for (Repository r: repoSnapshots) {
			snap.add(new Repository(r.label, r.url, Repository.TIMEOUT_ZERO, Repository.TIMEOUT_ZERO, r.cacheDirectory));
		}
		// releases
		List<Repository> releases = new ArrayList<>();
		for (Repository r: repoReleases) {
			releases.add(new Repository(r.label, r.url, Repository.TIMEOUT_ZERO, Repository.TIMEOUT_ZERO, r.cacheDirectory));
		}
		// mixed
		List<Repository> mixed = new ArrayList<>();
		for (Repository r: repoMixed) {
			mixed.add(new Repository(r.label, r.url, Repository.TIMEOUT_ZERO, Repository.TIMEOUT_ZERO, r.cacheDirectory));
		}

		// TODO Auto-generated method stub
		return new ExtensionProvider(

				snap.toArray(new Repository[snap.size()]),

				releases.toArray(new Repository[releases.size()]),

				mixed.toArray(new Repository[mixed.size()]),

				group

		);
	}

	/*
	 * private Set<String> listAllProjectsOld() throws IOException, InterruptedException {
	 * HtmlDirectoryScraper scraper = new HtmlDirectoryScraper(); String strURL; Set<String> subfolders
	 * = new HashSet<>(), tmp;
	 * 
	 * for (Repository r: repos) { strURL = (r.url.endsWith("/") ? r.url : (r.url + "/")) +
	 * group.replace('.', '/') + "/"; tmp = readFromCache(r); if (tmp == null) { tmp = new HashSet<>();
	 * print.e("-->" + strURL); scraper.getSubfolderLinks(strURL, tmp); } copy(tmp, subfolders);
	 * storeToCache(r, tmp); } return subfolders; }
	 */

	private Set<String> listAllProjects() throws InterruptedException, IOException {
		Set<String> subfolders = new HashSet<>();
		List<Thread> threads = new ArrayList<>();
		Stack<Exception> exceptions = new Stack<Exception>();
		for (Repository r: repos) {
			Thread thread = ThreadUtil.getThread(() -> {
				try {
					String strURL = (r.url.endsWith("/") ? r.url : (r.url + "/")) + group.replace('.', '/') + "/";
					Set<String> tmp = readFromCache(r);
					if (tmp == null) {
						tmp = new HashSet<>();
						new HtmlDirectoryScraper().getSubfolderLinks(strURL, tmp);
					}
					copy(tmp, subfolders);
					storeToCache(r, tmp);
				}
				catch (InterruptedException e) {
					exceptions.add(e);
				}
				catch (IOException e) {
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
			if (e instanceof InterruptedException) throw (InterruptedException) e;
			throw ExceptionUtil.toIOException(e);
		}

		return subfolders;
	}

	public String getGroup() {
		return group;
	}

	public String toArtifact(String uuid) throws PageException {
		return toArtifact(uuid, true);
	}

	public String toArtifactSimple(String uuid) {
		return uuidMapping.get(uuid.toUpperCase().trim());
	}

	public String toArtifact(String uuid, boolean investigate) throws PageException {
		uuid = uuid.toUpperCase().trim();
		String artifact = uuidMapping.get(uuid);
		if (artifact != null) return artifact;
		if (investigate) {
			try {
				return extractArtifactByUUID(uuid);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		return null;
	}

	public String toArtifact(String uuid, boolean investigate, String defaultValue) {
		try {
			return toArtifact(uuid, investigate);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public ExtensionDefintion toExtensionDefintion(Config config, GAVSO gavso, boolean investigate) {
		String uuid = null;
		if (group.equals(MavenUpdateProvider.DEFAULT_GROUP)) {
			for (Entry<String, String> e: uuidMapping.entrySet()) {
				if (e.getValue().equals(gavso.a)) {
					uuid = e.getKey();
				}
			}
			if (uuid != null) {
				ExtensionDefintion ed = new ExtensionDefintion();
				ed.setId(uuid);
				ed.setGAVSO(gavso);

				return ed;
			}
		}

		if (investigate) {
			try {
				Version version;
				if (gavso.v == null) {
					version = last(gavso.a);
				}
				else {
					version = Version.parseVersion(gavso.v);
				}

				Resource res = getResource((ConfigPro) config, gavso.a, version);
				return RHExtension.getInstance(config, res, null).toExtensionDefinition();
			}
			catch (Exception e) {
				LogUtil.log("mvn", e);
			}

		}
		return null;
	}

	public ExtensionDefintion toExtensionDefintion(Config config, GAVSO gavso, boolean investigate, ExtensionDefintion defaultValue) {
		try {
			return toExtensionDefintion(config, gavso, investigate);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public List<String> list() throws InterruptedException, IOException {
		List<String> artifacts = new ArrayList<>();
		for (String artifact: listAllProjects()) {
			if (artifact.endsWith("-extension")) artifacts.add(artifact);
		}
		Collections.sort(artifacts);
		return artifacts;
	}

	private void storeToCache(Repository repository, Set<String> subfolders) {
		try {
			Resource resLastmod = repository.cacheDirectory.getRealResource("artifacts_" + HashUtil.create64BitHashAsString(group + "_lastmod", Character.MAX_RADIX));
			Resource resVersions = repository.cacheDirectory.getRealResource("artifacts_" + HashUtil.create64BitHashAsString(group + "_versions", Character.MAX_RADIX));
			StringBuilder sb = new StringBuilder();
			for (String subfolder: subfolders) {
				sb.append(subfolder).append(',');
			}

			IOUtil.write(resVersions, sb.length() == 0 ? "" : sb.toString().substring(0, sb.length() - 1), CharsetUtil.UTF8, false);
			IOUtil.write(resLastmod, Caster.toString(System.currentTimeMillis()), CharsetUtil.UTF8, false);
		}
		catch (Exception e) {
			LogUtil.log("MetadataReader", e);
		}
	}

	private Set<String> readFromCache(Repository repository) {
		try {
			Resource resLastmod = repository.cacheDirectory.getRealResource("artifacts_" + HashUtil.create64BitHashAsString(group + "_lastmod", Character.MAX_RADIX));
			if (resLastmod.isFile()) {
				long lastmod = repository.timeoutList == Repository.TIMEOUT_NEVER ? Repository.TIMEOUT_NEVER
						: Caster.toLongValue(IOUtil.toString(resLastmod, CharsetUtil.UTF8), 0L);
				if (repository.timeoutList == Repository.TIMEOUT_NEVER || lastmod + repository.timeoutList > System.currentTimeMillis()) {
					Resource resVersions = repository.cacheDirectory.getRealResource("artifacts_" + HashUtil.create64BitHashAsString(group + "_versions", Character.MAX_RADIX));
					String content = IOUtil.toString(resVersions, CharsetUtil.UTF8);
					Set<String> subfolders = new HashSet<>();
					if (content.length() > 0) {
						List<String> list = ListUtil.listToList(content, ',', true);
						for (String v: list) {
							subfolders.add(v.trim());
						}
					}
					return subfolders;
				}
			}
		}
		catch (Exception e) {
			LogUtil.log("MetadataReader", e);
		}
		return null;
	}

	private static void copy(Set<String> from, Set<String> to) {
		for (String s: from) {
			to.add(s);
		}
	}

	public List<Version> list(String artifact) throws IOException, GeneralSecurityException, SAXException, InterruptedException {
		MavenUpdateProvider mup = new MavenUpdateProvider(this.repoSnapshots, this.repoReleases, this.repoMixed, this.group, artifact);
		return mup.list();
	}

	public Version last(String artifact) throws IOException, GeneralSecurityException, SAXException, InterruptedException {
		Version last = null;
		Version lastRel = null;

		for (Version v: list(artifact)) {
			if (!v.toString().toUpperCase().endsWith("-SNAPSHOT")) {
				if (lastRel == null || Version.compare(lastRel, v) < 0) {
					lastRel = v;
				}
			}

			if (last == null || Version.compare(last, v) < 0) {
				last = v;
			}

		}

		return lastRel != null ? lastRel : last;
	}

	public Map<String, Object> detail(String artifact, Version version) throws PageException, IOException, GeneralSecurityException, SAXException {
		MavenUpdateProvider mup;
		mup = new MavenUpdateProvider(this.repoSnapshots, this.repoReleases, this.repoMixed, this.group, artifact);
		Map<String, Object> detail = mup.detail(version, EXTENSION_EXTENSION, false);

		if (detail != null) return detail;
		throw new ApplicationException("there is no endpoint for [" + this.group + ":" + artifact + ":" + version + "]");
	}

	public Resource getResource(ConfigPro config, String artifact, Version version) throws IOException, PageException, GeneralSecurityException, SAXException {
		Log log = LogUtil.getLog(config, "mvn", "application");
		Resource local;
		try {
			POM pom = POM.getInstance(config.getMavenDir(), this.group, artifact, version.toString(), POM.SCOPES_FOR_RUNTIME, log);
			local = pom.getArtifact("lex");
		}
		catch (Exception e) {
			// Lucee repo does not always follow the maven rules a 100%, so we simply check for the file itself
			local = POM.local(config.getMavenDir(), this.group, artifact, version.toString(), "lex");
		}

		if (!local.isFile()) {
			synchronized (SystemUtil.createToken("ExtensionProvider", "getPOM:" + group + ":" + artifact)) {
				if (!local.isFile()) {
					local.getParentResource().mkdirs();
					IOUtil.copy(get(artifact, version), local, true);
				}
			}
		}
		return local;
	}

	public Map<String, Object> detail(String artifact, Version version, Map<String, Object> defaultValue) {
		MavenUpdateProvider mup;
		Map<String, Object> detail = null;
		mup = new MavenUpdateProvider(this.repoSnapshots, this.repoReleases, this.repoMixed, this.group, artifact);
		try {
			detail = mup.detail(version, EXTENSION_EXTENSION, false);
			if (detail != null) return detail;
		}
		catch (Exception e) {}

		return defaultValue;
	}

	private InputStream get(String artifact, Version version) throws PageException, IOException, GeneralSecurityException, SAXException {
		Map<String, Object> detail = detail(artifact, version);
		if (detail != null) {
			URL url = HTTPUtil.toURL(Caster.toString(detail.get(EXTENSION_EXTENSION), null), Http.ENCODED_NO, null);
			if (url != null) {
				return HTTPDownloader.get(url, DOWNLOAD_CONNECT_TIMEOUT, DOWNLOAD_READ_TIMEOUT, DOWNLOAD_USER_AGENT);
			}
		}
		throw new ApplicationException("there is no [" + EXTENSION_EXTENSION + "] artifact for [" + this.group + ":" + artifact + ":" + version + "]");
	}

	private InputStream get(String artifact, Version version, InputStream defaultValue) {
		Map<String, Object> detail = detail(artifact, version, null);
		if (detail != null) {
			URL url = HTTPUtil.toURL(Caster.toString(detail.get(EXTENSION_EXTENSION), null), Http.ENCODED_NO, null);
			if (url != null) {
				try {
					return HTTPDownloader.get(url, DOWNLOAD_CONNECT_TIMEOUT, DOWNLOAD_READ_TIMEOUT, DOWNLOAD_USER_AGENT);
				}
				catch (Exception e) {}
			}
		}
		return defaultValue;
	}

	/**
	 * Extracts the UUID from a JAR file's manifest
	 * 
	 * @param jarInputStream The InputStream of the JAR file
	 * @return The UUID string from the manifest's "id" attribute, or null if not found
	 * @throws IOException If there's an error reading the JAR
	 */
	private String extractUUIDFromJar(InputStream lexInputStream) {
		try (ZipInputStream zipStream = new ZipInputStream(lexInputStream)) {
			ZipEntry entry;

			// Look for the META-INF/MANIFEST.MF entry
			while ((entry = zipStream.getNextEntry()) != null) {
				if ("META-INF/MANIFEST.MF".equals(entry.getName())) {
					// Found the manifest, read it
					Manifest manifest = new Manifest(zipStream);

					if (manifest != null) {
						Attributes attributes = manifest.getMainAttributes();
						String id = attributes.getValue("id");

						if (id != null) {
							// Remove quotes if present
							return id.replaceAll("^\"|\"$", "");
						}
					}
					break; // Found manifest, no need to continue
				}
				zipStream.closeEntry();
			}
		}
		catch (Exception e) {}

		return null;
	}

	private String extractArtifactByUUID(String uuid) throws IOException, InterruptedException, GeneralSecurityException, SAXException, PageException {
		List<String> artifactsList = list();
		List<Version> versions;
		Map<Version, Map<String, Object>> artifacts;
		for (String artifact: artifactsList) {
			// if (artifact.indexOf("mssqlww") == -1) continue;
			versions = list(artifact);

			if (!ArrayUtil.isEmpty(versions)) {
				artifacts = null;

				Version last = versions.get(versions.size() - 1);

				{
					InputStream is = get(artifact, last, null);
					if (is == null) continue;

					// Now use the separate method to extract UUID
					String _uuid = extractUUIDFromJar(is);
					if (_uuid != null) {
						uuidMapping.put(_uuid.toUpperCase(), artifact);
						if (uuid.equalsIgnoreCase(_uuid)) {
							return artifact;
						}
						continue;
					}
				}
			}
		}
		throw new ApplicationException("cannot find the maven artifact for the id [" + uuid + "]");
	}

	private void listOld(List<String> list, Map<String, Map<Version, Map<String, Object>>> map)
			throws IOException, InterruptedException, GeneralSecurityException, SAXException, PageException {
		MavenUpdateProvider mup;
		List<String> artifactsList = list();
		List<Version> versions;
		Map<Version, Map<String, Object>> artifacts;
		outer: for (String artifact: artifactsList) {

			// if (artifact.indexOf("extension") == -1) continue;
			mup = new MavenUpdateProvider(this.repoSnapshots, this.repoReleases, this.repoMixed, this.group, artifact);
			versions = mup.list();
			if (!ArrayUtil.isEmpty(versions)) {
				artifacts = null;

				for (Version v: versions) {
					// if (v.toString().indexOf("-SNAPSHOT") == 0) continue;
					Map<String, Object> data = mup.detail(v, EXTENSION_EXTENSION, false);

					if (data != null) {
						if (list != null) {
							list.add(artifact);
							continue outer;
						}

						if (artifacts == null) artifacts = new HashMap<>();
						artifacts.put(v, data);
					}
					else {
						break;
					}
				}
				if (artifacts != null) {
					map.put(artifact, artifacts);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// TODO remove
		ExtensionProvider ep = new ExtensionProvider(new Repository[] {}, new Repository[] {},
				new Repository[] { new Repository("Maven Release Repository", "https://cdn.lucee.org/", Repository.TIMEOUT_5SECONDS, Repository.TIMEOUT_5SECONDS) }, "org.lucee");
		ep = new ExtensionProvider();

		long start = System.currentTimeMillis();
		// org.lucee:h2-jdbc-extension:2.1.214.0001L
		// aprint.e(ep.list());
		// aprint.e("list-all-extensions:" + (System.currentTimeMillis() - start));
		{
			start = System.currentTimeMillis();
			List<Version> list = ep.list("image-extension");
			for (Version v: list) {
				aprint.e(ep.detail("image-extension", v));
			}

			aprint.e(list);
			aprint.e("extension-image:" + (System.currentTimeMillis() - start));
		}
		if (true) return;

		start = System.currentTimeMillis();
		aprint.e(ep.list("ehcache-extension"));
		aprint.e("ehcache-extension:" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		aprint.e(ep.list("lucene-search-extension"));
		aprint.e("lucene-search-extension:" + (System.currentTimeMillis() - start));

		{
			start = System.currentTimeMillis();
			Map<String, Object> detail = ep.detail("redis-extension", Version.parseVersion("3.0.0.56-SNAPSHOT"));
			aprint.e("detail:" + (System.currentTimeMillis() - start));
			aprint.e(detail);

			ep.get("redis-extension", Version.parseVersion("3.0.0.56-SNAPSHOT"));

		}
		if (true) return;

		start = System.currentTimeMillis();
		String art = ep.toArtifact("99A4EF8D-F2FD-40C8-8FB8C2E67A4EEEB6");
		aprint.e("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		aprint.e("art:" + (System.currentTimeMillis() - start));
		aprint.e(art);
		aprint.e("");

		start = System.currentTimeMillis();
		List<String> list = ep.list();
		aprint.e("list-artifacts:" + (System.currentTimeMillis() - start));
		aprint.e(list);

		{
			List<Version> versions = ep.list("axis-extension");
			aprint.e("list-versions:" + (System.currentTimeMillis() - start));
			aprint.e(versions);

			for (Version v: versions) {
				Map<String, Object> detail = ep.detail("axis-extension", v);
				aprint.e(v + ":");
				aprint.e(detail);
			}
		}

		start = System.currentTimeMillis();
		List<Version> versions = ep.list("mysql-jdbc-extension");
		aprint.e("list-versions:" + (System.currentTimeMillis() - start));
		aprint.e(versions);

		start = System.currentTimeMillis();
		Map<String, Object> detail = ep.detail("mssql-jdbc-extension", Version.parseVersion("6.5.4"));
		aprint.e("detail:" + (System.currentTimeMillis() - start));
		aprint.e(detail);

		start = System.currentTimeMillis();
		InputStream is = ep.get("mysql-jdbc-extension", versions.get(versions.size() - 1));
		byte[] bytes = IOUtil.toBytes(is);
		aprint.e("get:" + (System.currentTimeMillis() - start));
		aprint.e(bytes.length);

		// read all projects

		try {
			// List<String> subfolders = scraper.getSubfolderLinks(url);

			// System.out.println("Found " + subfolders.size() + " subfolders:");
			// for (String folder: subfolders) {
			// System.out.println(" " + folder);
			// }

		}
		catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		// MavenUpdateProvider mup = new MavenUpdateProvider(MavenUpdateProvider.DEFAULT_GROUP, "axis");

		// print.e(mup.list());
	}
}
