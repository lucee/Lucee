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

import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogEngine;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.log.log4j2.appender.ConsoleAppender;
import lucee.commons.io.log.log4j2.appender.DatasourceAppender;
import lucee.commons.io.log.log4j2.appender.ResourceAppender;
import lucee.commons.io.log.log4j2.layout.ClassicLayout;
import lucee.commons.io.log.log4j2.layout.DataDogLayout;
import lucee.commons.io.log.log4j2.layout.XMLLayout;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.config.Password;
import lucee.runtime.config.XMLConfigAdmin;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.ParamSyntax;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.gateway.GatewayEntryImpl;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.util.Cast;
import lucee.runtime.util.PageContextUtil;
import lucee.transformer.library.ClassDefinitionImpl;

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
	// private Tag tag;
	// private DynamicAttributes dynAttr;
	private String type = "server";
	private CFMLEngine engine;
	private final ConfigPro config;
	private Struct placeHolderData;
	private Struct data;
	private boolean pwCheckedServer = false;
	private boolean pwCheckedWeb = false;
	private final boolean setPasswordIfNecessary;
	private final boolean validatePassword;
	private JspException exd = null;

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
			setPasswordIfNecessary((ConfigPro) config);
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
			setPasswordIfNecessary((ConfigPro) config);
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

			// dynAttr = (DynamicAttributes) tag;
			boolean isServer = "server".equalsIgnoreCase(type);
			String strPW = ConfigWebUtil.decrypt(password);
			Password pw; // hash password if
			if (isServer && config instanceof ConfigWebPro) {
				pw = ((ConfigWebPro) config).isServerPasswordEqual(strPW);
			}
			else {
				pw = config.isPasswordEqual(strPW);
			}

			boolean updated = setPasswordIfNecessary(config);
			XMLConfigAdmin admin = XMLConfigAdmin.newInstance(config, pw, updated || !validatePassword);
			String str;
			Boolean bool;
			Integer in;
			RefBoolean empty = new RefBooleanImpl();
			TimeSpan ts;
			// TEST: applicationMode

			// charset
			str = getAsString(json, "resourcecharset");
			if (str != null) {
				try {
					admin.updateResourceCharset(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			str = getAsString(json, "templateCharset");
			if (str != null) {
				try {
					admin.updateTemplateCharset(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			str = getAsString(json, "webcharset");
			if (str != null) {
				try {
					admin.updateWebCharset(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// regional
			str = getAsString(json, "locale");
			if (str != null) {
				try {
					admin.updateLocale(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			str = getAsString(json, "timezone");
			if (str != null) {
				try {
					admin.updateTimeZone(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			str = getAsString(json, "timeserver");
			if (str != null) {
				try {
					admin.updateTimeServer(str, false);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			bool = getAsBoolean(json, empty, "usetimeserver");
			if (bool != null) {
				try {
					admin.updateUseTimeServer(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// application listener
			str = getAsString(json, "applicationListener", "applicationType", "listenertype");
			if (str != null) {
				try {
					admin.updateApplicationListenerType(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// application mode
			str = getAsString(json, "applicationMode", "listenerMode");
			if (str != null) {
				try {
					admin.updateApplicationListenerMode(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// Performance Settings
			str = getAsString(json, "inspecttemplate");
			if (str != null) {
				try {
					admin.updateInspectTemplate(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			bool = getAsBoolean(json, empty, "udftypechecking", "typechecking");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateTypeChecking(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// cached after
			ts = getAsTimespan(json, empty, null, 1000, "querycachedafter", "cachedafter");
			if (ts != null || empty.toBooleanValue()) {
				try {
					admin.updateCachedAfterTimeRange(ts);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// ApplicationSetting
			ts = getAsTimespan(json, empty, null, 1000, "requesttimeout");
			if (ts != null || empty.toBooleanValue()) {
				try {
					admin.updateRequestTimeout(ts);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			str = getAsString(json, "scriptprotect");
			if (str != null) {
				try {
					admin.updateScriptProtect(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "allowurlrequesttimeout", "requestTimeoutInURL");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateAllowURLRequestTimeout(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// Compiler Settings
			bool = getAsBoolean(json, empty, "dotnotationuppercase");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateCompilerSettingsDotNotationUpperCase(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			in = getAsInteger(json, empty, "externalizestringgte");
			if (in != null || empty.toBooleanValue()) {
				try {
					admin.updateCompilerSettingsExternalizeStringGTE(in);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "handleunquotedattrvalueasstring");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateCompilerSettingsHandleUnQuotedAttrValueAsString(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "nullsupport");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateCompilerSettingsNullSupport(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "suppressWhitespaceBeforeArgument", "suppresswsbeforearg");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateCompilerSettingsSuppressWSBeforeArg(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			str = getAsString(json, "templatecharset");
			if (str != null) {
				try {
					admin.updateTemplateCharset(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// security
			str = getAsString(json, "varusage");
			if (str != null) {
				try {
					admin.updateSecurity(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// output settings
			str = getAsString(json, "whitespaceManagement", "cfmlwriter");
			if (str != null) {
				try {
					admin.updateCFMLWriterType(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "suppresscontent", "suppresscontentForCFCRemotimg");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateSuppressContent(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "allowcompression");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateAllowCompression(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "showContentLength", "contentlength");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateContentLength(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "bufferTagBodyOutput", "bufferoutput");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateBufferOutput(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// regex
			str = getAsString(json, "regextype", "regex");
			if (str != null) {
				try {
					admin.updateRegexType(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// ORM settings
			/*
			 * TODO set(pc, json, "updateORMSetting", new Item("ormconfig"), new Item("ormsqlscript",
			 * "sqlscript"), new Item("ormusedbformapping", "usedbformapping"), new Item("ormeventhandling",
			 * "eventhandling"), new Item("ormsecondarycacheenabled", "secondarycacheenabled"), new
			 * Item("ormautogenmap", "autogenmap"), new Item("ormlogsql", "logsql"), new Item("ormcacheconfig",
			 * "cacheconfig"), new Item("ormsavemapping", "savemapping"), new Item("ormschema", "schema"), new
			 * Item("ormdbcreate", "dbcreate"), new Item("ormcfclocation", "cfclocation"), new
			 * Item("ormflushatrequestend", "flushatrequestend"), new Item("ormcacheprovider", "cacheprovider"),
			 * new Item("ormcatalog", "catalog"));
			 */

			// mail
			bool = getAsBoolean(json, empty, "mailSpoolEnable", "spoolenable");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.setMailSpoolEnable(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			in = getAsInteger(json, empty, "mailConnectionTimeout", "mailTimeout");
			if (in != null || empty.toBooleanValue()) {
				try {
					admin.setMailTimeout(in);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			else {
				ts = getAsTimespan(json, empty, null, 1000, "mailConnectionTimeout", "mailTimeout");
				if (ts != null || empty.toBooleanValue()) {
					try {
						admin.setMailTimeout((int) (ts.getMillis() / 1000));
					}
					catch (Throwable t) {
						handleException(pc, t);
					}
				}
			}
			str = getAsString(json, "mailDefaultEncoding", "mailEncoding", "mailDefaultCharset", "mailCharset");
			if (str != null) {
				try {
					admin.setMailDefaultCharset(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// rest
			bool = getAsBoolean(json, empty, "Restlist");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateRestList(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// component
			bool = getAsBoolean(json, empty, "componentDeepSearch", "componentSearchSubdirectories");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateComponentDeepSearch(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			str = getAsString(json, "basecomponenttemplatecfml", "basecomponenttemplate");
			if (str != null) {
				try {
					admin.updateBaseComponent(str, null);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			str = getAsString(json, "componentdumptemplate");
			if (str != null) {
				try {
					admin.updateComponentDumpTemplate(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			str = getAsString(json, "componentdatamemberdefaultaccess");
			if (str != null) {
				try {
					admin.updateComponentDataMemberDefaultAccess(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "componentImplicitNotation", "triggerdatamember", "componenttriggerdatamember");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateTriggerDataMember(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "componentUseVariablesScope", "useshadow");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateComponentUseShadow(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			str = getAsString(json, "componentdefaultimport");
			if (str != null) {
				try {
					admin.updateComponentDefaultImport(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "componentlocalsearch");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateComponentLocalSearch(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "componentpathcache");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateComponentPathCache(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// custom tag
			bool = getAsBoolean(json, empty, "customTagDeepSearch");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateCustomTagDeepSearch(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "customTagLocalSearch");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateCustomTagLocalSearch(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "customtagpathcache");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateCTPathCache(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			str = getAsString(json, "customTagExtensions");
			if (str != null) {
				try {
					admin.updateCustomTagExtensions(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			// debug
			bool = getAsBoolean(json, empty, "debuggingEnable", "debuggingEnabled");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateDebug(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "debuggingDatabase");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateDebugDatabase(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "debuggingDump");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateDebugDump(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "debuggingException");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateDebugException(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "debuggingImplicitAccess");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateDebugImplicitAccess(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "debuggingQueryUsage");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateDebugQueryUsage(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "debuggingTemplate");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateDebugTemplate(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "debuggingTimer");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateDebugTimer(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "debuggingTracing");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateDebugTracing(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			str = getAsString(json, "debugTemplate");
			if (str != null) {
				try {
					admin.updateDebugTemplate(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// error
			str = getAsString(json, "generalErrorTemplate", "errortemplate500");
			if (str != null) {
				if (str.trim().equalsIgnoreCase("default")) {
					str = "";
				}
				try {
					admin.updateErrorTemplate(500, str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			str = getAsString(json, "missingErrorTemplate", "errortemplate404");
			if (str != null) {
				if (str.trim().equalsIgnoreCase("default")) {
					str = "";
				}
				try {
					admin.updateErrorTemplate(404, str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "errorStatusCode");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateErrorStatusCode(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// datasources
			Collection coll = getAsCollection(json, "datasources");
			if (coll != null) {

				if (coll instanceof Struct) {
					Boolean psq = getAsBoolean((Struct) coll, empty, "psq", "preserveSingleQuote");
					if (psq != null) admin.updatePSQ(psq);
				}

				Iterator<Entry<Key, Object>> it = coll.entryIterator();
				Entry<Key, Object> e;
				Struct data;
				while (it.hasNext()) {
					e = it.next();
					data = cast.toStruct(e.getValue(), null);
					if (data == null) continue;
					try {
						ParamSyntax ps = ParamSyntax.toParamSyntax(data, ParamSyntax.DEFAULT);
						admin.updateDataSource(getAsString(data, "id"), e.getKey().getString(), getAsString(data, "newname"), getClassDefinition(data, null),
								getAsString(data, "connectionString", "dsn"), getAsString(data, "username", "dbusername"), getAsString(data, "password", "dbpassword"),
								getAsString(data, "host"), getAsString(data, "database"), getAsInt(data, empty, -1, "port"), getAsInt(data, empty, -1, "connectionlimit"),
								getAsInt(data, empty, -1, "connectiontimeout", "idletimeout"), getAsInt(data, empty, -1, "livetimeout"),
								getAsLong(data, empty, 60000L, "metacachetimeout"), getAsBoolean(data, empty, false, "blob"), getAsBoolean(data, empty, false, "clob"),
								extractDatasourceAllow(data, empty, DataSource.ALLOW_ALL), getAsBoolean(data, empty, false, "validate"),
								getAsBoolean(data, empty, false, "storage"), getAsString(data, "timezone"), getAsStruct(data, "custom"), getAsString(data, "dbdriver"), ps,
								getAsBoolean(data, empty, false, "literalTimestampWithTSOffset"), getAsBoolean(data, empty, false, "alwaysSetTimeout"),
								getAsBoolean(data, empty, false, "requestExclusive"), getAsBoolean(data, empty, false, "alwaysResetConnections"));
					}
					catch (Throwable t) {
						handleException(pc, t);
					}
				}
			}

			// cache connections
			coll = getAsCollection(json, "caches");
			if (coll != null) {
				Iterator<Entry<Key, Object>> it = coll.entryIterator();
				Entry<Key, Object> e;
				Struct data;
				while (it.hasNext()) {
					e = it.next();
					data = cast.toStruct(e.getValue(), null);
					if (data == null) continue;
					try {
						admin.updateCacheConnection(e.getKey().getString(), getClassDefinition(data, null),
								Admin.toCacheConstant(getAsString(data, "default"), ConfigPro.CACHE_TYPE_NONE), getAsStruct(data, "custom"),
								getAsBoolean(data, empty, false, "readOnly"), getAsBoolean(data, empty, false, "storage"));
					}
					catch (Throwable t) {
						handleException(pc, t);
					}
				}
			}

			// gateways
			coll = getAsCollection(json, "gateways");
			if (coll != null) {
				Iterator<Entry<Key, Object>> it = coll.entryIterator();
				Entry<Key, Object> e;
				Struct data;
				while (it.hasNext()) {
					e = it.next();
					data = cast.toStruct(e.getValue(), null);
					if (data == null) continue;
					try {
						// custom validation
						Struct custom = getAsStruct(data, "custom");
						if (custom != null) {
							String path = Caster.toString(custom.get("directory", null), null);
							if (!StringUtil.isEmpty(path)) { //
								Resource dir = ResourceUtil.toResourceNotExisting(pc, path);
								if (!dir.isDirectory()) throw new ApplicationException("Directory [" + path + " ] not exists ");
							}
						}
						// to is id the key or is it an array?
						admin.updateGatewayEntry(e.getKey().getString(), getClassDefinition(data, null), getAsString(data, "cfcPath"), getAsString(data, "listenerCfcPath"),
								GatewayEntryImpl.toStartup(getAsString(data, "startupMode")), custom, getAsBoolean(data, empty, false, "readOnly")

						);
					}
					catch (Throwable t) {
						handleException(pc, t);
					}
				}
			}

			// log settings
			coll = getAsCollection(json, "loggers");
			if (coll != null) {
				Iterator<Entry<Key, Object>> it = coll.entryIterator();
				Entry<Key, Object> e;
				Struct data;
				while (it.hasNext()) {
					e = it.next();
					data = cast.toStruct(e.getValue(), null);
					if (data == null) continue;
					try {
						LogEngine eng = config.getLogEngine();

						// appender
						String className = getAsString(data, "appender", "appenderClass", "appenderClassName");
						String bundleName = getAsString(data, "appenderBundleName");
						String bundleVersion = getAsString(data, "appenderBundleVersion");
						ClassDefinition acd = StringUtil.isEmpty(bundleName) ? eng.appenderClassDefintion(className)
								: new ClassDefinitionImpl(className, bundleName, bundleVersion, config.getIdentification());

						// layout
						className = getAsString(data, "layout", "layoutClass", "layoutClassName");
						bundleName = getAsString(data, "layoutBundleName");
						bundleVersion = getAsString(data, "layoutBundleVersion");
						ClassDefinition lcd = StringUtil.isEmpty(bundleName) ? eng.layoutClassDefintion(className)
								: new ClassDefinitionImpl(className, bundleName, bundleVersion, config.getIdentification());

						admin.updateLogSettings(e.getKey().getString(), LogUtil.toLevel(getAsString(data, "level")), acd, getAsStruct(data, "appenderArgs", "appenderArguments"),
								lcd, getAsStruct(data, "layoutArgs", "layoutArguments"));

					}
					catch (Throwable t) {
						handleException(pc, t);
					}
				}
			}

			// mail servers
			coll = getAsCollection(json, "mailServers");
			if (coll != null) {
				Iterator<Entry<Key, Object>> it = coll.entryIterator();
				Entry<Key, Object> e;
				Struct data;
				while (it.hasNext()) {
					e = it.next();
					data = cast.toStruct(e.getValue(), null);
					if (data == null) continue;
					try {
						admin.updateMailServer(getAsInt(data, empty, 0, "id"), getAsString(data, "smtp", "host", "server", "hostname"), getAsString(data, "username", "dbusername"),
								ConfigWebUtil.decrypt(getAsString(data, "password", "dbpassword")), getAsInt(data, empty, -1, "port"), getAsBoolean(data, empty, false, "tls"),
								getAsBoolean(data, empty, false, "ssl"), getAsTimespan(data, empty, TimeSpanImpl.fromMillis(1000 * 60 * 5), 0, "life").getMillis(),
								getAsTimespan(data, empty, TimeSpanImpl.fromMillis(1000 * 60 * 5), 0, "idle").getMillis(), getAsBoolean(data, empty, true, "reuseConnection"));
					}
					catch (Throwable t) {
						handleException(pc, t);
					}
				}
			}

			// scheduler
			coll = getAsCollection(json, "scheduledTasks");
			if (coll != null) {
				Iterator<Entry<Key, Object>> it = coll.entryIterator();
				Entry<Key, Object> e;
				Struct data;
				while (it.hasNext()) {
					e = it.next();
					data = cast.toStruct(e.getValue(), null);
					if (data == null) continue;
					try {
						admin.updateScheduledTask(pc, "update", getAsString(data, "name", "label", "task"), getAsBoolean(data, empty, false, "hidden"),
								getAsBoolean(data, empty, false, "readonly"), getAsString(data, "operation"), getAsString(data, "file"), getAsString(data, "path"),
								getAsObject(data, "startDate"), getAsObject(data, "startTime"), getAsObject(data, "endDate"), getAsObject(data, "endTime"),
								getAsString(data, "url"), getAsBoolean(data, empty, false, "publish"), getAsString(data, "interval"),
								getAsLong(data, empty, -1L, "requestTimeOut", "timeout"), getAsString(data, "username"), getAsString(data, "password"),
								getAsString(data, "proxyServer", "proxyHost"), getAsString(data, "proxyUser", "proxyUsername"), getAsString(data, "proxyPassword"),
								getAsInt(data, empty, 80, "proxyPort"), getAsBoolean(data, empty, false, "resolveURL"), getAsInt(data, empty, -1, "port"),
								getAsBoolean(data, empty, false, "unique"), getAsBoolean(data, empty, false, "autodelete"), getAsBoolean(data, empty, false, "paused"));

					}
					catch (Throwable t) {
						handleException(pc, t);
					}
				}
			}

			// mappings
			coll = getAsCollection(json, "mappings", "cfmappings");
			if (coll != null) {
				Iterator<Entry<Key, Object>> it = coll.entryIterator();
				Entry<Key, Object> e;
				Struct data;
				while (it.hasNext()) {
					e = it.next();
					data = cast.toStruct(e.getValue(), null);
					if (data == null) continue;
					try {
						admin.updateMapping(e.getKey().getString(), getAsString(data, "physical"), getAsString(data, "archive"), getAsString("physical", data, "primary"),
								ConfigWebUtil.inspectTemplate(getAsString(data, "inspect"), ConfigPro.INSPECT_UNDEFINED), getAsBoolean(data, empty, true, "toplevel"),
								ConfigWebUtil.toListenerMode(getAsString(data, "listenerMode"), -1), ConfigWebUtil.toListenerType(getAsString(data, "listenerType"), -1),
								getAsBoolean(data, empty, false, "readonly"));
					}
					catch (Throwable t) {
						handleException(pc, t);
					}
				}
			}

			// custom tags mappings
			coll = getAsCollection(json, "customTagMappings", "customTagPaths");
			if (coll != null) {
				Iterator<Entry<Key, Object>> it = coll.entryIterator();
				Entry<Key, Object> e;
				Struct data;
				while (it.hasNext()) {
					e = it.next();
					data = cast.toStruct(e.getValue(), null);
					if (data == null) continue;
					try {
						String p = getAsString(data, "physical");
						String a = getAsString(data, "archive");

						admin.updateCustomTag(getAsString(coll instanceof Array ? HashUtil.create64BitHashAsString(p + ":" + a) : e.getKey().getString(), data, "name", "id"), p, a,
								getAsString("physical", data, "primary"), ConfigWebUtil.inspectTemplate(getAsString(data, "inspect"), ConfigPro.INSPECT_UNDEFINED));
					}
					catch (Throwable t) {
						handleException(pc, t);
					}
				}
			}

			// custom tags mappings
			coll = getAsCollection(json, "componentMappings", "componentPaths");
			if (coll != null) {
				Iterator<Entry<Key, Object>> it = coll.entryIterator();
				Entry<Key, Object> e;
				Struct data;
				while (it.hasNext()) {
					e = it.next();
					data = cast.toStruct(e.getValue(), null);
					if (data == null) continue;
					try {
						String p = getAsString(data, "physical");
						String a = getAsString(data, "archive");
						admin.updateComponentMapping(
								getAsString(coll instanceof Array ? HashUtil.create64BitHashAsString(p + ":" + a) : e.getKey().getString(), data, "name", "id"), p, a,
								getAsString("physical", data, "primary"), ConfigWebUtil.inspectTemplate(getAsString(data, "inspect"), ConfigPro.INSPECT_UNDEFINED));
					}
					catch (Throwable t) {
						handleException(pc, t);
					}
				}
			}

			// debugging templates (array)
			coll = getAsCollection(json, "debugTemplates", "debugging");
			if (coll != null) {
				Iterator<Entry<Key, Object>> it = coll.entryIterator();
				Entry<Key, Object> e;
				Struct data;
				while (it.hasNext()) {
					e = it.next();
					data = cast.toStruct(e.getValue(), null);
					if (data == null) continue;
					try {
						admin.updateDebugEntry(getAsString(data, "type", "debugtype"), getAsString(data, "iprange"), getAsString(data, "label"), getAsString(data, "path"),
								getAsString(data, "fullname"), getAsStruct(data, "custom"));
					}
					catch (Throwable t) {
						handleException(pc, t);
					}
				}
			}

			// additional function and tag directories
			data = getAsStruct(json, "fileSystem");
			if (data != null) {
				try {
					admin.updateFilesystem(getAsString(data, "fldDefaultDirectory"), getAsString(data, "functionDefaultDirectory"), getAsString(data, "tagDefaultDirectory"),
							getAsString(data, "tldDefaultDirectory"), getAsString(data, "functionAdditionalDirectory", "functionAddionalDirectory"),
							getAsString(data, "tagAdditionalDirectory", "tagAddionalDirectory"));
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			// extensions
			optimizeExtensions(config, json);
			coll = getAsCollection(json, "extensions");
			if (coll != null) {
				Iterator<Entry<Key, Object>> it = coll.entryIterator();
				Entry<Key, Object> e;
				Struct data;
				while (it.hasNext()) {
					e = it.next();
					data = cast.toStruct(e.getValue(), null);
					if (data == null) continue;
					try {

						// ID
						String id = getAsString(data, "id");

						// this can be a binary that represent the extension, a string that is a path to the extension or a
						// base64 base encoded string
						String strSrc = getAsString(data, "source");

						// boolean fromCfConfig = getBoolV("fromCFConfig", false);

						if (!StringUtil.isEmpty(strSrc)) {
							// path
							Resource src = ResourceUtil.toResourceExisting(config, strSrc);
							admin.updateRHExtension(config, src, false, true, true);

						}
						else if (!StringUtil.isEmpty(id)) {
							ExtensionDefintion ed;
							String version = getAsString(data, "version");
							if (!StringUtil.isEmpty(version, true) && !"latest".equalsIgnoreCase(version)) ed = new ExtensionDefintion(id, version);
							else ed = RHExtension.toExtensionDefinition(id);
							DeployHandler.deployExtension(config, ed, ThreadLocalPageContext.getLog(config, "deploy"), false, true, true, admin);
						}
						else {
							throw new ApplicationException("cannot install extension, no source or id defined.");
						}

					}
					catch (Throwable t) {
						handleException(pc, t);
					}
				}
			}

			// scope (need to be at the end)
			str = getAsString(json, "scopecascadingtype");
			if (str != null) {
				try {
					admin.updateScopeCascadingType(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "allowimplicidquerycall");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateAllowImplicidQueryCall(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "mergeformandurl", "mergeformandurlScope");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateMergeFormAndUrl(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "sessionManagement");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateSessionManagement(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "clientManagement");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateClientManagement(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "setdomaincookies", "domaincookies");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateDomaincookies(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "setclientcookies", "clientcookies");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateClientCookies(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			ts = getAsTimespan(json, empty, null, -1, "clienttimeout");
			if (ts != null || empty.toBooleanValue()) {
				try {
					admin.updateClientTimeout(ts);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			ts = getAsTimespan(json, empty, null, 1000, "sessiontimeout");
			if (ts != null || empty.toBooleanValue()) {
				try {
					admin.updateSessionTimeout(ts);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			ts = getAsTimespan(json, empty, null, 1000, "applicationtimeout");
			if (ts != null || empty.toBooleanValue()) {
				try {
					admin.updateApplicationTimeout(ts);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			String clientStorage = getAsString(json, "clientstorage");
			if (!StringUtil.isEmpty(clientStorage, true)) {
				try {
					admin.updateClientStorage(clientStorage.trim(), false);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			String sessionStorage = getAsString(json, "sessionstorage");
			if (!StringUtil.isEmpty(sessionStorage, true)) {
				try {
					admin.updateSessionStorage(sessionStorage.trim(), false);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			str = getAsString(json, "sessiontype");
			if (str != null) {
				try {
					admin.updateSessionType(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			str = getAsString(json, "localScopeMode", "localmode");
			if (str != null) {
				try {
					admin.updateLocalMode(str);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}
			bool = getAsBoolean(json, empty, "cgiScopeReadonly", "cgireadonly");
			if (bool != null || empty.toBooleanValue()) {
				try {
					admin.updateCGIReadonly(bool);
				}
				catch (Throwable t) {
					handleException(pc, t);
				}
			}

			admin.storeAndReload();

			// validate
			if (!StringUtil.isEmpty(sessionStorage, true)) {
				admin.validateStorage(sessionStorage.trim());
			}
			if (!StringUtil.isEmpty(clientStorage, true)) {
				admin.validateStorage(clientStorage.trim());
			}

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
		if (throwException && exd != null) throw Caster.toPageException(exd);
		return json;

	}

	private ClassDefinition getClassDefinition(Struct data, String prefix) {
		if (!StringUtil.isEmpty(prefix, true)) return new ClassDefinitionImpl(getAsString(data, prefix + "class", prefix + "classname"), getAsString(data, prefix + "bundleName"),
				getAsString(data, prefix + "bundleVersion"), config.getIdentification());
		return new ClassDefinitionImpl(getAsString(data, "class", "classname"), getAsString(data, "bundleName"), getAsString(data, "bundleVersion"), config.getIdentification());
	}

	private void handleException(PageContext pc, Throwable t) {
		ExceptionUtil.rethrowIfNecessary(t);
		LogUtil.log(pc, "deploy", t);
		if (t instanceof JspException) exd = (JspException) t;
		else exd = Caster.toPageException(t);
	}

	private static Struct getAsStruct(Struct data, String... names) {
		Object val;
		Struct sct;
		for (String name: names) {
			val = data.get(name, null);
			if (val != null) {

				sct = Caster.toStruct(val, null);
				if (sct == null && Decision.isString(val)) {
					try {
						sct = ConfigWebUtil.toStruct(Caster.toString(val));
					}
					catch (PageException pee) {
					}
				}
				if (sct != null) return sct;
			}
		}
		return new StructImpl();
	}

	private static String getAsString(Struct data, String... names) {
		return getAsString(null, data, names);
	}

	private static String getAsString(String defaultValue, Struct data, String... names) {
		Object val;
		String str;
		for (String name: names) {
			val = data.get(name, null);
			if (val != null) {
				str = Caster.toString(val, null);
				if (str != null) return str;
			}
		}
		return defaultValue;
	}

	private static Boolean getAsBoolean(Struct data, RefBoolean empty, String... names) {
		Object val;
		Boolean b;
		empty.setValue(false);
		for (String name: names) {
			val = data.get(name, null);
			if (val != null) {
				b = Caster.toBoolean(val, null);
				if (b != null) return b;
				else if (StringUtil.isEmpty(val, true)) empty.setValue(true);
			}
		}
		return null;
	}

	private static boolean getAsBoolean(Struct data, RefBoolean empty, boolean defaultValue, String... names) {
		Object val;
		Boolean b;
		empty.setValue(false);
		for (String name: names) {
			val = data.get(name, null);
			if (val != null) {
				b = Caster.toBoolean(val, null);
				if (b != null) return b;
				else if (StringUtil.isEmpty(val, true)) empty.setValue(true);
			}
		}
		return defaultValue;
	}

	private static Integer getAsInteger(Struct data, RefBoolean empty, String... names) {
		Object val;
		Integer i;
		empty.setValue(false);
		for (String name: names) {
			val = data.get(name, null);
			if (val != null) {
				i = Caster.toInteger(val, null);
				if (i != null) return i;
				else if (StringUtil.isEmpty(val, true)) empty.setValue(true);
			}
		}
		return null;
	}

	private static int getAsInt(Struct data, RefBoolean empty, int defaultValue, String... names) {
		Object val;
		Integer i;
		empty.setValue(false);
		for (String name: names) {
			val = data.get(name, null);
			if (val != null) {
				i = Caster.toInteger(val, null);
				if (i != null) return i;
				else if (StringUtil.isEmpty(val, true)) empty.setValue(true);
			}
		}
		return defaultValue;
	}

	private static long getAsLong(Struct data, RefBoolean empty, long defaultValue, String... names) {
		Object val;
		Long l;
		empty.setValue(false);
		for (String name: names) {
			val = data.get(name, null);
			if (val != null) {
				l = Caster.toLong(val, null);
				if (l != null) return l;
				else if (StringUtil.isEmpty(val, true)) empty.setValue(true);
			}
		}
		return defaultValue;
	}

	private static TimeSpan getAsTimespan(Struct data, RefBoolean empty, TimeSpan defaultValue, long milliThreashold, String... names) {
		Object val;
		TimeSpan ts;
		empty.setValue(false);
		for (String name: names) {
			val = data.get(name, null);
			if (val != null) {
				ts = Caster.toTimespan(val, milliThreashold, null);
				if (ts != null) return ts;
				else if (StringUtil.isEmpty(val, true)) empty.setValue(true);
			}
		}
		return defaultValue;
	}

	private static Object getAsObject(Struct data, String... names) {
		Object val;
		Boolean b;
		for (String name: names) {
			val = data.get(name, null);
			if (val != null) return val;
		}
		return null;
	}

	private static Collection getAsCollection(Struct data, String... names) {
		Object val;
		Collection coll;
		for (String name: names) {
			val = data.get(name, null);
			if (val != null) {
				coll = Caster.toCollection(val, null);
				if (coll != null) return coll;
			}
		}
		return null;
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
		String res;
		boolean modified = false;
		int startIndex = -1;
		while (true) {
			startIndex = str.indexOf("${", startIndex + 1);
			if (startIndex == -1) break;
			int endIndex = str.indexOf("}", startIndex + 1);
			if (endIndex == -1) break;
			modified = true;
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

			if (val != null) res = Caster.toString(val, "");
			else res = defaultValue;

			str = str.substring(0, startIndex) + res + str.substring(endIndex + 1);
		}
		if (modified) e.setValue(str);
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

	private static int extractDatasourceAllow(Struct data, RefBoolean empty, int defaultValue) {
		Integer allow = getAsInt(data, empty, -1, "allow");
		if (allow != -1) return allow;

		Boolean allowed_select = getAsBoolean(data, empty, "allowSelect");
		if (allowed_select == null) return defaultValue; // if allowSelect isn't present, assume none provided

		allow = (getAsBoolean(data, empty, false, "allowSelect") ? DataSource.ALLOW_SELECT : 0) + (getAsBoolean(data, empty, false, "allowInsert") ? DataSource.ALLOW_INSERT : 0)
				+ (getAsBoolean(data, empty, false, "allowUpdate") ? DataSource.ALLOW_UPDATE : 0) + (getAsBoolean(data, empty, false, "allowDelete") ? DataSource.ALLOW_DELETE : 0)
				+ (getAsBoolean(data, empty, false, "allowAlter") ? DataSource.ALLOW_ALTER : 0) + (getAsBoolean(data, empty, false, "allowDrop") ? DataSource.ALLOW_DROP : 0)
				+ (getAsBoolean(data, empty, false, "allowRevoke") ? DataSource.ALLOW_REVOKE : 0) + (getAsBoolean(data, empty, false, "allowGrant") ? DataSource.ALLOW_GRANT : 0)
				+ (getAsBoolean(data, empty, false, "allowCreate") ? DataSource.ALLOW_CREATE : 0);
		return allow;
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

	private boolean setPasswordIfNecessary(ConfigPro config) throws PageException {
		if (!setPasswordIfNecessary) return false;
		boolean isServer = "server".equalsIgnoreCase(type);
		if ((isServer && !pwCheckedServer) || (!isServer && !pwCheckedWeb)) {
			boolean hasPassword = isServer ? config.hasServerPassword() : config.hasPassword();
			if (!hasPassword) {
				// create password
				try {
					if (config instanceof ConfigWebPro && isServer) ((ConfigWebPro) config).updatePassword(isServer, null, password);
					else config.updatePassword(null, password);
					return true;
				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
			}
			if (isServer) pwCheckedServer = true;
			else pwCheckedWeb = true;
		}
		return false;
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