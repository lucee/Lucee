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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.print;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.runtime.type.util.ListUtil;

public class MavenUtil {
	private static Map<String, String> sysprops;
	private static Object token = new SerializableObject();

	public static Map<String, String> getProperties(Element root, POM parent) throws IOException {
		Map<String, String> properties = parent != null ? parent.getProperties() : new LinkedHashMap<>();

		Element elProperties = getElement(root, "properties", null);
		NodeList nodeList = null;
		int size = properties.size();
		if (elProperties != null) {
			nodeList = elProperties.getChildNodes();
			size += nodeList.getLength();
		}

		Map<String, String> newProperties = new HashMap<>(size);

		// copy data from parent
		for (Entry<String, String> e: properties.entrySet()) {
			newProperties.put(e.getKey(), e.getValue());
		}

		// add new data

		if (nodeList != null && nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node propertyNode = nodeList.item(i);
				if (propertyNode instanceof Element) {
					// print.e(getValue(root, "groupId", null) + ":" + getValue(root, "artifactId", null) + " ---->" +
					// propertyNode.getNodeName().trim());
					newProperties.put(propertyNode.getNodeName().trim(), propertyNode.getTextContent().trim());
				}
			}
		}
		return newProperties;
	}

	public static Map<String, String> toMap(NodeList nodeList) {
		Map<String, String> newProperties = new HashMap<>(nodeList.getLength());
		if (nodeList.getLength() > 0) {
			Element el = (Element) nodeList.item(0);
			NodeList propertyNodes = el.getChildNodes();
			for (int i = 0; i < propertyNodes.getLength(); i++) {
				Node propertyNode = propertyNodes.item(i);
				if (propertyNode instanceof Element) {
					newProperties.put(propertyNode.getNodeName(), propertyNode.getTextContent());
				}
			}
		}
		return newProperties;
	}

	public static String parent(Element rootElement) {
		// parent
		Element elParent = getElement(rootElement, "parent", null);
		if (elParent != null) {
			NodeList parentNodes = elParent.getChildNodes();
			if (parentNodes != null && parentNodes.getLength() > 0) {
				Element parentElement = (Element) parentNodes.item(0);
				return getValue(parentElement, "version", null);
			}
		}
		return null;
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

	public static String getValue(Element el, String name, String childName, String defaultValue) {
		if (el == null) return defaultValue;

		NodeList nodes = el.getChildNodes();
		int len = nodes.getLength();
		Node n;
		for (int i = 0; i < len; i++) {
			n = nodes.item(i);
			if (name.equals(n.getNodeName()) && n instanceof Element) {
				return getValue((Element) n, childName, defaultValue);
			}
		}
		return defaultValue;
	}

	public static String getValue(Element el, String name, String defaultValue) {
		if (el == null) return defaultValue;

		NodeList nodes = el.getChildNodes();
		int len = nodes.getLength();
		Node n;
		for (int i = 0; i < len; i++) {
			n = nodes.item(i);
			if (name.equals(n.getNodeName())) {
				String str = n.getTextContent();
				if (StringUtil.isEmpty(str, true)) return defaultValue;
				return str.trim();
			}
		}
		return defaultValue;
	}

	public static Element getElement(Element el, String name, Element defaultValue) {
		if (el == null) return defaultValue;

		NodeList nodes = el.getChildNodes();
		int len = nodes.getLength();
		Node n;
		for (int i = 0; i < len; i++) {
			n = nodes.item(i);
			if (name.equals(n.getNodeName())) {
				if (n instanceof Element) return (Element) n;
			}
		}
		return defaultValue;
	}

	public static Collection<Repository> getRepositories(Element rootElement, POM current, POM parent, Map<String, String> properties, Repository defaultRepository)
			throws IOException {
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

		// TODO should we pass that in instead?
		NodeList nodes = rootElement.getChildNodes();
		int len = nodes.getLength();
		Node n, nn;
		Element el;
		for (int i = 0; i < len; i++) {
			n = nodes.item(i);
			if ("repositories".equals(n.getNodeName())) {

				NodeList innderNodes = n.getChildNodes();
				int innerLen = innderNodes.getLength();
				POM p;

				for (int ii = 0; ii < innerLen; ii++) {
					nn = innderNodes.item(ii);
					if (nn instanceof Element) {
						el = (Element) nn;

						if ("repository".equals(el.getNodeName())) {
							String id = MavenUtil.getValue(el, "id", null);
							id = resolvePlaceholders(current, id, properties);

							String name = MavenUtil.getValue(el, "name", null);
							name = resolvePlaceholders(current, name, properties);

							String url = MavenUtil.getValue(el, "url", null);
							url = resolvePlaceholders(current, url, properties);
							Repository r = new Repository(id, name, url);
							repositories.put(r.getUrl(), r);
						}
					}
				}
				break;
			}
		}
		return repositories.values();
	}

	public static List<POM> getDependencies(Element rootElement, POM current, POM parent, Map<String, String> properties, Resource localDirectory, boolean management)
			throws IOException {
		List<POM> dependencies = new ArrayList<>();
		List<POM> parentDendencyManagement = null;

		if (parent != null) {
			parentDendencyManagement = current.getDependencyManagement();
			for (POM pom: parent.getDependencies()) {
				dependencies.add(pom); // TODO clone?
			}
		}

		// TODO should we pass that in instead?
		NodeList nodes = rootElement.getChildNodes();
		int len = nodes.getLength();
		Node n, nn;
		Element el;
		for (int i = 0; i < len; i++) {
			n = nodes.item(i);
			if ("dependencies".equals(n.getNodeName())) {

				NodeList innderNodes = n.getChildNodes();
				int innerLen = innderNodes.getLength();
				POM p;

				for (int ii = 0; ii < innerLen; ii++) {
					nn = innderNodes.item(ii);
					if (nn instanceof Element) {
						el = (Element) nn;
						if ("dependency".equals(el.getNodeName())) {
							GAVSO gavso = getDependency(el, parent, current, properties, parentDendencyManagement, management);
							if (gavso == null) continue;
							p = POM.getInstance(localDirectory, current.getRepositories(), gavso.g, gavso.a, gavso.v, gavso.s, gavso.o, current.getDependencyScope(),
									current.getDependencyScopeManagement());
							dependencies.add(p);
						}
					}
				}
				break;
			}
		}
		return dependencies;
	}

	public static GAVSO getDependency(Element el, POM parent, POM current, Map<String, String> properties, List<POM> parentDendencyManagement, boolean management)
			throws IOException {
		POM pdm = null;// TODO move out of here so multiple loop elements can profit

		String g = MavenUtil.getValue(el, "groupId", null);
		g = resolvePlaceholders(current, g, properties);

		String a = MavenUtil.getValue(el, "artifactId", null);
		a = resolvePlaceholders(current, a, properties);

		// scope
		String s = MavenUtil.getValue(el, "scope", null);
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
		String v = MavenUtil.getValue(el, "version", null);
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
		String o = MavenUtil.getValue(el, "optional", null);
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

	public static List<POM> getDependencyManagement(Element rootElement, POM current, POM parent, Map<String, String> properties, Resource localDirectory) throws IOException {
		Element root = getElement(rootElement, "dependencyManagement", null);

		List<POM> dependencies = new ArrayList<>();

		if (parent != null) {
			List<POM> deps = parent.getDependencyManagement();
			if (deps != null) {
				for (POM pom: deps) {
					dependencies.add(pom); // TODO clone?
				}
			}
		}
		if (root != null) {
			NodeList nodes = root.getChildNodes();
			int len = nodes.getLength();
			Node n, nn;
			Element el;
			for (int i = 0; i < len; i++) {
				n = nodes.item(i);
				if ("dependencies".equals(n.getNodeName())) {

					NodeList innderNodes = n.getChildNodes();
					int innerLen = innderNodes.getLength();
					POM p;

					for (int ii = 0; ii < innerLen; ii++) {
						nn = innderNodes.item(ii);
						if (nn instanceof Element) {
							el = (Element) nn;
							if ("dependency".equals(el.getNodeName())) {
								GAVSO gavso = getDependency(el, parent, current, properties, null, true);
								if (gavso == null) continue;
								p = POM.getInstance(localDirectory, current.getRepositories(), gavso.g, gavso.a, gavso.v, gavso.s, gavso.o, current.getDependencyScope(),
										current.getDependencyScopeManagement());
								dependencies.add(p);
							}
						}
					}
					break;
				}
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

	public static void downloadPOM(POM pom, Collection<Repository> repositories) throws IOException {
		Resource res = pom.getPath();
		if (!res.isFile()) {
			URL url = pom.getArtifact("pom", repositories);
			print.o("download:" + url);
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				HttpGet request = new HttpGet(pom.getArtifact("pom", repositories).toExternalForm());
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

	public static POM toPOM(Resource localDirectory, Collection<Repository> repositories, Element el, Map<String, String> properties, int dependencyScope,
			int dependencyScopeManagement) throws IOException {

		return POM.getInstance(localDirectory, repositories,

				resolvePlaceholders(null, MavenUtil.getValue(el, "groupId", null), properties),

				resolvePlaceholders(null, MavenUtil.getValue(el, "artifactId", null), properties),

				resolvePlaceholders(null, MavenUtil.getValue(el, "version", null), properties),

				null, null,

				dependencyScope, dependencyScopeManagement

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
