package lucee.runtime.security;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.config.Config;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public class FileSecretProvider extends SecretProviderSupport {
	private static final int TYPE_JSON = 1;
	private static final int TYPE_ENV = 2;

	private boolean caseSensitive;
	private Resource res;
	private Map<String, String> data;
	private int type;
	// private boolean readonly;

	@Override
	public void init(Config config, Struct properties, String name) throws PageException {
		super.init(config, properties, name);
		caseSensitive = Caster.toBooleanValue(properties.get("caseSensitive", null), false);
		// readonly = Caster.toBooleanValue(properties.get("readonly", null), true);

		boolean doThrow = false;
		String type = Caster.toString(properties.get("type", null), null);
		if (type == null) doThrow = true;
		else {
			type = type.trim();
			if ("json".equalsIgnoreCase(type)) this.type = TYPE_JSON;
			else if ("env".equalsIgnoreCase(type)) this.type = TYPE_ENV;
			else doThrow = true;
		}
		if (doThrow) throw new ApplicationException("the property [type] is required for the File secrets provider, with one of the following values [json,env]");

		// data
		String strFile = Caster.toStringTrim(properties.get(KeyConstants._file, null), null);
		if (Util.isEmpty(strFile, true)) throw new ApplicationException("the property [file] containing the secrets is required for the File secrets provider");
		res = ResourceUtil.toResourceExisting(config, strFile);
		init();
	}

	private void init() throws PageException {
		// json
		if (type == TYPE_JSON) {
			Struct raw;
			try {
				raw = CFMLEngineFactory.getInstance().getCastUtil().fromJsonStringToStruct(IOUtil.toString(res, (Charset) null));
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
			Map<String, String> tmp = new ConcurrentHashMap<>();
			Iterator<Entry<Key, Object>> it = raw.entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				tmp.put(caseSensitive ? e.getKey().getString() : e.getKey().getLowerString(), Caster.toString(e.getValue()));
			}
			this.data = tmp;
		}
		// env
		else if (type == TYPE_ENV) {
			InputStream is = null;
			try {
				is = res.getInputStream();
				Properties properties = new Properties();
				properties.load(is);
				Map<String, String> tmp = new ConcurrentHashMap<>();
				for (Entry<Object, Object> e: properties.entrySet()) {
					tmp.put(caseSensitive ? Caster.toString(e.getKey()) : Caster.toString(e.getKey()).toLowerCase(), Caster.toString(e.getValue()));
				}
				this.data = tmp;
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
			finally {
				IOUtil.closeEL(is);
			}
		}
	}

	@Override
	public String getSecret(String key) throws PageException {
		String tmp = data.get(caseSensitive ? key : key.toLowerCase());
		if (tmp == null) throw new ApplicationException(ExceptionUtil.similarKeyMessage(data.keySet(), key, "key", "keys", getName() + " secret provider", false));
		return Caster.toString(tmp);
	}

	@Override
	public String getSecret(String key, String defaultValue) {
		String tmp = data.get(caseSensitive ? key : key.toLowerCase());
		if (tmp == null) return defaultValue;
		return Caster.toString(tmp, defaultValue);
	}

	@Override
	public boolean hasSecret(String key) {
		return data.containsKey(caseSensitive ? key : key.toLowerCase());
	}

	@Override
	public void refresh() throws PageException {
		init();
	}

	@Override
	public void setSecret(String key, String value) throws PageException {
		throw new ApplicationException("The File secret provider is read-only at runtime.");
	}

	@Override
	public void removeSecret(String key) throws PageException {
		throw new ApplicationException("The File secret provider is read-only at runtime.");
	}

	@Override
	public List<String> listSecretNames() throws PageException {
		return new ArrayList<>(new TreeSet<>(data.keySet()));
	}

	private static String toType(int type, String defaultValue) {
		if (TYPE_ENV == type) return "env";
		if (TYPE_JSON == type) return "json";
		return defaultValue;
	}
}