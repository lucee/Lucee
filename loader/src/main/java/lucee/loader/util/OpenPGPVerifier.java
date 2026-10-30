package lucee.loader.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Minimal OpenPGP (RFC 4880) verifier for the detached signatures (.asc) Maven Central requires on
 * every artifact. It only uses the JDK, so it can run before anything else is loaded. It can read
 * RSA public keys and verify binary detached version 4 signatures with SHA-224/256/384/512, nothing
 * else (no signing, no key trust model, no revocation, no expiring keys).
 */
public final class OpenPGPVerifier {

	private static final int TAG_SIGNATURE = 2;
	private static final int TAG_PUBLIC_KEY = 6;

	private static final int ALGO_RSA = 1;
	private static final int ALGO_RSA_SIGN_ONLY = 3;

	private static final int SIG_BINARY_DOCUMENT = 0;

	private static final int SUB_CREATION_TIME = 2;
	private static final int SUB_EXPIRATION_TIME = 3;
	private static final int SUB_ISSUER_KEY_ID = 16;
	private static final int SUB_ISSUER_FINGERPRINT = 33;

	// artifacts are small compared to this, it only protects against a bogus endless "signature"
	private static final int MAX_SIGNATURE_SIZE = 1024 * 1024;

	private OpenPGPVerifier() {
	}

	/**
	 * The data to verify, it is opened once for every candidate key.
	 */
	public interface Source {
		InputStream open() throws IOException;
	}

	/**
	 * thrown when the signature is missing, malformed or does not match one of the trusted keys
	 */
	public static class VerificationException extends IOException {
		private static final long serialVersionUID = 1L;

		public VerificationException(String message) {
			super(message);
		}

