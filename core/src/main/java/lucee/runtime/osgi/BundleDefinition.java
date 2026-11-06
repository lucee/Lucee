package lucee.runtime.osgi;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lucee.commons.lang.StringUtil;
import lucee.loader.util.VersionImpl;
import lucee.runtime.config.maven.Version;

/**
 * Name and (optional) exact version of a bundle, does not load anything.
 */
public class BundleDefinition implements Serializable {

	private static final long serialVersionUID = -144133130941294618L;

	private final String name;
	private Version version;

	private static final Map<String, Data> mappings = new HashMap<>();
	static {
		mappings.put("mongodb.extension", new Data("org.lucee.mongodb.extension", "3.12.13.9-SNAPSHOT"));
		mappings.put("chart.extension", new Data("org.lucee.chart.extension", "2.0.0.1-SNAPSHOT"));
		mappings.put("compress.extension", new Data("org.lucee.compress.extension", "2.0.0.1-SNAPSHOT"));
		mappings.put("lucee.image.extension", new Data("org.lucee.image.extension", "2.0.0.29-SNAPSHOT"));
		mappings.put("pdf.extension", new Data("org.lucee.pdf.extension", "1.2.0.11-SNAPSHOT"));
		mappings.put("s3.extension", new Data("org.lucee.s3.extension", "2.0.2.19-SNAPSHOT"));
	}

	private static class Data {

		private final String newBundleName;
		private final Version minVersion;

		public Data(String newBundleName, String minVersion) {
			this.newBundleName = newBundleName;
			this.minVersion = VersionImpl.parseOSGi(minVersion, null);
		}
	}

	public BundleDefinition(String name) {
		this(name, (Version) null);
	}

	public BundleDefinition(String name, String version) throws IOException {
		this(name, !StringUtil.isEmpty(version, true) ? VersionImpl.parseOSGi(version.trim()) : null);
	}

	public BundleDefinition(String name, Version version) {
		if (name == null) throw new IllegalArgumentException("Bundle Symbolic Name should not be null");
		// translate old mappings to new mappings FUTURE remove
		Data data = mappings.get(name);
		if (data != null && (version == null || VersionImpl.compare(data.minVersion, version) <= 0)) {
			name = data.newBundleName;
		}
		this.version = version;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return the version or null when there is no version
	 */
	public Version getVersion() {
		return version;
	}

	public String getVersionAsString() {
		return version == null ? null : version.toString();
	}

	@Override
	public String toString() {
		return "name:" + name + ";version:" + version + ";";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BundleDefinition)) return false;
		BundleDefinition other = (BundleDefinition) obj;
		if (!name.equals(other.name)) return false;
		if (version == null || other.version == null) return version == other.version;
		return VersionImpl.compare(version, other.version) == 0;
	}

	@Override
	public int hashCode() {
		return name.hashCode() * 31 + (version == null ? 0 : (version.getMajor() * 31 + version.getMinor()) * 31 + version.getMicro());
	}
}
