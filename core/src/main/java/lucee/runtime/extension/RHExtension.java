/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
package lucee.runtime.extension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.Info;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.loader.util.Util;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigAdmin;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebFactory;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.Constants;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.engine.ThreadLocalConfig;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.functions.conversion.DeserializeJSON;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.osgi.BundleFile;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.osgi.VersionRange;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

/**
 * Extension completely handled by the engine and not by the Install/config.xml
 */
public class RHExtension implements Serializable {

	public static final short INSTALL_OPTION_NOT = 0;
	public static final short INSTALL_OPTION_IF_NECESSARY = 1;
	public static final short INSTALL_OPTION_FORCE = 2;

	public static final short ACTION_NONE = 0;
	public static final short ACTION_COPY = 1;
	public static final short ACTION_MOVE = 2;

	private static final long serialVersionUID = 2904020095330689714L;

	private static final String[] EMPTY = new String[0];
	private static final BundleDefinition[] EMPTY_BD = new BundleDefinition[0];

	public static final int RELEASE_TYPE_ALL = 0;
	public static final int RELEASE_TYPE_SERVER = 1;
	public static final int RELEASE_TYPE_WEB = 2;

	private static final ExtensionResourceFilter LEX_FILTER = new ExtensionResourceFilter("lex");

	private static Set<String> metadataFilesChecked = new HashSet<>();

	private String id;
	private int releaseType;
	private String version;
	private String name;
	private String symbolicName;

	private String description;
	private boolean trial;
	private String image;
	private boolean startBundles;
	private BundleInfo[] bundles;
	private String[] jars;
	private String[] flds;
	private String[] tlds;
	private String[] tags;
	private String[] functions;
	private String[] archives;
	private String[] applications;
	private String[] components;
	private String[] plugins;
	private String[] contexts;
	private String[] configs;
	private String[] webContexts;
	private String[] categories;
	private String[] gateways;

	private List<Map<String, String>> caches;
	private List<Map<String, String>> cacheHandlers;
	private List<Map<String, String>> orms;
	private List<Map<String, String>> webservices;
	private List<Map<String, String>> monitors;
	private List<Map<String, String>> resources;
	private List<Map<String, String>> searchs;
	private List<Map<String, String>> amfs;
	private List<Map<String, String>> jdbcs;
	private List<Map<String, String>> startupHooks;
	private List<Map<String, String>> mappings;
	private List<Map<String, Object>> eventGatewayInstances;

	private Resource extensionFile;

	private String type;

	private VersionRange minCoreVersion;

	private double minLoaderVersion;

	private String amfsJson;

	private String resourcesJson;
	// private Config config;

	private String searchsJson;

	private String ormsJson;
	private String webservicesJson;

	private String monitorsJson;

	private String cachesJson;

	private String cacheHandlersJson;

	private String jdbcsJson;

	private String startupHooksJson;

	private String mappingsJson;

	private String eventGatewayInstancesJson;

	private boolean loaded;

	private final Config config;

	public boolean softLoaded = false;

	public RHExtension(Config config, Resource ext) throws PageException, IOException, BundleException, ConverterException {
		this.config = config;
		init(ext);
	}

	public RHExtension(Config config, String id, String version) throws PageException, IOException, BundleException, ConverterException {
		this.config = config;

		Struct data = getMetaData(config, id, version, (Struct) null);
		this.extensionFile = getExtensionInstalledFile(config, id, version, false);
		// do we have usefull meta data?
		if (data != null && data.containsKey("startBundles")) {
			try {
				readManifestConfig(id, data, extensionFile.getAbsolutePath(), null);
				softLoaded = true;
				return;
			}
			catch (InvalidVersion iv) {
				throw iv;
			}
			catch (ApplicationException ae) {
			}
		}

		init(this.extensionFile);
		softLoaded = false;
	}

	private void init(Resource ext) throws PageException, IOException, BundleException, ConverterException {
		// make sure the config is registerd with the thread
		if (ThreadLocalPageContext.getConfig() == null) ThreadLocalConfig.register(config);
		// is it a web or server context?
		this.type = config instanceof ConfigWeb ? "web" : "server";
		this.extensionFile = ext;

		load(ext);
		// write metadata to XML
		Resource mdf = getMetaDataFile(config, id, version);
		if (!metadataFilesChecked.contains(mdf.getAbsolutePath()) && !mdf.isFile()) {
			Struct data = new StructImpl(Struct.TYPE_LINKED);
			populate(data, true);
			storeMetaData(mdf, data);
			metadataFilesChecked.add(mdf.getAbsolutePath()); // that way we only have to check this once
		}
	}

	public static RHExtension installExtension(ConfigPro config, String id, String version, String resource, boolean force)
			throws PageException, IOException, BundleException, ConverterException {

		// get installed res
		Resource res = StringUtil.isEmpty(version) ? null : getExtensionInstalledFile(config, id, version, false);
		boolean installed = (res != null && res.isFile());

		if (!installed) {
			if (!StringUtil.isEmpty(resource) && (res = ResourceUtil.toResourceExisting(config, resource, null)) != null) {
				return DeployHandler.deployExtension(config, res, false, true, RHExtension.ACTION_COPY);
			}
			else if (!StringUtil.isEmpty(id)) {
				return DeployHandler.deployExtension(config, new ExtensionDefintion(id, version), null, false, true, true, new RefBooleanImpl());
			}
			else {
				throw new IOException("cannot install extension based on the given data [id:" + id + ";version:" + version + ";resource:" + resource + "]");
			}
		}
		// if forced we also install if it already is
		else if (force) {
			return DeployHandler.deployExtension(config, res, false, true, RHExtension.ACTION_NONE);
		}
		return new RHExtension(config, res);
	}

	public static boolean isInstalled(Config config, String id, String version) throws PageException {
		Resource res = getExtensionInstalledFile(config, id, version, false);
		return res != null && res.isFile();
	}

	/**
	 * copy the extension resource file to the installed folder
	 * 
	 * @param ext
	 * @return
	 * @throws PageException
	 * @throws ConverterException
	 * @throws IOException
	 */
	public Resource copyToInstalled() throws PageException, ConverterException, IOException {
		if (extensionFile == null) throw new IOException("no extension file defined");
		if (!extensionFile.isFile()) throw new IOException("given extension file [" + extensionFile + "] does not exist");

		addToAvailable(extensionFile);
		return act(extensionFile, RHExtension.ACTION_COPY);
	}

	/**
	 * copy the extension resource file to the installed folder
	 * 
	 * @param ext
	 * @return
	 * @throws PageException
	 * @throws ConverterException
	 * @throws IOException
	 */
	public Resource moveToInstalled() throws PageException, ConverterException, IOException {
		if (extensionFile == null) throw new IOException("no extension file defined");
		if (!extensionFile.isFile()) throw new IOException("given extension file [" + extensionFile + "] does not exist");

		addToAvailable(extensionFile);
		return act(extensionFile, RHExtension.ACTION_MOVE);
	}

	public static void storeMetaData(Config config, String id, String version, Struct data) throws ConverterException, IOException {
		storeMetaData(getMetaDataFile(config, id, version), data);
	}

	private static void storeMetaData(Resource file, Struct data) throws ConverterException, IOException {
		JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
		String str = json.serialize(null, data, SerializationSettings.SERIALIZE_AS_ROW, true);
		ResourceUtil.createParentDirectoryIfNecessary(file);

		IOUtil.write(file, str, CharsetUtil.UTF8, false);
	}

