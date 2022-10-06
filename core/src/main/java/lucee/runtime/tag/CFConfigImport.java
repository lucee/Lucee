package lucee.runtime.tag;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.Cookie;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;

import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.log.log4j2.appender.ConsoleAppender;
import lucee.commons.io.log.log4j2.appender.DatasourceAppender;
import lucee.commons.io.log.log4j2.appender.ResourceAppender;
import lucee.commons.io.log.log4j2.layout.ClassicLayout;
import lucee.commons.io.log.log4j2.layout.DataDogLayout;
import lucee.commons.io.log.log4j2.layout.XMLLayout;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.DynamicAttributes;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;
import lucee.runtime.util.PageContextUtil;

public class CFConfigImport {

	private static Key FROM_CFCONFIG;
	private static Key ACTION;
	private static Key TYPE;
	private static Key PASSWORD;
	private static Key MAPPINGS;
	private static Key DATASOURCES;
	private static Key VIRTUAL;
	private static Key NAME;
	private static Key DATABASE;

	private Resource file;
	private Charset charset;
	private String password;
	private Tag tag;
	private DynamicAttributes dynAttr;
	private String type = "server";
	private CFMLEngine engine;
	private final ConfigPro config;
	private Struct placeHolderData;
	private Struct data;
	private boolean pwCheckedServer = false;
	private boolean pwCheckedWeb = false;
	private final boolean setPasswordIfNecessary;
	private final boolean validatePassword;

	public CFConfigImport(Config config, Resource file, Charset charset, String password, String type, Struct placeHolderData, boolean setPasswordIfNecessary,
			boolean validatePassword) throws PageException {

		this.file = file;
		this.charset = charset;
		this.password = password;
		this.type = type;
		this.placeHolderData = placeHolderData;
		this.setPasswordIfNecessary = setPasswordIfNecessary;
		this.validatePassword = validatePassword;
		this.engine = CFMLEngineFactory.getInstance();
		if ("web".equalsIgnoreCase(type) && !(config instanceof ConfigWeb))
			throw engine.getExceptionUtil().createApplicationException("cannot manipulate a web context when you pass in a server config to the constructor!");

		if ("server".equalsIgnoreCase(type) && config instanceof ConfigWeb) {
			setPasswordIfNecessary((ConfigWeb) config);
			this.config = (ConfigPro) config.getConfigServer(password);
		}
		else this.config = (ConfigPro) config;
	}

	public CFConfigImport(Config config, Struct data, Charset charset, String password, String type, Struct placeHolderData, boolean setPasswordIfNecessary,
			boolean validatePassword) throws PageException {
		this.data = data;
		this.charset = charset;
		this.password = password;
		this.validatePassword = validatePassword;
		this.type = type;
		this.placeHolderData = placeHolderData;
		this.engine = CFMLEngineFactory.getInstance();
		this.setPasswordIfNecessary = setPasswordIfNecessary;
		if ("web".equalsIgnoreCase(type) && !(config instanceof ConfigWeb))
			throw engine.getExceptionUtil().createApplicationException("cannot manipulate a web context when you pass in a server config to the constructor!");
		if ("server".equalsIgnoreCase(type) && config instanceof ConfigWeb) {
			setPasswordIfNecessary((ConfigWeb) config);
			this.config = (ConfigPro) config.getConfigServer(password);
		}
		else this.config = (ConfigPro) config;
	}

