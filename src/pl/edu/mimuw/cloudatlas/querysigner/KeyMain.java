package pl.edu.mimuw.cloudatlas.querysigner;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;

public class KeyMain {
	private final static String ENCRYPTION_ALGORITHM = "RSA";
	private final static int NUM_KEY_BITS = 1024;

	public static void main(String[] args) {
		try {
			// Generate a key pair.
			KeyPairGenerator keyGenerator = KeyPairGenerator
					.getInstance(ENCRYPTION_ALGORITHM);
			keyGenerator.initialize(NUM_KEY_BITS, SecureRandom.getInstance("SHA1PRNG", "SUN"));
			KeyPair keyPair = keyGenerator.generateKeyPair();
			PrivateKey privateKey = keyPair.getPrivate();
			PublicKey publicKey = keyPair.getPublic();
			System.out.println("Private:");
			System.out.println(DatatypeConverter.printHexBinary(privateKey
					.getEncoded()));
			System.out.println("Public:");
			System.out.println(DatatypeConverter.printHexBinary(publicKey
					.getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
