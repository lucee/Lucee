/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import lucee.Info;
import lucee.commons.io.IOUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.Constants;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.op.Caster;
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

	private Version version;
	private long fullVersionInfo;

	private long releaseTime;
	private DateTime releaseDate;
	private String versionName;
	private String versionNameExplanation;
	private String level;
	private List<ExtensionDefintion> requiredExtensions;
	public String requiredExtensionsRaw;

	public InfoImpl(Bundle bundle) {

		Dictionary<String, String> headers = bundle.getHeaders();

		// version
		this.version = bundle.getVersion();
		this.fullVersionInfo = KeyImpl.createHash64(this.version.toString());
		this.versionName = headers.get("Minor-Name");
		if (this.versionName == null) throw new RuntimeException("missing Minor-Name");
		this.versionNameExplanation = headers.get("Minor-Name-Explanation");

		// release date
		this.releaseTime = parseDateTime(headers.get("Built-Date"));
		this.level = "os";

		// required extension
		String str = headers.get("Require-Extension");
		if (StringUtil.isEmpty(str, true)) {
			this.requiredExtensions = new ArrayList<ExtensionDefintion>();
		}
		else {
			this.requiredExtensionsRaw = str;
		}
	}

	public static long parseDateTime(String dateStr) {

		int index1 = dateStr.indexOf('/');
		int index2 = dateStr.indexOf('/', index1 + 1);
		int index3 = dateStr.indexOf(' ', index2 + 1);
		int index4 = dateStr.indexOf(':', index3 + 1);
		int index5 = dateStr.indexOf(':', index4 + 1);
		int index6 = dateStr.indexOf(' ', index5 + 1);

		if (index6 != -1) {
			LocalDateTime ldt = LocalDateTime.of(Integer.parseInt(dateStr.substring(0, index1)), // year
					Integer.parseInt(dateStr.substring(index1 + 1, index2)), // month
					Integer.parseInt(dateStr.substring(index2 + 1, index3)), // day
					Integer.parseInt(dateStr.substring(index3 + 1, index4)), // hour
					Integer.parseInt(dateStr.substring(index4 + 1, index5)), // minute
					Integer.parseInt(dateStr.substring(index5 + 1, index6)) // second
			);
			String zone = "IST".equals(dateStr.substring(index6 + 1)) ? "Asia/Kolkata" : dateStr.substring(index6 + 1);
			return ldt.atZone(ZoneId.of(zone)).toInstant().toEpochMilli();
		}
		// fallback (slower, but more forgiving)
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z", Locale.ENGLISH);
		ZonedDateTime zdt = ZonedDateTime.parse(dateStr, formatter);
		return zdt.toInstant().toEpochMilli();
	}

	@Override
	public String getLevel() {
		return level;
	}

	@Override
	public long getRealeaseTime() {
		return releaseTime;
	}

	@Override
	public Version getVersion() {
		return version;
	}

	public List<ExtensionDefintion> getRequiredExtension() {
		if (requiredExtensions == null) {
			this.requiredExtensions = RHExtension.toExtensionDefinitions(requiredExtensionsRaw);
		}
		return requiredExtensions;
	}

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
		return fullVersionInfo;
	}

	@Override
	public String[] getCFMLTemplateExtensions() {
		return Constants.getCFMLTemplateExtensions();
	}

	@Override
	public String[] getLuceeTemplateExtensions() {
		return null;
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
		return null;
	}

	@Override
	public String toString() {
		return "releaseDate:" + releaseDate + ";versionName:" + versionName + ";versionNameExplanation:" + versionNameExplanation + ";level" + level + ";requiredExtensions:"
				+ requiredExtensionsRaw;
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

}