	public Struct execute(boolean throwException) throws PageException {
		boolean unregister = false;
		PageContext pc = ThreadLocalPageContext.get();
		Struct json = null;
		try {
			if (pc == null) {

				pc = PageContextUtil.getPageContext(config, null, (File) SystemUtil.getTempDirectory(), "localhost", "/", "", new Cookie[0], null, null, null,
						DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, true, 100000, false);
				unregister = true;

			}
			if (validatePassword && Util.isEmpty(password)) {
				String sysprop = "lucee." + type.toUpperCase() + ".admin.password";
				String envVarName = sysprop.replace('.', '_').toUpperCase();
				password = SystemUtil.getSystemPropOrEnvVar(sysprop, null);
				if (password == null) throw engine.getExceptionUtil()
						.createApplicationException("missing password to access the Lucee configutation. This can be set in two ways, as enviroment variable [" + envVarName
								+ "] or as system property [" + sysprop + "].");
			}

			Cast cast = engine.getCastUtil();
			if (ACTION == null) ACTION = cast.toKey("action");
			if (TYPE == null) TYPE = cast.toKey("type");
			if (PASSWORD == null) PASSWORD = cast.toKey("password");
			if (MAPPINGS == null) MAPPINGS = cast.toKey("mappings");
			if (VIRTUAL == null) VIRTUAL = cast.toKey("virtual");
			if (DATASOURCES == null) DATASOURCES = cast.toKey("datasources");
			if (NAME == null) NAME = cast.toKey("name");
			if (DATABASE == null) DATABASE = cast.toKey("database");
			if (FROM_CFCONFIG == null) FROM_CFCONFIG = cast.toKey("fromCFConfig");

			if (data != null) {
				json = data;
			}
			else {
				String raw = engine.getIOUtil().toString(file, charset);
				json = cast.toStruct(new JSONExpressionInterpreter().interpret(null, raw));
			}

			replacePlaceHolder(json, placeHolderData);
			tag = new Admin(!validatePassword);

			dynAttr = (DynamicAttributes) tag;

			set(pc, json, "updateCharset", throwException, new Item("webcharset"), new Item("resourcecharset"), new Item("templatecharset"));

			set(pc, json, "updateRegional", throwException, new Item("usetimeserver").setDefault(true), new Item("locale").setDefault(config.getLocale()),
					new Item("timeserver").setDefault(config.getTimeServer()), new Item("timezone").setDefault(config.getTimeZone()));

			set(pc, json, "updateApplicationListener", throwException, new Item(new String[] { "applicationMode" }, "listenermode", null),
					new Item(new String[] { "applicationListener", "applicationType", "listenertype" }, "listenertype", null)
							.setDefault(config.getApplicationListener().getType()));

			set(pc, json, "updatePerformanceSettings", throwException, new Item("inspecttemplate"), new Item("cachedafter"), new Item("typechecking"));

			set(pc, json, "updateApplicationSetting", throwException, new Item("applicationpathtimeout"), new Item("requesttimeout"), new Item("scriptprotect"),
					new Item("requestTimeoutInURL", "allowurlrequesttimeout"));

			set(pc, json, "updateCompilerSettings", throwException, new Item("nullsupport"), new Item("handleunquotedattrvalueasstring"), new Item("externalizestringgte"),
					new Item("dotnotationuppercase", "dotNotationUpperCase"), new Item("suppressWhitespaceBeforeArgument", "suppresswsbeforearg"), new Item("templatecharset"));

			set(pc, json, "updateSecurity", throwException, new Item("varusage"));

			set(pc, json, "updateOutputSetting", throwException, new Item("allowcompression"), new Item("whitespaceManagement", "cfmlwriter"), new Item("suppresscontent"),
					new Item("bufferTagBodyOutput", "bufferoutput"), new Item("showContentLength", "contentlength"));

			set(pc, json, "updateRegex", throwException, new Item("regextype"));

			set(pc, json, "updateORMSetting", throwException, new Item("ormconfig"), new Item("ormsqlscript", "sqlscript"), new Item("ormusedbformapping", "usedbformapping"),
					new Item("ormeventhandling", "eventhandling"), new Item("ormsecondarycacheenabled", "secondarycacheenabled"), new Item("ormautogenmap", "autogenmap"),
					new Item("ormlogsql", "logsql"), new Item("ormcacheconfig", "cacheconfig"), new Item("ormsavemapping", "savemapping"), new Item("ormschema", "schema"),
					new Item("ormdbcreate", "dbcreate"), new Item("ormcfclocation", "cfclocation"), new Item("ormflushatrequestend", "flushatrequestend"),
					new Item("ormcacheprovider", "cacheprovider"), new Item("ormcatalog", "catalog"));

			set(pc, json, "updateMailSetting", throwException, new Item(new String[] { "mailDefaultEncoding" }, "defaultencoding", null),
					new Item(new String[] { "mailConnectionTimeout" }, "timeout", null), new Item("mailSpoolEnable", "spoolenable"));

			set(pc, json, "updateRestSettings", throwException, new Item(new String[] { "Restlist" }, "list", null));

			set(pc, json, "updateComponent", throwException, new Item("componentUseVariablesScope", "useshadow"), new Item("componentdumptemplate"),
					new Item(new String[] { "componentDeepSearch" }, "deepsearch", null).setDefault(false), new Item("basecomponenttemplatelucee"), new Item("componentpathcache"),
					new Item("componentdatamemberdefaultaccess"), new Item("basecomponenttemplatecfml"), new Item("componentlocalsearch"), new Item("componentdefaultimport"),
					new Item("componentImplicitNotation", "triggerdatamember"));

			set(pc, json, "updateCustomTagSetting", throwException, new Item(new String[] { "customTagLocalSearch" }, "localsearch", null).setDefault(config.doLocalCustomTag()),
					new Item(new String[] { "customTagDeepSearch" }, "deepsearch", null).setDefault(config.doCustomTagDeepSearch()),
					new Item(new String[] { "customTagExtensions" }, "extensions", null).setDefault(config.getCustomTagExtensions()),
					new Item("customtagpathcache").setDefault(true));

			set(pc, json, "updateDebug", throwException, new Item(new String[] { "debuggingException" }, "exception", null),
					new Item(new String[] { "debuggingImplicitAccess" }, "implicitaccess", null), new Item(new String[] { "debuggingTracing" }, "tracing", null),
					new Item(new String[] { "debuggingQueryUsage" }, "queryusage", null), new Item(new String[] { "debuggingTemplate" }, "template", null),
					new Item(new String[] { "debuggingDatabase" }, "database", null), new Item(new String[] { "debuggingDump" }, "dump", null), new Item("debugtemplate"),
					new Item(new String[] { "debuggingEnable", "debuggingEnabled" }, "debug", null), new Item(new String[] { "debuggingTimer" }, "timer", null));

			set(pc, json, "updatemailsetting", throwException, new Item(new String[] { "mailSpoolEnable", "mailSpoolEnabled" }, "spoolenable", null),
					new Item(new String[] { "mailConnectionTimeout", "mailTimeout" }, "timeout", null),
					new Item(new String[] { "maildefaultencoding", "mailencoding" }, "defaultencoding", null));

			set(pc, json, "updateError", throwException, new Item(new String[] { "generalErrorTemplate" }, "template500", null),
					new Item(new String[] { "missingErrorTemplate" }, "template404", null), new Item(new String[] { "errorStatusCode" }, "statuscode", null).setDefault(true));

			setGroup(pc, json, "updateDatasource", "datasources", new String[] { "name", "databases" }, throwException, new Item("class", "classname"), new Item("bundleName"),
					new Item("bundleVersion"), new Item("connectionlimit").setDefault(-1), new Item("connectiontimeout").setDefault(-1), new Item("livetimeout").setDefault(-1),
					new Item("custom"), new Item("validate").setDefault(false), new Item("verify").setDefault(true), new Item("host"), new Item("port").setDefault(-1),
					new Item("connectionString", "dsn"), new Item("username", "dbusername"), new Item("password", "dbpassword"), new Item("storage").setDefault(false),
					new Item("metacachetimeout").setDefault(60000), new Item("alwayssettimeout").setDefault(false), new Item("dbdriver"), new Item("database"),
					new Item("blob").setDefault(false), new Item("name"), new Item("requestexclusive").setDefault(false), new Item("customparametersyntax"),
					new Item("alwaysresetconnections").setDefault(false), new Item("timezone"), new Item("clob").setDefault(false),
					new Item("literaltimestampwithtsoffset").setDefault(false), new Item("newname")

			);
			setGroup(pc, json, "updateCacheConnection", "caches", new String[] { "name" }, throwException, new Item("bundlename"), new Item("default"),
					new Item("storage").setDefault(false), new Item("bundleversion"), new Item("name"), new Item("custom"), new Item("class"));
			setGroup(pc, json, "updateGatewayEntry", "gateways", new String[] { "id" }, throwException, new Item("startupmode"), new Item("listenercfcpath"), new Item("cfcpath"),
					new Item("id"), new Item("custom"), new Item("class"));

			setGroup(pc, json, "updateLogSettings", "loggers", new String[] { "name" }, throwException, new Item("layoutbundlename"), new Item("level"),
					new Item("appenderArguments", "appenderargs").setDefault(engine.getCreationUtil().createStruct()), new Item("name"),
					new Item("layoutArguments", "layoutargs").setDefault(engine.getCreationUtil().createStruct()), new Item("appenderClass", new AppenderModifier()),
					new Item("appender"), new Item("layoutClass", new LayoutModifier()), new Item("layout"));

			setGroup(pc, json, "updateMailServer", "mailServers", new String[] {}, throwException, new Item("life").setDefault(60), new Item("tls").setDefault(false),
					new Item("idle").setDefault(10), new Item("username", "dbusername"), new Item(new String[] { "smtp", "host", "server" }, "hostname", null), new Item("id"),
					new Item("port").setDefault(-1), new Item("password", "dbpassword"), new Item("ssl").setDefault(false));

			setGroup(pc, json, "updateMapping", new String[] { "mappings", "cfmappings" }, new String[] { "virtual" }, throwException, new Item("virtual"),
					new Item("inspect").addName("inspectTemplate"), new Item("physical"), new Item("primary"), new Item("toplevel").setDefault(true), new Item("archive"));

			setGroup(pc, json, "updateCustomTag", new String[] { "customTagMappings", "customTagPaths" }, new String[] { "virtual" }, throwException,
					new Item("virtual", new CreateHashModifier(new String[] { "virtual", "name", "label" }, "physical")), new Item("inspect").addName("inspectTemplate"),
					new Item("physical"), new Item("primary"), new Item("archive"));

			setGroup(pc, json, "updateComponentMapping", new String[] { "componentMappings", "componentPaths" }, new String[] { "virtual" }, throwException,
					new Item("virtual", new CreateHashModifier(new String[] { "virtual", "name", "label" }, "physical")), new Item("inspect").addName("inspectTemplate"),
					new Item("physical"), new Item("primary"), new Item("archive"));

			optimizeExtensions(config, json);
			setGroup(pc, json, "updateRHExtension", "extensions", new String[] {}, throwException, new Item("source"), new Item("id"), new Item("version"));

			// need to be at the end
			set(pc, json, "updateScope", throwException, new Item("sessiontype"), new Item("sessionmanagement"), new Item("setdomaincookies", "domaincookies"),
					new Item("allowimplicidquerycall"), new Item("setclientcookies", "clientcookies"), new Item("mergeformandurl"), new Item("localScopeMode", "localmode"),
					new Item("cgiScopeReadonly", "cgireadonly"), new Item("scopecascadingtype"), new Item("sessiontimeout"), new Item("clienttimeout"), new Item("clientstorage"),
					new Item("clientmanagement"), new Item("applicationtimeout"), new Item("sessionstorage"));

			// TODO cacheDefaultQuery
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (throwException) {
				throw Caster.toPageException(t);
			}
			else LogUtil.log(pc, "deploy", t);
		}
		finally {
			if (unregister) {
				pc.getConfig().getFactory().releaseLuceePageContext(pc, true);
			}
		}
		return json;

	}

