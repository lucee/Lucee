/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.engine;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import lucee.Info;
import lucee.commons.io.IOUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.ListUtil;

/**
 * Info to this Version
 */
public final class InfoImpl implements Info {

	public static final int STATE_ALPHA = 2 * 100000000;
	public static final int STATE_BETA = 1 * 100000000;
	public static final int STATE_RC = 3 * 100000000;
	public static final int STATE_FINAL = 0;

	// Mod this
	private DateTime releaseDate;
	private String versionName;
	private String versionNameExplanation;
	private final long releaseTime;
	private Version version;
	private String level;
	private List<ExtensionDefintion> requiredExtensions;

	// private int state;
	// private final String strState;

	public InfoImpl() {
		this(null);
	}

	public InfoImpl(Bundle bundle) {

		try {
			Manifest manifest = getManifest(bundle);
			if (manifest == null)
				throw new IllegalArgumentException("Failed to get manifest from bundle");
			Attributes mf = manifest.getMainAttributes();

			versionName = mf.getValue("Minor-Name");
			if (versionName == null) throw new RuntimeException("missing Minor-Name");

			versionNameExplanation = mf.getValue("Minor-Name-Explanation");
			releaseDate = DateCaster.toDateAdvanced(mf.getValue("Built-Date"), null);
			// state=toIntState(mf.getValue("State"));
			level = "os";
			version = OSGiUtil.toVersion(mf.getValue("Bundle-Version"));

			String str = mf.getValue("Require-Extension");
			if (StringUtil.isEmpty(str, true)) requiredExtensions = new ArrayList<ExtensionDefintion>();
			else requiredExtensions = RHExtension.toExtensionDefinitions(str);

			// ListUtil.trimItems(ListUtil.listToStringArray(str, ','));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new PageRuntimeException(Caster.toPageException(t));
		}

		releaseTime = releaseDate.getTime();
		// strState=toStringState(state);
	}

