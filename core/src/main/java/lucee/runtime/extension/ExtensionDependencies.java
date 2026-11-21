package lucee.runtime.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import lucee.commons.lang.StringUtil;
import lucee.loader.util.VersionImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.maven.Version;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.mvn.MavenUtil.MavenCoordinate;

/**
 * LDEV-6808 an extension can declare the extensions it needs with the manifest key
 * "require-extension", with the same syntax as the header Require-Extension of the core. The version
 * is a minimum, a newer installed version is fine.
 */
public final class ExtensionDependencies {

	private ExtensionDependencies() {
	}

	/**
	 * Resolves the extensions the given extension needs, including the extensions they need. Nothing is
	 * installed or changed. The result is in install order, an extension is always after the extensions
	 * it needs, extensions that are installed in a version that is good enough are not part of it.
	 *
	 * @throws ApplicationException when a required extension cannot be resolved, or there is a cycle
	 */
	public static List<RHExtension> resolve(Config config, RHExtension rhext) throws PageException {
		// when two extensions need the same extension in different minimum versions, the higher one wins: the
		// resolution starts again with the higher minimum until nothing changes
		Map<String, ExtensionDefintion> minimum = new HashMap<String, ExtensionDefintion>();
		for (int i = 0; i < 50; i++) {
			List<RHExtension> result = new ArrayList<RHExtension>();
			AtomicBoolean restart = new AtomicBoolean();
			resolve(config, rhext, new LinkedHashSet<String>(), new HashMap<String, RHExtension>(), result, minimum, restart);
			if (!restart.get()) return result;
		}
		throw new ApplicationException("The extension [" + label(rhext) + "] cannot be installed, the versions of the required extensions cannot be resolved.");
	}

	private static void resolve(Config config, RHExtension rhext, Set<String> path, Map<String, RHExtension> done, List<RHExtension> result,
			Map<String, ExtensionDefintion> minimum, AtomicBoolean restart) throws PageException {
		List<ExtensionDefintion> required = rhext.getRequiredExtensions();
		if (required.isEmpty()) return;

		String key = key(rhext);
		path.add(key);
		try {
			for (ExtensionDefintion ed: required) {
				// a higher minimum version that was found elsewhere
				ExtensionDefintion min = higher(minimum.get(edKey(config, ed)), minimum.get(idKey(ed)));
				if (min != null && compare(min, ed) > 0) ed = min;

				RHExtension installed = findInstalled(config, ed);
				if (installed != null && !installed.isOlderThan(ed)) continue;

				RHExtension dep;
				try {
					dep = ed.toRHExtension(config);
				}
				catch (PageException pe) {
					throw new ApplicationException("The extension [" + label(rhext) + "] cannot be installed, the required extension [" + ed + "] cannot be resolved: "
							+ pe.getMessage());
				}
				if (dep == null) throw new ApplicationException("The extension [" + label(rhext) + "] cannot be installed, the required extension [" + ed + "] cannot be resolved.");

				String depKey = key(dep);
				if (path.contains(depKey)) {
					throw new ApplicationException("The extension [" + label(rhext) + "] cannot be installed, there is a cycle in the required extensions: "
							+ String.join(" -> ", path) + " -> " + depKey);
				}

				// the extension that is found must be good enough
				if (dep.isOlderThan(ed)) {
					throw new ApplicationException("The extension [" + label(rhext) + "] cannot be installed, the required extension [" + ed + "] is only available in version ["
							+ dep.getVersion() + "].");
				}

				// already part of the result, in a version that is not good enough for this requirement
				RHExtension chosen = done.get(depKey);
				if (chosen != null) {
					if (chosen.isOlderThan(ed)) {
						// the extension can be required by its id or by its maven coordinate
						minimum.put(edKey(config, ed), ed);
						minimum.put(idKey(ed), ed);
						minimum.put(depKey, ed);
						minimum.put(dep.getId().toLowerCase(), ed);
						restart.set(true);
					}
					continue;
				}

				resolve(config, dep, path, done, result, minimum, restart);
				done.put(depKey, dep);
				result.add(dep);
			}
		}
		finally {
			path.remove(key);
		}
	}

	private static String edKey(Config config, ExtensionDefintion ed) {
		MavenCoordinate coordinate = ed.getCoordinate(config, false);
		if (coordinate != null && coordinate.isValid()) return (coordinate.groupId + ":" + coordinate.artifactId).toLowerCase();
		String id = ed.getId();
		return id == null ? "" : id.trim().toLowerCase();
	}

