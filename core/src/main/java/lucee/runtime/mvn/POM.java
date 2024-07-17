package lucee.runtime.mvn;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SerializableObject;
import lucee.commons.tree.TreeNode;
import lucee.runtime.mvn.POMReader.Dependency;
import lucee.runtime.op.Caster;

public class POM {

	public static final Repository DEFAULT_REPOSITORY = new Repository("maven-central", "Maven Central", "https://repo1.maven.org/maven2/");

	public static final int SCOPE_COMPILE = 1;
	public static final int SCOPE_TEST = 2;
	public static final int SCOPE_PROVIDED = 4;
	public static final int SCOPE_RUNTIME = 8;
	public static final int SCOPE_SYSTEM = 16;
	public static final int SCOPE_IMPORT = 32;

	public static final int SCOPE_NONE = 0;
	public static final int SCOPE_NOT_TEST = SCOPE_COMPILE + SCOPE_PROVIDED + SCOPE_RUNTIME + SCOPE_SYSTEM + SCOPE_IMPORT;
	public static final int SCOPE_ALL = SCOPE_NOT_TEST + SCOPE_TEST;

	private Resource localDirectory;
	private final String groupId;
	private final String artifactId;
	private final String version;
	private String scope;
	private String optional;
	private int dependencyScope = SCOPE_ALL;
	private int dependencyScopeManagement = SCOPE_ALL;

	private List<POM> dependencies;
	private List<POM> dependencyManagement;
	private Collection<Repository> initRepositories;
	private Collection<Repository> childRepositories;

	private Map<String, String> properties;
	private POM parent;
	private boolean isInit = false;
	private boolean isInitParent = false;
	private boolean isInitRepositories = false;
	private boolean isInitProperties = false;
	private boolean isInitDependencies = false;
	private boolean isInitDependencyManagement = false;
	private boolean isInitXML = false;
	public static final Map<String, POM> cache = new HashMap<>();

	private String packaging;
	private String name;
	private String description;
	private String url;

	private Object token = new SerializableObject();

	private POMReader reader;

	private Log log;

	private String artifactExtension;

	private String hash;

	public static POM getInstance(Resource localDirectory, String groupId, String artifactId, String version, Log log) {
		return getInstance(localDirectory, null, groupId, artifactId, version, null, null, SCOPE_NOT_TEST, SCOPE_ALL, log);
	}

	public static POM getInstance(Resource localDirectory, Collection<Repository> repositories, String groupId, String artifactId, String version, Log log) {
		return getInstance(localDirectory, repositories, groupId, artifactId, version, null, null, SCOPE_NOT_TEST, SCOPE_ALL, log);
	}

	public static POM getInstance(Resource localDirectory, String groupId, String artifactId, String version, int dependencyScope, Log log) {
		return getInstance(localDirectory, null, groupId, artifactId, version, null, null, dependencyScope, POM.SCOPE_ALL, log);
	}

	public static POM getInstance(Resource localDirectory, Collection<Repository> repositories, String groupId, String artifactId, String version, int dependencyScope, Log log) {
		return getInstance(localDirectory, repositories, groupId, artifactId, version, null, null, dependencyScope, POM.SCOPE_ALL, log);
	}

	public static POM getInstance(Resource localDirectory, Collection<Repository> repositories, String groupId, String artifactId, String version, int dependencyScope,
			int dependencyScopeManagement, Log log) {
		return getInstance(localDirectory, repositories, groupId, artifactId, version, null, null, dependencyScope, dependencyScopeManagement, log);
	}

	public static POM getInstance(Resource localDirectory, String groupId, String artifactId, String version, int dependencyScope, int dependencyScopeManagement, Log log) {
		return getInstance(localDirectory, null, groupId, artifactId, version, null, null, dependencyScope, dependencyScopeManagement, log);
	}

	public static POM getInstance(Resource localDirectory, Collection<Repository> repositories, String groupId, String artifactId, String version, String scope, String optional,
			int dependencyScope, int dependencyScopeManagement, Log log) {
		String id = toId(localDirectory, groupId, artifactId, version, scope, optional, dependencyScope, dependencyScopeManagement);
		POM pom = cache.get(id);
		if (pom != null) {
			return pom;
		}

		pom = new POM(localDirectory, repositories, groupId, artifactId, version, scope, optional, dependencyScope, dependencyScopeManagement, log);
		cache.put(id, pom);
		return pom;
	}

