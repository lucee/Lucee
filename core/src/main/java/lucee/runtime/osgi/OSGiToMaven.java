/**
 * Copyright (c) 2026, Lucee Association Switzerland. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.mvn.MavenUtil;
import lucee.runtime.mvn.MavenUtil.GAVSO;
import lucee.runtime.mvn.POM;
import lucee.transformer.library.ClassDefinitionImpl;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibTag;

/**
 * LDEV-6973: compiled templates bake in the class definition of extension tags and functions. Many
 * official Lucee extensions switched their TLD/FLD from an OSGi bundle to Maven coordinates, so
 * pages compiled against the old extension ask for an OSGi bundle that no longer exists locally.
 * 
 * Like for OSGi bundles, the version only matters when downloading: we use whatever is installed
 * locally. This class maps the old OSGi bundle names to their Maven replacement and resolves a
 * class from the locally installed Maven artifact. The goal is not to support every bundle, only
 * the official Lucee tag and function libraries that moved to Maven.
 */
public final class OSGiToMaven {

	// lower case OSGi bundle name -> Maven coordinates (groupId and artifactId, no version)
	private static final Map<String, GAVSO> MAPPING;

	static {
		Map<String, GAVSO> m = new HashMap<>();
		add(m, "org.lucee", "chart", "org.lucee.chart.extension", "chart.extension");
		add(m, "org.lucee", "image", "org.lucee.image.extension", "image.extension", "lucee.image.extension");
		add(m, "org.lucee", "pdf", "org.lucee.pdf.extension", "pdf.extension");
		add(m, "org.lucee", "s3", "org.lucee.s3.extension", "s3.extension");
		add(m, "org.lucee", "esapi", "esapi.extension", "org.lucee.esapi.extension");
		add(m, "org.lucee", "compress", "org.lucee.compress.extension", "compress.extension");
		add(m, "org.lucee", "redis", "redis.extension", "org.lucee.redis.extension");
		add(m, "org.lucee", "markdown", "org.lucee.markdown", "markdown.extension");
		add(m, "org.lucee", "argon2-functions", "org.lucee.argon2.extension", "argon2.extension");
		MAPPING = Collections.unmodifiableMap(m);
	}

	private static void add(Map<String, GAVSO> m, String groupId, String artifactId, String... bundleNames) {
		GAVSO ga = new GAVSO(groupId, artifactId, null);
		for (String bn: bundleNames) {
			m.put(bn.toLowerCase(), ga);
		}
	}

	/**
	 * @param bundleName OSGi bundle symbolic name
	 * @return the Maven coordinates (groupId:artifactId) that replace the given OSGi bundle or null
	 */
	public static String getMavenFor(String bundleName) {
		GAVSO ga = getGA(bundleName);
		return ga == null ? null : ga.g + ":" + ga.a;
	}

	public static boolean isMapped(String bundleName) {
		return getGA(bundleName) != null;
	}

	private static GAVSO getGA(String bundleName) {
		if (StringUtil.isEmpty(bundleName, true)) return null;
		return MAPPING.get(bundleName.trim().toLowerCase());
	}

	/**
	 * loads the class from the locally installed Maven replacement of the given OSGi bundle
	 * 
	 * @param config config to use, if null the config of the current thread is used
	 * @param className class name baked into the compiled code
	 * @param bundleName OSGi bundle name baked into the compiled code
	 * @return the loaded class or null when the bundle is not mapped or nothing is installed locally
	 */
	public static Class<?> loadClass(Config config, String className, String bundleName) {
		GAVSO ga = getGA(bundleName);
		if (ga == null || StringUtil.isEmpty(className, true)) return null;
		config = ThreadLocalPageContext.getConfig(config);
		if (!(config instanceof ConfigPro)) return null;
		ConfigPro cp = (ConfigPro) config;
		className = className.trim();

		// the class definitions of the currently installed extension (TLD/FLD) for that artifact
		List<ClassDefinitionImpl<?>> installed = getInstalledClassDefinitions(cp, ga.g, ga.a);

		// 1. the same class is still defined in a loaded TLD/FLD
		for (ClassDefinitionImpl<?> cd: installed) {
			if (cd.isClassNameEqualTo(className)) {
				Class<?> clazz = load(cd, bundleName, className);
				if (clazz != null) return clazz;
			}
		}

		// 2. the class still exists in the installed artifact, but is not (directly) used by a TLD/FLD
		String version = getInstalledVersion(cp, ga.g, ga.a, installed);
		if (version != null) {
			Class<?> clazz = load(new ClassDefinitionImpl<>(className, ga.g + ":" + ga.a + ":" + version), bundleName, className);
			if (clazz != null) return clazz;
		}

		// 3. the class was renamed with the switch to Maven (e.g. tag.Chart -> tag.jakarta.Chart), we look
		// for a TLD/FLD entry with the same simple class name
		String simpleName = simpleName(className);
		for (ClassDefinitionImpl<?> cd: installed) {
			if (simpleName.equals(simpleName(cd.getClassName()))) {
				Class<?> clazz = load(cd, bundleName, className);
				if (clazz != null) return clazz;
			}
		}
		return null;
	}

