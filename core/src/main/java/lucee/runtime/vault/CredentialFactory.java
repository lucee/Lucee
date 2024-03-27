package lucee.runtime.vault;

import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import lucee.commons.digest.RSA;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class CredentialFactory {

	private static KeyPair kp;

	static {
		try {
			kp = RSA.createKeyPair();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	public static Credential getCredential(String key, String username, String password) throws PageException {
		byte[] usr = RSA.encrypt(username, kp.getPrivate());
		byte[] pw = RSA.encrypt(password, kp.getPrivate());
		try {
			return new Credential(key, usr, pw);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public static String getUsername(Credential c) throws PageException, UnsupportedEncodingException {
		return c.getUsername(kp.getPublic());
	}

	public static String getPassword(Credential c) throws PageException, UnsupportedEncodingException {
		return c.getPassword(kp.getPublic());
	}
}
