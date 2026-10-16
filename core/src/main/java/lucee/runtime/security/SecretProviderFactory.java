package lucee.runtime.security;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.osgi.framework.BundleException;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpRow;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.SimpleValue;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.KeyConstants;

public class SecretProviderFactory {

	public static SecretProvider getInstance(Config config, String name, Struct data) throws PageException, ClassException, BundleException {
		ClassDefinition<SecretProvider> cd;

		cd = ConfigFactoryImpl.getClassDefinition(config, data, "", config.getIdentification());
		if (cd.hasClass()) {

			Struct custom = Caster.toStruct(data.get(KeyConstants._custom, null), null);
			if (custom == null) custom = Caster.toStruct(data.get(KeyConstants._properties, null), null);
			if (custom == null) custom = Caster.toStruct(data.get(KeyConstants._arguments, null), null);
			String _default = Caster.toString(data.get(KeyConstants._default, null), null);
			return getInstance(config, cd, custom, name, _default);
		}
		throw new ApplicationException("class defintion is invalid");
	}

	public static SecretProvider getInstance(Config config, ClassDefinition<? extends SecretProvider> cd, Struct properties, String name, String _default)
			throws PageException, ClassException, BundleException {
		// String id = createId(cd, properties, name, _default);

		SecretProvider sp = (SecretProvider) ClassUtil.loadInstance(cd.getClazz());
		LogUtil.logx(config, Log.LEVEL_TRACE, "secret-provider-factory", "create Secret Provider instance [" + cd.toString() + "]", "application");
		sp.init(config, properties, name);
		return sp;
	}

	public static class Ref implements SimpleValue, Dumpable, Castable, CharSequence {

		private SecretProvider sp;
		private String key;

		public Ref(SecretProvider sp, String key) {
			this.sp = sp;
			this.key = key;
		}

		@Override
		public Boolean castToBoolean(Boolean defaultValue) {
			String str = castToString(null);
			if (str == null) return defaultValue;
			return Caster.toBoolean(str, defaultValue);
		}

		@Override
		public boolean castToBooleanValue() throws PageException {
			return Caster.toBooleanValue(castToString());
		}

		@Override
		public DateTime castToDateTime() throws PageException {
			return Caster.toDate(castToString());
		}

		@Override
		public DateTime castToDateTime(DateTime defaultValue) {
			String str = castToString(null);
			if (str == null) return defaultValue;
			return Caster.toDate(str, false, null, defaultValue);
		}

		@Override
		public double castToDoubleValue() throws PageException {
			return Caster.toDoubleValue(castToString());
		}

		@Override
		public double castToDoubleValue(double defaultValue) {
			String str = castToString(null);
			if (str == null) return defaultValue;
			return Caster.toDoubleValue(str, defaultValue);
		}

		@Override
		public String castToString() throws PageException {
			if (LogUtil.doesTrace(sp.getLog())) {
				sp.getLog().log(Log.LEVEL_TRACE, "secret", "read secret [" + key + "] from provider [" + sp.getName() + "] at " + LogUtil.caller(null, ""));
			}
			return sp.getSecret(key);
		}

		@Override
		public String castToString(String defaultValue) {
			if (LogUtil.doesTrace(sp.getLog())) {
				sp.getLog().log(Log.LEVEL_TRACE, "secret", "read secret [" + key + "] from provider [" + sp.getName() + "] at " + LogUtil.caller(null, ""));
			}
			return sp.getSecret(key, defaultValue);
		}

		@Override
		public String toString() {
			String str = sp.getSecret(key, null);
			if (str == null) return super.toString();
			return str;
		}