	private static String toId(Resource localDirectory, String groupId, String artifactId, String version, String scope, String optional, int dependencyScope,
			int dependencyScopeManagement) {
		// TODO Auto-generated method stub
		return localDirectory + ":" + groupId + ":" + artifactId + ":" + version + ":" + scope + ":" + optional + ":" + dependencyScope + ":" + dependencyScopeManagement;
	}

	private POM(Resource localDirectory, Collection<Repository> repositories, String groupId, String artifactId, String version, String scope, String optional, int dependencyScope,
			int dependencyScopeManagement, Log log) {
		if (groupId == null) throw new IllegalArgumentException("groupId cannot be null");
		if (artifactId == null) throw new IllegalArgumentException("artifactId cannot be null");
		if (version == null) throw new IllegalArgumentException("version cannot be null");

		this.localDirectory = localDirectory;

		if (repositories == null) {
			this.initRepositories = new ArrayList<>();
			this.initRepositories.add(DEFAULT_REPOSITORY);
		}
		else this.initRepositories = repositories;
		this.groupId = groupId.trim();
		this.artifactId = artifactId.trim();
		this.version = version == null ? null : version.trim();
		this.scope = scope == null ? null : scope.trim();
		this.optional = optional == null ? null : optional.trim();
		this.dependencyScopeManagement = dependencyScopeManagement;
		this.dependencyScope = dependencyScope;
		this.log = log;

		cache.put(id(), this);
	}

	void initXML() throws IOException {
		if (!isInitXML) {
			synchronized (token) {
				if (!isInitXML) {
					MavenUtil.download(this, initRepositories, "pom", log);
					reader = new POMReader(getPath());
					try {
						reader.read();
					}
					catch (SAXException e) {
						IOException cause = ExceptionUtil.toIOException(e);
						IOException ioe = new IOException("failed to load pom file [" + getArtifact("pom", initRepositories) + "]");
						ExceptionUtil.initCauseEL(ioe, cause);
						throw ioe;
					}
					this.packaging = reader.getPackaging();
					this.artifactExtension = this.packaging;
					if (artifactExtension == null || "bundle".equalsIgnoreCase(artifactExtension)) this.artifactExtension = "jar";
					this.name = reader.getName();
					this.description = reader.getDescription();
					this.url = reader.getURL();

					if (this.artifactExtension != null && !"pom".equalsIgnoreCase(this.artifactExtension)) MavenUtil.download(this, initRepositories, artifactExtension, log);

					isInitXML = true;
				}
			}
		}
	}

	private void initParent() throws IOException {
		if (isInitParent) return;
		isInitParent = true;
		if (log != null) log.debug("maven", "int parent for " + this);
		initXML();

		Dependency p = reader.getParent();
		if (p != null) {
			// chicken egg, because there is no parent yet, this cannot use properties from parent
			this.parent = MavenUtil.toPOM(this.localDirectory, initRepositories, p, reader.getProperties(), dependencyScope, dependencyScopeManagement, log);
			parent.init();
		}
	}

	private void initProperties() throws IOException {
		if (isInitProperties) return;
		isInitProperties = true;
		initParent();
		if (log != null) log.debug("maven", "int properties for " + this);
		properties = MavenUtil.getProperties(reader.getProperties(), parent);

	}

	private void initRepositories() throws IOException {
		if (isInitRepositories) return;
		isInitRepositories = true;
		if (log != null) log.debug("maven", "int repositories for " + this);
		initProperties();
		childRepositories = MavenUtil.getRepositories(reader.getRepositories(), this, parent, properties, DEFAULT_REPOSITORY);
	}

	private void initDependencies() throws IOException {
		if (isInitDependencies) return;
		isInitDependencies = true;
		if (log != null) log.debug("maven", "int dependencies for " + this);
		initProperties();
		if (dependencyScope > 0) dependencies = MavenUtil.getDependencies(reader.getDependencies(), this, parent, properties, localDirectory, false, log);
	}

