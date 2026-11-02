package lucee.runtime.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.mvn.MavenUtil.MavenCoordinate;
import lucee.runtime.mvn.POMReader;
import lucee.runtime.mvn.POMReader.Dependency;
import lucee.runtime.mvn.POMReader.Repository;
import lucee.runtime.op.Caster;

/**
 * LDEV-5607 a pom file ({@code *.pom} or {@code pom.xml}) in the deploy folder installs the
 * dependencies it declares, the same way an extension in the deploy folder is installed. The
 * dependencies (and their transitive dependencies) are downloaded and added to the server wide
 * javasettings maven dependencies.
 * 
 * Not supported are parent poms, repositories declared in the pom (the maven configuration of Lucee
 * is used), and dependencies that are not needed at runtime (scope test, provided, system, import,
 * optional dependencies and dependencies that are not a jar).
 */
public final class DeployPom {

	private static final String DEPLOYED_DIR = "deployed-poms";

	private DeployPom() {
	}

	public static boolean isPom(String name) {
		if (name == null) return false;
		return StringUtil.endsWithIgnoreCase(name, ".pom") || "pom.xml".equalsIgnoreCase(name);
	}

	/**
	 * installs the dependencies declared in the pom and moves the pom to the folder of the deployed
	 * poms. If that fails, the pom is moved to the failed-to-deploy folder.
	 */
	public static void deploy(ConfigPro config, Resource pom, Log log) throws PageException {
		try {
			ConfigServer cs = ConfigUtil.getConfigServerImpl(config);
			List<MavenCoordinate> coordinates = toCoordinates(pom, log);

			if (coordinates.isEmpty()) {
				log.log(Log.LEVEL_WARN, "deploy handler", "the pom [" + pom.getName() + "] does not declare any dependency to install");
			}
			else {
				// download first, so a dependency that does not exist is never added to the configuration
				WarmupJavaSettings.download(cs, coordinates.toArray(new MavenCoordinate[coordinates.size()]));
				ConfigAdmin.updateMaven(config, coordinates);
				log.log(Log.LEVEL_INFO, "deploy handler", "installed [" + coordinates.size() + "] dependencies of the pom [" + pom.getName() + "]: " + toString(coordinates));
			}
			move(config, pom);
		}
		catch (Exception e) {
			DeployHandler.moveToFailedFolder(config.getDeployDirectory(), pom);
			throw Caster.toPageException(e);
		}
	}

