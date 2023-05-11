package lucee.runtime.coder;

import java.io.IOException;
import java.security.InvalidKeyException;
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

public class RSA {

	private static final int KEY_SIZE = 1024;
	private Cipher encCipher;
	private Cipher decCipher;

	public RSA(PrivateKey privateKeyToEncrypt, PublicKey publicKeyToDecrypt) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		if (privateKeyToEncrypt != null) {
			encCipher = Cipher.getInstance("RSA");
			encCipher.init(Cipher.ENCRYPT_MODE, privateKeyToEncrypt);
		}
		if (publicKeyToDecrypt != null) {
			decCipher = Cipher.getInstance("RSA");
			decCipher.init(Cipher.DECRYPT_MODE, publicKeyToDecrypt);
		}
	}

	public static String toString(PrivateKey privateKey) {
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
		return toString(pkcs8EncodedKeySpec.getEncoded());
	}

	public static String toString(PublicKey publicKey) {
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
		return toString(x509EncodedKeySpec.getEncoded());
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
		return Base64Coder.encode(barr);
	}

	private static byte[] toBytes(String str) throws CoderException {
		return Base64Coder.decode(str, true);
	}

	public static KeyPair createKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(KEY_SIZE);
		return kpg.genKeyPair();
	}

	private static byte[] encrypt(byte[] data, PrivateKey privateKeyToEncrypt)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return new RSA(privateKeyToEncrypt, null).encrypt(data);
	}

	public byte[] encrypt(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		if (encCipher == null) throw new RuntimeException("Cipher is not initialized!");
		int max = (KEY_SIZE / 8) - 11;

		// we need to split in pieces, because RSA cannot handle pieces bigger than the key size
		List<byte[]> list = new ArrayList<byte[]>();
		int offset = 0, len = data.length, l, total = 0;
		byte[] part;
		while (offset < len) {
			l = len - offset < max ? len - offset : max;
			part = encCipher.doFinal(data, offset, l);
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

	private static byte[] decrypt(byte[] data, PublicKey publicKeyToDecrypt, int offset)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return new RSA(null, publicKeyToDecrypt).decrypt(data, offset);
	}

	public byte[] decrypt(byte[] data, int offset) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		if (decCipher == null) throw new RuntimeException("Cipher is not initialized!");

		int max = (KEY_SIZE / 8);

		// we need to split in pieces, because RSA cannot handle pieces bigger than the key size
		List<byte[]> list = new ArrayList<byte[]>();
		int off = offset, len = data.length, l, total = 0;
		byte[] part;
		while (off < len) {
			l = len - off < max ? len - off : max;
			part = decCipher.doFinal(data, off, l);
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

	/*
	 * public static void main(String[] args) throws Exception { KeyPair kp = createKeyPair(); String
	 * str = ""; for (int i = 0; i <= 1700; i++) {
	 * 
	 * str += i + "Hello there how are you?\n"; } byte[] bytes = str.getBytes();
	 * 
	 * long start = System.currentTimeMillis(); byte[] enc = encrypt(bytes, kp.getPrivate()); byte[]
	 * encPlus = new byte[enc.length + 2];
	 * 
	 * for (int i = 0; i < enc.length; i++) { encPlus[i + 2] = enc[i]; }
	 * 
	 * System. out.println(enc.length); byte[] dec = decrypt(encPlus, kp.getPublic(), 2); System
	 * .out.println(new String(dec)); System .out.println("->" + (System.currentTimeMillis() - start));
	 * }
	 */

}
