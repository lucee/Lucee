/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.osgi.framework.BundleContext;

import lucee.commons.digest.MD5;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.util.ResourceClassLoader;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.crypt.BlowfishEasy;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.listener.ClassicAppListener;
import lucee.runtime.listener.MixedAppListener;
import lucee.runtime.listener.ModernAppListener;
import lucee.runtime.listener.NoneAppListener;
import lucee.runtime.monitor.Monitor;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.BundleBuilderFactory;
import lucee.runtime.osgi.BundleFile;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;

/**
 * 
 */
public final class ConfigWebUtil {

	private static String enckey;

	/**
	 * default encryption for configuration (not very secure)
	 * 
	 * @param str
	 * @return
	 */
	public static String decrypt(String str) {
		if (StringUtil.isEmpty(str) || !StringUtil.startsWithIgnoreCase(str, "encrypted:")) return str;
		str = str.substring(10);
		return new BlowfishEasy(getEncKey()).decryptString(str);
	}

	/**
	 * default encryption for configuration (not very secure)
	 * 
	 * @param str
	 * @return
	 */
	public static String encrypt(String str) {
		if (StringUtil.isEmpty(str)) return "";
		if (StringUtil.startsWithIgnoreCase(str, "encrypted:")) return str;
		return "encrypted:" + new BlowfishEasy(getEncKey()).encryptString(str);
	}

	private static String getEncKey() {
		if (enckey == null) {
			enckey = SystemUtil.getSystemPropOrEnvVar("lucee.password.enc.key", "sdfsdfs");
		}
		return enckey;
	}

