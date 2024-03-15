package lucee.runtime.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.op.Caster;
import lucee.runtime.thread.SerializableCookie;
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
	private final boolean flushExistingData;
	private PageException exd = null;

	public CFConfigImport(Config config, Resource file, Charset charset, String password, String type, Struct placeHolderData, boolean setPasswordIfNecessary,
			boolean validatePassword, boolean flushExistingData) throws PageException {

		this.file = file;
		this.charset = charset;
		this.password = password;
		this.type = type;
		this.placeHolderData = placeHolderData;
		this.setPasswordIfNecessary = setPasswordIfNecessary;
		this.validatePassword = validatePassword;
		this.flushExistingData = flushExistingData;
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
			boolean validatePassword, boolean flushExistingData) throws PageException {
		this.data = data;
		this.charset = charset;
		this.password = password;
		this.validatePassword = validatePassword;
		this.type = type;
		this.placeHolderData = placeHolderData;
		this.flushExistingData = flushExistingData;
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

				pc = PageContextUtil.getPageContext(config, null, (File) SystemUtil.getTempDirectory(), "localhost", "/", "", SerializableCookie.COOKIES0, null, null, null,
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
			ConfigAdmin admin = ConfigAdmin.newInstance(config, pw, updated || !validatePassword);

			admin.updateConfig(json, flushExistingData);
			admin.storeAndReload();
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
		if (throwException && exd != null) throw exd;
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
					else {
						PasswordImpl.updatePassword(config, null, password);
					}
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