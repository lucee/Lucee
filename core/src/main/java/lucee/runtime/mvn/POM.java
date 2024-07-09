package lucee.runtime.mvn;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import lucee.print;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.tree.TreeNode;
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
	private String groupId;
	private String artifactId;
	private String version;
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

	private boolean debug = false;

	private Element root;

	public static POM getInstance(Resource localDirectory, String groupId, String version, String artifactId) {
		return getInstance(localDirectory, null, groupId, artifactId, version, null, null, SCOPE_NOT_TEST, SCOPE_ALL);
	}

	public static POM getInstance(Resource localDirectory, Collection<Repository> repositories, String groupId, String version, String artifactId) {
		return getInstance(localDirectory, repositories, groupId, artifactId, version, null, null, SCOPE_NOT_TEST, SCOPE_ALL);
	}

	public static POM getInstance(Resource localDirectory, String groupId, String artifactId, String version, int dependencyScope) {
		return getInstance(localDirectory, null, groupId, artifactId, version, null, null, dependencyScope, POM.SCOPE_ALL);
	}

	public static POM getInstance(Resource localDirectory, Collection<Repository> repositories, String groupId, String artifactId, String version, int dependencyScope) {
		return getInstance(localDirectory, repositories, groupId, artifactId, version, null, null, dependencyScope, POM.SCOPE_ALL);
	}

	public static POM getInstance(Resource localDirectory, Collection<Repository> repositories, String groupId, String artifactId, String version, int dependencyScope,
			int dependencyScopeManagement) {
		return getInstance(localDirectory, repositories, groupId, artifactId, version, null, null, dependencyScope, dependencyScopeManagement);
	}

	public static POM getInstance(Resource localDirectory, String groupId, String artifactId, String version, int dependencyScope, int dependencyScopeManagement) {
		return getInstance(localDirectory, null, groupId, artifactId, version, null, null, dependencyScope, dependencyScopeManagement);
	}

	public static POM getInstance(Resource localDirectory, Collection<Repository> repositories, String groupId, String artifactId, String version, String scope, String optional,
			int dependencyScope, int dependencyScopeManagement) {
		String id = toId(localDirectory, groupId, artifactId, version, scope, optional, dependencyScope, dependencyScopeManagement);
		POM pom = cache.get(id);
		if (pom != null) {
			return pom;
		}
		pom = new POM(localDirectory, repositories, groupId, artifactId, version, scope, optional, dependencyScope, dependencyScopeManagement);
		cache.put(id, pom);
		return pom;
	}

	private static String toId(Resource localDirectory, String groupId, String artifactId, String version, String scope, String optional, int dependencyScope,
			int dependencyScopeManagement) {
		// TODO Auto-generated method stub
		return localDirectory + ":" + groupId + ":" + artifactId + ":" + version + ":" + scope + ":" + optional + ":" + dependencyScope + ":" + dependencyScopeManagement;
	}

	private POM(Resource localDirectory, Collection<Repository> repositories, String groupId, String artifactId, String version, String scope, String optional, int dependencyScope,
			int dependencyScopeManagement) {
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

		cache.put(id(), this);
	}

	private void initXML() throws IOException {
		if (isInitXML) return;
		isInitXML = true;
		if (debug) print.e("xxxxxx initXML " + this + " xxxxxx");

		MavenUtil.downloadPOM(this, initRepositories);

		try (InputStream inputStream = getPath().getInputStream()) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(inputStream);
			this.root = doc.getDocumentElement();
			root.normalize();
		}
		catch (SAXException | ParserConfigurationException e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	private void initParent() throws IOException {
		if (isInitParent) return;
		isInitParent = true;
		if (debug) print.e("xxxxxx initParent " + this + " xxxxxx");
		initXML();

		Element elParent = MavenUtil.getElement(root, "parent", null);
		if (elParent != null) {
			// chicken egg, because there is no parent yet, this cannot use properties from parent
			this.parent = MavenUtil.toPOM(this.localDirectory, initRepositories, elParent, MavenUtil.getProperties(root, null), dependencyScope, dependencyScopeManagement);
			parent.init();
		}
	}

	private void initProperties() throws IOException {
		if (isInitProperties) return;
		isInitProperties = true;
		initParent();
		if (debug) print.e("xxxxxx initProperties " + this + " xxxxxx");
		properties = MavenUtil.getProperties(root, parent);

	}

	private void initRepositories() throws IOException {
		if (isInitRepositories) return;
		isInitRepositories = true;
		if (debug) print.e("xxxxxx initRepositories " + this + " xxxxxx");
		initProperties();
		childRepositories = MavenUtil.getRepositories(root, this, parent, properties, DEFAULT_REPOSITORY);
	}

	private void initDependencies() throws IOException {
		if (isInitDependencies) return;
		isInitDependencies = true;
		if (debug) print.e("xxxxxx initDependencies " + this + " xxxxxx");
		initProperties();
		if (dependencyScope > 0) dependencies = MavenUtil.getDependencies(root, this, parent, properties, localDirectory, false);
	}

	private void initDependencyManagement() throws IOException {
		if (isInitDependencyManagement) return;
		isInitDependencyManagement = true;
		if (debug) print.e("xxxxxx initDependencyManagement " + this + " xxxxxx");
		initProperties();

		if (dependencyScopeManagement > 0) dependencyManagement = MavenUtil.getDependencyManagement(root, this, parent, properties, localDirectory);

	}

	private void init() throws IOException {
		if (isInit) return;
		isInit = true;
		if (debug) print.e("xxxxxx init " + this + " xxxxxx");
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

		return Boolean.TRUE.equals(optional);
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
		print.e(repositories);

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
		List<POM> deps = pom.getDependencies();
		if (deps != null) {
			for (POM p: deps) {
				if (!node.addChild(p)) continue;
				if (recursive) getDependencies(p, recursive, level + 1, node);
			}
		}
		return node;
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
		List<POM> deps = pom.getDependencyManagement();
		if (deps != null) {
			for (POM p: deps) {
				if (!node.addChild(p)) continue;
				if (recursive) getDependencyManagement(p, recursive, level + 1, node);
			}
		}
		return node;
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
}
