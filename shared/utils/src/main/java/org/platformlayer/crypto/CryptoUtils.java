package org.platformlayer.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.SecretKeySpec;

import net.iharder.Base64;

import org.openstack.utils.Utf8;
import org.platformlayer.IoUtils;

public class CryptoUtils {
	private static final String UTF8 = "UTF-8";
	// private static final String ALGORITHM_3DES = "DES/ECB/PKCS5Padding";
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	private static final String SHA1_ALGORITHM = "SHA-1";

	public static final int HMAC_SHA1_BYTES = 20;

	static final SecureRandom SECURE_RANDOM = new SecureRandom();

	public static String fromBytesUtf8(byte[] bytes) {
		try {
			return new String(bytes, UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Error decoding utf-8 bytes", e);
		}
	}

	public static Sha1Hash sha1(byte[] plainText) {
		MessageDigest digest = getSha1();

		byte[] hash = digest.digest(plainText);
		return new Sha1Hash(hash);
	}

	public static Sha1Hash sha1(String a) {
		MessageDigest digest = getSha1();

		byte[] hash = digest.digest(toBytesUtf8(a));
		return new Sha1Hash(hash);
	}

	public static Md5Hash md5(String a) {
		return md5(toBytesUtf8(a));
	}

	public static Md5Hash md5(byte[] data) {
		MessageDigest digest = buildMd5();

		byte[] hash = digest.digest(data);
		return new Md5Hash(hash);
	}

	public static Md5Hash md5(byte[] b1, byte[] b2) {
		MessageDigest digest = buildMd5();

		digest.update(b1);
		byte[] hash = digest.digest(b2);
		return new Md5Hash(hash);
	}

	public static Md5Hash md5(byte[] b1, byte[] b2, byte[] b3) {
		MessageDigest digest = buildMd5();

		digest.update(b1);
		digest.update(b2);
		byte[] hash = digest.digest(b3);
		return new Md5Hash(hash);
	}

	public static Sha1Hash sha1(byte[] buffer1, byte[] buffer2) {
		MessageDigest digest = getSha1();

		digest.update(buffer1);
		byte[] hash = digest.digest(buffer2);
		return new Sha1Hash(hash);
	}

	public static Sha1Hash sha1(String a, String b) {
		MessageDigest digest = getSha1();

		digest.update(toBytesUtf8(a));
		byte[] hash = digest.digest(toBytesUtf8(b));
		return new Sha1Hash(hash);
	}

	public static Sha1Hash sha1(String... data) {
		MessageDigest digest = getSha1();

		for (int i = 0; i < data.length - 1; i++) {
			digest.update(toBytesUtf8(data[i]));
		}

		byte[] hash = digest.digest(toBytesUtf8(data[data.length - 1]));
		return new Sha1Hash(hash);
	}

	public Sha1Hash sha1(byte[] a, byte[] b, byte[] c) {
		MessageDigest digest = getSha1();

		digest.update(a);
		digest.update(b);
		byte[] hash = digest.digest(c);
		return new Sha1Hash(hash);
	}

	public Sha1Hash sha1(byte[]... data) {
		MessageDigest digest = getSha1();

		for (int i = 0; i < data.length - 1; i++) {
			digest.update(data[i]);
		}

		byte[] hash = digest.digest(data[data.length - 1]);
		return new Sha1Hash(hash);
	}

	public static Mac buildHmacSha1(SecretKey key) {
		Mac mac = getHmacSha1();
		try {
			mac.init(key);
		} catch (InvalidKeyException e) {
			throw new IllegalStateException("Unexpected encryption error", e);
		}
		return mac;
	}

	public static SecretKeySpec buildHmacSha1Key(byte[] keyData) {
		SecretKeySpec signingKey = new SecretKeySpec(keyData, HMAC_SHA1_ALGORITHM);
		return signingKey;
	}

	public static SecretKeySpec deriveHmacSha1Key(String keyData) {
		// We want a consistent salt; it can't be empty
		byte[] salt = Utf8.getBytes(keyData);
		int keySize = 128; // ??
		PBEKey pbeKey = KeyDerivationFunctions.doPbkdf2(salt, keyData, keySize);

		return buildHmacSha1Key(pbeKey.getEncoded());
	}

	public static byte[] hmacSha1(SecretKeySpec signingKey, byte[]... data) {
		Mac mac = buildHmacSha1(signingKey);

		return hmacSha1(mac, data);
	}

	public static byte[] hmacSha1(Mac mac, byte[]... data) {
		for (int i = 0; i < data.length - 1; i++) {
			mac.update(data[i]);
		}

		byte[] hash = mac.doFinal(data[data.length - 1]);
		return hash;
	}

	public static byte[] hmacSha1(SecretKeySpec signingKey, byte[] buffer, int offset, int length) {
		Mac mac = buildHmacSha1(signingKey);

		mac.update(buffer, offset, length);
		byte[] hash = mac.doFinal();
		return hash;
	}

	private static MessageDigest getMessageDigest(String algorithm) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			return digest;
		} catch (NoSuchAlgorithmException e) {
			// should not happen
			throw new IllegalStateException("Could not find MessageDigest algorithm: " + algorithm, e);
		}
	}

	private static Mac getMac(String algorithm) {
		try {
			Mac digest = Mac.getInstance(algorithm);
			return digest;
		} catch (NoSuchAlgorithmException e) {
			// should not happen
			throw new IllegalStateException("Could not find Mac algorithm: " + algorithm, e);
		}
	}

	public static MessageDigest getSha1() {
		return getMessageDigest(SHA1_ALGORITHM);
	}

	private static Mac getHmacSha1() {
		return getMac(HMAC_SHA1_ALGORITHM);
	}

	// public static byte[] decrypt3Des(byte[] key, byte[] cipherText) throws CryptoException {
	// try {
	// KeySpec ks = new DESKeySpec(key);
	// SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
	// SecretKey secret = keyFactory.generateSecret(ks);
	// Cipher cipher = Cipher.getInstance(ALGORITHM_3DES);
	// cipher.init(Cipher.DECRYPT_MODE, secret);
	// byte[] plainText = cipher.doFinal(cipherText);
	// return plainText;
	// } catch (Exception e) {
	// if (e instanceof InterruptedException) {
	// Thread.currentThread().interrupt();
	// }
	// throw new CryptoException("Error while decrypting data", e);
	// }
	// }
	//
	// public static byte[] encrypt3Des(byte[] key, byte[] plainText) throws CryptoException {
	// try {
	// KeySpec ks = new DESKeySpec(key);
	// SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
	// SecretKey secret = keyFactory.generateSecret(ks);
	// Cipher cipher = Cipher.getInstance(ALGORITHM_3DES);
	// cipher.init(Cipher.ENCRYPT_MODE, secret);
	// byte[] cipherText = cipher.doFinal(plainText);
	// return cipherText;
	// } catch (Exception e) {
	// if (e instanceof InterruptedException) {
	// Thread.currentThread().interrupt();
	// }
	// throw new CryptoException("Error while decrypting data", e);
	// }
	// }

	public static String toBase64(byte[] data) {
		String base64 = Base64.encodeBytes(data); // new sun.misc.BASE64Encoder().encode(data);
		while (base64.endsWith("\r") || base64.endsWith("\n")) {
			base64 = base64.substring(0, base64.length() - 1);
		}
		return base64;
	}

	public static byte[] fromBase64(String base64) {
		try {
			return Base64.decode(base64);
			// return new sun.misc.BASE64Decoder().decodeBuffer(base64);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error decoding base-64 encoded string", e);
		}
	}

	public static byte[] toBytesUtf8(String s) {
		try {
			return s.getBytes(UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Error getting utf-8 bytes", e);
		}
	}

	public static String toStringUtf8(byte[] bytes) {
		try {
			return new String(bytes, UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Error reading utf-8 bytes", e);
		}
	}

	public static String toHex(byte[] bytes) {
		return toHex(bytes, 0, bytes.length);
	}

	public static String toHex(byte[] bytes, int offset, int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = offset; i < offset + length; i++) {
			String s = Integer.toHexString(bytes[i] & 0xff);
			if (s.length() == 1) {
				sb.append('0');
			}
			sb.append(s);
		}
		return sb.toString();
	}

	public static String toHex(byte b) {
		String s = Integer.toHexString(b & 0xff);
		if (s.length() == 1) {
			return "0" + s;
		}
		return s;
	}

	public static Sha1Hash sha1(InputStream inputStream) throws IOException {
		MessageDigest digest = getSha1();
		return new Sha1Hash(hash(digest, inputStream).hash);
	}

	public static Md5Hash md5(InputStream inputStream) throws IOException {
		MessageDigest digest = buildMd5();
		return new Md5Hash(hash(digest, inputStream).hash);
	}

	static class HashAndLength {
		final byte[] hash;
		final long length;

		HashAndLength(byte[] hash, long length) {
			this.hash = hash;
			this.length = length;
		}
	}

	public static HashAndLength md5AndLength(InputStream inputStream) throws IOException {
		MessageDigest digest = buildMd5();
		return hash(digest, inputStream);
	}

	public static HashAndLength hash(MessageDigest digest, File file) throws IOException {
		InputStream is = new FileInputStream(file);
		try {
			return hash(digest, is);
		} finally {
			IoUtils.safeClose(is);
		}
	}

	// public static HashAndLength hash(MessageDigest digest, File file, long segmentOffset, long segmentLength) throws
	// IOException {
	// InputStream is = IoUtils.openFileSegment(file, segmentOffset, segmentLength);
	// try {
	// return hash(digest, is);
	// } finally {
	// IoUtils.safeClose(is);
	// }
	// }

	private static HashAndLength hash(MessageDigest digest, InputStream inputStream) throws IOException {
		byte[] buffer = new byte[32768];

		long totalBytesRead = 0;
		while (true) {
			int bytesRead = inputStream.read(buffer);
			if (bytesRead < 0) {
				break;
			}
			totalBytesRead += bytesRead;
			digest.update(buffer, 0, bytesRead);
		}

		byte[] hash = digest.digest();

		return new HashAndLength(hash, totalBytesRead);
	}

	public static MessageDigest buildMd5() {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			return digest;
		} catch (NoSuchAlgorithmException e) {
			// should not happen
			throw new RuntimeException("Could not find md5 algorithm", e);
		}
	}

	public static Sha1Hash sha1(File file) throws IOException {
		MessageDigest digest = getSha1();
		return new Sha1Hash(hash(digest, file).hash);
	}

	public static Md5Hash md5(File file) throws IOException {
		MessageDigest digest = buildMd5();
		return new Md5Hash(hash(digest, file).hash);
	}

	public static byte[] fromHex(String hexString) {
		if (hexString == null) {
			return null;
		}

		int len = hexString.length();
		if (len % 2 != 0) {
			throw new IllegalArgumentException();
		}

		byte[] data = new byte[len / 2];
		for (int i = 0; i < data.length; i++) {
			// String hexByte = hexString.substring(i * 2, i * 2 + 2);
			//
			// Integer num = Integer.decode("0x" + hexByte);
			// data[i] = num.byteValue();
			data[i] = (byte) (Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16) & 0xff);
		}

		return data;
	}

	public static byte[] generateSecureRandom(int length) {
		synchronized (SECURE_RANDOM) {
			byte[] data = new byte[length];
			SECURE_RANDOM.nextBytes(data);
			return data;
		}
	}

	public static KeyPair generateKeyPair(String algorithm, int keysize) {
		KeyPairGenerator generator;
		try {
			generator = KeyPairGenerator.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Error loading crypto provider", e);
		}
		generator.initialize(keysize);
		KeyPair keyPair = generator.generateKeyPair();
		return keyPair;

	}

	public static SecretKey generateKey(String algorithm, int keysize) {
		KeyGenerator generator;
		try {
			generator = KeyGenerator.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Error loading crypto provider", e);
		}
		generator.init(keysize);
		SecretKey key = generator.generateKey();
		return key;

	}

	public static Cipher getCipher(String algorithm) {
		try {
			return Cipher.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Error loading crypto provider", e);
		} catch (NoSuchPaddingException e) {
			throw new IllegalArgumentException("Error loading crypto provider", e);
		}
	}

	public static byte[] decrypt(Cipher cipher, Key key, byte[] cipherText) {
		try {
			cipher.init(Cipher.DECRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException("Invalid key", e);
		}
		byte[] plainText;
		try {
			plainText = cipher.doFinal(cipherText);
		} catch (IllegalBlockSizeException e) {
			throw new IllegalArgumentException("Error in decryption", e);
		} catch (BadPaddingException e) {
			throw new IllegalArgumentException("Error in decryption", e);
		}
		return plainText;

	}

	public static byte[] encrypt(Cipher cipher, Key key, byte[] plaintext) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException("Invalid key", e);
		}
		byte[] encryptedBytes;
		try {
			encryptedBytes = cipher.doFinal(plaintext);
		} catch (IllegalBlockSizeException e) {
			throw new IllegalArgumentException("Error in encryption", e);
		} catch (BadPaddingException e) {
			throw new IllegalArgumentException("Error in encryption", e);
		}
		return encryptedBytes;
	}

	public static byte[] concat(byte[]... arrays) {
		int length = 0;
		for (int i = 0; i < arrays.length; i++) {
			length += arrays[i].length;
		}
		byte[] dest = new byte[length];
		int pos = 0;
		for (int i = 0; i < arrays.length; i++) {
			System.arraycopy(arrays[i], 0, dest, pos, arrays[i].length);
			pos += arrays[i].length;
		}
		return dest;
	}

}
