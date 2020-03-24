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
package lucee.runtime.extension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.osgi.framework.BundleException;
import org.w3c.dom.Element;

import lucee.Info;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.util.Util;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.Constants;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.config.XMLConfigAdmin;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.engine.ThreadLocalConfig;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.functions.conversion.DeserializeJSON;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.osgi.BundleFile;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.osgi.VersionRange;
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

	private static final long serialVersionUID = 2904020095330689714L;

	// public static final Key JARS = KeyImpl.init("jars");
	private static final Key BUNDLES = KeyImpl.init("bundles");
	private static final Key TLDS = KeyImpl.init("tlds");
	private static final Key FLDS = KeyImpl.init("flds");
	private static final Key EVENT_GATEWAYS = KeyImpl.init("eventGateways");
	private static final Key TAGS = KeyImpl.init("tags");
	private static final Key FUNCTIONS = KeyConstants._functions;
	private static final Key ARCHIVES = KeyImpl.init("archives");
	private static final Key CONTEXTS = KeyImpl.init("contexts");
	private static final Key WEBCONTEXTS = KeyImpl.init("webcontexts");
	private static final Key CONFIG = KeyImpl.init("config");
	private static final Key COMPONENTS = KeyImpl.init("components");
	private static final Key APPLICATIONS = KeyImpl.init("applications");
	private static final Key CATEGORIES = KeyImpl.init("categories");
	private static final Key PLUGINS = KeyImpl.init("plugins");
	private static final Key START_BUNDLES = KeyImpl.init("startBundles");
	private static final Key TRIAL = KeyImpl.init("trial");
	private static final Key RELEASE_TYPE = KeyImpl.init("releaseType");
	private static final Key SYMBOLIC_NAME = KeyImpl.init("symbolicName");

	private static final String[] EMPTY = new String[0];
	private static final BundleDefinition[] EMPTY_BD = new BundleDefinition[0];

	public static final int RELEASE_TYPE_ALL = 0;
	public static final int RELEASE_TYPE_SERVER = 1;
	public static final int RELEASE_TYPE_WEB = 2;

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
	private List<Map<String, String>> searchs;
	private List<Map<String, String>> resources;
	private List<Map<String, String>> amfs;
	private List<Map<String, String>> jdbcs;
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

	private String mappingsJson;

	private String eventGatewayInstancesJson;

	private boolean loaded;

	private final Config config;

	public final boolean softLoaded;

	public RHExtension(Config config, Element el) throws PageException, IOException, BundleException {
		this.config = config;
		// we have a newer version that holds the Manifest data
		if (el.hasAttribute("start-bundles")) {
			this.extensionFile = toResource(config, el);
			boolean _softLoaded;
			try {
				readManifestConfig(el, extensionFile.getAbsolutePath(), null);
				_softLoaded = true;
			}
			catch (InvalidVersion iv) {
				throw iv;
			}
			catch (ApplicationException ae) {
				init(toResource(config, el), false);
				_softLoaded = false;
			}
			softLoaded = _softLoaded;
		}
		else {
			init(toResource(config, el), false);
			softLoaded = false;
		}
	}

	public RHExtension(Config config, Resource ext, boolean moveIfNecessary) throws PageException, IOException, BundleException {
		this.config = config;
		init(ext, moveIfNecessary);
		softLoaded = false;
	}

	private void init(Resource ext, boolean moveIfNecessary) throws PageException, IOException, BundleException {
		// make sure the config is registerd with the thread
		if (ThreadLocalPageContext.getConfig() == null) ThreadLocalConfig.register(config);

		// is it a web or server context?
		type = config instanceof ConfigWeb ? "web" : "server";

		load(ext);

		this.extensionFile = ext;
		if (moveIfNecessary) move(ext);
	}

	// copy the file to extension dir if it is not already there
	private void move(Resource ext) throws PageException {
		Resource trg;
		Resource trgDir;
		try {
			trg = getExtensionFile(config, ext, id, name, version);
			trgDir = trg.getParentResource();
			trgDir.mkdirs();
			if (!ext.getParentResource().equals(trgDir)) {
				if (trg.exists()) trg.delete();
				ResourceUtil.moveTo(ext, trg, true);
				this.extensionFile = trg;
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public static Manifest getManifestFromFile(Config config, Resource file) throws IOException, BundleException, ApplicationException {
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
		boolean isPack200;

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
				isPack200 = false;

				if (!entry.isDirectory() && path.equalsIgnoreCase("META-INF/MANIFEST.MF")) {
					manifest = toManifest(config, zis, null);
				}
				else if (!entry.isDirectory() && path.equalsIgnoreCase("META-INF/logo.png")) {
					_img = toBase64(zis, null);
				}

				// jars
				else if (!entry.isDirectory()
						&& (startsWith(path, type, "jars") || startsWith(path, type, "jar") || startsWith(path, type, "bundles") || startsWith(path, type, "bundle")
								|| startsWith(path, type, "lib") || startsWith(path, type, "libs"))
						&& (StringUtil.endsWithIgnoreCase(path, ".jar") || (isPack200 = StringUtil.endsWithIgnoreCase(path, ".jar.pack.gz")))) {

					jars.add(fileName);
					BundleInfo bi = BundleInfo.getInstance(fileName, zis, false, isPack200);
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
						&& (StringUtil.endsWithIgnoreCase(path, "." + Constants.getCFMLComponentExtension())
								|| StringUtil.endsWithIgnoreCase(path, "." + Constants.getLuceeComponentExtension())))
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
		Log logger = ((ConfigImpl) config).getLog("deploy");
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
		readMapping(label, StringUtil.unwrap(attr.getValue("mapping")), logger);
		readEventGatewayInstances(label, StringUtil.unwrap(attr.getValue("event-gateway-instance")), logger);
	}

	private void readManifestConfig(Element el, String label, String _img) throws ApplicationException {
		boolean isWeb = config instanceof ConfigWeb;
		type = isWeb ? "web" : "server";

		Log logger = ((ConfigImpl) config).getLog("deploy");
		Info info = ConfigWebUtil.getEngine(config).getInfo();

		readSymbolicName(label, el.getAttribute("symbolic-name"));
		readName(label, el.getAttribute("name"));
		label = name;
		readVersion(label, el.getAttribute("version"));
		label += " : " + version;
		readId(label, el.getAttribute("id"));
		readReleaseType(label, el.getAttribute("release-type"), isWeb);
		description = el.getAttribute("description");
		trial = Caster.toBooleanValue(el.getAttribute("trial"), false);
		if (_img == null) _img = el.getAttribute("image");
		image = _img;
		String cat = el.getAttribute("category");
		if (StringUtil.isEmpty(cat, true)) cat = el.getAttribute("categories");
		readCategories(label, cat);
		readCoreVersion(label, el.getAttribute("lucee-core-version"), info);
		readLoaderVersion(label, el.getAttribute("lucee-loader-version"));
		startBundles = Caster.toBooleanValue(el.getAttribute("start-bundles"), true);

		readAMF(label, el.getAttribute("amf"), logger);
		readResource(label, el.getAttribute("resource"), logger);
		readSearch(label, el.getAttribute("search"), logger);
		readORM(label, el.getAttribute("orm"), logger);
		readWebservice(label, el.getAttribute("webservice"), logger);
		readMonitor(label, el.getAttribute("monitor"), logger);
		readCache(label, el.getAttribute("cache"), logger);
		readCacheHandler(label, el.getAttribute("cache-handler"), logger);
		readJDBC(label, el.getAttribute("jdbc"), logger);
		readMapping(label, el.getAttribute("mapping"), logger);
		readEventGatewayInstances(label, el.getAttribute("event-gateway-instance"), logger);
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

	private void readLoaderVersion(String label, String str) throws ApplicationException {
		minLoaderVersion = Caster.toDoubleValue(str, 0);
		/*
		 * if (minLoaderVersion > SystemUtil.getLoaderVersion()) { throw new InvalidVersion(
		 * "The Extension [" + label + "] cannot be loaded, " + Constants.NAME +
		 * " Loader Version must be at least [" + str + "], update the Lucee.jar first."); }
		 */
	}

	private void readCoreVersion(String label, String str, Info info) throws ApplicationException {

		minCoreVersion = StringUtil.isEmpty(str, true) ? null : new VersionRange(str);
		/*
		 * if (minCoreVersion != null && Util.isNewerThan(minCoreVersion, info.getVersion())) { throw new
		 * InvalidVersion("The Extension [" + label + "] cannot be loaded, " + Constants.NAME +
		 * " Version must be at least [" + minCoreVersion.toString() + "], version is [" +
		 * info.getVersion().toString() + "]."); }
		 */
	}

	public void validate() throws ApplicationException {
		Info info = ConfigWebUtil.getEngine(config).getInfo();

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

	private void readSymbolicName(String label, String str) throws ApplicationException {
		str = StringUtil.unwrap(str);
		if (!StringUtil.isEmpty(str, true)) symbolicName = str.trim();
	}

	public void deployBundles(Config config) throws IOException, BundleException {
		// no we read the content of the zip
		ZipInputStream zis = new ZipInputStream(IOUtil.toBufferedInputStream(extensionFile.getInputStream()));
		ZipEntry entry;
		String path;
		String fileName;
		boolean isPack200;
		try {
			while ((entry = zis.getNextEntry()) != null) {
				path = entry.getName();
				fileName = fileName(entry);
				isPack200 = false;
				// jars
				if (!entry.isDirectory()
						&& (startsWith(path, type, "jars") || startsWith(path, type, "jar") || startsWith(path, type, "bundles") || startsWith(path, type, "bundle")
								|| startsWith(path, type, "lib") || startsWith(path, type, "libs"))
						&& (StringUtil.endsWithIgnoreCase(path, ".jar") || (isPack200 = StringUtil.endsWithIgnoreCase(path, ".jar.pack.gz")))) {

					Object obj = XMLConfigAdmin.installBundle(config, zis, fileName, version, false, false, isPack200);
					// jar is not a bundle, only a regular jar
					if (!(obj instanceof BundleFile)) {
						Resource tmp = (Resource) obj;
						Resource tmpJar = tmp.getParentResource().getRealResource(ListUtil.last(path, "\\/"));
						tmp.moveTo(tmpJar);
						XMLConfigAdmin.updateJar(config, tmpJar, false);
					}
				}

				zis.closeEntry();
			}
		}
		finally {
			IOUtil.close(zis);
		}
	}

	public static Resource toResource(Config config, Element el) throws ApplicationException {
		String fileName = el.getAttribute("file-name");
		if (StringUtil.isEmpty(fileName)) throw new ApplicationException("missing attribute [file-name]");
		Resource res = getExtensionDir(config).getRealResource(fileName);
		if (!res.exists()) throw new ApplicationException("Extension [" + fileName + "] was not found at [" + res + "]");
		return res;
	}

	public static Resource toResource(Config config, Element el, Resource defaultValue) {
		String fileName = el.getAttribute("file-name");
		if (StringUtil.isEmpty(fileName)) return defaultValue;
		Resource res = getExtensionDir(config).getRealResource(fileName);
		if (!res.exists()) return defaultValue;
		return res;
	}

	private static Resource getExtensionFile(Config config, Resource ext, String id, String name, String version) {
		String fileName = toHash(id, name, version, ResourceUtil.getExtension(ext, "lex"));
		// String
		// fileName=HashUtil.create64BitHashAsString(id+version,Character.MAX_RADIX)+"."+ResourceUtil.getExtension(ext,
		// "lex");
		return getExtensionDir(config).getRealResource(fileName);
	}

	public static String toHash(String id, String name, String version, String ext) {
		if (ext == null) ext = "lex";
		return HashUtil.create64BitHashAsString(id + version, Character.MAX_RADIX) + "." + ext;
	}

	private static Resource getExtensionDir(Config config) {
		return config.getConfigDir().getRealResource("extensions/installed");
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

	public static void populate(Element el, Manifest manifest) {
		Attributes attr = manifest.getMainAttributes();
		pop(el, attr, "id", null);
		pop(el, attr, "name", null);
		pop(el, attr, "version", null);
		pop(el, attr, "start-bundles", "false");
		pop(el, attr, "release-type", "all");
		pop(el, attr, "description", null);
		pop(el, attr, "trial", null);
		pop(el, attr, "image", null);
		pop(el, attr, "categories", null);
		pop(el, attr, "category", null);
		pop(el, attr, "lucee-core-version", null);
		pop(el, attr, "lucee-loader-version", null);
		pop(el, attr, "amf", null);
		pop(el, attr, "resource", null);
		pop(el, attr, "search", null);
		pop(el, attr, "orm", null);
		pop(el, attr, "webservice", null);
		pop(el, attr, "monitor", null);
		pop(el, attr, "cache", null);
		pop(el, attr, "cache-handler", null);
		pop(el, attr, "jdbc", null);
		pop(el, attr, "mapping", null);
		pop(el, attr, "event-gateway-instance", null);
	}

	private static void pop(Element el, Attributes attr, String name, String defaultValue) {
		String val = StringUtil.unwrap(attr.getValue(name));
		if (!StringUtil.isEmpty(val)) el.setAttribute(name, val);
		else if (defaultValue != null) el.setAttribute(name, defaultValue);
		else el.removeAttribute(name);
	}

	public void populate(Element el) {
		el.setAttribute("file-name", extensionFile.getName());
		String id = getId();
		String name = getName();
		if (StringUtil.isEmpty(name)) name = id;
		el.setAttribute("id", id);
		el.setAttribute("name", name);
		el.setAttribute("version", getVersion());

		// newly added
		// start bundles (IMPORTANT:this key is used to reconize a newer entry, so do not change)
		el.setAttribute("start-bundles", Caster.toString(getStartBundles()));

		// release type
		el.setAttribute("release-type", toReleaseType(getReleaseType(), "all"));

		// Description
		if (StringUtil.isEmpty(getDescription())) el.setAttribute("description", toStringForAttr(getDescription()));
		else el.removeAttribute("description");

		// Trial
		el.setAttribute("trial", Caster.toString(isTrial()));

		// Image
		if (StringUtil.isEmpty(getImage())) el.setAttribute("image", toStringForAttr(getImage()));
		else el.removeAttribute("image");

		// Categories
		String[] cats = getCategories();
		if (!ArrayUtil.isEmpty(cats)) {
			StringBuilder sb = new StringBuilder();
			for (String cat: cats) {
				if (sb.length() > 0) sb.append(',');
				sb.append(toStringForAttr(cat).replace(',', ' '));
			}
			el.setAttribute("categories", sb.toString());
		}
		else el.removeAttribute("categories");

		// core version
		if (minCoreVersion != null) el.setAttribute("lucee-core-version", toStringForAttr(minCoreVersion.toString()));
		else el.removeAttribute("lucee-core-version");

		// loader version
		if (minLoaderVersion > 0) el.setAttribute("loader-version", Caster.toString(minLoaderVersion));
		else el.removeAttribute("loader-version");

		// amf
		if (!StringUtil.isEmpty(amfsJson)) el.setAttribute("amf", toStringForAttr(amfsJson));
		else el.removeAttribute("amf");

		// resource
		if (!StringUtil.isEmpty(resourcesJson)) el.setAttribute("resource", toStringForAttr(resourcesJson));
		else el.removeAttribute("resource");

		// search
		if (!StringUtil.isEmpty(searchsJson)) el.setAttribute("search", toStringForAttr(searchsJson));
		else el.removeAttribute("search");

		// orm
		if (!StringUtil.isEmpty(ormsJson)) el.setAttribute("orm", toStringForAttr(ormsJson));
		else el.removeAttribute("orm");

		// webservice
		if (!StringUtil.isEmpty(webservicesJson)) el.setAttribute("webservice", toStringForAttr(webservicesJson));
		else el.removeAttribute("webservice");

		// monitor
		if (!StringUtil.isEmpty(monitorsJson)) el.setAttribute("monitor", toStringForAttr(monitorsJson));
		else el.removeAttribute("monitor");

		// cache
		if (!StringUtil.isEmpty(cachesJson)) el.setAttribute("cache", toStringForAttr(cachesJson));
		else el.removeAttribute("cache");

		// cache-handler
		if (!StringUtil.isEmpty(cacheHandlersJson)) el.setAttribute("cache-handler", toStringForAttr(cacheHandlersJson));
		else el.removeAttribute("cache-handler");

		// jdbc
		if (!StringUtil.isEmpty(jdbcsJson)) el.setAttribute("jdbc", toStringForAttr(jdbcsJson));
		else el.removeAttribute("jdbc");

		// mapping
		if (!StringUtil.isEmpty(mappingsJson)) el.setAttribute("mapping", toStringForAttr(mappingsJson));
		else el.removeAttribute("mapping");

		// event-gateway-instances
		if (!StringUtil.isEmpty(eventGatewayInstancesJson)) el.setAttribute("event-gateway-instances", toStringForAttr(eventGatewayInstancesJson));
		else el.removeAttribute("event-gateway-instances");
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
		Log log = config.getLog("deploy");
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
		Log log = config.getLog("deploy");
		if (qry == null) qry = createQuery();
		for (int i = 0; i < children.length; i++) {
			try {
				children[i].populate(qry); // ,i+1
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log.error("extension", t);
			}
		}
		return qry;
	}

	public static Query toQuery(Config config, Element[] children) throws PageException {
		Log log = config.getLog("deploy");
		Query qry = createQuery();
		for (int i = 0; i < children.length; i++) {
			try {
				new RHExtension(config, children[i]).populate(qry); // ,i+1
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log.error("extension", t);
			}
		}
		return qry;
	}

	private static Query createQuery() throws DatabaseException {
		return new QueryImpl(new Key[] { KeyConstants._id, KeyConstants._version, KeyConstants._name, SYMBOLIC_NAME, KeyConstants._type, KeyConstants._description,
				KeyConstants._image, RELEASE_TYPE, TRIAL, CATEGORIES, START_BUNDLES, BUNDLES, FLDS, TLDS, TAGS, FUNCTIONS, CONTEXTS, WEBCONTEXTS, CONFIG, APPLICATIONS, COMPONENTS,
				PLUGINS, EVENT_GATEWAYS, ARCHIVES }, 0, "Extensions");
	}

	private void populate(Query qry) throws PageException, IOException, BundleException {
		int row = qry.addRow();
		qry.setAt(KeyConstants._id, row, getId());
		qry.setAt(KeyConstants._name, row, getName());
		qry.setAt(SYMBOLIC_NAME, row, getSymbolicName());
		qry.setAt(KeyConstants._image, row, getImage());
		qry.setAt(KeyConstants._type, row, type);
		qry.setAt(KeyConstants._description, row, description);
		qry.setAt(KeyConstants._version, row, getVersion() == null ? null : getVersion().toString());
		qry.setAt(TRIAL, row, isTrial());
		qry.setAt(RELEASE_TYPE, row, toReleaseType(getReleaseType(), "all"));
		// qry.setAt(JARS, row,Caster.toArray(getJars()));
		qry.setAt(FLDS, row, Caster.toArray(getFlds()));
		qry.setAt(TLDS, row, Caster.toArray(getTlds()));
		qry.setAt(FUNCTIONS, row, Caster.toArray(getFunctions()));
		qry.setAt(ARCHIVES, row, Caster.toArray(getArchives()));
		qry.setAt(TAGS, row, Caster.toArray(getTags()));
		qry.setAt(CONTEXTS, row, Caster.toArray(getContexts()));
		qry.setAt(WEBCONTEXTS, row, Caster.toArray(getWebContexts()));
		qry.setAt(CONFIG, row, Caster.toArray(getConfigs()));
		qry.setAt(EVENT_GATEWAYS, row, Caster.toArray(getEventGateways()));
		qry.setAt(CATEGORIES, row, Caster.toArray(getCategories()));
		qry.setAt(APPLICATIONS, row, Caster.toArray(getApplications()));
		qry.setAt(COMPONENTS, row, Caster.toArray(getComponents()));
		qry.setAt(PLUGINS, row, Caster.toArray(getPlugins()));
		qry.setAt(START_BUNDLES, row, Caster.toBoolean(getStartBundles()));

		BundleInfo[] bfs = getBundles();
		Query qryBundles = new QueryImpl(new Key[] { KeyConstants._name, KeyConstants._version }, bfs.length, "bundles");
		for (int i = 0; i < bfs.length; i++) {
			qryBundles.setAt(KeyConstants._name, i + 1, bfs[i].getSymbolicName());
			if (bfs[i].getVersion() != null) qryBundles.setAt(KeyConstants._version, i + 1, bfs[i].getVersionAsString());
		}
		qry.setAt(BUNDLES, row, qryBundles);
	}

	public Struct toStruct() throws PageException {
		Struct sct = new StructImpl();
		sct.set(KeyConstants._id, getId());
		sct.set(SYMBOLIC_NAME, getSymbolicName());
		sct.set(KeyConstants._name, getName());
		sct.set(KeyConstants._image, getImage());
		sct.set(KeyConstants._description, description);
		sct.set(KeyConstants._version, getVersion() == null ? null : getVersion().toString());
		sct.set(TRIAL, isTrial());
		sct.set(RELEASE_TYPE, toReleaseType(getReleaseType(), "all"));
		// sct.set(JARS, row,Caster.toArray(getJars()));
		try {
			sct.set(FLDS, Caster.toArray(getFlds()));
			sct.set(TLDS, Caster.toArray(getTlds()));
			sct.set(FUNCTIONS, Caster.toArray(getFunctions()));
			sct.set(ARCHIVES, Caster.toArray(getArchives()));
			sct.set(TAGS, Caster.toArray(getTags()));
			sct.set(CONTEXTS, Caster.toArray(getContexts()));
			sct.set(WEBCONTEXTS, Caster.toArray(getWebContexts()));
			sct.set(CONFIG, Caster.toArray(getConfigs()));
			sct.set(EVENT_GATEWAYS, Caster.toArray(getEventGateways()));
			sct.set(CATEGORIES, Caster.toArray(getCategories()));
			sct.set(APPLICATIONS, Caster.toArray(getApplications()));
			sct.set(COMPONENTS, Caster.toArray(getComponents()));
			sct.set(PLUGINS, Caster.toArray(getPlugins()));
			sct.set(START_BUNDLES, Caster.toBoolean(getStartBundles()));

			BundleInfo[] bfs = getBundles();
			Query qryBundles = new QueryImpl(new Key[] { KeyConstants._name, KeyConstants._version }, bfs.length, "bundles");
			for (int i = 0; i < bfs.length; i++) {
				qryBundles.setAt(KeyConstants._name, i + 1, bfs[i].getSymbolicName());
				if (bfs[i].getVersion() != null) qryBundles.setAt(KeyConstants._version, i + 1, bfs[i].getVersionAsString());
			}
			sct.set(BUNDLES, qryBundles);
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

	private static BundleDefinition toBundleDefinition(InputStream is, String name, String extensionVersion, boolean closeStream)
			throws IOException, BundleException, ApplicationException {
		Resource tmp = SystemUtil.getTempDirectory().getRealResource(name);
		try {
			IOUtil.copy(is, tmp, closeStream);
			BundleFile bf = BundleFile.getInstance(tmp);
			if (bf.isBundle()) throw new ApplicationException("Jar [" + name + "] is not a valid OSGi Bundle");
			return new BundleDefinition(bf.getSymbolicName(), bf.getVersion());
		}
		finally {
			tmp.delete();
		}
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

	public static ExtensionDefintion toExtensionDefinition(String s) {
		if (StringUtil.isEmpty(s, true)) return null;
		s = s.trim();

		String[] arrr;
		int index;
		arrr = ListUtil.trimItems(ListUtil.listToStringArray(s, ';'));
		ExtensionDefintion ed = new ExtensionDefintion();
		for (String ss: arrr) {
			index = ss.indexOf('=');
			if (index != -1) {
				ed.setParam(ss.substring(0, index).trim(), ss.substring(index + 1).trim());
			}
			else if (ed.getId() == null || Decision.isUUId(ed.getId())) {
				ed.setId(ss);
			}
		}
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
}