	public static Properties getDefaultProperties(Bundle bundle) {
		InputStream is = null;
		Properties prop = new Properties();
		String keyToValidate = "felix.log.level";
		try {
			// check the bundle for the default.properties
			if (bundle != null) {
				try {
					is = bundle.getEntry("default.properties").openStream();
					prop.load(is);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
				finally {
					IOUtil.closeEL(is);
				}
			}
			if (prop.getProperty(keyToValidate) != null) return prop;

			// try from core classloader without leading slash
			prop = new Properties();
			Class clazz = PageSourceImpl.class;
			ClassLoader cl = clazz.getClassLoader();
			try {
				is = cl.getResourceAsStream("default.properties");
				prop.load(is);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			finally {
				IOUtil.closeEL(is);
			}
			if (prop.getProperty(keyToValidate) != null) return prop;

			// try from core classloader with leading slash
			prop = new Properties();
			try {
				is = cl.getResourceAsStream("/default.properties");
				prop.load(is);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			finally {
				IOUtil.closeEL(is);
			}
			if (prop.getProperty(keyToValidate) != null) return prop;

			// try from core class with leading slash
			prop = new Properties();
			try {
				is = clazz.getResourceAsStream("/default.properties");
				prop.load(is);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			finally {
				IOUtil.closeEL(is);
			}
			if (prop.getProperty(keyToValidate) != null) return prop;

			prop = new Properties();
			try {
				is = clazz.getResourceAsStream("../../default.properties");
				prop.load(is);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			finally {
				IOUtil.closeEL(is);
			}
			if (prop.getProperty(keyToValidate) != null) return prop;

			return new Properties();

		}
		finally {
			IOUtil.closeEL(is);
		}

	}

	public static Manifest getManifest(Bundle bundle) {
		InputStream is = null;
		Manifest manifest;
		try {
			// check the bundle for the default.properties
			if (bundle != null) {
				try {
					manifest = load(bundle.getEntry("META-INF/MANIFEST.MF").openStream());
					if (manifest != null) return manifest;
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}

			// try from core classloader without leading slash
			Class clazz = PageSourceImpl.class;
			ClassLoader cl = clazz.getClassLoader();
			manifest = load(cl.getResourceAsStream("META-INF/MANIFEST.MF"));
			if (manifest != null) return manifest;

			// try from core classloader with leading slash
			manifest = load(cl.getResourceAsStream("/META-INF/MANIFEST.MF"));
			if (manifest != null) return manifest;

			// try from core class with leading slash
			manifest = load(clazz.getResourceAsStream("/META-INF/MANIFEST.MF"));
			if (manifest != null) return manifest;

			manifest = load(clazz.getResourceAsStream("../../META-INF/MANIFEST.MF"));
			if (manifest != null) return manifest;

			// check all resources
			try {
				Enumeration<URL> e = cl.getResources("META-INF/MANIFEST.MF");
				while (e.hasMoreElements()) {
					manifest = load(e.nextElement().openStream());
					if (manifest != null) return manifest;
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}

			return null;

		}
		finally {
			IOUtil.closeEL(is);
		}

	}

	private static Manifest load(InputStream is) {
		try {
			Manifest m = new Manifest(is);
			String sn = m.getMainAttributes().getValue("Bundle-SymbolicName");
			if ("lucee.core".equals(sn)) return m;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		finally {
			IOUtil.closeEL(is);
		}
		return null;
	}

	private static boolean valid(Manifest manifest) {

		return false;
	}

	/**
	 * @return the level
	 */
	@Override
	public String getLevel() {
		return level;
	}

	public static int toIntVersion(String version, int defaultValue) {
		try {
			String[] aVersion = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(version, '.'));
			int ma = Caster.toIntValue(aVersion[0]);
			int mi = Caster.toIntValue(aVersion[1]);
			int re = Caster.toIntValue(aVersion[2]);
			int pa = Caster.toIntValue(aVersion[3]);
			return (ma * 1000000) + (mi * 10000) + (re * 100) + pa;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	// Version <version>.<major>.<minor>.<patches>

	/**
	 * @return Returns the releaseDate.
	 */
	public DateTime getRealeaseDate() {
		return releaseDate;
	}

	/**
	 * @return Returns the releaseTime.
	 */
	@Override
	public long getRealeaseTime() {
		return releaseTime;
	}

	@Override
	public Version getVersion() {
		return version;
	}

	public List<ExtensionDefintion> getRequiredExtension() {
		return requiredExtensions;
	}

	/**
	 * @return returns the state
	 * 
	 * @Override public int getStateAsInt() { return state; }
	 */

	/**
	 * @return returns the state
	 * 
	 * @Override public String getStateAsString() { return strState; }
	 */

	/*
	 * *
	 * 
	 * @return returns the state
	 * 
	 * public static String toStringState(int state) { if(state==STATE_FINAL) return "final"; else
	 * if(state==STATE_BETA) return "beta"; else if(state==STATE_RC) return "rc"; else return "alpha"; }
	 */

	/*
	 * *
	 * 
	 * @return returns the state
	 * 
	 * public int toIntState(String state) { state=state.trim().toLowerCase(); if("final".equals(state))
	 * return STATE_FINAL; else if("beta".equals(state)) return STATE_BETA; else if("rc".equals(state))
	 * return STATE_RC; else return STATE_ALPHA; }
	 */

	@Override
	public String getVersionName() {
		return versionName;
	}

	@Override
	public String getVersionNameExplanation() {
		return versionNameExplanation;
	}

	@Override
	public long getFullVersionInfo() {
		return KeyImpl.createHash64(getVersion().toString());// +state;
	}

	@Override
	public String[] getCFMLTemplateExtensions() {
		return Constants.getCFMLTemplateExtensions();
	}

	@Override
	public String[] getLuceeTemplateExtensions() {
		return Constants.getLuceeTemplateExtensions();
	}

	@Override
	public String[] getCFMLComponentExtensions() {
		return new String[] { getCFMLComponentExtension() };
	}

	@Override
	public String[] getLuceeComponentExtensions() {
		return new String[] { getLuceeComponentExtension() };
	}

	@Override
	public String getCFMLComponentExtension() {
		return Constants.getCFMLComponentExtension();
	}

	@Override
	public String getLuceeComponentExtension() {
		return Constants.getLuceeComponentExtension();
	}
}