	private void initDependencyManagement() throws IOException {
		if (isInitDependencyManagement) return;
		isInitDependencyManagement = true;
		if (log != null) log.debug("maven", "int dependencx management for " + this);
		initProperties();

		if (dependencyScopeManagement > 0)
			dependencyManagement = MavenUtil.getDependencyManagement(reader.getDependencyManagements(), this, parent, properties, localDirectory, log);

	}

	private void init() throws IOException {
		if (isInit) return;
		isInit = true;
		if (log != null) log.debug("maven", "int for " + this);
		initProperties();
		initRepositories();
		initDependencyManagement();
		initDependencies();
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public String getPackaging() throws IOException {
		initXML();
		return this.packaging == null ? "jar" : this.packaging;
	}

	public String getName() throws IOException {
		initXML();
		return this.name;
	}

	public String getDescription() throws IOException {
		initXML();
		return this.description;
	}

	public String getURL() throws IOException {
		initXML();
		return this.url;
	}

	public int getDependencyScopeManagement() {
		return dependencyScopeManagement;
	}

	public int getDependencyScope() {
		return dependencyScope;
	}

	public String getScopeUnresolved() {
		return scope;
	}

	public String getScopeAsString() throws IOException {
		initProperties();
		return scope;
	}

	public int getScope() throws IOException {
		initProperties();
		return MavenUtil.toScope(getScopeAsString(), SCOPE_COMPILE);
	}

	public String getOptionaUnresolved() {
		return optional;
	}

	public String getOptionalAsString() throws IOException {
		initProperties();
		return optional;
	}

	public boolean getOptional() throws IOException {
		initProperties();
		return Boolean.TRUE.equals(Caster.toBoolean(optional, null));
	}

	public Resource getPath() {
		return local(localDirectory, "pom");
	}

	private StringBuilder _hash(StringBuilder sb) throws IOException {
		List<POM> deps = getDependencies();
		if (deps != null) {
			for (POM p: deps) {
				p._hash(sb);
			}
		}
		sb.append(groupId).append(';').append(artifactId).append(';').append(version);
		return sb;
	}

	public String hash() throws IOException {
		if (hash == null) {
			synchronized (groupId) {
				if (hash == null) {
					hash = HashUtil.create64BitHashAsString(_hash(new StringBuilder()));
				}
			}
		}
		return hash;
	}

	Resource getArtifact(String type) {
		return local(localDirectory, type);
	}

	public Resource getArtifact() throws IOException {
		initXML();
		if (artifactExtension == null) return null;
		return local(localDirectory, artifactExtension);
	}

	public boolean isInit() {
		return isInit;
	}

	public POM getParent() throws IOException {
		initParent();
		return parent;
	}

	public Map<String, String> getProperties() throws IOException {
		initProperties();
		return properties;
	}

	public List<POM> getDependencies() throws IOException {
		initDependencies();
		return dependencies;
	}

	public List<POM> getDependencyManagement() throws IOException {
		initDependencyManagement();
		return dependencyManagement;
	}

	public Collection<Repository> getRepositories() throws IOException {
		initRepositories();
		return childRepositories;
	}

	public Resource getLocalDirectory() {
		return localDirectory;
	}

	public String id() {
		return groupId + ":" + artifactId + ":" + version;
	}

	public boolean isOptional() {
		return Boolean.TRUE.equals(Caster.toBoolean(optional, null));
	}

	private Resource local(Resource dir, String extension) {
		Resource parent = dir.getRealResource(groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/");
		if (!parent.isDirectory()) parent.mkdirs();
		return parent.getRealResource(artifactId + "-" + version + "." + extension);
	}

	public URL getArtifact(String type, Collection<Repository> repositories) throws IOException {
		// TODO type check
		StringBuilder sb = null;
		URL url;
		if (repositories == null || repositories.isEmpty()) repositories = getRepositories();
		for (Repository r: repositories) {
			url = new URL(r.getUrl() + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + "." + type);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			if (responseCode == 200) {
				return url;
			}
			if (sb == null) sb = new StringBuilder();
			else sb.append(", ");
			sb.append(url.toExternalForm());
		}
		throw new IOException("could not find a valid endpoint for [" + this + "], possibles endpoint are [" + sb + "]");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		// sb.append("level:").append(level);
		sb.append("groupID:").append(groupId);
		sb.append(";artifactId:").append(artifactId);
		if (version != null) sb.append(";version:").append(version);
		if (scope != null) sb.append(";scope:").append(scope);
		if (optional != null) sb.append(";optional:").append(optional);
		return sb.toString();
	}

	/*
	 * ===========================================================================================
	 * ================================== HELPER METHODS =========================================
	 * ===========================================================================================
	 */
	public List<TreeNode<POM>> getAllDependenciesAsTrees() throws IOException {
		return getDependencies(this, true, 0, new TreeNode<POM>(this)).getChildren();
	}

	public List<POM> getAllDependencies() throws IOException {
		List<POM> list = getDependencies(this, true, 0, new TreeNode<POM>(this)).asList();
		list.remove(0);
		return list;
	}

	private static TreeNode<POM> getDependencies(POM pom, boolean recursive, int level, TreeNode<POM> node) throws IOException {
		try {
			List<POM> deps = pom.getDependencies();
			if (deps != null) {
				for (POM p: deps) {
					try {
						if (!node.addChild(p)) continue;
						if (recursive) getDependencies(p, recursive, level + 1, node);
					}
					catch (IOException ioe) {
						node.removeChild(p);
						// if optional we let it go
						if (!p.isOptional()) throw ioe;
					}
				}
			}
			return node;
		}
		catch (IOException cause) {
			IOException e = new IOException("failed to load dependencies in [" + pom + "]");
			ExceptionUtil.initCauseEL(e, cause);
			throw e;
		}
	}

	public List<TreeNode<POM>> getAllDependencyManagementAsTrees() throws IOException {
		return getDependencyManagement(this, true, 0, new TreeNode<POM>(this)).getChildren();
	}

	public List<POM> getAllDependencyManagement() throws IOException {
		List<POM> list = getDependencyManagement(this, true, 0, new TreeNode<POM>(this)).asList();
		list.remove(0);
		return list;
	}

	private static TreeNode<POM> getDependencyManagement(POM pom, boolean recursive, int level, TreeNode<POM> node) throws IOException {
		try {
			List<POM> deps = pom.getDependencyManagement();
			if (deps != null) {
				for (POM p: deps) {
					try {
						if (!node.addChild(p)) continue;
						if (recursive) getDependencyManagement(p, recursive, level + 1, node);
					}
					catch (IOException ioe) {
						node.removeChild(p);
						// if (!p.isOptional()) throw ioe;
					}
				}
			}
			return node;
		}
		catch (IOException cause) {
			IOException e = new IOException("failed to load dependency management in [" + pom + "]");
			ExceptionUtil.initCauseEL(e, cause);
			throw e;
		}
	}

	public TreeNode<POM> getAllParentsAsTree() throws IOException {
		return getParents(this, null);
	}

	public List<POM> getAllParents() throws IOException {
		TreeNode<POM> parents = getParents(this, null);
		if (parents == null) return new ArrayList<POM>();
		return parents.asList();
	}

	private static TreeNode<POM> getParents(POM pom, TreeNode<POM> parents) throws IOException {
		if (pom != null) {
			POM parent = pom.getParent();
			if (parent != null) {
				if (parents == null) parents = new TreeNode<POM>(parent);
				else {
					if (!parents.addChild(parent)) return parents;
				}
				getParents(parent, parents);
			}
		}
		return parents;
	}

	public Resource[] getJars() throws IOException {
		List<Resource> jars = new ArrayList<>();
		initXML();
		// current
		if ("jar".equalsIgnoreCase(this.artifactExtension)) {
			Resource r = getArtifact();
			if (r != null) {
				jars.add(r);
			}
		}

		List<POM> dependencies = getAllDependencies();
		if (dependencies != null) {
			for (POM p: dependencies) {
				if ("jar".equalsIgnoreCase(p.artifactExtension)) {
					Resource r = p.getArtifact();
					if (r != null) {
						jars.add(r);
					}
				}
			}
		}
		return jars.toArray(new Resource[jars.size()]);
	}
}