	private static String idKey(ExtensionDefintion ed) {
		String id = ed.getId();
		return id == null ? "" : id.trim().toLowerCase();
	}

	private static ExtensionDefintion higher(ExtensionDefintion a, ExtensionDefintion b) {
		if (a == null) return b;
		if (b == null) return a;
		return compare(a, b) >= 0 ? a : b;
	}

	/** compares the versions of two definitions, a definition without a version is the lowest */
	private static int compare(ExtensionDefintion a, ExtensionDefintion b) {
		Version va = StringUtil.isEmpty(a.getVersion(), true) ? null : VersionImpl.parseOSGi(a.getVersion(), null);
		Version vb = StringUtil.isEmpty(b.getVersion(), true) ? null : VersionImpl.parseOSGi(b.getVersion(), null);
		if (va == null) return vb == null ? 0 : -1;
		if (vb == null) return 1;
		return VersionImpl.compare(va, vb);
	}

	/**
	 * the installed extension that matches the definition (by maven coordinate or id, regardless of the
	 * version), null if there is none
	 */
	public static RHExtension findInstalled(Config config, ExtensionDefintion ed) {
		for (RHExtension ext: ((ConfigPro) config).getRHExtensions()) {
			if (matches(config, ed, ext)) return ext;
		}
		return null;
	}

	/**
	 * the installed extensions that require the given extension
	 *
	 * @param config
	 * @param rhext the extension that is required
	 * @return the extensions that need it, in the installed version
	 */
	public static List<RHExtension> getDependents(Config config, RHExtension rhext) {
		return getDependents(config, rhext, ((ConfigPro) config).getRHExtensions());
	}

	public static List<RHExtension> getDependents(Config config, RHExtension rhext, RHExtension[] installed) {
		List<RHExtension> result = new ArrayList<RHExtension>();
		for (RHExtension other: installed) {
			if (other.getId().equalsIgnoreCase(rhext.getId())) continue;
			for (ExtensionDefintion req: other.getRequiredExtensionsEL()) {
				if (matches(config, req, rhext)) {
					result.add(other);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * installing the given version of an extension that is installed in another version is an error
	 * when an installed extension needs a version that is not satisfied by it
	 */
	public static void checkDependentsAccept(Config config, RHExtension rhext) throws ApplicationException {
		for (RHExtension other: ((ConfigPro) config).getRHExtensions()) {
			if (other.getId().equalsIgnoreCase(rhext.getId())) continue;
			for (ExtensionDefintion req: other.getRequiredExtensionsEL()) {
				if (matches(config, req, rhext) && rhext.isOlderThan(req)) {
					throw new ApplicationException("The extension [" + label(rhext) + "] cannot be installed in version [" + rhext.getVersion() + "], the installed extension ["
							+ label(other) + "] requires at least version [" + req.getVersion() + "].");
				}
			}
		}
	}

	/**
	 * removing an extension that another installed extension requires is refused
	 */
	public static void checkNoDependents(Config config, RHExtension rhext) throws ApplicationException {
		List<RHExtension> dependents = getDependents(config, rhext);
		if (dependents.isEmpty()) return;
		StringBuilder sb = new StringBuilder();
		for (RHExtension d: dependents) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(label(d));
		}
		throw new ApplicationException("The extension [" + label(rhext) + "] cannot be removed, it is required by the extension" + (dependents.size() > 1 ? "s" : "") + " [" + sb
				+ "]. Remove them first or force the removal.");
	}

	private static boolean matches(Config config, ExtensionDefintion ed, RHExtension ext) {
		MavenCoordinate coordinate = ed.getCoordinate(config, false);
		if (coordinate != null && coordinate.isValid() && ext.hasGAV() && coordinate.groupId.equalsIgnoreCase(ext.getGroupId())
				&& coordinate.artifactId.equalsIgnoreCase(ext.getArtifactId())) {
			return true;
		}
		String id = ed.getId();
		return !StringUtil.isEmpty(id, true) && id.trim().equalsIgnoreCase(ext.getId());
	}

	private static String key(RHExtension ext) {
		return (ext.hasGAV() ? ext.getGroupId() + ":" + ext.getArtifactId() : ext.getId()).toLowerCase();
	}

	private static String label(RHExtension ext) {
		String name = ext.getMetadata().getName();
		if (StringUtil.isEmpty(name, true)) name = ext.getId();
		return name + ":" + ext.getVersion();
	}
}
