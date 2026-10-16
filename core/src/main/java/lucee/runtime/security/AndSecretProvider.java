package lucee.runtime.security;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ListUtil;

public class AndSecretProvider extends SecretProviderSupport {

	private List<String> providers = new ArrayList<>();

	@Override
	public void init(Config config, Struct properties, String name) throws PageException {
		super.init(config, properties, name);

		Object objProviders = properties.get("providers", null);
		if (objProviders == null) throw new ApplicationException("the property [providers] is required for the AND secrets provider [" + name + "]");

		Array arrProviders;
		if (objProviders instanceof String) {
			String strProviders = objProviders.toString().trim();
			if (StringUtil.isEmpty(strProviders, true)) throw new ApplicationException("the property [providers] is required for the AND secrets provider [" + name + "]");
			arrProviders = ListUtil.listToArrayRemoveEmpty(strProviders, ",");
		}
		else {
			arrProviders = Caster.toArray(objProviders);
		}

		Iterator<Object> it = arrProviders.valueIterator();
		String str;
		while (it.hasNext()) {
			str = Caster.toStringTrim(it.next(), null);
			if (!StringUtil.isEmpty(str, true)) providers.add(str);
		}

		if (providers.size() == 0) throw new ApplicationException("unable to load providers from the property [providers] for the AND secret provider [" + name + "]");
	}

	@Override
	public String getSecret(String key) throws PageException {
		String secret;
		for (String pn: providers) {
			secret = ((ConfigPro) getConfig()).getSecretProvider(pn).getSecret(key, null);
			if (secret != null) return secret;
		}
		throw new ApplicationException("No secret found for the key [" + key + "]");
	}

	@Override
	public String getSecret(String key, String defaultValue) {
		SecretProvider sp;
		String secret;
		for (String pn: providers) {
			try {
				sp = ((ConfigPro) getConfig()).getSecretProvider(pn);
			}
			catch (Exception e) {
				continue;
			}
			secret = sp.getSecret(key, null);
			if (secret != null) return secret;
		}
		return defaultValue;
	}

	@Override
	public boolean hasSecret(String key) {
		return getSecret(key, null) != null;
	}

	@Override
	public void refresh() throws PageException {
		for (String pn: providers) {
			try {
				((ConfigPro) getConfig()).getSecretProvider(pn).refresh();
			}
			catch (Exception e) {
				// continue refreshing other providers
			}
		}
	}

	@Override
	public void setSecret(String key, String value) throws PageException {
		throw new ApplicationException("The AND secret provider [" + getName() + "] is read-only. To set the secret [" + key + "], use one of the underlying providers directly: ["
				+ String.join(", ", providers) + "].");
	}

	@Override
	public void removeSecret(String key) throws PageException {
		throw new ApplicationException("The AND secret provider [" + getName() + "] is read-only. To remove the secret [" + key
				+ "], use one of the underlying providers directly: [" + String.join(", ", providers) + "].");
	}

	@Override
	public List<String> listSecretNames() throws PageException {
		TreeSet<String> names = new TreeSet<>();
		SecretProviderExtended spe;
		for (String pn: providers) {
			try {
				spe = SecretProviderUtil.toSecretProviderExtended(((ConfigPro) getConfig()).getSecretProvider(pn));
				names.addAll(spe.listSecretNames());
			}
			catch (Exception e) {
				// continue with other providers
			}
		}
		return new ArrayList<>(names);
	}
}