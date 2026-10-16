package lucee.runtime.security;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import lucee.runtime.config.Config;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public class SystemPropSecretProvider extends SecretProviderSupport {

	private boolean caseSensitive;

	@Override
	public void init(Config config, Struct properties, String name) throws PageException {
		super.init(config, properties, name);
		caseSensitive = Caster.toBooleanValue(properties.get("caseSensitive", null), true);
	}

	@Override
	public String getSecret(String key) throws ApplicationException {
		String val = getSecret(key, null);
		if (val != null) return val;
		throw new ApplicationException("No system property was found with the name [" + key + "]");
	}

	@Override
	public String getSecret(String key, String defaultValue) {
		if (!caseSensitive) {
			for (String name: System.getProperties().stringPropertyNames()) {
				if (name.equalsIgnoreCase(key)) return System.getProperty(name);
			}
		}
		else {
			String sec = System.getProperty(key);
			if (sec != null) return sec;
		}
		return defaultValue;
	}

	@Override
	public boolean hasSecret(String key) {
		return getSecret(key, null) != null;
	}

	@Override
	public void refresh() {
		// no cache so no refresh needed
	}

	@Override
	public void setSecret(String key, String value) throws PageException {
		System.setProperty(key, value);
	}

	@Override
	public void removeSecret(String key) throws PageException {
		if (!hasSecret(key)) {
			throw new ApplicationException("No system property was found with the name [" + key + "]");
		}

		if (!caseSensitive) {
			for (String name: System.getProperties().stringPropertyNames()) {
				if (name.equalsIgnoreCase(key)) {
					System.clearProperty(name);
					return;
				}
			}
		}
		else {
			System.clearProperty(key);
		}
	}

	@Override
	public List<String> listSecretNames() throws PageException {
		return new ArrayList<>(new TreeSet<>(System.getProperties().stringPropertyNames()));
	}
}