	private static void replacePlaceHolder(Collection coll, Struct placeHolderData) {
		// ${MAILSERVER_HOST:smtp.sendgrid.net}
		Iterator<Entry<Key, Object>> it = coll.entryIterator();
		Entry<Key, Object> e;
		Object obj;
		while (it.hasNext()) {
			e = it.next();
			obj = e.getValue();
			if (obj instanceof String) replacePlaceHolder(e, placeHolderData);
			if (obj instanceof Collection) replacePlaceHolder((Collection) obj, placeHolderData);
		}
	}

	private static void replacePlaceHolder(Entry<Key, Object> e, Struct placeHolderData) {
		String str = (String) e.getValue();

		int startIndex = str.indexOf("${");
		if (startIndex == -1) return;
		int endIndex = str.indexOf("}", startIndex + 1);
		if (endIndex == -1) return;
		String content = str.substring(startIndex + 2, endIndex);
		String envVarName, defaultValue = "";
		int index = content.indexOf(':');
		if (index == -1) {
			envVarName = content;
		}
		else {
			envVarName = content.substring(0, index);
			defaultValue = content.substring(index + 1);
		}

		Object val = null;
		if (placeHolderData != null) val = placeHolderData.get(KeyImpl.init(envVarName), null);
		if (val == null) val = SystemUtil.getSystemPropOrEnvVar(envVarName, null);
		if (val != null) e.setValue(val);
		else e.setValue(defaultValue);

	}