	/**
	 * deploys all content in "web-deployment" to a web context, used for new context mostly or update
	 * existings
	 * 
	 * @param cs
	 * @param cw
	 * @param throwError
	 * @throws IOException
	 */
	public static void deployWeb(ConfigServer cs, ConfigWeb cw, boolean throwError) throws IOException {
		Resource deploy = cs.getConfigDir().getRealResource("web-deployment"), trg;
		if (!deploy.isDirectory()) return;

		trg = cw.getRootDirectory();
		try {
			_deploy(cw, deploy, trg);
		}
		catch (IOException ioe) {
			if (throwError) throw ioe;
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs), ConfigWebUtil.class.getName(), ioe);
		}
	}

	/**
	 * deploys all content in "web-context-deployment" to a web context, used for new context mostly or
	 * update existings
	 * 
	 * @param cs
	 * @param cw
	 * @param throwError
	 * @throws IOException
	 */
	public static void deployWebContext(ConfigServer cs, ConfigWeb cw, boolean throwError) throws IOException {
		Resource deploy = cs.getConfigDir().getRealResource("web-context-deployment"), trg;
		if (!deploy.isDirectory()) return;
		trg = cw.getConfigDir().getRealResource("context");
		try {
			_deploy(cw, deploy, trg);
		}
		catch (IOException ioe) {
			if (throwError) throw ioe;
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs != null ? cs : cw), ConfigAdmin.class.getName(), ioe);
		}
	}

	private static void _deploy(ConfigWeb cw, Resource src, Resource trg) throws IOException {
		if (!src.isDirectory()) return;
		if (trg.isFile()) trg.delete();
		if (!trg.exists()) trg.mkdirs();
		Resource _src, _trg;
		Resource[] children = src.listResources();
		if (ArrayUtil.isEmpty(children)) return;

		for (int i = 0; i < children.length; i++) {
			_src = children[i];
			_trg = trg.getRealResource(_src.getName());
			if (_src.isDirectory()) _deploy(cw, _src, _trg);
			if (_src.isFile()) {
				if (_src.length() != _trg.length()) {
					_src.copyTo(_trg, false);
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cw), Log.LEVEL_INFO, ConfigWebUtil.class.getName(), "write file:" + _trg);

				}
			}
		}
	}

	public static void reloadLib(Config config) throws IOException {
		if (config instanceof ConfigWeb) loadLib(((ConfigWebImpl) config).getConfigServerImpl(), (ConfigPro) config);
		else loadLib(null, (ConfigPro) config);
	}

	static void loadLib(ConfigServer configServer, ConfigPro config) throws IOException {
		// get lib and classes resources
		Resource lib = config.getLibraryDirectory();
		Resource[] libs = lib.listResources(ExtensionResourceFilter.EXTENSION_JAR_NO_DIR);

		// get resources from server config and merge
		if (configServer != null) {
			ResourceClassLoader rcl = ((ConfigPro) configServer).getResourceClassLoader();
			libs = ResourceUtil.merge(libs, rcl.getResources());
		}

		CFMLEngine engine = ConfigWebUtil.getEngine(config);
		BundleContext bc = engine.getBundleContext();
		Log log = config.getLog("application");
		BundleFile bf;
		List<Resource> list = new ArrayList<Resource>();
		for (int i = 0; i < libs.length; i++) {
			try {
				bf = BundleFile.getInstance(libs[i], true);
				// jar is not a bundle
				if (bf == null) {
					// convert to a bundle
					BundleBuilderFactory factory = new BundleBuilderFactory(libs[i]);
					factory.setVersion("0.0.0.0");
					Resource tmp = SystemUtil.getTempFile("jar", false);
					factory.build(tmp);
					IOUtil.copy(tmp, libs[i]);
					bf = BundleFile.getInstance(libs[i], true);
				}

				OSGiUtil.start(OSGiUtil.installBundle(bc, libs[i], true));

			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				list.add(libs[i]);
				log.log(Log.LEVEL_ERROR, "OSGi", t);
			}
		}

		// set classloader

		ClassLoader parent = SystemUtil.getCoreClassLoader();
		((ConfigImpl) config).setResourceClassLoader(new ResourceClassLoader(list.toArray(new Resource[list.size()]), parent));
	}

	/**
	 * touch a file object by the string definition
	 * 
	 * @param config
	 * @param directory
	 * @param path
	 * @param type
	 * @return matching file
	 */
	public static Resource getFile(Config config, Resource directory, String path, short type) {
		path = replacePlaceholder(path, config);
		if (!StringUtil.isEmpty(path, true)) {
			Resource file = getFile(directory.getRealResource(path), type);
			if (file != null) return file;

			file = getFile(config.getResource(path), type);

			if (file != null) return file;
		}
		return null;
	}

	/**
	 * generate a file object by the string definition
	 * 
	 * @param rootDir
	 * @param strDir
	 * @param defaultDir
	 * @param configDir
	 * @param type
	 * @param config
	 * @return file
	 */
	static Resource getFile(Resource rootDir, String strDir, String defaultDir, Resource configDir, short type, ConfigPro config) {
		strDir = replacePlaceholder(strDir, config);
		if (!StringUtil.isEmpty(strDir, true)) {
			Resource res;
			if (strDir.indexOf("://") != -1) { // TODO better impl.
				res = getFile(config.getResource(strDir), type);
				if (res != null) return res;
			}
			res = rootDir == null ? null : getFile(rootDir.getRealResource(strDir), type);
			if (res != null) return res;

			res = getFile(config.getResource(strDir), type);
			if (res != null) return res;
		}
		if (defaultDir == null) return null;
		Resource file = getFile(configDir.getRealResource(defaultDir), type);
		return file;
	}

	// do not change, used in extension
	public static String replacePlaceholder(String str, Config config) {
		if (StringUtil.isEmpty(str)) return str;

		if (StringUtil.startsWith(str, '{')) {

			// Config Server
			if (str.startsWith("{lucee-config")) {
				if (str.startsWith("}", 13)) str = checkResult(str, config.getConfigDir().getReal(str.substring(14)));
				else if (str.startsWith("-dir}", 13)) str = checkResult(str, config.getConfigDir().getReal(str.substring(18)));
				else if (str.startsWith("-directory}", 13)) str = checkResult(str, config.getConfigDir().getReal(str.substring(24)));
			}

			else if (config != null && str.startsWith("{lucee-server")) {
				Resource dir = config instanceof ConfigWeb ? ((ConfigWeb) config).getConfigServerDir() : config.getConfigDir();
				// if(config instanceof ConfigServer && cs==null) cs=(ConfigServer) cw;
				if (dir != null) {
					if (str.startsWith("}", 13)) str = checkResult(str, dir.getReal(str.substring(14)));
					else if (str.startsWith("-dir}", 13)) str = checkResult(str, dir.getReal(str.substring(18)));
					else if (str.startsWith("-directory}", 13)) str = checkResult(str, dir.getReal(str.substring(24)));
				}
			}
			// Config Web
			else if (str.startsWith("{lucee-web")) {
				if (str.startsWith("}", 10)) str = checkResult(str, config.getConfigDir().getReal(str.substring(11)));
				else if (str.startsWith("-dir}", 10)) str = checkResult(str, config.getConfigDir().getReal(str.substring(15)));
				else if (str.startsWith("-directory}", 10)) str = checkResult(str, config.getConfigDir().getReal(str.substring(21)));
			}
			// Web Root
			else if (str.startsWith("{web-root")) {
				if (config instanceof ConfigWeb) {
					if (str.startsWith("}", 9)) str = checkResult(str, config.getRootDirectory().getReal(str.substring(10)));
					else if (str.startsWith("-dir}", 9)) str = checkResult(str, config.getRootDirectory().getReal(str.substring(14)));
					else if (str.startsWith("-directory}", 9)) str = checkResult(str, config.getRootDirectory().getReal(str.substring(20)));
				}
			}
			// Temp
			else if (str.startsWith("{temp")) {
				if (str.startsWith("}", 5)) str = checkResult(str, config.getTempDirectory().getRealResource(str.substring(6)).toString());
				else if (str.startsWith("-dir}", 5)) str = checkResult(str, config.getTempDirectory().getRealResource(str.substring(10)).toString());
				else if (str.startsWith("-directory}", 5)) str = checkResult(str, config.getTempDirectory().getRealResource(str.substring(16)).toString());
			}
			else if (config instanceof ServletConfig) {
				Map<String, String> labels = null;
				// web
				if (config instanceof ConfigWebPro) {
					labels = ((ConfigWebPro) config).getAllLabels();
				}
				// server
				else if (config instanceof ConfigServerImpl) {
					labels = ((ConfigServerImpl) config).getLabels();
				}
				if (labels != null) str = SystemUtil.parsePlaceHolder(str, ((ServletConfig) config).getServletContext(), labels);
			}
			else str = SystemUtil.parsePlaceHolder(str);

			if (StringUtil.startsWith(str, '{')) {
				Struct constants = config.getConstants();
				Iterator<Entry<Key, Object>> it = constants.entryIterator();
				Entry<Key, Object> e;
				while (it.hasNext()) {
					e = it.next();
					if (StringUtil.startsWithIgnoreCase(str, "{" + e.getKey().getString() + "}")) {
						String value = (String) e.getValue();
						str = checkResult(str, config.getResource(value).getReal(str.substring(e.getKey().getString().length() + 2)));
						break;

					}
				}
			}
		}
		return str;
	}

	private static String checkResult(String src, String res) {
		boolean srcEndWithSep = StringUtil.endsWith(src, ResourceUtil.FILE_SEPERATOR) || StringUtil.endsWith(src, '/') || StringUtil.endsWith(src, '\\');
		boolean resEndWithSep = StringUtil.endsWith(res, ResourceUtil.FILE_SEPERATOR) || StringUtil.endsWith(res, '/') || StringUtil.endsWith(res, '\\');
		if (srcEndWithSep && !resEndWithSep) return res + ResourceUtil.FILE_SEPERATOR;
		if (!srcEndWithSep && resEndWithSep) return res.substring(0, res.length() - 1);

		return res;
	}

	/**
	 * get only an existing file, dont create it
	 * 
	 * @param sc
	 * @param strDir
	 * @param defaultDir
	 * @param configDir
	 * @param type
	 * @param config
	 * @return existing file
	 */
	public static Resource getExistingResource(ServletContext sc, String strDir, String defaultDir, Resource configDir, short type, Config config, boolean checkFromWebroot) {
		// ARP

		strDir = replacePlaceholder(strDir, config);
		// checkFromWebroot &&
		if (strDir != null && strDir.trim().length() > 0) {
			Resource res = sc == null ? null : _getExistingFile(config.getResource(ResourceUtil.merge(ReqRspUtil.getRootPath(sc), strDir)), type);
			if (res != null) return res;

			res = _getExistingFile(config.getResource(strDir), type);
			if (res != null) return res;
		}
		if (defaultDir == null) return null;
		return _getExistingFile(configDir.getRealResource(defaultDir), type);

	}

	private static Resource _getExistingFile(Resource file, short type) {

		boolean asDir = type == ResourceUtil.TYPE_DIR;
		// File
		if (file.exists() && ((file.isDirectory() && asDir) || (file.isFile() && !asDir))) {
			return ResourceUtil.getCanonicalResourceEL(file);
		}
		return null;
	}

	/**
	 * 
	 * @param file
	 * @param type (FileUtil.TYPE_X)
	 * @return created file
	 */
	public static Resource getFile(Resource file, short type) {
		return ResourceUtil.createResource(file, ResourceUtil.LEVEL_GRAND_PARENT_FILE, type);
	}

	/**
	 * checks if file is a directory or not, if directory doesn't exist, it will be created
	 * 
	 * @param directory
	 * @return is directory or not
	 */
	public static boolean isDirectory(Resource directory) {
		if (directory.exists()) return directory.isDirectory();
		return directory.mkdirs();
	}

	/**
	 * checks if file is a file or not, if file doesn't exist, it will be created
	 * 
	 * @param file
	 * @return is file or not
	 */
	public static boolean isFile(Resource file) {
		if (file.exists()) return file.isFile();
		Resource parent = file.getParentResource();
		return parent.mkdirs() && file.createNewFile();
	}

	/**
	 * has access checks if config object has access to given type
	 * 
	 * @param config
	 * @param type
	 * @return has access
	 */
	public static boolean hasAccess(Config config, int type) {

		boolean has = true;
		if (config instanceof ConfigWeb) {
			has = ((ConfigWeb) config)

					.getSecurityManager()

					.getAccess(type) != SecurityManager.VALUE_NO;
		}
		return has;
	}

	public static String translateOldPath(String path) {
		if (path == null) return path;
		if (path.startsWith("/WEB-INF/lucee/")) {
			path = "{web-root}" + path;
		}
		return path;
	}

	public static Object getIdMapping(Mapping m) {
		StringBuilder id = new StringBuilder(m.getVirtualLowerCase());
		if (m.hasPhysical()) id.append(m.getStrPhysical());
		if (m.hasArchive()) id.append(m.getStrPhysical());
		return m.toString().toLowerCase();
	}

	public static void checkGeneralReadAccess(ConfigPro config, Password password) throws SecurityException {
		SecurityManager sm = config.getSecurityManager();
		short access = sm.getAccess(SecurityManager.TYPE_ACCESS_READ);
		if (config instanceof ConfigServer) access = SecurityManager.ACCESS_PROTECTED;
		if (access == SecurityManager.ACCESS_PROTECTED) {
			checkPassword(config, "read", password);
		}
		else if (access == SecurityManager.ACCESS_CLOSE) {
			throw new SecurityException("can't access, read access is disabled");
		}
	}

	public static void checkGeneralWriteAccess(ConfigPro config, Password password) throws SecurityException {
		SecurityManager sm = config.getSecurityManager();
		if (sm == null) return;
		short access = sm.getAccess(SecurityManager.TYPE_ACCESS_WRITE);

		if (config instanceof ConfigServer) access = SecurityManager.ACCESS_PROTECTED;
		if (access == SecurityManager.ACCESS_PROTECTED) {
			checkPassword(config, "write", password);
		}
		else if (access == SecurityManager.ACCESS_CLOSE) {
			throw new SecurityException("can't access, write access is disabled");
		}
	}

	public static void checkPassword(ConfigPro config, String type, Password password) throws SecurityException {
		if (!config.hasPassword()) throw new SecurityException("can't access password protected information from the configuration, no password is defined for "
				+ (config instanceof ConfigServer ? "the server context" : "this web context")); // TODO make the message more clear for someone using the admin indirectly in
		// source code by using ACF specific interfaces
		if (!config.passwordEqual(password)) {
			if (StringUtil.isEmpty(password)) {
				if (type == null) throw new SecurityException("Access is protected",
						"to access the configuration without a password, you need to change the access to [open] in the Server Administrator");
				throw new SecurityException(type + " access is protected",
						"to access the configuration without a password, you need to change the " + type + " access to [open] in the Server Administrator");
			}
			throw new SecurityException("No access, password is invalid");
		}
	}

	public static String createMD5FromResource(Resource resource) throws IOException {
		InputStream is = null;
		try {
			is = resource.getInputStream();
			byte[] barr = IOUtil.toBytes(is);
			return MD5.getDigestAsString(barr);
		}
		finally {
			IOUtil.close(is);
		}
	}

	public static int toListenerMode(String strListenerMode, int defaultValue) {
		if (StringUtil.isEmpty(strListenerMode, true)) return defaultValue;
		strListenerMode = strListenerMode.trim();

		if ("current".equalsIgnoreCase(strListenerMode) || "curr".equalsIgnoreCase(strListenerMode)) return ApplicationListener.MODE_CURRENT;
		else if ("currenttoroot".equalsIgnoreCase(strListenerMode) || "current2root".equalsIgnoreCase(strListenerMode) || "curr2root".equalsIgnoreCase(strListenerMode))
			return ApplicationListener.MODE_CURRENT2ROOT;
		else if ("currentorroot".equalsIgnoreCase(strListenerMode) || "currorroot".equalsIgnoreCase(strListenerMode)) return ApplicationListener.MODE_CURRENT_OR_ROOT;
		else if ("root".equalsIgnoreCase(strListenerMode)) return ApplicationListener.MODE_ROOT;

		return defaultValue;
	}

	public static String toListenerMode(int listenerMode, String defaultValue) {
		if (ApplicationListener.MODE_CURRENT == listenerMode) return "current";
		else if (ApplicationListener.MODE_CURRENT2ROOT == listenerMode) return "curr2root";
		else if (ApplicationListener.MODE_CURRENT_OR_ROOT == listenerMode) return "currorroot";
		else if (ApplicationListener.MODE_ROOT == listenerMode) return "root";
		return defaultValue;
	}

	public static int toListenerType(String strListenerType, int defaultValue) {
		if (StringUtil.isEmpty(strListenerType, true)) return defaultValue;
		strListenerType = strListenerType.trim();

		if ("none".equalsIgnoreCase(strListenerType)) return ApplicationListener.TYPE_NONE;
		else if ("classic".equalsIgnoreCase(strListenerType)) return ApplicationListener.TYPE_CLASSIC;
		else if ("modern".equalsIgnoreCase(strListenerType)) return ApplicationListener.TYPE_MODERN;
		else if ("mixed".equalsIgnoreCase(strListenerType)) return ApplicationListener.TYPE_MIXED;

		return defaultValue;
	}

	public static String toListenerType(int listenerType, String defaultValue) {
		if (ApplicationListener.TYPE_NONE == listenerType) return "none";
		else if (ApplicationListener.TYPE_CLASSIC == listenerType) return "classic";
		else if (ApplicationListener.TYPE_MODERN == listenerType) return "modern";
		else if (ApplicationListener.TYPE_MIXED == listenerType) return "mixed";

		return defaultValue;
	}

	public static ApplicationListener loadListener(String type, ApplicationListener defaultValue) {
		return loadListener(toListenerType(type, -1), defaultValue);
	}

	public static ApplicationListener loadListener(int type, ApplicationListener defaultValue) {
		// none
		if (ApplicationListener.TYPE_NONE == type) return new NoneAppListener();
		// classic
		if (ApplicationListener.TYPE_CLASSIC == type) return new ClassicAppListener();
		// modern
		if (ApplicationListener.TYPE_MODERN == type) return new ModernAppListener();
		// mixed
		if (ApplicationListener.TYPE_MIXED == type) return new MixedAppListener();

		return defaultValue;
	}

	public static short inspectTemplate(String str, short defaultValue) {
		if (str == null) return defaultValue;
		str = str.trim().toLowerCase();
		if (str.equals("always")) return Config.INSPECT_ALWAYS;
		else if (str.equals("never")) return Config.INSPECT_NEVER;
		else if (str.equals("once")) return Config.INSPECT_ONCE;
		return defaultValue;
	}

	public static String inspectTemplate(short s, String defaultValue) {
		switch (s) {
		case Config.INSPECT_ALWAYS:
			return "always";
		case Config.INSPECT_NEVER:
			return "never";
		case Config.INSPECT_ONCE:
			return "once";
		default:
			return defaultValue;
		}
	}

	public static short toScopeCascading(String type, short defaultValue) {
		if (StringUtil.isEmpty(type)) return defaultValue;
		if (type.equalsIgnoreCase("strict")) return Config.SCOPE_STRICT;
		else if (type.equalsIgnoreCase("small")) return Config.SCOPE_SMALL;
		else if (type.equalsIgnoreCase("standard")) return Config.SCOPE_STANDARD;
		else if (type.equalsIgnoreCase("standart")) return Config.SCOPE_STANDARD;
		return defaultValue;
	}

	public static short toScopeCascading(boolean searchImplicitScopes) {
		if (searchImplicitScopes) return Config.SCOPE_STANDARD;
		return Config.SCOPE_STRICT;

	}

	public static String toScopeCascading(short type, String defaultValue) {
		switch (type) {
		case Config.SCOPE_STRICT:
			return "strict";
		case Config.SCOPE_SMALL:
			return "small";
		case Config.SCOPE_STANDARD:
			return "standard";
		default:
			return defaultValue;
		}
	}

	public static CFMLEngine getEngine(Config config) {
		if (config instanceof ConfigWeb) return ((ConfigWeb) config).getFactory().getEngine();
		if (config instanceof ConfigServer) return ((ConfigServer) config).getEngine();
		return CFMLEngineFactory.getInstance();
	}

	public static Resource getConfigServerDirectory(Config config) {
		if (config == null) config = ThreadLocalPageContext.getConfig();
		if (config instanceof ConfigWeb) return ((ConfigWeb) config).getConfigServerDir();
		if (config == null) return null;
		return ((ConfigServer) config).getConfigDir();
	}

	public static Mapping[] getAllMappings(PageContext pc) {
		List<Mapping> list = new ArrayList<Mapping>();
		getAllMappings(list, pc.getConfig().getMappings());
		getAllMappings(list, pc.getConfig().getCustomTagMappings());
		getAllMappings(list, pc.getConfig().getComponentMappings());
		getAllMappings(list, pc.getApplicationContext().getMappings());
		return list.toArray(new Mapping[list.size()]);
	}

	public static Mapping[] getAllMappings(Config cw) {
		List<Mapping> list = new ArrayList<Mapping>();
		getAllMappings(list, cw.getMappings());
		getAllMappings(list, cw.getCustomTagMappings());
		getAllMappings(list, cw.getComponentMappings());
		return list.toArray(new Mapping[list.size()]);
	}

	private static void getAllMappings(List<Mapping> list, Mapping[] mappings) {
		if (!ArrayUtil.isEmpty(mappings)) for (int i = 0; i < mappings.length; i++) {
			list.add(mappings[i]);
		}
	}

	public static int toDialect(String strDialect, int defaultValue) {
		if ("cfml".equalsIgnoreCase(strDialect)) return CFMLEngine.DIALECT_CFML;
		if ("cfm".equalsIgnoreCase(strDialect)) return CFMLEngine.DIALECT_CFML;
		if ("cfc".equalsIgnoreCase(strDialect)) return CFMLEngine.DIALECT_CFML;
		if ("lucee".equalsIgnoreCase(strDialect)) return CFMLEngine.DIALECT_LUCEE;

		return defaultValue;
	}

	public static String toDialect(int dialect, String defaultValue) {
		if (dialect == CFMLEngine.DIALECT_CFML) return "cfml";
		if (dialect == CFMLEngine.DIALECT_LUCEE) return "lucee";
		return defaultValue;
	}

	public static int toMonitorType(String type, int defaultValue) {
		if (type == null) return defaultValue;

		type = type.trim();
		if ("request".equalsIgnoreCase(type)) return Monitor.TYPE_REQUEST;
		else if ("action".equalsIgnoreCase(type)) return Monitor.TYPE_ACTION;
		else if ("interval".equalsIgnoreCase(type) || "intervall".equalsIgnoreCase(type)) return Monitor.TYPE_INTERVAL;

		return defaultValue;
	}

	public static Mapping[] sort(Mapping[] mappings) {
		Arrays.sort(mappings, new Comparator() {
			@Override
			public int compare(Object left, Object right) {
				Mapping r = ((Mapping) right);
				Mapping l = ((Mapping) left);
				int rtn = r.getVirtualLowerCaseWithSlash().length() - l.getVirtualLowerCaseWithSlash().length();
				if (rtn == 0) return slashCount(r) - slashCount(l);
				return rtn;
			}

			private int slashCount(Mapping l) {
				String str = l.getVirtualLowerCaseWithSlash();
				int count = 0, lastIndex = -1;
				while ((lastIndex = str.indexOf('/', lastIndex)) != -1) {
					count++;
					lastIndex++;
				}
				return count;
			}
		});
		return mappings;
	}

	public static ConfigServer getConfigServer(Config config, Password password) throws PageException {
		if (config instanceof ConfigServer) return (ConfigServer) config;
		return ((ConfigWeb) config).getConfigServer(password);
	}

	protected static TagLib[] duplicate(TagLib[] tlds, boolean deepCopy) {
		TagLib[] rst = new TagLib[tlds.length];
		for (int i = 0; i < tlds.length; i++) {
			rst[i] = tlds[i].duplicate(deepCopy);
		}
		return rst;
	}

	protected static FunctionLib[] duplicate(FunctionLib[] flds, boolean deepCopy) {
		FunctionLib[] rst = new FunctionLib[flds.length];
		for (int i = 0; i < flds.length; i++) {
			rst[i] = flds[i].duplicate(deepCopy);
		}
		return rst;
	}

	public static Array getAsArray(String parent, String child, Struct sct) {
		return getAsArray(child, getAsStruct(parent, sct));
	}

	public static Struct getAsStruct(String name, Struct sct) {
		Object obj = sct.get(name, null);
		if (obj == null) {
			Struct tmp = new StructImpl(Struct.TYPE_LINKED);
			sct.put(name, tmp);
			return tmp;
		}
		return (Struct) obj;
	}

	public static Array getAsArray(String name, Struct sct) {
		Object obj = sct.get(KeyImpl.init(name), null);
		if (obj == null) {
			Array tmp = new ArrayImpl();
			sct.put(name, tmp);
			return tmp;
		}

		if (obj instanceof Array) return (Array) obj;

		Array tmp = new ArrayImpl();
		tmp.appendEL(obj);
		sct.put(name, tmp);
		return tmp;
	}

	public static String getAsString(String name, Struct sct, String defaultValue) {
		if (sct == null) return defaultValue;
		Object obj = sct.get(KeyImpl.init(name), null);
		if (obj == null) return defaultValue;
		return Caster.toString(obj, defaultValue);
	}

	public static double getAsDouble(String name, Struct sct, double defaultValue) {
		if (sct == null) return defaultValue;
		Object obj = sct.get(KeyImpl.init(name), null);
		if (obj == null) return defaultValue;
		return Caster.toDoubleValue(obj, false, defaultValue);
	}
}