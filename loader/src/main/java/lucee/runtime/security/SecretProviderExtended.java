package lucee.runtime.security;

import java.util.List;

import lucee.runtime.exp.PageException;

public interface SecretProviderExtended extends SecretProvider {

	public void setSecret(String key, String value) throws PageException;

	public void setSecret(String key, boolean value) throws PageException;

	public void setSecret(String key, int value) throws PageException;

	public void removeSecret(String key) throws PageException;

	public List<String> listSecretNames() throws PageException;

}
