package lucee.runtime.security;

import java.lang.reflect.Method;
import java.util.List;

import lucee.commons.io.log.Log;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.config.Config;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public class SecretProviderUtil {

	public static SecretProviderExtended toSecretProviderExtended(SecretProvider sp) {
		if (sp instanceof SecretProviderExtended) return (SecretProviderExtended) sp;
		return new SecretProviderExtendedWrapper(sp);
	}

	private static class SecretProviderExtendedWrapper implements SecretProviderExtended {

		private final SecretProvider provider;
		private final Class<?> providerClass;

		// Cached methods (null if not found)
		private Method setSecretString;
		private Method setSecretBoolean;
		private Method setSecretInteger;
		private Method removeSecret;
		private Method listSecretNames;

		private boolean methodsResolved = false;

		public SecretProviderExtendedWrapper(SecretProvider provider) {
			this.provider = provider;
			this.providerClass = provider.getClass();
		}

		private synchronized void resolveMethods() {
			if (methodsResolved) return;

			try {
				setSecretString = providerClass.getMethod("setSecret", String.class, String.class);
			}
			catch (NoSuchMethodException e) {
				// method not available
			}

			try {
				setSecretBoolean = providerClass.getMethod("setSecret", String.class, boolean.class);
			}
			catch (NoSuchMethodException e) {
				// method not available
			}

			try {
				setSecretInteger = providerClass.getMethod("setSecret", String.class, int.class);
			}
			catch (NoSuchMethodException e) {
				// method not available
			}

			try {
				removeSecret = providerClass.getMethod("removeSecret", String.class);
			}
			catch (NoSuchMethodException e) {
				// method not available
			}

			try {
				listSecretNames = providerClass.getMethod("listSecretNames");
			}
			catch (NoSuchMethodException e) {
				// method not available
			}

			methodsResolved = true;
		}

		// ========== Delegated methods from SecretProvider ==========

		@Override
		public void init(Config config, Struct properties, String name) throws PageException {
			provider.init(config, properties, name);
		}

		@Override
		public String getSecret(String key) throws PageException {
			return provider.getSecret(key);
		}

		@Override
		public String getSecret(String key, String defaultValue) {
			return provider.getSecret(key, defaultValue);
		}

		@Override
		public boolean getSecretAsBoolean(String key) throws PageException {
			return provider.getSecretAsBoolean(key);
		}

		@Override
		public boolean getSecretAsBoolean(String key, boolean defaultValue) {
			return provider.getSecretAsBoolean(key, defaultValue);
		}

		@Override
		public int getSecretAsInteger(String key) throws PageException {
			return provider.getSecretAsInteger(key);
		}

		@Override
		public int getSecretAsInteger(String key, int defaultValue) {
			return provider.getSecretAsInteger(key, defaultValue);
		}

		@Override
		public boolean hasSecret(String key) {
			return provider.hasSecret(key);
		}

		@Override
		public void refresh() throws PageException {
			provider.refresh();
		}

		@Override
		public Log getLog() {
			return provider.getLog();
		}

		@Override
		public String getName() {
			return provider.getName();
		}

		// ========== Extended methods via reflection ==========

		@Override
		public void setSecret(String key, String value) throws PageException {
			resolveMethods();
			if (setSecretString == null) {
				throw new ApplicationException("The secret provider [" + provider.getName() + "] does not support setSecret(String, String).");
			}
			invokeMethod(setSecretString, key, value);
		}

		@Override
		public void setSecret(String key, boolean value) throws PageException {
			resolveMethods();
			if (setSecretBoolean == null) {
				throw new ApplicationException("The secret provider [" + provider.getName() + "] does not support setSecret(String, Boolean).");
			}
			invokeMethod(setSecretBoolean, key, value);
		}

		@Override
		public void setSecret(String key, int value) throws PageException {
			resolveMethods();
			if (setSecretInteger == null) {
				throw new ApplicationException("The secret provider [" + provider.getName() + "] does not support setSecret(String, Integer).");
			}
			invokeMethod(setSecretInteger, key, value);
		}

		@Override
		public void removeSecret(String key) throws PageException {
			resolveMethods();
			if (removeSecret == null) {
				throw new ApplicationException("The secret provider [" + provider.getName() + "] does not support removeSecret(String).");
			}
			invokeMethod(removeSecret, key);
		}

		@Override
		@SuppressWarnings("unchecked")
		public List<String> listSecretNames() throws PageException {
			resolveMethods();
			if (listSecretNames == null) {
				throw new ApplicationException("The secret provider [" + provider.getName() + "] does not support listSecretNames().");
			}
			return (List<String>) invokeMethod(listSecretNames);
		}

		private Object invokeMethod(Method method, Object... args) throws PageException {
			try {
				return method.invoke(provider, args);
			}
			catch (Exception e) {
				ApplicationException ae = new ApplicationException("Error invoking method [" + method.getName() + "] on secret provider [" + provider.getName() + "]");
				ExceptionUtil.initCauseEL(ae, e);
				throw ae;
			}
		}
	}
}