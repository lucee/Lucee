package lucee.runtime.extension;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.Version;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.maven.ExtensionProvider;
import lucee.runtime.exp.PageException;
import lucee.runtime.mvn.MavenUtil.MavenCoordinate;
import lucee.runtime.op.Decision;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.util.ListUtil;

public final class ExtensionDefintion {

	private String id;
	private Map<String, String> params = new HashMap<String, String>();
	private Resource source;
	private Config config;
	private RHExtension rhe;
	private MavenCoordinate coordinate;

	public ExtensionDefintion() {}

	public ExtensionDefintion(String id) {
		this.id = id;
	}

	public ExtensionDefintion(String id, String version) {
		this.id = id;
		setParam("version", version);
	}

	public ExtensionDefintion(RHExtension rhe) {
		this.rhe = rhe;
	}

	public ExtensionDefintion(RHExtension rhe, String id, String version) {
		this.rhe = rhe;
		this.id = id;
		setParam("version", version);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public MavenCoordinate getCoordinate(Config config, boolean investigate) {
		if (coordinate != null) return coordinate;
		return coordinate = ExtensionProvider.toCoordinate((ConfigPro) config, getId(), investigate, coordinate);
	}

	public ExtensionDefintion setCoordinate(MavenCoordinate coordinate) {
		this.coordinate = coordinate;
		if (coordinate != null) {
			if (!StringUtil.isEmpty(coordinate.groupId, true)) setParam("groupId", coordinate.groupId);
			if (!StringUtil.isEmpty(coordinate.artifactId, true)) setParam("artifactId", coordinate.artifactId);
			if (!StringUtil.isEmpty(coordinate.version, true)) setParam("version", coordinate.version);
			if (!StringUtil.isEmpty(coordinate.scope, true)) setParam("scope", coordinate.scope);
			if (!StringUtil.isEmpty(coordinate.optional, true)) setParam("optional", coordinate.optional);
		}
		return this;
	}

	public String getArtifactId() {
		if (coordinate != null) return coordinate.artifactId;

		if (getId() != null) {
			// TODO only check once
			MavenCoordinate tmp = ExtensionProvider.toCoordinate((ConfigPro) config, getId(), true, coordinate);
			if (tmp != null) {
				coordinate = new MavenCoordinate(tmp.groupId, tmp.artifactId, getVersion());
				return tmp.artifactId;
			}
		}

		return null;
	}

	public String getGroupId() {
		if (coordinate != null) return coordinate.groupId;

		if (getId() != null) {
			// TODO only check once
			MavenCoordinate tmp = ExtensionProvider.toCoordinate((ConfigPro) config, getId(), true, coordinate);
			if (tmp != null) {
				coordinate = new MavenCoordinate(tmp.groupId, tmp.artifactId, getVersion());
				return tmp.groupId;
			}
		}
		return null;
	}

	public String getSymbolicName() {
		String sn = params.get("symbolic-name");
		if (StringUtil.isEmpty(sn, true)) return getId();
		return sn.trim();
	}

	public void setParam(String name, String value) {
		params.put(name, value);
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getVersion() {

		String version = params.get("version");
		if (StringUtil.isEmpty(version)) version = params.get("extension-version");
		if (coordinate != null && !StringUtil.isEmpty(coordinate.version)) return coordinate.version;
		if (StringUtil.isEmpty(version)) return null;
		return version;
	}

	public Version getSince() {
		String since = params.get("since");
		if (StringUtil.isEmpty(since)) return null;
		return OSGiUtil.toVersion(since, null);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (getId() != null) sb.append(getId());
		Iterator<Entry<String, String>> it = params.entrySet().iterator();
		Entry<String, String> e;
		while (it.hasNext()) {
			e = it.next();
			if (sb.length() > 0) sb.append(';');
			sb.append(e.getKey()).append('=').append(e.getValue());
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ExtensionDefintion) {
			ExtensionDefintion ed = (ExtensionDefintion) other;

			if (ed.getCoordinate(config, false) != null && getGroupId() != null && getArtifactId() != null) {
				return ed.getGroupId().equalsIgnoreCase(getGroupId()) && ed.getArtifactId().equalsIgnoreCase(getArtifactId());
			}

			if (!ed.getId().equalsIgnoreCase(getId())) return false;
			if (ed.getVersion() == null || getVersion() == null) return true;
			return ed.getVersion().equalsIgnoreCase(getVersion());

		}
		else if (other instanceof RHExtension) {
			throw new RuntimeException("invalid call for equals");
		}
		return false;
	}

	public void setSource(RHExtension rhe) {
		this.rhe = rhe;
		if (rhe.getExtensionFile() != null) this.source = rhe.getExtensionFile();
	}

	public ExtensionDefintion setSource(Config config, Resource source) {
		if (config != null) this.config = config;
		this.source = source;
		return this;
	}

	public RHExtension toRHExtension(RHExtension defaultValue) {
		if (rhe != null) return rhe;

		if (source == null) {
			return defaultValue;
		}
		rhe = RHExtension.getInstance(config, source, defaultValue, config.getLog("deploy"));
		return rhe;
	}

	public RHExtension toRHExtension(Config config) throws PageException {
		if (rhe != null) return rhe;

		return rhe = RHExtension.getInstance(config, this, true, config.getLog("deploy"));
	}

	//
	public Resource getSource(Config config, Resource defaultValue) {
		if (source != null) return source;
		try {
			return toRHExtension(config).getExtensionFile();
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public Resource getSource(Config config) throws PageException {
		if (source != null) return source;
		return toRHExtension(config).getExtensionFile();
	}

	Resource _getSource(Resource defaultValue) {
		if (source != null) return source;
		if (rhe != null) return rhe.getExtensionFile();

		return defaultValue;
	}

	public String getStorageName(boolean investigate) {
		MavenCoordinate coordinate = getCoordinate(config, investigate);

		if (coordinate != null) {
			return getStorageName(coordinate);
		}
		if (!StringUtil.isEmpty(getId()) && !StringUtil.isEmpty(getVersion())) {
			return getStorageName(getId(), getVersion());
		}
		return null;

	}

	public static String getStorageName(MavenCoordinate coordinate) {
		return "mvn_" + coordinate.groupId + "_" + coordinate.artifactId + "_" + coordinate.version + ".lex";
	}

	public static String getStorageName(String id, String version) {
		return id + "-" + version + ".lex";
	}

	public static ExtensionDefintion toExtensionDefinitionFromStorageName(String filename) {
		int index = filename.lastIndexOf(".lex");

		if (index == -1) {
			return null;
		}
		filename = filename.substring(0, index);
		String[] arr = ListUtil.listToStringArray(filename, '_');

		// maven
		if (arr.length == 4 && arr[0].equals("mvn") && OSGiUtil.toVersion(arr[3], null) != null) {
			return new ExtensionDefintion().setCoordinate(new MavenCoordinate(arr[1], arr[2], arr[3]));
		}
		// id
		if (arr.length == 3 && arr[0].equals("id") && Decision.isUUId(arr[1]) && OSGiUtil.toVersion(arr[2], null) != null) {
			return new ExtensionDefintion(arr[1], arr[2]);
		}
		if (filename.length() > 37) {
			String uuid = filename.substring(0, 35);
			if (Decision.isUUId(uuid) && filename.charAt(35) == '-') {
				String version = filename.substring(36, index);
				if (OSGiUtil.toVersion(version, null) != null) {
					return new ExtensionDefintion(uuid, version);
				}
			}
		}

		return null;
	}

	public static boolean startsWithUUID(String filename) {
		if (filename.length() > 37) {
			String uuid = filename.substring(0, 35);
			return (Decision.isUUId(uuid) && filename.charAt(35) == '-');
		}
		return false;
	}

	/*
	 * public static void main(String[] args) {
	 * print.e(toExtensionDefinitionFromStorageName("mvn_org.lucee_administrator-extension_1.0.0.5.lex")
	 * ); print.e(toExtensionDefinitionFromStorageName(
	 * "id_E0ACA85A-22DB-48FF-B2D6CD89D5D1709F_1.0.0.0-BETA.lex"));
	 * print.e(toExtensionDefinitionFromStorageName("7E673D15-D87C-41A6-8B5F1956528C605F-1.2.lex")); }
	 */
}
