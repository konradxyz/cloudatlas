package pl.edu.mimuw.cloudatlas.CA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;

import pl.edu.mimuw.cloudatlas.common.model.PathName;

public class Main {
	static String baseDir = "base";
	static String publicKeyName = "publicKey.txt";
	static String privateKeyName = "privateKey.txt";

	public static void createKey(String path, String key)
			throws FileNotFoundException, UnsupportedEncodingException {
		System.out.println(path+ " "+key);
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		writer.println(key);
		writer.close();
	}

	public static void generateKeys(String path)
			throws NoSuchAlgorithmException, NoSuchProviderException,
			FileNotFoundException, UnsupportedEncodingException {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
		keyGenerator.initialize(1024,
				SecureRandom.getInstance("SHA1PRNG", "SUN"));
		KeyPair keyPair = keyGenerator.generateKeyPair();
		PrivateKey privateKeyNew = keyPair.getPrivate();
		PublicKey publicKeyNew = keyPair.getPublic();
		createKey(path + publicKeyName,
				DatatypeConverter.printHexBinary(publicKeyNew.getEncoded()));
		createKey(path + privateKeyName,
				DatatypeConverter.printHexBinary(privateKeyNew.getEncoded()));
	}

	public static void main(String[] args) throws NoSuchAlgorithmException,
			NoSuchProviderException, FileNotFoundException,
			UnsupportedEncodingException {
		// System.out.println(Arrays.asList(args));
		if (args[0].equals("--create-zone")) {
			System.out.println("a");
			PathName pathName;
			try {
				pathName = new PathName(args[1]);
			} catch (IllegalArgumentException e) {
				System.err.println("Wrong path name " + args[1]);
				return;
			}
			if (pathName.equals(PathName.ROOT)) {
				if (!(new File(baseDir).exists())) {
					System.err.println("Base dir doesn't exist: " + baseDir);
					return;
				}
				File publicKey = new File(baseDir + "/" + publicKeyName);
				if (publicKey.exists()) {
					System.err.println("Public key of the zone " + pathName
							+ " exists");
					return;
				}
				generateKeys(baseDir + "/");
			} else {
				PathName levelUp = pathName.levelUp();
				String privateKeyLevelUpPath = baseDir + levelUp.toString()
						+ privateKeyName;
				System.out.println(levelUp);
				if (!new File(privateKeyLevelUpPath).exists()) {
					System.err.println("There is no private key for the zone "
							+ levelUp);
					System.err
							.println("This zone doesn't exist or this CA has no authorization to create subzones of this zone");
					return;
				}
				File zoneDirectory = new File(baseDir + pathName.toString());
				if (zoneDirectory.exists()) {
					System.err.println("This zone " + pathName + " exists");
					return;
				}
				if (!(zoneDirectory.mkdir())) {
					System.err.println("Cannot create " + zoneDirectory);
					return;
				}
				generateKeys(baseDir + pathName.toString() + "/");
			}
		}
	}
}
