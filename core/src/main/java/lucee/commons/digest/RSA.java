package lucee.commons.digest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lucee.runtime.coder.CoderException;
import lucee.runtime.crypt.Cryptor;

public class RSA {

	private static final int KEY_SIZE = 1024;

	public static String toString(PrivateKey privateKey) {
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
		return toString(pkcs8EncodedKeySpec.getEncoded());
	}

	public static String toString(PublicKey publicKey) {
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
		return toString(x509EncodedKeySpec.getEncoded());
	}

	public static Key toKey(String key) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		try {
			return toPrivateKey(key);
		}
		catch (InvalidKeySpecException ikse) {
			return toPublicKey(key);
		}
	}

	public static PrivateKey toPrivateKey(String privateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] bytes = toBytes(privateKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bytes);
		return keyFactory.generatePrivate(privateKeySpec);
	}

	public static PublicKey toPublicKey(String publicKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] bytes = toBytes(publicKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bytes);
		return keyFactory.generatePublic(publicKeySpec);
	}

	private static String toString(byte[] barr) {
		return Base64Encoder.encode(barr);
	}

	private static byte[] toBytes(String str) throws CoderException {
		return Base64Encoder.decode(str);
	}

	public static KeyPair createKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(KEY_SIZE);
		return kpg.genKeyPair();
	}

	public static byte[] encrypt(String data, Key key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		return encrypt(data.getBytes(Cryptor.DEFAULT_CHARSET), key);
	}

	public static byte[] encrypt(byte[] data, Key key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA");

		cipher.init(Cipher.ENCRYPT_MODE, key);
		int max = (KEY_SIZE / 8) - 11;

		// we need to split in pieces, because RSA cannot handle pices bigger than the key size
		List<byte[]> list = new ArrayList<byte[]>();
		int offset = 0, len = data.length, l, total = 0;
		byte[] part;
		while (offset < len) {
			l = len - offset < max ? len - offset : max;
			part = cipher.doFinal(data, offset, l);
			total += part.length;
			list.add(part);
			offset += l;
		}

		// now we merge to one piece
		byte[] bytes = new byte[total];
		Iterator<byte[]> it = list.iterator();
		int count = 0;
		while (it.hasNext()) {
			part = it.next();
			for (int i = 0; i < part.length; i++) {
				bytes[count++] = part[i];
			}
		}

		return bytes;
	}

	public static String decryptAsString(byte[] data, Key key, int offset)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		return new String(decrypt(data, key, offset), Cryptor.DEFAULT_CHARSET);
	}

	public static byte[] decrypt(byte[] data, Key key, int offset)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		int max = (KEY_SIZE / 8);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);

		// we need to split in pieces, because RSA cannot handle pieces bigger than the key size
		List<byte[]> list = new ArrayList<byte[]>();
		int off = offset, len = data.length, l, total = 0;
		byte[] part;
		while (off < len) {
			l = len - off < max ? len - off : max;
			part = cipher.doFinal(data, off, l);
			total += part.length;
			list.add(part);
			off += l;
		}

		// now we merge to one piece
		byte[] bytes = new byte[total];
		Iterator<byte[]> it = list.iterator();
		int count = 0;
		while (it.hasNext()) {
			part = it.next();
			for (int i = 0; i < part.length; i++) {
				bytes[count++] = part[i];
			}
		}
		return bytes;
	}
}