	/**
	 * replaces Maven coordinates that are not available locally with a version of the same artifact
	 * that is available locally (version only matters when downloading)
	 */
	public static GAVSO[] toLocallyAvailable(Config config, GAVSO[] gavsos) {
		if (gavsos == null || gavsos.length == 0) return gavsos;
		config = ThreadLocalPageContext.getConfig(config);
		if (!(config instanceof ConfigPro)) return gavsos;
		ConfigPro cp = (ConfigPro) config;
		Resource dir = cp.getMavenDir();
		if (dir == null) return gavsos;

		GAVSO[] rtn = null;
		for (int i = 0; i < gavsos.length; i++) {
			GAVSO g = gavsos[i];
			if (g == null || StringUtil.isEmpty(g.v, true) || isLocal(dir, g.g, g.a, g.v)) continue;
			String v = getInstalledVersion(cp, g.g, g.a, getInstalledClassDefinitions(cp, g.g, g.a));
			if (v == null || v.equals(g.v)) continue;
			if (rtn == null) rtn = gavsos.clone();
			rtn[i] = new GAVSO(g.g, g.a, v, g.s, g.o, null);
			log(cp, "use locally installed [" + g.g + ":" + g.a + ":" + v + "] instead of [" + g.g + ":" + g.a + ":" + g.v + "], which is not available locally");
		}
		return rtn == null ? gavsos : rtn;
	}

	private static Class<?> load(ClassDefinitionImpl<?> cd, String bundleName, String className) {
		try {
			Class<?> clazz = cd.getClazz();
			log(null, "OSGi bundle [" + bundleName + "] is not available locally, loaded class [" + className + "] from Maven [" + cd.getMavenRaw() + "] as ["
					+ clazz.getName() + "] instead");
			return clazz;
		}
		catch (Exception e) {
			LogUtil.log(Log.LEVEL_DEBUG, "class-loading", e);
			return null;
		}
	}

	private static List<ClassDefinitionImpl<?>> getInstalledClassDefinitions(ConfigPro config, String groupId, String artifactId) {
		List<ClassDefinitionImpl<?>> list = new ArrayList<>();
		// tags
		TagLib[] tlds = config.getTLDs();
		if (tlds != null) {
			for (TagLib tl: tlds) {
				if (tl == null) continue;
				for (TagLibTag tlt: tl.getTags().values()) {
					addIfMatch(list, tlt.getTagClassDefinition(), groupId, artifactId);
				}
			}
		}
		// functions
		FunctionLib fl = config.getFLDs();
		if (fl != null) {
			for (FunctionLibFunction flf: fl.getFunctions().values()) {
				addIfMatch(list, flf.getFunctionClassDefinition(), groupId, artifactId);
			}
		}
		return list;
	}

	private static void addIfMatch(List<ClassDefinitionImpl<?>> list, ClassDefinition<?> cd, String groupId, String artifactId) {
		if (!ClassDefinitionImpl.isMaven(cd)) return;
		ClassDefinitionImpl<?> cdi = (ClassDefinitionImpl<?>) cd;
		if (getVersion(cdi, groupId, artifactId) != null) list.add(cdi);
	}

	private static String getVersion(ClassDefinitionImpl<?> cd, String groupId, String artifactId) {
		List<GAVSO> gavsos = MavenUtil.toGAVSOs(cd.getMavenRaw(), null);
		if (gavsos == null) return null;
		for (GAVSO g: gavsos) {
			if (groupId.equalsIgnoreCase(g.g) && artifactId.equalsIgnoreCase(g.a) && !StringUtil.isEmpty(g.v, true)) return g.v;
		}
		return null;
	}

	/**
	 * the version used by the currently loaded TLD/FLD wins, if none is available locally the newest
	 * version in the local Maven repository is used
	 */
	private static String getInstalledVersion(ConfigPro config, String groupId, String artifactId, List<ClassDefinitionImpl<?>> installed) {
		Resource dir = config.getMavenDir();
		if (dir == null) return null;
		for (ClassDefinitionImpl<?> cd: installed) {
			String v = getVersion(cd, groupId, artifactId);
			if (v != null && isLocal(dir, groupId, artifactId, v)) return v;
		}

		String newest = null;
		try {
			Resource artifactDir = POM.localArtifactDir(dir, groupId, artifactId);
			Resource[] children = artifactDir.isDirectory() ? artifactDir.listResources() : null;
			if (children != null) {
				for (Resource child: children) {
					if (!child.isDirectory()) continue;
					String v = child.getName();
					if (!isLocal(dir, groupId, artifactId, v)) continue;
					if (newest == null || OSGiUtil.compare(v, newest) > 0) newest = v;
				}
			}
		}
		catch (Exception e) {
			LogUtil.log(Log.LEVEL_DEBUG, "class-loading", e);
		}
		return newest;
	}

	private static boolean isLocal(Resource dir, String groupId, String artifactId, String version) {
		try {
			return POM.localDir(dir, groupId, artifactId, version).getRealResource(artifactId + "-" + version + ".jar").isFile();
		}
		catch (Exception e) {
			return false;
		}
	}

	private static String simpleName(String className) {
		int index = className.lastIndexOf('.');
		return index == -1 ? className : className.substring(index + 1);
	}

	private static void log(Config config, String msg) {
		LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_DEBUG, "class-loading", msg);
	}
}
