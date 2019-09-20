package lucee.runtime.vault;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lucee.commons.digest.RSA;
import lucee.runtime.coder.CoderException;

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

	public static Credential getCredential(String key, String username, String password)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, IOException {
		byte[] usr = RSA.encrypt(username, kp.getPrivate());
		byte[] pw = RSA.encrypt(password, kp.getPrivate());
		return new Credential(key, usr, pw);
	}

	public static String getUsername(Credential c) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, CoderException {
		return c.getUsername(kp.getPublic());
	}

	public static String getPassword(Credential c) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, CoderException {
		return c.getPassword(kp.getPublic());
	}
}
