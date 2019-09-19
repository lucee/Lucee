package lucee.runtime.vault;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lucee.commons.digest.RSA;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.coder.CoderException;
import lucee.runtime.crypt.Cryptor;
import lucee.runtime.util.Excepton;

public class Credential {

	private String key;
	private byte[] username;
	private byte[] password;

	public Credential(String key, byte[] username, byte[] password) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		this.username = username;
		this.password = password;
	}

	public static void validate() {
		StackTraceElement caller = Caller.caller(5);
		if (!caller.getClassName().startsWith("lucee.runtime.")) {
			Excepton util = CFMLEngineFactory.getInstance().getExceptionUtil();
			util.createPageRuntimeException(util.createApplicationException("You cannot access the credentials info from your context"));
		}
	}

	String getUsername(PublicKey decryptKey) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, CoderException {
		validate();
		return new String(RSA.decrypt(username, decryptKey, 0), Cryptor.DEFAULT_CHARSET);
	}

	String getPassword(PublicKey decryptKey) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, CoderException {
		validate();
		return new String(RSA.decrypt(password, decryptKey, 0), Cryptor.DEFAULT_CHARSET);
	}

	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "credential:" + key;
	}
}