		@Override
		public int compareTo(String other) throws PageException {
			return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), other);
		}

		@Override
		public int compareTo(boolean other) throws PageException {
			return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), other);
		}

		@Override
		public int compareTo(double other) throws PageException {
			return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), other);
		}

		@Override
		public int compareTo(DateTime other) throws PageException {
			return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), (Date) other);
		}

		@Override
		public DumpData toDumpData(PageContext pc, int maxlevel, DumpProperties props) {
			DumpTable table = new DumpTable("secret", "#90b800", "#cad1b0", "#000000");
			table.appendRow(new DumpRow(0, new SimpleDumpData(obfuscate(castToString(null)))));
			table.setTitle("Secret");
			return table;
		}

		public Ref touch() throws PageException {
			castToString();
			return this;
		}

		public Ref touch(boolean touch) throws PageException {
			if (touch) castToString();
			return this;
		}

		@Override
		public int length() {
			try {
				return castToString().length();
			}
			catch (PageException e) {
				throw new PageRuntimeException(e);
			}
		}

		@Override
		public char charAt(int index) {
			try {
				return castToString().charAt(index);
			}
			catch (PageException e) {
				throw new PageRuntimeException(e);
			}
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			try {
				return castToString().subSequence(start, end);
			}
			catch (PageException e) {
				throw new PageRuntimeException(e);
			}
		}
	}

	private static String obfuscate(String secret) {
		String val;
		if (StringUtil.isEmpty(secret, true)) val = "***";
		else if (secret.length() < 6) {
			StringBuilder sb = new StringBuilder(secret.length());
			for (int i = secret.length(); i >= 0; i--) {
				sb.append('*');
			}
			val = sb.toString();
		}
		else {
			StringBuilder sb = new StringBuilder(secret.length());
			for (int i = secret.length(); i > 1; i--) {
				sb.append('*');
			}
			val = secret.substring(0, 2) + sb.toString() + secret.substring(secret.length() - 2);
		}
		return val;
	}

	public static Ref getSecret(Config config, String secretProvider, String key, boolean resolve) throws PageException {
		if (StringUtil.isEmpty(secretProvider, true)) {
			for (SecretProvider sp: ((ConfigPro) config).getSecretProviders().values()) {
				if (sp.getSecret(key, null) != null) {
					return new SecretProviderFactory.Ref(sp, key).touch(resolve);
				}
			}
			throw new ApplicationException("no secret provider found that provides the key [" + key + "]");
		}

		SecretProvider sp = ((ConfigPro) config).getSecretProvider(secretProvider);
		return new SecretProviderFactory.Ref(sp, key).touch(resolve);
	}

	public static Collection<String> listSecretNames(Config config, String secretProvider) throws PageException {
		if (StringUtil.isEmpty(secretProvider, true)) {
			Set<String> allNames = new TreeSet<>(); // TreeSet for sorted output
			for (SecretProvider sp: ((ConfigPro) config).getSecretProviders().values()) {
				try {
					allNames.addAll(SecretProviderUtil.toSecretProviderExtended(sp).listSecretNames());
				}
				catch (PageException pe) {
					// provider doesn't support listSecretNames, skip
				}
			}
			return allNames;
		}
		return SecretProviderUtil.toSecretProviderExtended(((ConfigPro) config).getSecretProvider(secretProvider)).listSecretNames();
	}

	public static Map<String, Ref> listSecrets(Config config, String secretProvider, boolean resolve) throws PageException {
		Map<String, Ref> all = new HashMap<>();

		if (StringUtil.isEmpty(secretProvider, true)) {
			for (SecretProvider sp: ((ConfigPro) config).getSecretProviders().values()) {
				try {
					for (String name: SecretProviderUtil.toSecretProviderExtended(sp).listSecretNames()) {
						if (!all.containsKey(name)) {
							all.put(name, new SecretProviderFactory.Ref(sp, name).touch(resolve));
						}
					}
				}
				catch (PageException pe) {
					// provider doesn't support listSecretNames, skip
				}
			}
			return all;
		}

		SecretProvider sp = ((ConfigPro) config).getSecretProvider(secretProvider);
		try {
			for (String name: SecretProviderUtil.toSecretProviderExtended(sp).listSecretNames()) {
				all.put(name, new SecretProviderFactory.Ref(sp, name).touch(resolve));
			}
		}
		catch (PageException pe) {
			// provider doesn't support listSecretNames, return empty map
		}
		return all;
	}

	public static void removeSecret(Config config, String key, String secretProvider) throws PageException {
		if (StringUtil.isEmpty(secretProvider, true)) {
			for (SecretProvider sp: ((ConfigPro) config).getSecretProviders().values()) {
				if (sp.getSecret(key, null) != null) {
					SecretProviderUtil.toSecretProviderExtended(sp).removeSecret(key);
					return;
				}
			}
			throw new ApplicationException("No secret provider found that provides the key [" + key + "]");
		}

		SecretProviderUtil.toSecretProviderExtended(((ConfigPro) config).getSecretProvider(secretProvider)).removeSecret(key);
	}

}