	// copy the file to extension dir if it is not already there
	private Resource act(Resource ext, short action) throws PageException {
		Resource trg;
		Resource trgDir;
		try {
			trg = getExtensionInstalledFile(config, id, version, false);
			trgDir = trg.getParentResource();
			trgDir.mkdirs();
			if (!ext.getParentResource().equals(trgDir)) {
				if (trg.exists()) trg.delete();
				if (action == ACTION_COPY) {
					ext.copyTo(trg, false);
				}
				else if (action == ACTION_MOVE) {
					ResourceUtil.moveTo(ext, trg, true);
				}
				this.extensionFile = trg;
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		return trg;
	}

	public void addToAvailable() {
		addToAvailable(getExtensionFile());
	}

	private void addToAvailable(Resource ext) {
		if (id == null) {
			try {
				load(ext);
			}
			catch (Exception e) {
				LogUtil.log("deploy", "extension", e);
			}
		}
		if (ext == null || ext.length() == 0 || id == null) return;
		Log logger = ThreadLocalPageContext.getLog(config, "deploy");
		Resource res;
		if (config instanceof ConfigWeb) {
			res = ((ConfigWeb) config).getConfigServerDir().getRealResource("extensions/");
		}
		else {
			res = config.getConfigDir().getRealResource("extensions/");
		}

		// parent exist?
		if (!res.isDirectory()) {
			logger.warn("extension", "directory [" + res + "] does not exist");
			return;
		}
		res = res.getRealResource("available/");

		// exist?
		if (!res.isDirectory()) {
			try {
				res.createDirectory(true);
			}
			catch (IOException e) {
				logger.error("extension", e);
				return;
			}
		}
		res = res.getRealResource(id + "-" + version + ".lex");
		if (res.length() == ext.length()) return;
		try {
			ResourceUtil.copy(ext, res);
			logger.info("extension", "copy [" + id + ":" + version + "] to [" + res + "]");
		}
		catch (IOException e) {
			logger.error("extension", e);
		}
	}

	public static Manifest getManifestFromFile(Config config, Resource file) throws IOException {
		ZipInputStream zis = new ZipInputStream(IOUtil.toBufferedInputStream(file.getInputStream()));
		ZipEntry entry;
		Manifest manifest = null;

		try {
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory() && entry.getName().equalsIgnoreCase("META-INF/MANIFEST.MF")) {
					manifest = toManifest(config, zis, null);
				}
				zis.closeEntry();
				if (manifest != null) return manifest;
			}
		}
		finally {
			IOUtil.close(zis);
		}
		return null;

	}

	private void load(Resource ext) throws IOException, BundleException, ApplicationException {
		// print.ds(ext.getAbsolutePath());
		loaded = true;
		// no we read the content of the zip
		ZipInputStream zis = new ZipInputStream(IOUtil.toBufferedInputStream(ext.getInputStream()));
		ZipEntry entry;
		Manifest manifest = null;
		String _img = null;
		String path;
		String fileName, sub;

		List<BundleInfo> bundles = new ArrayList<BundleInfo>();
		List<String> jars = new ArrayList<String>();
		List<String> flds = new ArrayList<String>();
		List<String> tlds = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();
		List<String> functions = new ArrayList<String>();
		List<String> contexts = new ArrayList<String>();
		List<String> configs = new ArrayList<String>();
		List<String> webContexts = new ArrayList<String>();
		List<String> applications = new ArrayList<String>();
		List<String> components = new ArrayList<String>();
		List<String> plugins = new ArrayList<String>();
		List<String> gateways = new ArrayList<String>();
		List<String> archives = new ArrayList<String>();

		try {
			while ((entry = zis.getNextEntry()) != null) {
				path = entry.getName();
				fileName = fileName(entry);
				sub = subFolder(entry);

				if (!entry.isDirectory() && path.equalsIgnoreCase("META-INF/MANIFEST.MF")) {
					manifest = toManifest(config, zis, null);
				}
				else if (!entry.isDirectory() && path.equalsIgnoreCase("META-INF/logo.png")) {
					_img = toBase64(zis, null);
				}

				// jars
				else if (!entry.isDirectory() && (startsWith(path, type, "jars") || startsWith(path, type, "jar") || startsWith(path, type, "bundles")
						|| startsWith(path, type, "bundle") || startsWith(path, type, "lib") || startsWith(path, type, "libs")) && (StringUtil.endsWithIgnoreCase(path, ".jar"))) {

							jars.add(fileName);
							BundleInfo bi = BundleInfo.getInstance(fileName, zis, false);
							if (bi.isBundle()) bundles.add(bi);
						}

				// flds
				else if (!entry.isDirectory() && startsWith(path, type, "flds") && (StringUtil.endsWithIgnoreCase(path, ".fld") || StringUtil.endsWithIgnoreCase(path, ".fldx")))
					flds.add(fileName);

				// tlds
				else if (!entry.isDirectory() && startsWith(path, type, "tlds") && (StringUtil.endsWithIgnoreCase(path, ".tld") || StringUtil.endsWithIgnoreCase(path, ".tldx")))
					tlds.add(fileName);

				// archives
				else if (!entry.isDirectory() && (startsWith(path, type, "archives") || startsWith(path, type, "mappings")) && StringUtil.endsWithIgnoreCase(path, ".lar"))
					archives.add(fileName);

				// event-gateway
				else if (!entry.isDirectory() && (startsWith(path, type, "event-gateways") || startsWith(path, type, "eventGateways"))
						&& (StringUtil.endsWithIgnoreCase(path, "." + Constants.getCFMLComponentExtension())))
					gateways.add(sub);

				// tags
				else if (!entry.isDirectory() && startsWith(path, type, "tags")) tags.add(sub);

				// functions
				else if (!entry.isDirectory() && startsWith(path, type, "functions")) functions.add(sub);

				// context
				else if (!entry.isDirectory() && startsWith(path, type, "context") && !StringUtil.startsWith(fileName(entry), '.')) contexts.add(sub);

				// web contextS
				else if (!entry.isDirectory() && (startsWith(path, type, "webcontexts") || startsWith(path, type, "web.contexts")) && !StringUtil.startsWith(fileName(entry), '.'))
					webContexts.add(sub);

				// config
				else if (!entry.isDirectory() && startsWith(path, type, "config") && !StringUtil.startsWith(fileName(entry), '.')) configs.add(sub);

				// applications
				else if (!entry.isDirectory() && (startsWith(path, type, "web.applications") || startsWith(path, type, "applications") || startsWith(path, type, "web"))
						&& !StringUtil.startsWith(fileName(entry), '.'))
					applications.add(sub);

				// components
				else if (!entry.isDirectory() && (startsWith(path, type, "components")) && !StringUtil.startsWith(fileName(entry), '.')) components.add(sub);

				// plugins
				else if (!entry.isDirectory() && (startsWith(path, type, "plugins")) && !StringUtil.startsWith(fileName(entry), '.')) plugins.add(sub);

				zis.closeEntry();
			}
		}
		finally {
			IOUtil.close(zis);
		}

		// read the manifest
		if (manifest == null) throw new ApplicationException("The Extension [" + ext + "] is invalid,no Manifest file was found at [META-INF/MANIFEST.MF].");
		readManifestConfig(manifest, ext.getAbsolutePath(), _img);

		this.jars = jars.toArray(new String[jars.size()]);
		this.flds = flds.toArray(new String[flds.size()]);
		this.tlds = tlds.toArray(new String[tlds.size()]);
		this.tags = tags.toArray(new String[tags.size()]);
		this.gateways = gateways.toArray(new String[gateways.size()]);
		this.functions = functions.toArray(new String[functions.size()]);
		this.archives = archives.toArray(new String[archives.size()]);

		this.contexts = contexts.toArray(new String[contexts.size()]);
		this.configs = configs.toArray(new String[configs.size()]);
		this.webContexts = webContexts.toArray(new String[webContexts.size()]);
		this.applications = applications.toArray(new String[applications.size()]);
		this.components = components.toArray(new String[components.size()]);
		this.plugins = plugins.toArray(new String[plugins.size()]);
		this.bundles = bundles.toArray(new BundleInfo[bundles.size()]);

	}

