package pl.edu.mimuw.cloudatlas.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {
	public static byte[] computeHash(byte[] value) {
		try {
			MessageDigest digestGenerator = MessageDigest
					.getInstance("SHA-256");
			return digestGenerator.digest(value);
		} catch (NoSuchAlgorithmException e) {
			throw new InternalError(e.getMessage());
		}
	}
	
	public static final String ENCRYPTION_ALGORITHM = "RSA";
}