		public VerificationException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static final class PublicKey {
		private final long keyId;
		private final byte[] fingerprint;
		private final RSAPublicKey key;

		private PublicKey(long keyId, byte[] fingerprint, RSAPublicKey key) {
			this.keyId = keyId;
			this.fingerprint = fingerprint;
			this.key = key;
		}

		public String getKeyId() {
			return String.format("%016X", keyId);
		}

		public String getFingerprint() {
			StringBuilder sb = new StringBuilder();
			for (byte b: fingerprint) {
				sb.append(String.format("%02X", b & 0xff));
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			return getFingerprint();
		}
	}

	private static final class Packet {
		final int tag;
		final byte[] body;

		Packet(int tag, byte[] body) {
			this.tag = tag;
			this.body = body;
		}
	}

	private static final class ParsedSignature {
		byte[] hashedPart; // version, type, public key algorithm, hash algorithm, length of hashed subpackets and the hashed subpackets
		int hashAlgorithm;
		long creationTime = -1;
		long expiresAfter = 0;
		long issuerKeyId = 0;
		BigInteger value;
	}

	/**
	 * reads the RSA public keys of a key file, it can be ASCII armored or binary. Only primary keys are
	 * taken, subkeys are ignored, so every key that is allowed to verify has to be in the file as a
	 * primary key.
	 */
	public static List<PublicKey> readKeys(InputStream is) throws IOException {
		List<PublicKey> keys = new ArrayList<>();
		for (Packet p: readPackets(readAll(is))) {
			if (p.tag != TAG_PUBLIC_KEY) continue;
			PublicKey key = toPublicKey(p.body);
			if (key != null) keys.add(key);
		}
		return keys;
	}

	/**
	 * verifies the detached signature (armored or binary) of the data against the given keys.
	 * 
	 * @return the key the signature is valid for
	 * @throws VerificationException when there is no valid signature of one of the given keys
	 */
	public static PublicKey verify(File data, byte[] signature, List<PublicKey> keys) throws IOException {
		return verify(new Source() {
			@Override
			public InputStream open() throws IOException {
				return new FileInputStream(data);
			}
		}, signature, keys);
	}

	public static PublicKey verify(Source data, byte[] signature, List<PublicKey> keys) throws IOException {
		if (keys == null || keys.isEmpty()) throw new VerificationException("no trusted public key available");
		if (signature == null || signature.length == 0) throw new VerificationException("the signature is empty");

		List<Packet> packets;
		try {
			packets = readPackets(signature);
		}
		catch (IOException e) {
			throw new VerificationException("the signature is not a valid OpenPGP signature: " + e.getMessage(), e);
		}

		StringBuilder reasons = new StringBuilder();
		boolean found = false;
		for (Packet p: packets) {
			if (p.tag != TAG_SIGNATURE) continue;
			found = true;
			ParsedSignature sig;
			try {
				sig = parseSignature(p.body);
			}
			catch (IOException e) {
				addReason(reasons, e.getMessage());
				continue;
			}

			long now = System.currentTimeMillis() / 1000L;
			if (sig.expiresAfter > 0 && sig.creationTime >= 0 && sig.creationTime + sig.expiresAfter < now) {
				addReason(reasons, "the signature has expired");
				continue;
			}

			boolean candidate = false;
			for (PublicKey key: keys) {
				if (sig.issuerKeyId != 0 && sig.issuerKeyId != key.keyId) continue;
				candidate = true;
				if (matches(data, sig, key)) return key;
				addReason(reasons, "the signature of the key [" + key.getKeyId() + "] does not match the data");
			}
			if (!candidate) addReason(reasons, "the signature was made with the key [" + String.format("%016X", sig.issuerKeyId) + "], which is not a trusted key");
		}
		if (!found) throw new VerificationException("the signature file does not contain a signature");
		throw new VerificationException(reasons.toString());
	}

	private static void addReason(StringBuilder sb, String reason) {
		if (sb.indexOf(reason) != -1) return;
		if (sb.length() > 0) sb.append("; ");
		sb.append(reason);
	}

	private static boolean matches(Source data, ParsedSignature sig, PublicKey key) throws IOException {
		Signature s;
		try {
			s = Signature.getInstance(toJcaName(sig.hashAlgorithm));
			s.initVerify(key.key);

			InputStream is = data.open();
			try {
				byte[] buffer = new byte[64 * 1024];
				int len;
				while ((len = is.read(buffer)) != -1) {
					s.update(buffer, 0, len);
				}
			}
			finally {
				is.close();
			}
			// trailer of a version 4 signature
			s.update(sig.hashedPart);
			int l = sig.hashedPart.length;
			s.update(new byte[] { 4, (byte) 0xff, (byte) (l >>> 24), (byte) (l >>> 16), (byte) (l >>> 8), (byte) l });

			// the signature is stored as an MPI without leading zeros, the JDK wants the length of the modulus
			int size = (key.key.getModulus().bitLength() + 7) / 8;
			byte[] raw = toUnsigned(sig.value);
			if (raw.length > size) return false;
			byte[] padded = new byte[size];
			System.arraycopy(raw, 0, padded, size - raw.length, raw.length);
			return s.verify(padded);
		}
		catch (SignatureException e) {
			// malformed signature value
			return false;
		}
		catch (GeneralSecurityException e) {
			throw new VerificationException("failed to verify the signature: " + e.getMessage(), e);
		}
	}

	private static String toJcaName(int hashAlgorithm) throws VerificationException {
		switch (hashAlgorithm) {
		case 8:
			return "SHA256withRSA";
		case 9:
			return "SHA384withRSA";
		case 10:
			return "SHA512withRSA";
		case 11:
			return "SHA224withRSA";
		default:
			throw new VerificationException("unsupported hash algorithm [" + hashAlgorithm + "] in the signature, only SHA-224, SHA-256, SHA-384 and SHA-512 are accepted");
		}
	}

	private static byte[] toUnsigned(BigInteger bi) {
		byte[] b = bi.toByteArray();
		if (b.length > 1 && b[0] == 0) {
			byte[] tmp = new byte[b.length - 1];
			System.arraycopy(b, 1, tmp, 0, tmp.length);
			return tmp;
		}
		return b;
	}

	private static ParsedSignature parseSignature(byte[] body) throws IOException {
		if (body.length < 10) throw new IOException("truncated signature packet");
		int version = body[0] & 0xff;
		if (version != 4) throw new IOException("unsupported signature version [" + version + "], only version 4 is supported");
		int type = body[1] & 0xff;
		if (type != SIG_BINARY_DOCUMENT) throw new IOException("unsupported signature type [" + type + "], only signatures of binary documents are supported");
		int pubAlgo = body[2] & 0xff;
		if (pubAlgo != ALGO_RSA && pubAlgo != ALGO_RSA_SIGN_ONLY) throw new IOException("unsupported public key algorithm [" + pubAlgo + "], only RSA is supported");

		ParsedSignature sig = new ParsedSignature();
		sig.hashAlgorithm = body[3] & 0xff;
		toJcaName(sig.hashAlgorithm);

		int hashedLen = u16(body, 4);
		int hashedEnd = 6 + hashedLen;
		if (hashedEnd + 2 > body.length) throw new IOException("truncated signature packet");
		sig.hashedPart = new byte[hashedEnd];
		System.arraycopy(body, 0, sig.hashedPart, 0, hashedEnd);
		int unhashedLen = u16(body, hashedEnd);
		int unhashedEnd = hashedEnd + 2 + unhashedLen;
		if (unhashedEnd + 2 > body.length) throw new IOException("truncated signature packet");

		parseSubpackets(body, 6, hashedEnd, sig, true);
		// the unhashed area is not protected by the signature, it is only a hint which key to use
		parseSubpackets(body, hashedEnd + 2, unhashedEnd, sig, false);

		// skip the left 16 bits of the hash
		int[] pos = new int[] { unhashedEnd + 2 };
		sig.value = readMPI(body, pos);
		return sig;
	}

	private static void parseSubpackets(byte[] b, int pos, int end, ParsedSignature sig, boolean hashed) throws IOException {
		while (pos < end) {
			int len = b[pos++] & 0xff;
			if (len >= 192 && len < 255) {
				if (pos >= end) throw new IOException("truncated signature subpacket");
				len = ((len - 192) << 8) + (b[pos++] & 0xff) + 192;
			}
			else if (len == 255) {
				if (pos + 4 > end) throw new IOException("truncated signature subpacket");
				len = (int) u32(b, pos);
				pos += 4;
			}
			if (len < 1 || pos + len > end) throw new IOException("invalid signature subpacket");
			int type = b[pos] & 0x7f;
			int dataStart = pos + 1;
			int dataLen = len - 1;
			if (hashed && type == SUB_CREATION_TIME && dataLen == 4) sig.creationTime = u32(b, dataStart);
			else if (hashed && type == SUB_EXPIRATION_TIME && dataLen == 4) sig.expiresAfter = u32(b, dataStart);
			else if (type == SUB_ISSUER_KEY_ID && dataLen == 8 && sig.issuerKeyId == 0) sig.issuerKeyId = u64(b, dataStart);
			else if (type == SUB_ISSUER_FINGERPRINT && dataLen == 21 && sig.issuerKeyId == 0) sig.issuerKeyId = u64(b, dataStart + 13);
			pos += len;
		}
	}

	private static PublicKey toPublicKey(byte[] body) throws IOException {
		if (body.length < 6) throw new IOException("truncated public key packet");
		int version = body[0] & 0xff;
		if (version != 4) return null;
		int algo = body[5] & 0xff;
		if (algo != ALGO_RSA && algo != ALGO_RSA_SIGN_ONLY) return null;
		int[] pos = new int[] { 6 };
		BigInteger n = readMPI(body, pos);
		BigInteger e = readMPI(body, pos);
		try {
			RSAPublicKey key = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(n, e));

			// the fingerprint only identifies the key, SHA-1 is what the format defines for that
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update((byte) 0x99);
			md.update((byte) (body.length >>> 8));
			md.update((byte) body.length);
			md.update(body);
			byte[] fingerprint = md.digest();
			return new PublicKey(u64(fingerprint, fingerprint.length - 8), fingerprint, key);
		}
		catch (GeneralSecurityException ex) {
			throw new IOException("failed to read the public key: " + ex.getMessage(), ex);
		}
	}

