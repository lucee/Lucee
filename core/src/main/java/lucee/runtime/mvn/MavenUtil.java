package lucee.runtime.mvn;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import lucee.print;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SerializableObject;
import lucee.runtime.mvn.POMReader.Dependency;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.util.ListUtil;

public class MavenUtil {
	private static Map<String, String> sysprops;
	private static Object token = new SerializableObject();

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
			Repository defaultRepository) throws IOException {
		Map<String, Repository> repositories = new LinkedHashMap<>();
		repositories.put(defaultRepository.getUrl(), defaultRepository);
		if (parent != null) {
			Collection<Repository> reps = parent.getRepositories();
			if (reps != null) {
				for (Repository r: reps) {
					repositories.put(r.getUrl(), r); // TODO clone?
				}
			}
		}
		if (rawRepositories != null) {
			for (POMReader.Repository rep: rawRepositories) {
				Repository r = new Repository(

						resolvePlaceholders(current, rep.id, properties),

						resolvePlaceholders(current, rep.name, properties),

						resolvePlaceholders(current, rep.url, properties)

				);
				repositories.put(r.getUrl(), r);
			}
		}
		return repositories.values();

	}

	public static List<POM> getDependencies(List<POMReader.Dependency> rawDependencies, POM current, POM parent, Map<String, String> properties, Resource localDirectory,
			boolean management, Log log) throws IOException {
		List<POM> dependencies = new ArrayList<>();
		List<POM> parentDendencyManagement = null;

		ExecutorService executor = ThreadUtil.createExecutorService(Runtime.getRuntime().availableProcessors());

		if (parent != null) {
			parentDendencyManagement = current.getDependencyManagement();
			List<POM> tmp = parent.getDependencies();
			if (tmp != null) {
				for (POM pom: tmp) {
					dependencies.add(pom); // TODO clone?
				}
			}
		}
		if (rawDependencies != null) {
			List<Future<POM>> futures = new ArrayList<>();
			for (POMReader.Dependency rd: rawDependencies) {
				GAVSO gavso = getDependency(rd, parent, current, properties, parentDendencyManagement, management);
				if (gavso == null) continue;

				Future<POM> future = executor.submit(() -> {
					POM p = POM.getInstance(localDirectory, current.getRepositories(), gavso.g, gavso.a, gavso.v, gavso.s, gavso.o, current.getDependencyScope(),
							current.getDependencyScopeManagement(), log);
					p.initXML();
					return p;
				});
				futures.add(future);
			}
			try {
				for (Future<POM> future: futures) {
					dependencies.add(future.get()); // Wait for init to complete
				}
			}
			catch (Exception e) {
				throw ExceptionUtil.toIOException(e);
			}
		}
		executor.shutdown();
		return dependencies;
	}

	public static GAVSO getDependency(POMReader.Dependency rd, POM parent, POM current, Map<String, String> properties, List<POM> parentDendencyManagement, boolean management)
			throws IOException {
		POM pdm = null;// TODO move out of here so multiple loop elements can profit

		String g = resolvePlaceholders(current, rd.groupId, properties);
		String a = resolvePlaceholders(current, rd.artifactId, properties);

		// scope
		String s = rd.scope;
		if (s == null && parentDendencyManagement != null) {
			if (pdm == null) pdm = getDendency(parentDendencyManagement, g, a);
			if (pdm != null) {
				s = pdm.getScopeAsString();
			}
		}
		if (s != null) s = resolvePlaceholders(current, s, properties);

		// scope allowed?
		if (!allowed(management ? current.getDependencyScopeManagement() : current.getDependencyScope(), toScope(s, POM.SCOPE_COMPILE))) {
			return null;
		}

		// version
		String v = rd.version;
		if (v == null) {
			pdm = getDendency(parentDendencyManagement, g, a);

			if (pdm != null) {
				v = pdm.getVersion();
			}
			if (v == null) {
				throw new IOException("could not find version for dependency [" + g + ":" + a + "] in [" + current + "]");
			}
		}
		v = resolvePlaceholders(current, v, properties);
		// PATCH TODO better solution for this
		if (v != null && v.startsWith("[")) {
			v = v.substring(1, v.indexOf(','));
		}

		// optional
		String o = rd.optional;
		if (o == null && parentDendencyManagement != null) {
			if (pdm == null) pdm = getDendency(parentDendencyManagement, g, a);
			if (pdm != null) {
				o = pdm.getOptionalAsString();
			}
		}
		if (o != null) s = resolvePlaceholders(current, o, properties);
		return new GAVSO(g, a, v, s, o);
		// p = POM.getInstance(localDirectory, g, a, v, s, o, current.getDependencyScope(),
		// current.getDependencyScopeManagement());

		// dependencies.add(p);
	}

	static class GAVSO {
		public final String g;
		public final String a;
		public final String v;
		public final String s;
		public final String o;

		public GAVSO(String g, String a, String v) {
			this.g = g;
			this.a = a;
			this.v = v;
			this.s = null;
			this.o = null;
		}

		public GAVSO(String g, String a, String v, String s, String o) {
			this.g = g;
			this.a = a;
			this.v = v;
			this.s = s;
			this.o = o;
		}
	}

	public static boolean allowed(int allowedScopes, int scope) {
		return (allowedScopes & scope) != 0;
	}

	private static POM getDendency(List<POM> dependencies, String groupId, String artifactId) {
		if (dependencies != null) {
			for (POM pom: dependencies) {
				if (pom.getGroupId().equals(groupId) && pom.getArtifactId().equals(artifactId)) return pom;
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
				GAVSO gavso = getDependency(rd, parent, current, properties, null, true);
				if (gavso == null) continue;
				POM p = POM.getInstance(localDirectory, current.getRepositories(), gavso.g, gavso.a, gavso.v, gavso.s, gavso.o, current.getDependencyScope(),
						current.getDependencyScopeManagement(), log);
				dependencies.add(p);
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

				if ("groupId".equals(placeholder)) {
					value = pom.getGroupId();
					modifed = true;
				}
				else if ("artifactId".equals(placeholder)) {
					value = pom.getArtifactId();
					modifed = true;
				}
				else if ("version".equals(placeholder)) {
					value = pom.getVersion();
					modifed = true;
				}
				else if ("scope".equals(placeholder) && pom.getScopeUnresolved() != null) {
					value = pom.getScopeUnresolved();
					modifed = true;
				}
				else if ("optional".equals(placeholder)) {
					value = pom.getOptionaUnresolved();
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
			throw new IOException("cannot resolve [" + value + "] for [" + pom + "], available properties are [" + ListUtil.toList(properties.keySet(), ", ") + "]");
		}
		return value;
	}

	public static boolean hasPlaceholders(String str) {
		return str != null && str.indexOf("${") != -1;
	}

	public static void download(POM pom, Collection<Repository> repositories, String type, Log log) throws IOException {
		Resource res = pom.getArtifact(type);
		if (!res.isFile()) {
			URL url = pom.getArtifact(type, repositories);
			print.e("download:" + url);
			if (log != null) log.info("maven", "download [" + url + "]");
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				HttpGet request = new HttpGet(pom.getArtifact(type, repositories).toExternalForm());
				HttpResponse response = httpClient.execute(request);
				HttpEntity entity = response.getEntity();
				int sc = response.getStatusLine().getStatusCode();
				if (sc == 200) {
					if (entity != null) {
						try (InputStream is = entity.getContent()) {
							IOUtil.copy(is, res, false);
						}
					}
				}
				else {
					EntityUtils.consume(entity); // Ensure the response entity is fully consumed
					throw new IOException("Failed to download: " + url + " for [" + pom + "] - " + response.getStatusLine().getStatusCode());
				}
			}
		} // TODO handle not 200
	}

	public static POM toPOM(Resource localDirectory, Collection<Repository> repositories, POMReader.Dependency dependency, Map<String, String> properties, int dependencyScope,
			int dependencyScopeManagement, Log log) throws IOException {

		return POM.getInstance(localDirectory, repositories,

				resolvePlaceholders(null, dependency.groupId, properties),

				resolvePlaceholders(null, dependency.artifactId, properties),

				resolvePlaceholders(null, dependency.version, properties),

				null, null,

				dependencyScope, dependencyScopeManagement,

				log

		);
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

}
