package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
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
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServerPro;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.mvn.MavenUtil.GAVSO;
import lucee.runtime.mvn.POM;
import lucee.runtime.mvn.POMReader;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.tag.Http;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.util.ListUtil;

public class ExtensionProvider {

	private static final String EXTENSION_EXTENSION = "lex";

	private static final int DOWNLOAD_CONNECT_TIMEOUT = 5000; // 5 seconds
	private static final int DOWNLOAD_READ_TIMEOUT = 60000; // 60 seconds
	private static final String DOWNLOAD_USER_AGENT = "Lucee Extension Provider 1.0";

	// mapping for extensions on org.lucee
	private static final Map<String, GAVSO> uuidMapping = new HashMap<>();
	private static final Set<String> uuidNoSet = new HashSet<>();
	static {
		uuidMapping.put("CED6227E-0F49-6367-A68D21AACA6B07E8", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "administrator-extension", null));
		uuidMapping.put("6E2CB28F-98FB-4B51-B6BE6C64ADF35473", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "ajax-extension", null));
		uuidMapping.put("7891D723-8F78-45F5-B7E333A22F8467CA", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "argon2-extension", null));
		uuidMapping.put("58110B5E-E7CB-47AF-8E80D70DDD80C46F", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "argus-monitor-extension", null));
		uuidMapping.put("DFE10517-14CE-4D8B-89A68091D9A6C81E", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "athena-jdbc-extension", null));
		uuidMapping.put("16953C9D-0A26-4283-904AD851B30506AF", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "aws-sm-extension", null));
		uuidMapping.put("DF28D0A4-6748-44B9-A2FDC12E4E2E4D38", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "axis-extension", null));
		uuidMapping.put("D46B46A9-A0E3-44E1-D972A04AC3A8DC10", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "chart-extension", null));
		uuidMapping.put("8D7FB0DF-08BB-1589-FE3975678F07DB17", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "compress-extension", null));
		uuidMapping.put("0F6E1F35-32A0-4B8C-B5A4BBA87EE621A8", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "couchbase-extension", null));
		uuidMapping.put("F81ADA62-BB10-552D-9ACEE5D43F3FFC46", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "derby-jdbc-extension", null));
		uuidMapping.put("1E12B23C-5B38-4764-8FF41B7FD9428468", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "distrokid-extension", null));
		uuidMapping.put("D46D49C3-EB85-8D97-30BEC2F38561E985", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "documentation-extension", null));
		uuidMapping.put("811918E2-796C-4354-8374B1F331118AEB", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "duckdb-jdbc-extension", null));
		uuidMapping.put("E0ACA85A-22DB-48FF-B2D6CD89D5D1709F", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "dynamodb-extension", null));
		uuidMapping.put("261114AC-7372-4CA8-BA7090895E01682D", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "ec2-extension", null));
		uuidMapping.put("87FE44E5-179C-43A3-A87B3D38BEF4652E", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "ehcache-extension", null));
		uuidMapping.put("37C61C0A-5D7E-4256-8572639BE0CF5838", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "esapi-extension", null));
		uuidMapping.put("0F5DEC68-DB34-42BB-A1C1B609175D7C57", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "exasol-jdbc-extension", null));
		uuidMapping.put("FAD67145-E3AE-30F8-1C11A6CCF544F0B7", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "form-extension", null));
		uuidMapping.put("C09479BE-3309-47AB-BD2D89F9973D8A03", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "ftp-extension", null));
		uuidMapping.put("1A1FA05C-CF89-4834-9BC71D617046A6A8", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "git-extension", null));
		uuidMapping.put("465E1E35-2425-4F4E-8B3FAB638BD7280A", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "h2-jdbc-extension", null));
		uuidMapping.put("FAD1E8CB-4F45-4184-86359145767C29DE", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "hibernate-extension", null));
		uuidMapping.put("6DD4728A-AB0C-4F67-9DCE1A91A8ACD114", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "hsqldb-jdbc-extension", null));
		uuidMapping.put("B737ABC4-D43F-4D91-8E8E973E37C40D1B", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "image-extension", null));
		uuidMapping.put("71BF38A8-6AC8-4704-8BC02C29893F56B3", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "infinispan-extension", null));
		uuidMapping.put("A03F4335-BDEF-44DE-946FB16C47802F96", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "json-extension", null));
		uuidMapping.put("2BCD080F-4E1E-48F5-BEFE794232A21AF6", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "jtds-jdbc-extension", null));
		uuidMapping.put("D6700FE4-E168-4512-9B95E1AE7784A3A5", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "kinesis-extension", null));
		uuidMapping.put("1C9A7C34-2555-4AAA-92FBB7FC7111140C", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "loganalyzer-extension", null));
		uuidMapping.put("EAF0AAF1-E068-4BA7-B72FF3D8E730696C", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "lsp-extension", null));
		uuidMapping.put("EFDEB172-F52E-4D84-9CD1A1F561B3DFC8", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "lucene-search-extension", null));
		uuidMapping.put("212BA548-F15A-4EBD-8B1EEDF8DD8A844D", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "mail-extension", null));
		uuidMapping.put("16FF9B13-C595-4FA7-B87DED467B7E61A0", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "memcached-extension", null));
		uuidMapping.put("E6634E1A-4CC5-4839-A83C67549ECA8D5B", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "mongodb-extension", null));
		uuidMapping.put("3A2EFA5E-94BA-CB7B-1D8DF106CBF81AE4", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "monitor-runningrequests-extension", null));
		uuidMapping.put("99A4EF8D-F2FD-40C8-8FB8C2E67A4EEEB6", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "mssql-jdbc-extension", null));
		uuidMapping.put("7E673D15-D87C-41A6-8B5F1956528C605F", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "mysql-jdbc-extension", null));
		uuidMapping.put("08C17A44-1AAE-41B1-8E31D8B6E3F30A28", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "oauth-extension", null));
		uuidMapping.put("D4EDFDBD-A9A3-E9AF-597322D767E0C949", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "oracle-jdbc-extension", null));
		uuidMapping.put("66E312DD-D083-27C0-64189D16753FD6F0", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "pdf-extension", null));
		uuidMapping.put("64B91581-2F6D-4316-8F21279369EB6F82", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "poi-extension", null));
		uuidMapping.put("671B01B8-B3B3-42B9-AC055A356BED5281", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "postgresql-jdbc-extension", null));
		uuidMapping.put("E99E43A5-C10E-41E9-878BFC82BAAD99CE", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "quartz-extension", null));
		uuidMapping.put("99614730-61EC-4F65-B78229B9555CDEFE", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "querybuilder-extension", null));
		uuidMapping.put("60772C12-F179-D555-8E2CD2B4F7428718", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "redis-extension", null));
		uuidMapping.put("17AB52DE-B300-A94B-E058BD978511E39E", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "s3-extension", null));
		uuidMapping.put("745C310A-5C54-4782-ACC8A821D1FEAFEB", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "sap-jdbc-extension", null));
		uuidMapping.put("97EB5427-F051-4684-91EBA6DBB5C5203F", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "scheduler-classic-extension", null));
		uuidMapping.put("83062C18-FA1F-4647-815BB663BCF98AC0", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "sentry-extension", null));
		uuidMapping.put("287B6309-9D31-8865-EA453D209B13882B", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "socket-server-extension", null));
		uuidMapping.put("037A27FF-0B80-4CBA-B954BEBD790B460E", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "spreadsheet-extension", null));
		uuidMapping.put("947C02B0-7AE4-4054-938A8E059DD7625A", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "tasks-extension", null));
		uuidMapping.put("337A9955-C0FA-848F-0B3F0AEBA155CA9B", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "teradata-jdbc-extension", null));
		uuidMapping.put("058215B3-5544-4392-A187A1649EB5CA90", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "websocket-client-extension", null));
		uuidMapping.put("3F9DFF32-B555-449D-B0EB5DB723044045", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "websocket-extension", null));
		uuidMapping.put("FA79A831-7D30-4D8A-B7F300DECEB00001", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "debugger-extension", null));
		uuidMapping.put("A71D636B-D668-4D93-886F9A5D34A9343D", new GAVSO(MavenUpdateProvider.DEFAULT_GROUP, "yaml-extension", null));

		// uuidMapping.clear(); // for testing
	}

	private String group;
	private Collection<Repository> repos;

	private boolean hasExtracted;

	private static Map<String, ExtensionProvider> instances = new ConcurrentHashMap<>();

	private ExtensionProvider(Config config, String group, Repository... repositories) {
		this.repos = Arrays.asList(repositories);
		this.group = group;
	}

	public static List<ExtensionProvider> getExtensionProviders(ConfigPro config) {
		List<String> groupIds = config.getExtensionProvidersGroupIds();
		List<ExtensionProvider> rtn = new ArrayList<>(groupIds.size());
		for (String groupId: groupIds) {
			rtn.add(new ExtensionProvider(config, groupId));
		}
		return rtn;
	}

	private ExtensionProvider(Config config, String group) {
		ConfigServerPro cw = ThreadLocalPageContext.getConfigServer(config);
		Repository[] repoSnapshots = cw == null ? MavenUpdateProvider.DEFAULT_REPOSITORIES_SNAPSHOTS : cw.getMavenSnapshotRepository();
		Repository[] repoReleases = cw == null ? MavenUpdateProvider.DEFAULT_REPOSITORIES_RELEASES : cw.getMavenRepository();
		this.repos = MavenUpdateProvider.merge(repoSnapshots, repoReleases, MavenUpdateProvider.DEFAULT_REPOSITORIES_ALL);
		this.group = group;

	}

	public static ExtensionProvider getInstance(Config config, String groupId) {
		ExtensionProvider instance = instances.get(groupId);
		if (instance == null) {
			synchronized (SystemUtil.createToken("ExtensionProvider", groupId)) {
				instance = instances.get(groupId);
				if (instance == null) {
					instance = new ExtensionProvider(config, groupId);
					instances.put(groupId, instance);
				}
			}
		}
		return instance;
	}

	private ExtensionProvider disableCache(Config config) {
		// snap
		List<Repository> list = new ArrayList<>();
		for (Repository r: this.repos) {
			list.add(new Repository(r.label, r.url, r.type, Repository.TIMEOUT_ZERO, Repository.TIMEOUT_ZERO, r.cacheDirectory, r.extensionLister));
		}

		// TODO Auto-generated method stub
		return new ExtensionProvider(config, group, list.toArray(new Repository[list.size()]));
	}

	private Set<String> listAllProjects() throws InterruptedException, IOException {
		Set<String> subfolders = new HashSet<>();
		List<Thread> threads = new ArrayList<>();
		Stack<Exception> exceptions = new Stack<Exception>();
		for (Repository r: repos) {
			Thread thread = ThreadUtil.getThread(() -> {
				try {
					Set<String> tmp = readFromCache(r);
					if (tmp == null) {
						tmp = r.extensionLister.list(r, group);
					}
					copy(tmp, subfolders);
					storeToCache(r, tmp);
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

		// only escalate when every endpoint failed, otherwise the reachable repos still provide a result
		Exception toThrow = (exceptions.size() > 0 && exceptions.size() >= repos.size()) ? exceptions.pop() : null;

		// log the endpoint failures we swallow (i.e. all except the one we rethrow, if any)
		for (Exception e: exceptions) {
			LogUtil.log((Config) null, "extension-provider", e, Log.LEVEL_WARN, "mvn");
		}

		if (toThrow != null) {
			if (toThrow instanceof InterruptedException) throw (InterruptedException) toThrow;
			throw ExceptionUtil.toIOException(toThrow);
		}
		return subfolders;
	}

	public String getGroup() {
		return group;
	}

	private static GAVSO toGAVSOSimple(String uuid, GAVSO defaultValue) {
		uuid = uuid.toUpperCase().trim();
		// org.lucee mappings
		GAVSO gav = uuidMapping.get(uuid);
		if (gav != null) {
			return new GAVSO(gav.g, gav.a, null);
		}
		return defaultValue;
	}

	public static GAVSO toGAVSO(ConfigPro config, String uuid, boolean investigate, GAVSO defaultValue) {

		GAVSO gavso = toGAVSOSimple(uuid, null);
		if (gavso != null) return gavso;
		if (uuidNoSet.contains(uuid)) return defaultValue;

		if (investigate) {
			config = ThreadLocalPageContext.getConfigServer(config);
			for (String groupId: config.getExtensionProvidersGroupIds()) {
				try {
					ExtensionProvider ep = ExtensionProvider.getInstance(config, groupId);
					ep.extractUUIDs(config);
				}
				catch (Exception e) {}
			}
			// try again
			gavso = toGAVSOSimple(uuid, null);
			if (gavso != null) return gavso;
			uuidNoSet.add(uuid);
		}
		return defaultValue;
	}

	public ExtensionDefintion toExtensionDefintion(Config config, GAVSO gavso, boolean investigate) {
		String uuid = null;
		for (Entry<String, GAVSO> e: uuidMapping.entrySet()) {
			GAVSO val = e.getValue();
			if (val != null && val.g.equals(gavso.g) && val.a.equals(gavso.a)) {
				uuid = e.getKey();
			}
		}
		if (uuid != null) {
			ExtensionDefintion ed = new ExtensionDefintion();
			ed.setId(uuid);
			ed.setGAVSO(gavso);

			return ed;
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

				Resource res = getLEXResource((ConfigPro) config, gavso.a, version);
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
			if (!artifact.endsWith("-extension")) continue;
			try {
				if (last(artifact) != null) artifacts.add(artifact);
			}
			catch (Exception e) {
				// skip artifacts with no resolvable version on any configured repo
			}
		}
		Collections.sort(artifacts);
		return artifacts;
	}

	private void storeToCache(Repository repository, Set<String> subfolders) {
		if (repository.cacheDirectory == null) return;
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
		if (repository.cacheDirectory == null) return null;
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
		MavenUpdateProvider mup = new MavenUpdateProvider(this.repos, this.group, artifact);
		List<Version> list = mup.list();
		return list;
	}

	public Version last(String artifact) throws IOException, GeneralSecurityException, SAXException, InterruptedException {
		// try release repos first — no point hitting snapshot repos if we find a release
		List<Version> versions = MavenUpdateProvider.list(this.repos, this.group, artifact, MavenUpdateProvider.TYPE_RELEASE);
		Version lastRel = null;
		for (Version v: versions) {
			if (!v.is(Version.SNAPSHOT)) {
				if (lastRel == null || Version.compare(lastRel, v) < 0) {
					lastRel = v;
				}
			}
		}
		if (lastRel != null) return lastRel;

		// no release found — fall back to snapshot repos
		versions = MavenUpdateProvider.list(this.repos, this.group, artifact, MavenUpdateProvider.TYPE_SNAPSHOT);
		Version last = null;
		for (Version v: versions) {
			if (last == null || Version.compare(last, v) < 0) {
				last = v;
			}
		}
		return last;
	}

	public Map<String, Object> detail(String artifact, Version version) throws PageException, IOException, SAXException {
		MavenUpdateProvider mup;
		mup = new MavenUpdateProvider(this.repos, this.group, artifact);
		Map<String, Object> detail = mup.detail(version, EXTENSION_EXTENSION, false);

		if (detail != null) return detail;
		throw new ApplicationException("there is no endpoint for [" + this.group + ":" + artifact + ":" + version + "]");
	}

	public Resource getPOMResource(ConfigPro config, String artifact, Version version, Resource defaultValue) {
		try {
			return getPOMResource(config, artifact, version);
		}
		catch (Exception ex) {
			return defaultValue;
		}
	}

	public Resource getPOMResource(ConfigPro config, String artifact, Version version) throws IOException, PageException, SAXException {
		return getResource(config, artifact, "pom", version);
	}

	public Resource getLEXResource(ConfigPro config, String artifact, Version version, Resource defaultValue) {
		try {
			return getLEXResource(config, artifact, version);
		}
		catch (Exception ex) {
			return defaultValue;
		}
	}

	public Resource getLEXResource(ConfigPro config, String artifact, Version version) throws IOException, PageException, SAXException {
		return getResource(config, artifact, "lex", version);
	}

	private Resource getResource(ConfigPro config, String artifact, String ext, Version version) throws IOException, PageException, SAXException {
		Log log = LogUtil.getLog(config, "mvn", "application");
		Resource local;
		try {
			POM pom = POM.getInstance(config.getMavenDir(), this.group, artifact, version.toString(), POM.SCOPES_FOR_RUNTIME, log);
			local = pom.getArtifact(ext);
		}
		catch (Exception e) {
			// Lucee repo does not always follow the maven rules a 100%, so we simply check for the file itself
			local = POM.local(config.getMavenDir(), this.group, artifact, version.toString(), ext);
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
		mup = new MavenUpdateProvider(this.repos, this.group, artifact);
		try {
			detail = mup.detail(version, EXTENSION_EXTENSION, false);
			if (detail != null) return detail;
		}
		catch (Exception e) {}

		return defaultValue;
	}

	private InputStream get(String artifact, Version version) throws PageException, IOException, SAXException {
		Map<String, Object> detail = detail(artifact, version);
		if (detail != null) {
			URL url = HTTPUtil.toURL(Caster.toString(detail.get(EXTENSION_EXTENSION), null), Http.ENCODED_NO, null);
			if (url != null) {
				return HTTPEngine.get(url, DOWNLOAD_CONNECT_TIMEOUT, DOWNLOAD_READ_TIMEOUT, DOWNLOAD_USER_AGENT);
			}
		}
		throw new ApplicationException("there is no [" + EXTENSION_EXTENSION + "] artifact for [" + this.group + ":" + artifact + ":" + version + "]");
	}

	/**
	 * Extracts the UUID from a JAR file's manifest
	 * 
	 * @param log
	 * 
	 * @param jarInputStream The InputStream of the JAR file
	 * @return The UUID string from the manifest's "id" attribute, or null if not found
	 * @throws IOException If there's an error reading the JAR
	 */
	private String extractUUIDFromJar(Resource res, Log log) {
		try (ZipInputStream zipStream = new ZipInputStream(res.getInputStream())) {
			ZipEntry entry;

			// Look for the META-INF/MANIFEST.MF entry
			while ((entry = zipStream.getNextEntry()) != null) {
				if ("META-INF/MANIFEST.MF".equals(entry.getName())) {
					// Found the manifest, read it
					Manifest manifest = new Manifest(zipStream);

					if (manifest != null) {
						Attributes attributes = manifest.getMainAttributes();
						String uuid = attributes.getValue("id");

						if (uuid != null) {
							// Remove quotes if present
							uuid = uuid.replaceAll("^\"|\"$", "");
							if (Decision.isUUId(uuid)) {
								if (log != null) {
									log.info("extension-provider", "extracted id [" + uuid + "] from LEX file [" + res + "]");
								}
								return uuid;
							}
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

	private String extractUUIDFromPOM(Resource res, Log log) {
		try {
			POMReader reader = POMReader.getInstance(res);
			Map<String, String> props = reader.getProperties();
			if (props != null) {
				String uuid = props.get("id");
				if (Decision.isUUId(uuid)) {
					if (log != null) {
						log.info("extension-provider", "extracted id [" + uuid + "] from POM file [" + res + "]");
					}
					return uuid;
				}
			}
		}
		catch (Exception e) {}
		return null;
	}

	private static boolean has(String groupId, String artifactId) {
		for (GAVSO val: uuidMapping.values()) {
			if (val != null && val.g.equals(groupId) && val.a.equals(artifactId)) {
				return true;
			}
		}
		return false;
	}

	private void extractUUIDs(Config config) throws IOException, InterruptedException, GeneralSecurityException, SAXException, PageException {

		if (!hasExtracted) {
			synchronized (SystemUtil.createToken("ExtensionProvider", "extract" + group)) {
				if (!hasExtracted) {
					List<String> artifactsList = list();
					Log log = LogUtil.getLog(config, "mvn", "application");

					List<Thread> threads = new ArrayList<>();
					Stack<Exception> exceptions = new Stack<>();

					for (String artifact: artifactsList) {
						Thread thread = ThreadUtil.getThread(() -> {
							try {
								if (!has(group, artifact)) {
									if (LogUtil.doesInfo(log)) {
										log.info("extension-provider", "extracting UUID for " + group + ":" + artifact);
									}

									// Determine the last version
									Version last = last(artifact);
									if (last != null) {

										boolean hasUUID = false;
										// Check if we already have it or need to extract from POM
										Resource res = getPOMResource((ConfigPro) config, artifact, last, null);
										if (res != null) {
											String _uuid = extractUUIDFromPOM(res, log);
											if (_uuid != null) {
												uuidMapping.put(_uuid.toUpperCase(), new GAVSO(getGroup(), artifact, null));
												if (LogUtil.doesDebug(log)) {
													log.debug("extension-provider", "found UUID [" + _uuid + "] via POM for " + artifact);
												}
												hasUUID = true;
											}
										}

										if (!hasUUID) {
											res = getLEXResource((ConfigPro) config, artifact, last, null);
											if (res != null) {
												// Now use the separate method to extract UUID
												String _uuid = extractUUIDFromJar(res, log);
												if (_uuid != null) {
													uuidMapping.put(_uuid.toUpperCase(), new GAVSO(getGroup(), artifact, null));
													if (LogUtil.doesDebug(log)) {
														log.debug("extension-provider", "found UUID [" + _uuid + "] via LEX for " + artifact);
													}
												}
											}
										}
									}
								}
							}
							catch (Exception e) {
								exceptions.push(e);
							}

						}, false);

						thread.start();
						threads.add(thread);
					}

					// 2. Wait for all extractions to finish
					for (Thread t: threads) {
						t.join();
					}

					// 3. Handle Exceptions (Re-throw the first significant one found)
					if (!exceptions.isEmpty()) {
						Exception e = exceptions.pop();
						if (e instanceof PageException) throw (PageException) e;
						if (e instanceof SAXException) throw (SAXException) e;
						if (e instanceof GeneralSecurityException) throw (GeneralSecurityException) e;
						throw ExceptionUtil.toIOException(e);
					}
					hasExtracted = true;
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// TODO remove
		ExtensionProvider ep = new ExtensionProvider(null, "org.lucee", new Repository[] {
				new Repository("Maven Release Repository", "https://cdn.lucee.org/", MavenUpdateProvider.TYPE_ALL, Repository.TIMEOUT_5SECONDS, Repository.TIMEOUT_5SECONDS) });

		ep = new ExtensionProvider(null, "org.lucee", MavenUpdateProvider.REPOSITORY_MAVEN_CENTRAL_RELEASES);

		aprint.e(ep.list("yaml-extension"));
		aprint.e(ep.detail("yaml-extension", Version.parseVersion("2.5.2-BETA")));

		if (true) return;

		Resource localDirectory = SystemUtil.getTempDirectory().getRealResource("mvvvn");

		ResourceUtil.deleteContent(localDirectory, null);
		POM pom = POM.getInstance(localDirectory, "org.lucee", "redis-extension", ("4.0.1.1-SNAPSHOT"), null);
		aprint.e(pom.getArtifact("lex"));

		aprint.e(ep.list("redis-extension"));
		aprint.e(ep.detail("redis-extension", Version.parseVersion("4.0.1.1-SNAPSHOT")));
		// org.lucee:yaml-extension:2.5.2.SNAPSHOT

		if (true) return;

		long start = System.currentTimeMillis();
		// org.lucee:h2-jdbc-extension:2.1.214.0001L
		aprint.e(ep.list());
		aprint.e("list-all-extensions:" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		aprint.e(ep.list("ehcache-extension"));
		aprint.e("ehcache-extension:" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		aprint.e(ep.list("lucene-search-extension"));
		aprint.e("lucene-search-extension:" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		GAVSO gav = toGAVSOSimple("99A4EF8D-F2FD-40C8-8FB8C2E67A4EEEB6", null);
		aprint.e("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		aprint.e("art:" + (System.currentTimeMillis() - start));
		aprint.e(gav);
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