	private static BigInteger readMPI(byte[] b, int[] pos) throws IOException {
		if (pos[0] + 2 > b.length) throw new IOException("truncated multiprecision integer");
		int bits = u16(b, pos[0]);
		int len = (bits + 7) / 8;
		int start = pos[0] + 2;
		if (start + len > b.length) throw new IOException("truncated multiprecision integer");
		pos[0] = start + len;
		return new BigInteger(1, java.util.Arrays.copyOfRange(b, start, start + len));
	}

	private static int u16(byte[] b, int pos) {
		return ((b[pos] & 0xff) << 8) | (b[pos + 1] & 0xff);
	}

	private static long u32(byte[] b, int pos) {
		return ((long) (b[pos] & 0xff) << 24) | ((b[pos + 1] & 0xff) << 16) | ((b[pos + 2] & 0xff) << 8) | (b[pos + 3] & 0xff);
	}

	private static long u64(byte[] b, int pos) {
		return (u32(b, pos) << 32) | u32(b, pos + 4);
	}

	private static byte[] readAll(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int len;
		try {
			while ((len = is.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
				if (baos.size() > MAX_SIGNATURE_SIZE) throw new IOException("the file is too big for a key or signature");
			}
		}
		finally {
			is.close();
		}
		return baos.toByteArray();
	}

	private static List<Packet> readPackets(byte[] data) throws IOException {
		if (data.length > MAX_SIGNATURE_SIZE) throw new IOException("the file is too big for a key or signature");
		byte[] b = isArmored(data) ? dearmor(data) : data;
		List<Packet> packets = new ArrayList<>();
		ByteArrayInputStream in = new ByteArrayInputStream(b);
		int c;
		while ((c = in.read()) != -1) {
			if ((c & 0x80) == 0) throw new IOException("invalid OpenPGP packet header");
			int tag;
			int len;
			if ((c & 0x40) != 0) {
				tag = c & 0x3f;
				int first = readByte(in);
				if (first < 192) len = first;
				else if (first < 224) len = ((first - 192) << 8) + readByte(in) + 192;
				else if (first == 255) len = (readByte(in) << 24) | (readByte(in) << 16) | (readByte(in) << 8) | readByte(in);
				else throw new IOException("partial length packets are not supported");
			}
			else {
				tag = (c & 0x3c) >> 2;
				switch (c & 3) {
				case 0:
					len = readByte(in);
					break;
				case 1:
					len = (readByte(in) << 8) | readByte(in);
					break;
				case 2:
					len = (readByte(in) << 24) | (readByte(in) << 16) | (readByte(in) << 8) | readByte(in);
					break;
				default:
					len = in.available();
				}
			}
			if (len < 0 || len > in.available()) throw new IOException("truncated OpenPGP packet");
			byte[] body = new byte[len];
			if (in.read(body, 0, len) != len && len > 0) throw new IOException("truncated OpenPGP packet");
			packets.add(new Packet(tag, body));
		}
		return packets;
	}

	private static int readByte(ByteArrayInputStream in) throws IOException {
		int i = in.read();
		if (i == -1) throw new IOException("truncated OpenPGP packet");
		return i;
	}

	private static boolean isArmored(byte[] data) {
		return data.length > 0 && (data[0] & 0x80) == 0;
	}

	// ASCII armor (RFC 4880 chapter 6), the first block is used
	private static byte[] dearmor(byte[] data) throws IOException {
		String[] lines = new String(data, java.nio.charset.StandardCharsets.US_ASCII).split("\r?\n");
		int i = 0;
		while (i < lines.length && !lines[i].startsWith("-----BEGIN PGP ")) {
			i++;
		}
		if (i == lines.length) throw new IOException("no OpenPGP armor header found");
		i++;
		// armor headers until the empty line
		while (i < lines.length && !lines[i].trim().isEmpty()) {
			i++;
		}
		StringBuilder sb = new StringBuilder();
		long crc = -1;
		boolean end = false;
		for (i++; i < lines.length; i++) {
			String line = lines[i].trim();
			if (line.startsWith("-----END PGP ")) {
				end = true;
				break;
			}
			if (line.startsWith("=")) {
				byte[] c = Base64.getDecoder().decode(line.substring(1));
				if (c.length != 3) throw new IOException("invalid armor checksum");
				crc = ((c[0] & 0xffL) << 16) | ((c[1] & 0xffL) << 8) | (c[2] & 0xffL);
				continue;
			}
			sb.append(line);
		}
		if (!end) throw new IOException("the OpenPGP armor is not terminated");
		byte[] decoded;
		try {
			decoded = Base64.getDecoder().decode(sb.toString());
		}
		catch (IllegalArgumentException e) {
			throw new IOException("invalid OpenPGP armor: " + e.getMessage(), e);
		}
		if (crc != -1 && crc != crc24(decoded)) throw new IOException("the OpenPGP armor checksum does not match");
		return decoded;
	}

	private static long crc24(byte[] data) {
		long crc = 0xB704CEL;
		for (byte d: data) {
			crc ^= (long) (d & 0xff) << 16;
			for (int i = 0; i < 8; i++) {
				crc <<= 1;
				if ((crc & 0x1000000L) != 0) crc ^= 0x1864CFBL;
			}
		}
		return crc & 0xFFFFFFL;
	}
}