	private void readManifestConfig(Manifest manifest, String label, String _img) throws ApplicationException {
		boolean isWeb = config instanceof ConfigWeb;
		type = isWeb ? "web" : "server";
		Log logger = ThreadLocalPageContext.getLog(config, "deploy");
		Info info = ConfigWebUtil.getEngine(config).getInfo();

		Attributes attr = manifest.getMainAttributes();

		readSymbolicName(label, StringUtil.unwrap(attr.getValue("symbolic-name")));
		readName(label, StringUtil.unwrap(attr.getValue("name")));
		label = name;
		readVersion(label, StringUtil.unwrap(attr.getValue("version")));
		label += " : " + version;
		readId(label, StringUtil.unwrap(attr.getValue("id")));
		readReleaseType(label, StringUtil.unwrap(attr.getValue("release-type")), isWeb);
		description = StringUtil.unwrap(attr.getValue("description"));
		trial = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("trial")), false);
		if (_img == null) _img = StringUtil.unwrap(attr.getValue("image"));
		image = _img;
		String cat = StringUtil.unwrap(attr.getValue("category"));
		if (StringUtil.isEmpty(cat, true)) cat = StringUtil.unwrap(attr.getValue("categories"));
		readCategories(label, cat);
		readCoreVersion(label, StringUtil.unwrap(attr.getValue("lucee-core-version")), info);
		readLoaderVersion(label, StringUtil.unwrap(attr.getValue("lucee-loader-version")));
		startBundles = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("start-bundles")), true);

		readAMF(label, StringUtil.unwrap(attr.getValue("amf")), logger);
		readResource(label, StringUtil.unwrap(attr.getValue("resource")), logger);
		readSearch(label, StringUtil.unwrap(attr.getValue("search")), logger);
		readORM(label, StringUtil.unwrap(attr.getValue("orm")), logger);
		readWebservice(label, StringUtil.unwrap(attr.getValue("webservice")), logger);
		readMonitor(label, StringUtil.unwrap(attr.getValue("monitor")), logger);
		readCache(label, StringUtil.unwrap(attr.getValue("cache")), logger);
		readCacheHandler(label, StringUtil.unwrap(attr.getValue("cache-handler")), logger);
		readJDBC(label, StringUtil.unwrap(attr.getValue("jdbc")), logger);
		readStartupHook(label, StringUtil.unwrap(attr.getValue("startup-hook")), logger);
		readMapping(label, StringUtil.unwrap(attr.getValue("mapping")), logger);
		readEventGatewayInstances(label, StringUtil.unwrap(attr.getValue("event-gateway-instance")), logger);
	}

	private void readManifestConfig(String id, Struct data, String label, String _img) throws ApplicationException {
		boolean isWeb = config instanceof ConfigWeb;
		type = isWeb ? "web" : "server";

		Log logger = ThreadLocalPageContext.getLog(config, "deploy");
		Info info = ConfigWebUtil.getEngine(config).getInfo();

		readSymbolicName(label, ConfigWebFactory.getAttr(data, "symbolicName", "symbolic-name"));
		readName(label, ConfigWebFactory.getAttr(data, "name"));
		label = name;
		readVersion(label, ConfigWebFactory.getAttr(data, "version"));
		label += " : " + version;
		readId(label, StringUtil.isEmpty(id) ? ConfigWebFactory.getAttr(data, "id") : id);
		readReleaseType(label, ConfigWebFactory.getAttr(data, "releaseType", "release-type"), isWeb);
		description = ConfigWebFactory.getAttr(data, "description");
		trial = Caster.toBooleanValue(ConfigWebFactory.getAttr(data, "trial"), false);
		if (_img == null) _img = ConfigWebFactory.getAttr(data, "image");
		image = _img;
		String cat = ConfigWebFactory.getAttr(data, "category");
		if (StringUtil.isEmpty(cat, true)) cat = ConfigWebFactory.getAttr(data, "categories");
		readCategories(label, cat);
		readCoreVersion(label, ConfigWebFactory.getAttr(data, "luceeCoreVersion", "lucee-core-version"), info);
		readLoaderVersion(label, ConfigWebFactory.getAttr(data, "luceeLoaderVersion", "lucee-loader-version"));
		startBundles = Caster.toBooleanValue(ConfigWebFactory.getAttr(data, "startBundles", "start-bundles"), true);

		readAMF(label, ConfigWebFactory.getAttr(data, "amf"), logger);
		readResource(label, ConfigWebFactory.getAttr(data, "resource"), logger);
		readSearch(label, ConfigWebFactory.getAttr(data, "search"), logger);
		readORM(label, ConfigWebFactory.getAttr(data, "orm"), logger);
		readWebservice(label, ConfigWebFactory.getAttr(data, "webservice"), logger);
		readMonitor(label, ConfigWebFactory.getAttr(data, "monitor"), logger);
		readCache(label, ConfigWebFactory.getAttr(data, "cache"), logger);
		readCacheHandler(label, ConfigWebFactory.getAttr(data, "cacheHandler", "cache-handler"), logger);
		readJDBC(label, ConfigWebFactory.getAttr(data, "jdbc"), logger);
		readMapping(label, ConfigWebFactory.getAttr(data, "mapping"), logger);
		readEventGatewayInstances(label, ConfigWebFactory.getAttr(data, "eventGatewayInstance", "event-gateway-instance"), logger);
	}

	private void readMapping(String label, String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			mappings = toSettings(logger, str);
			mappingsJson = str;
		}
		if (mappings == null) mappings = new ArrayList<Map<String, String>>();
	}

	private void readEventGatewayInstances(String label, String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			eventGatewayInstances = toSettingsObj(logger, str);
			eventGatewayInstancesJson = str;
		}
		if (eventGatewayInstances == null) eventGatewayInstances = new ArrayList<Map<String, Object>>();
	}

	private void readJDBC(String label, String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			jdbcs = toSettings(logger, str);
			jdbcsJson = str;
		}
		if (jdbcs == null) jdbcs = new ArrayList<Map<String, String>>();
	}

	private void readStartupHook(String label, String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			startupHooks = toSettings(logger, str);
			startupHooksJson = str;
		}
		if (startupHooks == null) startupHooks = new ArrayList<Map<String, String>>();
	}

	private void readCacheHandler(String label, String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			cacheHandlers = toSettings(logger, str);
			cacheHandlersJson = str;
		}
		if (cacheHandlers == null) cacheHandlers = new ArrayList<Map<String, String>>();
	}

	private void readCache(String label, String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			caches = toSettings(logger, str);
			cachesJson = str;
		}
		if (caches == null) caches = new ArrayList<Map<String, String>>();
	}

	private void readMonitor(String label, String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			monitors = toSettings(logger, str);
			monitorsJson = str;
		}
		if (monitors == null) monitors = new ArrayList<Map<String, String>>();
	}

	private void readORM(String label, String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			orms = toSettings(logger, str);
			ormsJson = str;
		}
		if (orms == null) orms = new ArrayList<Map<String, String>>();
	}

	private void readWebservice(String label, String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			webservices = toSettings(logger, str);
			webservicesJson = str;
		}
		if (webservices == null) webservices = new ArrayList<Map<String, String>>();
	}

	private void readSearch(String label, String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			searchs = toSettings(logger, str);
			searchsJson = str;
		}
		if (searchs == null) searchs = new ArrayList<Map<String, String>>();
	}

	private void readResource(String label, String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			resources = toSettings(logger, str);
			resourcesJson = str;
		}
		if (resources == null) resources = new ArrayList<Map<String, String>>();

	}

	private void readAMF(String label, String str, Log logger) {
		if (!StringUtil.isEmpty(str, true)) {
			amfs = toSettings(logger, str);
			amfsJson = str;
		}
		if (amfs == null) amfs = new ArrayList<Map<String, String>>();
	}

	private void readLoaderVersion(String label, String str) {
		minLoaderVersion = Caster.toDoubleValue(str, 0);
		/*
		 * if (minLoaderVersion > SystemUtil.getLoaderVersion()) { throw new InvalidVersion(
		 * "The Extension [" + label + "] cannot be loaded, " + Constants.NAME +
		 * " Loader Version must be at least [" + str + "], update the Lucee.jar first."); }
		 */
	}

	private void readCoreVersion(String label, String str, Info info) {

		minCoreVersion = StringUtil.isEmpty(str, true) ? null : new VersionRange(str);
		/*
		 * if (minCoreVersion != null && OSGiUtil.isNewerThan(minCoreVersion, info.getVersion())) { throw
		 * new InvalidVersion("The Extension [" + label + "] cannot be loaded, " + Constants.NAME +
		 * " Version must be at least [" + minCoreVersion.toString() + "], version is [" +
		 * info.getVersion().toString() + "]."); }
		 */
	}

	public void validate() throws ApplicationException {
		validate(ConfigWebUtil.getEngine(config).getInfo());
	}

	public void validate(Info info) throws ApplicationException {

		if (minCoreVersion != null && !minCoreVersion.isWithin(info.getVersion())) {
			throw new InvalidVersion("The Extension [" + getName() + "] cannot be loaded, " + Constants.NAME + " Version must be at least [" + minCoreVersion.toString()
					+ "], version is [" + info.getVersion().toString() + "].");
		}
		if (minLoaderVersion > SystemUtil.getLoaderVersion()) {
			throw new InvalidVersion("The Extension [" + getName() + "] cannot be loaded, " + Constants.NAME + " Loader Version must be at least [" + minLoaderVersion
					+ "], update the Lucee.jar first.");
		}
	}

	public boolean isValidFor(Info info) {
		if (minCoreVersion != null && !minCoreVersion.isWithin(info.getVersion())) {
			return false;
		}
		if (minLoaderVersion > SystemUtil.getLoaderVersion()) {
			return false;
		}
		return true;
	}

	private void readCategories(String label, String cat) {
		if (!StringUtil.isEmpty(cat, true)) {
			categories = ListUtil.trimItems(ListUtil.listToStringArray(cat, ","));
		}
		else categories = null;
	}

	private void readReleaseType(String label, String str, boolean isWeb) throws ApplicationException {
		if (((ConfigPro) ThreadLocalPageContext.getConfig(config)).getAdminMode() == ConfigImpl.ADMINMODE_SINGLE) return;
		// release type
		int rt = RELEASE_TYPE_ALL;
		if (!Util.isEmpty(str)) {
			str = str.trim();
			if ("server".equalsIgnoreCase(str)) rt = RELEASE_TYPE_SERVER;
			else if ("web".equalsIgnoreCase(str)) rt = RELEASE_TYPE_WEB;
		}
		if ((rt == RELEASE_TYPE_SERVER && isWeb) || (rt == RELEASE_TYPE_WEB && !isWeb)) {
			throw new ApplicationException(
					"Cannot install the Extension [" + label + "] in the " + type + " context, this Extension has the release type [" + toReleaseType(rt, "") + "].");
		}
		releaseType = rt;
	}

	private void readId(String label, String id) throws ApplicationException {
		this.id = StringUtil.unwrap(id);
		if (!Decision.isUUId(id)) {
			throw new ApplicationException("The Extension [" + label + "] has no valid id defined (" + id + "),id must be a valid UUID.");
		}
	}

	private void readVersion(String label, String version) throws ApplicationException {
		this.version = version;
		if (StringUtil.isEmpty(version)) {
			throw new ApplicationException("cannot deploy extension [" + label + "], this Extension has no version information.");
		}

	}

	private void readName(String label, String str) throws ApplicationException {
		str = StringUtil.unwrap(str);
		if (StringUtil.isEmpty(str, true)) {
			throw new ApplicationException("The Extension [" + label + "] has no name defined, a name is necesary.");
		}
		name = str.trim();
	}

	private void readSymbolicName(String label, String str) {
		str = StringUtil.unwrap(str);
		if (!StringUtil.isEmpty(str, true)) symbolicName = str.trim();
	}

	public void deployBundles(Config config, boolean load) throws IOException, BundleException {
		// no we read the content of the zip
		ZipInputStream zis = new ZipInputStream(IOUtil.toBufferedInputStream(extensionFile.getInputStream()));
		ZipEntry entry;
		String path;
		String fileName;
		try {
			while ((entry = zis.getNextEntry()) != null) {
				path = entry.getName();
				fileName = fileName(entry);
				// jars
				if (!entry.isDirectory() && (startsWith(path, type, "jars") || startsWith(path, type, "jar") || startsWith(path, type, "bundles")
						|| startsWith(path, type, "bundle") || startsWith(path, type, "lib") || startsWith(path, type, "libs")) && (StringUtil.endsWithIgnoreCase(path, ".jar"))) {

					Object obj = ConfigAdmin.installBundle(config, zis, fileName, version, false, false);
					// jar is not a bundle, only a regular jar
					if (!(obj instanceof BundleFile)) {
						Resource tmp = (Resource) obj;
						Resource tmpJar = tmp.getParentResource().getRealResource(ListUtil.last(path, "\\/"));
						tmp.moveTo(tmpJar);
						ConfigAdmin.updateJar(config, tmpJar, false);
					}
					else if (load) {
						OSGiUtil.loadBundle((BundleFile) obj);
					}
				}

				zis.closeEntry();
			}
		}
		finally {
			IOUtil.close(zis);
		}
	}

	public static Resource getExtensionInstalledFile(Config config, String id, String version, boolean validate) throws ApplicationException {
		String fileName = toHash(id, version, "lex");
		Resource res = getExtensionInstalledDir(config).getRealResource(fileName);
		if (validate && !res.exists()) throw new ApplicationException("Extension [" + fileName + "] was not found at [" + res + "]");
		return res;
	}

	private Struct getMetaData(Config config, String id, String version, Struct defaultValue) throws PageException, IOException {
		Resource file = getMetaDataFile(config, id, version);
		if (file.isFile()) return Caster.toStruct(new JSONExpressionInterpreter().interpret(null, IOUtil.toString(file, CharsetUtil.UTF8)));
		return defaultValue;
	}

	public static Resource getMetaDataFile(Config config, String id, String version) {
		String fileName = toHash(id, version, "mf");
		return getExtensionInstalledDir(config).getRealResource(fileName);
	}

	public static String toHash(String id, String version, String ext) {
		if (ext == null) ext = "lex";
		return HashUtil.create64BitHashAsString(id + version, Character.MAX_RADIX) + "." + ext;
	}

	public static Resource getExtensionInstalledDir(Config config) {

		return ((ConfigPro) config).getExtensionInstalledDir();
	}

	private static int getPhysicalExtensionCount(Config config) {
		final RefInteger count = new RefIntegerImpl(0);
		getExtensionInstalledDir(config).list(new ResourceNameFilter() {
			@Override
			public boolean accept(Resource res, String name) {
				if (StringUtil.endsWithIgnoreCase(name, ".lex")) count.plus(1);
				return false;
			}
		});
		return count.toInt();
	}

	public static void correctExtensions(Config config) throws PageException, IOException, BundleException, ConverterException {
		// reduce the amount of extension stored in available
		{
			int max = 2;
			Resource dir = ((ConfigPro) config).getExtensionAvailableDir();
			Resource[] resources = dir.listResources(LEX_FILTER);
			Map<String, List<Pair<RHExtension, Resource>>> map = new HashMap<>();
			RHExtension ext;
			List<Pair<RHExtension, Resource>> versions;
			if (resources != null) {
				for (Resource r: resources) {
					ext = new RHExtension(config, r);
					versions = map.get(ext.getId());
					if (versions == null) map.put(ext.getId(), versions = new ArrayList<>());
					versions.add(new Pair<RHExtension, Resource>(ext, r));
				}
			}

			for (Entry<String, List<Pair<RHExtension, Resource>>> entry: map.entrySet()) {
				if (entry.getValue().size() > max) {
					List<Pair<RHExtension, Resource>> list = entry.getValue();
					Collections.sort(list, new Comparator<Pair<RHExtension, Resource>>() {
						@Override
						public int compare(Pair<RHExtension, Resource> l, Pair<RHExtension, Resource> r) {
							try {
								return OSGiUtil.compare(OSGiUtil.toVersion(r.getName().getVersion()), OSGiUtil.toVersion(l.getName().getVersion()));
							}
							catch (BundleException e) {
								return 0;
							}
						}
					});
					int count = 0;
					for (Pair<RHExtension, Resource> pair: list) {
						if (++count > max) {
							if (!pair.getValue().delete()) ResourceUtil.deleteOnExit(pair.getValue());
						}
					}

				}
			}
		}

		if (config instanceof ConfigWebPro && ((ConfigWebPro) config).isSingle()) return;
		// extension defined in xml
		RHExtension[] xmlArrExtensions = ((ConfigPro) config).getRHExtensions();
		if (xmlArrExtensions.length == getPhysicalExtensionCount(config)) return; // all is OK
		RHExtension ext;
		Map<String, RHExtension> xmlExtensions = new HashMap<>();
		for (int i = 0; i < xmlArrExtensions.length; i++) {
			ext = xmlArrExtensions[i];
			xmlExtensions.put(ext.getId(), ext);
		}

		// Extension defined in filesystem
		Resource[] resources = getExtensionInstalledDir(config).listResources(LEX_FILTER);

		if (resources == null || resources.length == 0) return;
		int rt;
		RHExtension xmlExt;
		for (int i = 0; i < resources.length; i++) {
			ext = new RHExtension(config, resources[i]);
			xmlExt = xmlExtensions.get(ext.getId());
			if (xmlExt != null && (xmlExt.getVersion() + "").equals(ext.getVersion() + "")) continue;
			rt = ext.getReleaseType();
			ConfigAdmin._updateRHExtension((ConfigPro) config, resources[i], true, true, RHExtension.ACTION_MOVE);
		}
	}

	public static BundleDefinition[] toBundleDefinitions(String strBundles) {
		if (StringUtil.isEmpty(strBundles, true)) return EMPTY_BD;

		String[] arrStrs = toArray(strBundles);
		BundleDefinition[] arrBDs;
		if (!ArrayUtil.isEmpty(arrStrs)) {
			arrBDs = new BundleDefinition[arrStrs.length];
			int index;
			for (int i = 0; i < arrStrs.length; i++) {
				index = arrStrs[i].indexOf(':');
				if (index == -1) arrBDs[i] = new BundleDefinition(arrStrs[i].trim());
				else {
					try {
						arrBDs[i] = new BundleDefinition(arrStrs[i].substring(0, index).trim(), arrStrs[i].substring(index + 1).trim());
					}
					catch (BundleException e) {
						throw new PageRuntimeException(e);// should not happen
					}
				}
			}
		}
		else arrBDs = EMPTY_BD;
		return arrBDs;
	}

	public void populate(Struct el, boolean full) {
		String id = getId();
		String name = getName();
		if (StringUtil.isEmpty(name)) name = id;

		if (!full) el.clear();

		el.setEL("id", id);
		el.setEL("name", name);
		el.setEL("version", getVersion());

		if (!full) return;

		// newly added
		// start bundles (IMPORTANT:this key is used to reconize a newer entry, so do not change)
		el.setEL("startBundles", Caster.toString(getStartBundles()));

		// release type
		el.setEL("releaseType", toReleaseType(getReleaseType(), "all"));

		// Description
		if (StringUtil.isEmpty(getDescription())) el.setEL("description", toStringForAttr(getDescription()));
		else el.removeEL(KeyImpl.init("description"));

		// Trial
		el.setEL("trial", Caster.toString(isTrial()));

		// Image
		if (StringUtil.isEmpty(getImage())) el.setEL("image", toStringForAttr(getImage()));
		else el.removeEL(KeyImpl.init("image"));

		// Categories
		String[] cats = getCategories();
		if (!ArrayUtil.isEmpty(cats)) {
			StringBuilder sb = new StringBuilder();
			for (String cat: cats) {
				if (sb.length() > 0) sb.append(',');
				sb.append(toStringForAttr(cat).replace(',', ' '));
			}
			el.setEL("categories", sb.toString());
		}
		else el.removeEL(KeyImpl.init("categories"));

		// core version
		if (minCoreVersion != null) el.setEL("luceeCoreVersion", toStringForAttr(minCoreVersion.toString()));
		else el.removeEL(KeyImpl.init("luceeCoreVersion"));

		// loader version
		if (minLoaderVersion > 0) el.setEL("loaderVersion", Caster.toString(minLoaderVersion));
		else el.removeEL(KeyImpl.init("loaderVersion"));

		// amf
		if (!StringUtil.isEmpty(amfsJson)) el.setEL("amf", toStringForAttr(amfsJson));
		else el.removeEL(KeyImpl.init("amf"));

		// resource
		if (!StringUtil.isEmpty(resourcesJson)) el.setEL("resource", toStringForAttr(resourcesJson));
		else el.removeEL(KeyImpl.init("resource"));

		// search
		if (!StringUtil.isEmpty(searchsJson)) el.setEL("search", toStringForAttr(searchsJson));
		else el.removeEL(KeyImpl.init("search"));

		// orm
		if (!StringUtil.isEmpty(ormsJson)) el.setEL("orm", toStringForAttr(ormsJson));
		else el.removeEL(KeyImpl.init("orm"));

		// webservice
		if (!StringUtil.isEmpty(webservicesJson)) el.setEL("webservice", toStringForAttr(webservicesJson));
		else el.removeEL(KeyImpl.init("webservice"));

		// monitor
		if (!StringUtil.isEmpty(monitorsJson)) el.setEL("monitor", toStringForAttr(monitorsJson));
		else el.removeEL(KeyImpl.init("monitor"));

		// cache
		if (!StringUtil.isEmpty(cachesJson)) el.setEL("cache", toStringForAttr(cachesJson));
		else el.removeEL(KeyImpl.init("cache"));

		// cache-handler
		if (!StringUtil.isEmpty(cacheHandlersJson)) el.setEL("cacheHandler", toStringForAttr(cacheHandlersJson));
		else el.removeEL(KeyImpl.init("cacheHandler"));

		// jdbc
		if (!StringUtil.isEmpty(jdbcsJson)) el.setEL("jdbc", toStringForAttr(jdbcsJson));
		else el.removeEL(KeyImpl.init("jdbc"));

		// startup-hook
		if (!StringUtil.isEmpty(startupHooksJson)) el.setEL("startupHook", toStringForAttr(startupHooksJson));
		else el.removeEL(KeyImpl.init("startupHook"));

		// mapping
		if (!StringUtil.isEmpty(mappingsJson)) el.setEL("mapping", toStringForAttr(mappingsJson));
		else el.removeEL(KeyImpl.init("mapping"));

		// event-gateway-instances
		if (!StringUtil.isEmpty(eventGatewayInstancesJson)) el.setEL("eventGatewayInstances", toStringForAttr(eventGatewayInstancesJson));
		else el.removeEL(KeyImpl.init("eventGatewayInstances"));
	}

	private String toStringForAttr(String str) {
		if (str == null) return "";
		return str;
	}

	private static String[] toArray(String str) {
		if (StringUtil.isEmpty(str, true)) return new String[0];
		return ListUtil.listToStringArray(str.trim(), ',');
	}

	public static Query toQuery(Config config, List<RHExtension> children, Query qry) throws PageException {
		Log log = ThreadLocalPageContext.getLog(config, "deploy");
		if (qry == null) qry = createQuery();
		Iterator<RHExtension> it = children.iterator();
		while (it.hasNext()) {
			try {
				it.next().populate(qry); // ,i+1
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log.error("extension", t);
			}
		}
		return qry;
	}

	public static Query toQuery(Config config, RHExtension[] children, Query qry) throws PageException {
		Log log = ThreadLocalPageContext.getLog(config, "deploy");
		if (qry == null) qry = createQuery();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				try {
					if (children[i] != null) children[i].populate(qry); // ,i+1
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log.log(Log.LEVEL_WARN, "extension", t);
				}
			}
		}
		return qry;
	}

	private static Query createQuery() throws DatabaseException {
		return new QueryImpl(
				new Key[] { KeyConstants._id, KeyConstants._version, KeyConstants._name, KeyConstants._symbolicName, KeyConstants._type, KeyConstants._description,
						KeyConstants._image, KeyConstants._releaseType, KeyConstants._trial, KeyConstants._categories, KeyConstants._startBundles, KeyConstants._bundles,
						KeyConstants._flds, KeyConstants._tlds, KeyConstants._tags, KeyConstants._functions, KeyConstants._contexts, KeyConstants._webcontexts,
						KeyConstants._config, KeyConstants._applications, KeyConstants._components, KeyConstants._plugins, KeyConstants._eventGateways, KeyConstants._archives },
				0, "Extensions");
	}

	private void populate(Query qry) throws PageException, IOException, BundleException {
		int row = qry.addRow();
		qry.setAt(KeyConstants._id, row, getId());
		qry.setAt(KeyConstants._name, row, getName());
		qry.setAt(KeyConstants._symbolicName, row, getSymbolicName());
		qry.setAt(KeyConstants._image, row, getImage());
		qry.setAt(KeyConstants._type, row, type);
		qry.setAt(KeyConstants._description, row, description);
		qry.setAt(KeyConstants._version, row, getVersion() == null ? null : getVersion().toString());
		qry.setAt(KeyConstants._trial, row, isTrial());
		qry.setAt(KeyConstants._releaseType, row, toReleaseType(getReleaseType(), "all"));
		// qry.setAt(JARS, row,Caster.toArray(getJars()));
		qry.setAt(KeyConstants._flds, row, Caster.toArray(getFlds()));
		qry.setAt(KeyConstants._tlds, row, Caster.toArray(getTlds()));
		qry.setAt(KeyConstants._functions, row, Caster.toArray(getFunctions()));
		qry.setAt(KeyConstants._archives, row, Caster.toArray(getArchives()));
		qry.setAt(KeyConstants._tags, row, Caster.toArray(getTags()));
		qry.setAt(KeyConstants._contexts, row, Caster.toArray(getContexts()));
		qry.setAt(KeyConstants._webcontexts, row, Caster.toArray(getWebContexts()));
		qry.setAt(KeyConstants._config, row, Caster.toArray(getConfigs()));
		qry.setAt(KeyConstants._eventGateways, row, Caster.toArray(getEventGateways()));
		qry.setAt(KeyConstants._categories, row, Caster.toArray(getCategories()));
		qry.setAt(KeyConstants._applications, row, Caster.toArray(getApplications()));
		qry.setAt(KeyConstants._components, row, Caster.toArray(getComponents()));
		qry.setAt(KeyConstants._plugins, row, Caster.toArray(getPlugins()));
		qry.setAt(KeyConstants._startBundles, row, Caster.toBoolean(getStartBundles()));

		BundleInfo[] bfs = getBundles();
		Query qryBundles = new QueryImpl(new Key[] { KeyConstants._name, KeyConstants._version }, bfs == null ? 0 : bfs.length, "bundles");
		if (bfs != null) {
			for (int i = 0; i < bfs.length; i++) {
				qryBundles.setAt(KeyConstants._name, i + 1, bfs[i].getSymbolicName());
				if (bfs[i].getVersion() != null) qryBundles.setAt(KeyConstants._version, i + 1, bfs[i].getVersionAsString());
			}
		}
		qry.setAt(KeyConstants._bundles, row, qryBundles);
	}

	public Struct toStruct() throws PageException {
		Struct sct = new StructImpl();
		sct.set(KeyConstants._id, getId());
		sct.set(KeyConstants._symbolicName, getSymbolicName());
		sct.set(KeyConstants._name, getName());
		sct.set(KeyConstants._image, getImage());
		sct.set(KeyConstants._description, description);
		sct.set(KeyConstants._version, getVersion() == null ? null : getVersion().toString());
		sct.set(KeyConstants._trial, isTrial());
		sct.set(KeyConstants._releaseType, toReleaseType(getReleaseType(), "all"));
		// sct.set(JARS, row,Caster.toArray(getJars()));
		try {
			sct.set(KeyConstants._flds, Caster.toArray(getFlds()));
			sct.set(KeyConstants._tlds, Caster.toArray(getTlds()));
			sct.set(KeyConstants._functions, Caster.toArray(getFunctions()));
			sct.set(KeyConstants._archives, Caster.toArray(getArchives()));
			sct.set(KeyConstants._tags, Caster.toArray(getTags()));
			sct.set(KeyConstants._contexts, Caster.toArray(getContexts()));
			sct.set(KeyConstants._webcontexts, Caster.toArray(getWebContexts()));
			sct.set(KeyConstants._config, Caster.toArray(getConfigs()));
			sct.set(KeyConstants._eventGateways, Caster.toArray(getEventGateways()));
			sct.set(KeyConstants._categories, Caster.toArray(getCategories()));
			sct.set(KeyConstants._applications, Caster.toArray(getApplications()));
			sct.set(KeyConstants._components, Caster.toArray(getComponents()));
			sct.set(KeyConstants._plugins, Caster.toArray(getPlugins()));
			sct.set(KeyConstants._startBundles, Caster.toBoolean(getStartBundles()));

			BundleInfo[] bfs = getBundles();
			Query qryBundles = new QueryImpl(new Key[] { KeyConstants._name, KeyConstants._version }, bfs == null ? 0 : bfs.length, "bundles");
			if (bfs != null) {
				for (int i = 0; i < bfs.length; i++) {
					qryBundles.setAt(KeyConstants._name, i + 1, bfs[i].getSymbolicName());
					if (bfs[i].getVersion() != null) qryBundles.setAt(KeyConstants._version, i + 1, bfs[i].getVersionAsString());
				}
			}
			sct.set(KeyConstants._bundles, qryBundles);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		return sct;
	}

	public String getId() {
		return id;
	}

	public String getImage() {
		return image;
	}

	public String getVersion() {
		return version;
	}

	public boolean getStartBundles() {
		return startBundles;
	}

	private static Manifest toManifest(Config config, InputStream is, Manifest defaultValue) {
		try {
			Charset cs = config.getResourceCharset();
			String str = IOUtil.toString(is, cs);
			if (StringUtil.isEmpty(str, true)) return defaultValue;
			str = str.trim() + "\n";
			return new Manifest(new ByteArrayInputStream(str.getBytes(cs)));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	private static String toBase64(InputStream is, String defaultValue) {
		try {
			byte[] bytes = IOUtil.toBytes(is);
			if (ArrayUtil.isEmpty(bytes)) return defaultValue;
			return Caster.toB64(bytes, defaultValue);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static ClassDefinition<?> toClassDefinition(Config config, Map<String, ?> map, ClassDefinition<?> defaultValue) {
		String _class = Caster.toString(map.get("class"), null);

		String _name = Caster.toString(map.get("bundle-name"), null);
		if (StringUtil.isEmpty(_name)) _name = Caster.toString(map.get("bundleName"), null);
		if (StringUtil.isEmpty(_name)) _name = Caster.toString(map.get("bundlename"), null);
		if (StringUtil.isEmpty(_name)) _name = Caster.toString(map.get("name"), null);

		String _version = Caster.toString(map.get("bundle-version"), null);
		if (StringUtil.isEmpty(_version)) _version = Caster.toString(map.get("bundleVersion"), null);
		if (StringUtil.isEmpty(_version)) _version = Caster.toString(map.get("bundleversion"), null);
		if (StringUtil.isEmpty(_version)) _version = Caster.toString(map.get("version"), null);
		if (StringUtil.isEmpty(_class)) return defaultValue;
		return new lucee.transformer.library.ClassDefinitionImpl(_class, _name, _version, config.getIdentification());
	}

	private static List<Map<String, String>> toSettings(Log log, String str) {
		List<Map<String, String>> list = new ArrayList<>();
		_toSettings(list, log, str, true);
		return list;
	}

	private static List<Map<String, Object>> toSettingsObj(Log log, String str) {
		List<Map<String, Object>> list = new ArrayList<>();
		_toSettings(list, log, str, false);
		return list;
	}

	private static void _toSettings(List list, Log log, String str, boolean valueAsString) {
		try {
			Object res = DeserializeJSON.call(null, str);
			// only a single row
			if (!Decision.isArray(res) && Decision.isStruct(res)) {
				_toSetting(list, Caster.toMap(res), valueAsString);
				return;
			}
			// multiple rows
			if (Decision.isArray(res)) {
				List tmpList = Caster.toList(res);
				Iterator it = tmpList.iterator();
				while (it.hasNext()) {
					_toSetting(list, Caster.toMap(it.next()), valueAsString);
				}
				return;
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log.error("Extension Installation", t);
		}

		return;
	}

	private static void _toSetting(List list, Map src, boolean valueAsString) throws PageException {
		Entry e;
		Iterator<Entry> it = src.entrySet().iterator();
		Map map = new HashMap();
		while (it.hasNext()) {
			e = it.next();
			map.put(Caster.toString(e.getKey()), valueAsString ? Caster.toString(e.getValue()) : e.getValue());
		}
		list.add(map);
	}

	private static boolean startsWith(String path, String type, String name) {
		return StringUtil.startsWithIgnoreCase(path, name + "/") || StringUtil.startsWithIgnoreCase(path, type + "/" + name + "/");
	}

	private static String fileName(ZipEntry entry) {
		String name = entry.getName();
		int index = name.lastIndexOf('/');
		if (index == -1) return name;
		return name.substring(index + 1);
	}

	private static String subFolder(ZipEntry entry) {
		String name = entry.getName();
		int index = name.indexOf('/');
		if (index == -1) return name;
		return name.substring(index + 1);
	}

	public String getName() {
		return name;
	}

	public String getSymbolicName() {
		return StringUtil.isEmpty(symbolicName) ? id : symbolicName;
	}

	public boolean isTrial() {
		return trial;
	}

	public String getDescription() {
		return description;
	}

	public int getReleaseType() {
		return releaseType;
	}

	public BundleInfo[] getBundles() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return bundles;
	}

	public BundleInfo[] getBundles(BundleInfo[] defaultValue) {
		if (!loaded) {
			try {
				load(extensionFile);
			}
			catch (Exception e) {
				return defaultValue;
			}
		}
		return bundles;
	}

	public String[] getFlds() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return flds == null ? EMPTY : flds;
	}

	public String[] getJars() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return jars == null ? EMPTY : jars;
	}

	public String[] getTlds() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return tlds == null ? EMPTY : tlds;
	}

	public String[] getFunctions() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return functions == null ? EMPTY : functions;
	}

	public String[] getArchives() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return archives == null ? EMPTY : archives;
	}

	public String[] getTags() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return tags == null ? EMPTY : tags;
	}

	public String[] getEventGateways() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return gateways == null ? EMPTY : gateways;
	}

	public String[] getApplications() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return applications == null ? EMPTY : applications;
	}

	public String[] getComponents() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return components == null ? EMPTY : components;
	}

	public String[] getPlugins() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return plugins == null ? EMPTY : plugins;
	}

	public String[] getContexts() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return contexts == null ? EMPTY : contexts;
	}

	public String[] getConfigs() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return configs == null ? EMPTY : configs;
	}

	public String[] getWebContexts() throws ApplicationException, IOException, BundleException {
		if (!loaded) load(extensionFile);
		return webContexts == null ? EMPTY : webContexts;
	}

	public String[] getCategories() {
		return categories == null ? EMPTY : categories;
	}

	public List<Map<String, String>> getCaches() {
		return caches;
	}

	public List<Map<String, String>> getCacheHandlers() {
		return cacheHandlers;
	}

	public List<Map<String, String>> getOrms() {
		return orms;
	}

	public List<Map<String, String>> getWebservices() {
		return webservices;
	}

	public List<Map<String, String>> getMonitors() {
		return monitors;
	}

	public List<Map<String, String>> getSearchs() {
		return searchs;
	}

	public List<Map<String, String>> getResources() {
		return resources;
	}

	public List<Map<String, String>> getAMFs() {
		return amfs;
	}

	public List<Map<String, String>> getJdbcs() {
		return jdbcs;
	}

	public List<Map<String, String>> getStartupHooks() {
		return startupHooks;
	}

	public List<Map<String, String>> getMappings() {
		return mappings;
	}

	public List<Map<String, Object>> getEventGatewayInstances() {
		return eventGatewayInstances;
	}

	public Resource getExtensionFile() {
		if (!extensionFile.exists()) {
			Config c = ThreadLocalPageContext.getConfig();
			if (c != null) {
				Resource res = DeployHandler.getExtension(c, new ExtensionDefintion(id, version), null);
				if (res != null && res.exists()) {
					try {
						IOUtil.copy(res, extensionFile);
					}
					catch (IOException e) {
						res.delete();
					}
				}
			}
		}
		return extensionFile;
	}

	@Override
	public boolean equals(Object objOther) {
		if (objOther == this) return true;

		if (objOther instanceof RHExtension) {
			RHExtension other = (RHExtension) objOther;

			if (!getId().equals(other.getId())) return false;
			if (!getName().equals(other.getName())) return false;
			if (!getVersion().equals(other.getVersion())) return false;
			if (isTrial() != other.isTrial()) return false;
			return true;
		}
		if (objOther instanceof ExtensionDefintion) {
			ExtensionDefintion ed = (ExtensionDefintion) objOther;
			if (!ed.getId().equalsIgnoreCase(getId())) return false;
			if (ed.getVersion() == null || getVersion() == null) return true;
			return ed.getVersion().equalsIgnoreCase(getVersion());
		}
		return false;
	}

	public static String toReleaseType(int releaseType, String defaultValue) {
		if (releaseType == RELEASE_TYPE_WEB) return "web";
		if (releaseType == RELEASE_TYPE_SERVER) return "server";
		if (releaseType == RELEASE_TYPE_ALL) return "all";
		return defaultValue;
	}

	public static int toReleaseType(String releaseType, int defaultValue) {
		if ("web".equalsIgnoreCase(releaseType)) return RELEASE_TYPE_WEB;
		if ("server".equalsIgnoreCase(releaseType)) return RELEASE_TYPE_SERVER;
		if ("all".equalsIgnoreCase(releaseType)) return RELEASE_TYPE_ALL;
		if ("both".equalsIgnoreCase(releaseType)) return RELEASE_TYPE_ALL;
		return defaultValue;
	}

	public static List<ExtensionDefintion> toExtensionDefinitions(String str) {
		// first we split the list
		List<ExtensionDefintion> rtn = new ArrayList<ExtensionDefintion>();
		if (StringUtil.isEmpty(str)) return rtn;

		String[] arr = ListUtil.trimItems(ListUtil.listToStringArray(str, ','));
		if (ArrayUtil.isEmpty(arr)) return rtn;
		ExtensionDefintion ed;
		for (int i = 0; i < arr.length; i++) {
			ed = toExtensionDefinition(arr[i]);
			if (ed != null) rtn.add(ed);
		}
		return rtn;
	}

	// TODO call public static ExtensionDefintion toExtensionDefinition(String id, Map<String, String>
	// data)
	public static ExtensionDefintion toExtensionDefinition(String s) {
		if (StringUtil.isEmpty(s, true)) return null;
		s = s.trim();

		String[] arrr;
		int index;
		arrr = ListUtil.trimItems(ListUtil.listToStringArray(s, ';'));
		ExtensionDefintion ed = new ExtensionDefintion();
		String name;
		Resource res;
		Config c = ThreadLocalPageContext.getConfig();
		for (String ss: arrr) {
			res = null;
			index = ss.indexOf('=');
			if (index != -1) {
				name = ss.substring(0, index).trim();
				ed.setParam(name, ss.substring(index + 1).trim());
				if ("path".equalsIgnoreCase(name) && c != null) {
					res = ResourceUtil.toResourceExisting(c, ss.substring(index + 1).trim(), null);
				}
			}
			else if (ed.getId() == null || Decision.isUUId(ed.getId())) {
				if (c == null || Decision.isUUId(ss) || (res = ResourceUtil.toResourceExisting(ThreadLocalPageContext.getConfig(), ss.trim(), null)) == null) ed.setId(ss);
			}

			if (res != null && res.isFile()) {

				Resource trgDir = c.getLocalExtensionProviderDirectory();
				Resource trg = trgDir.getRealResource(res.getName());
				if (!res.equals(trg) && !trg.isFile()) {
					try {
						IOUtil.copy(res, trg);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (!trg.isFile()) continue;

				try {
					return new RHExtension(c, trg).toExtensionDefinition();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return ed;
	}

	public static ExtensionDefintion toExtensionDefinition(Config config, String id, Map<String, String> data) {
		if (data == null || data.size() == 0) return null;

		ExtensionDefintion ed = new ExtensionDefintion();

		// validate id
		if (Decision.isUUId(id)) {
			ed.setId(id);
		}

		String name;
		Resource res;
		config = ThreadLocalPageContext.getConfig(config);
		for (Entry<String, String> entry: data.entrySet()) {
			name = entry.getKey().trim();
			if (!"id".equalsIgnoreCase(name)) ed.setParam(name, entry.getValue().trim());
			if ("path".equalsIgnoreCase(name) || "url".equalsIgnoreCase(name) || "resource".equalsIgnoreCase(name)) {
				res = ResourceUtil.toResourceExisting(config, entry.getValue().trim(), null);

				if (ed.getId() == null && res != null && res.isFile()) {

					Resource trgDir = config.getLocalExtensionProviderDirectory();
					Resource trg = trgDir.getRealResource(res.getName());
					if (!res.equals(trg) && !trg.isFile()) {
						try {
							IOUtil.copy(res, trg);
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (!trg.isFile()) continue;

					try {
						return new RHExtension(config, trg).toExtensionDefinition();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
		if (ed.getId() == null) return null;
		return ed;

	}

	public static List<RHExtension> toRHExtensions(List<ExtensionDefintion> eds) throws PageException {
		try {
			final List<RHExtension> rtn = new ArrayList<RHExtension>();
			Iterator<ExtensionDefintion> it = eds.iterator();
			ExtensionDefintion ed;
			while (it.hasNext()) {
				ed = it.next();
				if (ed != null) rtn.add(ed.toRHExtension());
			}
			return rtn;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public static class InvalidVersion extends ApplicationException {

		private static final long serialVersionUID = 8561299058941139724L;

		public InvalidVersion(String message) {
			super(message);
		}

	}

	public ExtensionDefintion toExtensionDefinition() {
		ExtensionDefintion ed = new ExtensionDefintion(getId(), getVersion());
		ed.setParam("symbolic-name", getSymbolicName());
		ed.setParam("description", getDescription());
		return ed;
	}

	@Override
	public String toString() {
		ExtensionDefintion ed = new ExtensionDefintion(getId(), getVersion());
		ed.setParam("symbolic-name", getSymbolicName());
		ed.setParam("description", getDescription());
		return ed.toString();
	}

	public static void removeDuplicates(Array arrExtensions) throws PageException, BundleException {
		Iterator<Entry<Key, Object>> it = arrExtensions.entryIterator();
		Entry<Key, Object> e;
		Struct child;
		String id, version;
		Map<String, Pair<Version, Key>> existing = new HashMap<>();
		List<Integer> toremove = null;
		Pair<Version, Key> pair;
		while (it.hasNext()) {
			e = it.next();
			child = Caster.toStruct(e.getValue(), null);
			if (child == null) continue;
			id = Caster.toString(child.get(KeyConstants._id, null), null);
			if (StringUtil.isEmpty(id)) continue;
			pair = existing.get(id);
			version = Caster.toString(child.get(KeyConstants._version, null), null);
			if (StringUtil.isEmpty(version)) continue;
			Version nv = OSGiUtil.toVersion(version);
			if (pair != null) {
				if (toremove == null) toremove = new ArrayList<>();
				toremove.add(Caster.toInteger(OSGiUtil.isNewerThan(pair.getName(), nv) ? e.getKey() : pair.getValue()));

			}
			existing.put(id, new Pair<Version, Key>(nv, e.getKey()));
		}

		if (toremove != null) {
			int[] removes = ArrayUtil.toIntArray(toremove);
			Arrays.sort(removes);
			for (int i = removes.length - 1; i >= 0; i--) {
				arrExtensions.removeE(removes[i]);
			}
		}
	}
}
