package lucee.runtime.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import lucee.runtime.config.Config;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public class EnvVarSecretProvider extends SecretProviderSupport {

	private boolean caseSensitive;

	@Override
	public void init(Config config, Struct properties, String name) throws PageException {
		super.init(config, properties, name);

		caseSensitive = Caster.toBooleanValue(properties.get("caseSensitive", null), false);

	}

	@Override
	public String getSecret(String key) throws ApplicationException {
		String val = getSecret(key, null);
		if (val != null) return val;
		throw new ApplicationException("No environment variable was found with the name [" + key + "]");
	}

	@Override
	public String getSecret(String key, String defaultValue) {
		if (!caseSensitive) {
			for (Entry<String, String> e: System.getenv().entrySet()) {
				if (e.getKey().equalsIgnoreCase(key)) return e.getValue();
			}
		}
		else {
			String sec = System.getenv(key);
			if (sec != null) return sec;
		}
		return defaultValue;
	}

	@Override
	public void removeSecret(String key) throws PageException {
		throw new ApplicationException("Environment variables are read-only at runtime and cannot be removed by the JVM. To remove the variable [" + key
				+ "], unset it in your OS environment, shell profile, Docker/container config, or system startup script before launching Lucee.");
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
		throw new ApplicationException("Environment variables are read-only at runtime and cannot be set or modified by the JVM. To set the variable [" + key
				+ "], define it in your OS environment, shell profile, Docker/container config, or system startup script before launching Lucee.");
	}

	@Override
	public List<String> listSecretNames() throws PageException {
		return new ArrayList<>(new TreeSet<>(System.getenv().keySet()));
	}

}