	/**
	 * reads the dependencies that need to be installed from the pom.
	 */
	public static List<MavenCoordinate> toCoordinates(Resource pom, Log log) throws IOException {
		POMReader reader;
		try {
			reader = POMReader.parse(pom);
		}
		catch (SAXException e) {
			throw new IOException("the pom [" + pom.getName() + "] is not valid: " + e.getMessage(), e);
		}

		if (reader.getParent() != null) {
			log.log(Log.LEVEL_WARN, "deploy handler", "the parent pom of [" + pom.getName() + "] is ignored, versions and properties defined in the parent are not available");
		}
		List<Repository> repositories = reader.getRepositories();
		if (repositories != null && !repositories.isEmpty()) {
			log.log(Log.LEVEL_WARN, "deploy handler",
					"the repositories declared in the pom [" + pom.getName() + "] are ignored, the maven configuration of the server is used: " + repositories);
		}

		Map<String, String> properties = new HashMap<>(reader.getProperties());
		putIfAbsent(properties, "project.groupId", reader.getGroupId() != null ? reader.getGroupId() : reader.getParent() != null ? reader.getParent().groupId : null);
		putIfAbsent(properties, "project.artifactId", reader.getArtifactId());
		putIfAbsent(properties, "project.version", reader.getVersion() != null ? reader.getVersion() : reader.getParent() != null ? reader.getParent().version : null);

		// versions defined by dependencyManagement
		Map<String, String> managed = new HashMap<>();
		for (Dependency d: reader.getDependencyManagements()) {
			String version = resolve(d.version, properties);
			if (version != null && !version.contains("${")) managed.put(key(resolve(d.groupId, properties), resolve(d.artifactId, properties), resolve(d.classifier, properties)), version);
		}

		Map<String, MavenCoordinate> result = new LinkedHashMap<>();
		for (Dependency d: reader.getDependencies()) {
			String id = d.groupId + ":" + d.artifactId;
			String scope = StringUtil.isEmpty(d.scope, true) ? "compile" : d.scope.trim();
			if (!"compile".equalsIgnoreCase(scope) && !"runtime".equalsIgnoreCase(scope)) {
				log.log(Log.LEVEL_INFO, "deploy handler", "ignored the dependency [" + id + "] of [" + pom.getName() + "], the scope [" + scope + "] is not needed at runtime");
				continue;
			}
			if ("true".equalsIgnoreCase(StringUtil.trim(d.optional, ""))) {
				log.log(Log.LEVEL_INFO, "deploy handler", "ignored the optional dependency [" + id + "] of [" + pom.getName() + "]");
				continue;
			}
			String type = StringUtil.isEmpty(d.type, true) ? "jar" : d.type.trim();
			if (!"jar".equalsIgnoreCase(type) && !"bundle".equalsIgnoreCase(type)) {
				log.log(Log.LEVEL_INFO, "deploy handler", "ignored the dependency [" + id + "] of [" + pom.getName() + "], the type [" + type + "] is not a jar");
				continue;
			}

			String groupId = resolve(d.groupId, properties);
			String artifactId = resolve(d.artifactId, properties);
			String classifier = resolve(d.classifier, properties);
			String version = resolve(d.version, properties);
			if (StringUtil.isEmpty(version, true)) version = managed.get(key(groupId, artifactId, classifier));

			String label = "the dependency [" + id + "] of [" + pom.getName() + "]";
			check(groupId, "groupId", label);
			check(artifactId, "artifactId", label);
			if (StringUtil.isEmpty(version, true)) {
				throw new IOException(label + " has no version, declare it in the dependency or in dependencyManagement");
			}
			check(version, "version", label);
			if (!StringUtil.isEmpty(classifier, true)) check(classifier, "classifier", label);

			MavenCoordinate mc = new MavenCoordinate(groupId.trim(), artifactId.trim(), version.trim(), classifier);
			result.put(mc.toString(), mc);
		}
		return new ArrayList<>(result.values());
	}

	private static void check(String value, String name, String label) throws IOException {
		if (StringUtil.isEmpty(value, true)) throw new IOException(label + " has no " + name);
		if (value.contains("${")) throw new IOException(label + " has an unresolved placeholder in the " + name + " [" + value + "]");
	}

	private static String key(String groupId, String artifactId, String classifier) {
		return groupId + ":" + artifactId + ":" + (classifier == null ? "" : classifier);
	}

	private static void putIfAbsent(Map<String, String> map, String key, String value) {
		if (!StringUtil.isEmpty(value, true) && !map.containsKey(key)) map.put(key, value.trim());
	}

	private static String resolve(String value, Map<String, String> properties) {
		if (value == null) return null;
		// a property can refer to another property, the depth is limited to stop circular definitions
		for (int i = 0; i < 10 && value.contains("${"); i++) {
			String prev = value;
			for (Map.Entry<String, String> e: properties.entrySet()) {
				value = StringUtil.replace(value, "${" + e.getKey() + "}", e.getValue(), false);
			}
			if (prev.equals(value)) break;
		}
		return value;
	}

	private static String toString(List<MavenCoordinate> coordinates) {
		StringBuilder sb = new StringBuilder();
		for (MavenCoordinate mc: coordinates) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(mc);
		}
		return sb.toString();
	}

	private static void move(ConfigPro config, Resource pom) throws IOException {
		Resource dir = ConfigUtil.getConfigServerImpl(config).getConfigDir().getRealResource(DEPLOYED_DIR);
		dir.mkdirs();
		Resource dst = dir.getRealResource(pom.getName());
		if (dst.exists()) dst.remove(true);
		ResourceUtil.moveTo(pom, dst, true);
	}
}
