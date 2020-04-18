package ServerClient;

/********************************************************************
 *  AES - Advanced Encryption Standard						        *
 *  For encrypting/decrypting messages between client-server.       *
 *  https://howtodoinjava.com/security/java-aes-encryption-example/ *
 *******************************************************************/

// Import libraries
import java.io.*;
import java.security.*;
import java.util.*;

// Import cipher and secretkey
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class AES {
	// Key
	private static SecretKeySpec secretKey;
	private static byte[] key;
	
	// Set key
	public static void setKey(String myKey) {
		MessageDigest sha = null;
		try {
			key = myKey.getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	// Encrypt message
	public static String encrypt (String strToEncrypt, String secret) {
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		}
		catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}
	
	// Decrypt message
	public static String decrypt (String strToDecrypt, String secret) {
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		}
		catch (Exception e) {
			System.out.println("Error while decrypting: " + e.toString());
		}
		return null;
	}
}