	private void optimizeExtensions(Config config, Struct json) throws IOException {
		Cast cast = engine.getCastUtil();
		Array arr = cast.toArray(json.get("extensions", null), null);
		if (arr == null) return;
		Struct data;
		String path;
		Resource src;
		Iterator<Object> it = arr.valueIterator();
		while (it.hasNext()) {
			data = cast.toStruct(it.next(), null);
			if (data == null) continue;
			path = cast.toString(data.get("source", null), null);
			if (Util.isEmpty(path)) continue;
			src = cast.toResource(path, null);
			if (!src.isFile()) continue;
			String[] idAndVersion = extractExtesnionInfoFromManifest(src);
			if (idAndVersion == null) continue;
			Resource extDir = config.getLocalExtensionProviderDirectory();
			Resource trg = extDir.getRealResource(idAndVersion[0] + "-" + idAndVersion[1] + ".lex");
			if (!trg.isFile()) {
				engine.getIOUtil().copy(src, trg);
			}
			data.setEL("id", idAndVersion[0]);
			data.setEL("version", idAndVersion[1]);
		}

	}

	private static Manifest extractManifest(Resource src) throws IOException {

		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(Util.toBufferedInputStream(src.getInputStream()));
			ZipEntry entry;
			Manifest mf = null;
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					if (entry.getName().indexOf("META-INF/MANIFEST.MF") != -1) {
						mf = new Manifest(zis);
					}
				}
				zis.closeEntry();
				if (mf != null) return mf;
			}
		}
		finally {
			Util.closeEL(zis);
		}
		return null;
	}

	private static String[] extractExtesnionInfoFromManifest(Resource src) throws IOException {
		Manifest mf = extractManifest(src);
		if (mf != null) {
			Attributes attrs = mf.getMainAttributes();
			String id = unwrap(attrs.getValue("id"));
			String version = unwrap(attrs.getValue("version"));
			return new String[] { id, version };
		}
		return null;
	}

	private void setGroup(PageContext pc, final Struct json, String trgActionName, String srcGroupName, String[] keyNames, boolean throwException, Item... items)
			throws JspException {
		setGroup(pc, json, trgActionName, new String[] { srcGroupName }, keyNames, throwException, items);
	}

	private void setGroup(PageContext pc, final Struct json, String trgActionName, String[] srcGroupNames, String[] keyNames, boolean throwException, Item... items)
			throws JspException {
		try {
			Cast cast = engine.getCastUtil();
			Collection group = null;
			for (String srcGroupName: srcGroupNames) {
				group = cast.toCollection(json.get(cast.toKey(srcGroupName), null), null);
				if (group != null) break;
			}

			if (group != null) {
				Iterator<Entry<Key, Object>> it = group.entryIterator();
				Entry<Key, Object> e;
				Struct data;
				while (it.hasNext()) {
					e = it.next();
					data = cast.toStruct(e.getValue(), null);
					if (data == null) continue;
					if (!(group instanceof Array)) {
						for (String keyName: keyNames) {
							data.set(cast.toKey(keyName), e.getKey().getString());
						}
					}
					set(pc, data, trgActionName, throwException, items);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (throwException) {
				if (t instanceof JspException) throw (JspException) t;
				throw Caster.toPageException(t);
			}
			else LogUtil.log(pc, "deploy", t);
		}
	}

	private void setPasswordIfNecessary(ConfigWeb config) throws PageException {
		if (!setPasswordIfNecessary) return;
		boolean isServer = "server".equalsIgnoreCase(type);
		if ((isServer && !pwCheckedServer) || (!isServer && !pwCheckedWeb)) {
			boolean hasPassword = isServer ? config.hasServerPassword() : config.hasPassword();
			if (!hasPassword) {
				// create password
				try {
					((ConfigWebPro) config).updatePassword(isServer, null, password);
				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
			}
			if (isServer) pwCheckedServer = true;
			else pwCheckedWeb = true;
		}

	}

	private void set(PageContext pc, final Struct json, String trgActionName, boolean throwException, Item... items) throws JspException {
		setPasswordIfNecessary(pc.getConfig());

		Object val;
		try {
			tag.setPageContext(pc);
			boolean empty = true;
			for (Item item: items) {
				val = item.getValue(json);
				if (val != null) empty = false;
				else val = item.getDefault();
				dynAttr.setDynamicAttribute(null, item.getTargetAttrName(), val);
			}
			if (empty) {
				tag.release();
				return;
			}
			dynAttr.setDynamicAttribute(null, FROM_CFCONFIG, Boolean.TRUE);
			dynAttr.setDynamicAttribute(null, ACTION, trgActionName);
			dynAttr.setDynamicAttribute(null, TYPE, type);
			dynAttr.setDynamicAttribute(null, PASSWORD, password);

			tag.doStartTag();
			tag.doEndTag();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (throwException) {
				if (t instanceof JspException) throw (JspException) t;
				throw Caster.toPageException(t);
			}
			else LogUtil.log(pc, "deploy", t);
		}
		finally {
			tag.release();
		}
	}

	private static class Item {
		private String[] srcKeyNames;
		private final String trgAttrName;
		private CFMLEngine e;
		private Modifier modifier;
		private Object def = "";

		public Item(String name) {
			this(name, (Modifier) null);
		}

		public Item(String name, Modifier modifier) {
			this.srcKeyNames = new String[] { name };
			this.trgAttrName = name;
			this.e = CFMLEngineFactory.getInstance();
			this.modifier = modifier;
		}

		public Item(String srcKeyName, String trgAttrName) {
			this(srcKeyName, trgAttrName, (Modifier) null);
		}

		public Item(String srcKeyName, String trgAttrName, Modifier modifier) {
			this.srcKeyNames = new String[] { srcKeyName, trgAttrName };
			this.trgAttrName = trgAttrName;
			this.e = CFMLEngineFactory.getInstance();
			this.modifier = modifier;
		}

		public Item(String[] srcKeyNames, String trgAttrName, Modifier modifier) {
			this.srcKeyNames = srcKeyNames;
			this.trgAttrName = trgAttrName;
			this.e = CFMLEngineFactory.getInstance();
			this.modifier = modifier;
		}

		public Item setDefault(Object def) {
			this.def = def;
			return this;
		}

		public Item addName(String name) {
			String[] tmp = new String[srcKeyNames.length + 1];
			for (int i = 0; i < srcKeyNames.length; i++) {
				tmp[i] = srcKeyNames[i];
			}
			tmp[tmp.length - 1] = name;
			srcKeyNames = tmp;
			return this;
		}

		public Object getDefault() {
			return def;
		}

		public Key getTargetAttrName() {
			return e.getCastUtil().toKey(trgAttrName);
		}

		private Object getValue(Struct json) {
			if (modifier != null) {
				return modifier.getValue(json);
			}
			Object obj = null;
			for (String srcKeyName: srcKeyNames) {
				obj = json.get(e.getCastUtil().toKey(srcKeyName), null);
				if (obj == null) continue;
				if (!(obj instanceof String)) break;
				if (!Util.isEmpty((String) obj, true)) break;
				obj = null;
			}
			return obj;
		}

	}

	private static String unwrap(String str) {
		if (str == null) return null;
		if (Util.isEmpty(str)) return "";
		str = str.trim();
		if (str.startsWith("\"") && str.endsWith("\"")) return str.substring(1, str.length() - 1).trim();
		return str;
	}

	private static interface Modifier {

		String getValue(Struct json);

	}

	private abstract static class ALModifier implements Modifier {

		public String getValue(Struct json, String name) {
			// to we have the main key?
			CFMLEngine e = CFMLEngineFactory.getInstance();
			String data = null;
			data = e.getCastUtil().toString(json.get(e.getCastUtil().toKey(name), null), null);
			if (!Util.isEmpty(data, true)) return data;
			return null;
		}

	}

	private static class AppenderModifier extends ALModifier {

		@Override
		public String getValue(Struct json) {
			String val = getValue(json, "appenderclass");
			if (val != null) return val;
			val = getValue(json, "appender");

			if ("console".equalsIgnoreCase(val)) return ConsoleAppender.class.getName();
			if ("resource".equalsIgnoreCase(val)) return ResourceAppender.class.getName();
			if ("datasource".equalsIgnoreCase(val)) return DatasourceAppender.class.getName();

			return val;
		}

	}

	private static class LayoutModifier extends ALModifier {

		@Override
		public String getValue(Struct json) {
			String val = getValue(json, "layoutclass");
			if (val != null) return val;
			val = getValue(json, "layout");

			if ("classic".equalsIgnoreCase(val)) return ClassicLayout.class.getName();
			;
			if ("datasource".equalsIgnoreCase(val)) return ClassicLayout.class.getName();
			if ("html".equalsIgnoreCase(val)) return HtmlLayout.class.getName();
			if ("xml".equalsIgnoreCase(val)) return XMLLayout.class.getName();
			if ("pattern".equalsIgnoreCase(val)) return PatternLayout.class.getName();
			if ("datadog".equalsIgnoreCase(val)) return DataDogLayout.class.getName();

			return val;
		}

	}

	private static class CreateHashModifier implements Modifier {

		private String[] keysToHash;
		private String[] mains;

		public CreateHashModifier(String[] mains, String... keysToHash) {
			this.mains = mains;
			this.keysToHash = keysToHash;
		}

		@Override
		public String getValue(Struct json) {
			// to we have the main key?
			CFMLEngine e = CFMLEngineFactory.getInstance();
			String data = null;
			for (String main: mains) {
				data = e.getCastUtil().toString(json.get(e.getCastUtil().toKey(main), null), null);
				if (!Util.isEmpty(data, true)) break;
			}
			if (!Util.isEmpty(data, true)) return data;

			// if not we create a hash instead
			StringBuilder sb = new StringBuilder();
			for (String keyToHash: keysToHash) {
				data = e.getCastUtil().toString(json.get(e.getCastUtil().toKey(keyToHash), null), null);
				if (!Util.isEmpty(data, true)) sb.append(data).append(';');
			}

			return e.getSystemUtil().hash64b(sb.toString());
		}

	}

}