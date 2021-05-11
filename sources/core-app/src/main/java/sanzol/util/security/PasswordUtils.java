/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.util.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(PasswordUtils.class);

	private static byte[] getSalt() throws NoSuchAlgorithmException
	{
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}

	public static String hashPassword(String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		byte[] salt = getSalt();
		final int iterations = 8192;
		final int keyLength = 512;

		PBEKeySpec spec = new PBEKeySpec(username.concat("||").concat(password).toCharArray(), salt, iterations, keyLength);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = skf.generateSecret(spec).getEncoded();
		return iterations + ":" + Hex.encodeHexString(salt) + ":" + Hex.encodeHexString(hash);
	}

	public static boolean validatePassword(String username, String password, String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException, DecoderException
	{
		try
		{
			String[] parts = storedPassword.split(":");
			int iterations = Integer.parseInt(parts[0]);
			byte[] storedSalt = Hex.decodeHex(parts[1]);
			byte[] storedHash = Hex.decodeHex(parts[2]);
			int keyLength = storedHash.length * 8;

			PBEKeySpec spec = new PBEKeySpec(username.concat("||").concat(password).toCharArray(), storedSalt, iterations, keyLength);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = skf.generateSecret(spec).getEncoded();

			return Arrays.equals(hash, storedHash);
		}
		catch (Exception e)
		{
			LOG.error("validatePassword failed", e);
			return false;
		}
	}

}
