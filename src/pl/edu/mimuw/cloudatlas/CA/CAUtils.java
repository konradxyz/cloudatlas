package pl.edu.mimuw.cloudatlas.CA;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.ValueKey;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;
import pl.edu.mimuw.cloudatlas.common.serialization.KryoUtils;

import com.esotericsoftware.kryo.Kryo;

public class CAUtils {
	public static final String DEFAULT_DIR = "base";
	static String publicKeyName = "publicKey.txt";
	static String privateKeyName = "privateKey.txt";
	static String publicKeyZoneName = "publicKeyZone.txt";
	static String privateKeyZoneName = "privateKeyZone.txt";
	static String certificateName = "certificate.txt";
	static String ccPath = "cc";
	static String clientAuthenticationName = "clientAuthentication";
	
	public static void createFile(String path, String key)
			throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		writer.println(key);
		writer.close();
	}
	
	
	
	public static PrivateKey readPrivateKey(String filePath) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
		String keyString = KryoUtils.readFileString(filePath);
		byte[] data = DatatypeConverter.parseHexBinary(keyString);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = factory
				.generatePrivate(new PKCS8EncodedKeySpec(data));
		return privateKey;
	}

	
	public static PublicKey readPublicKey(String filePath) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
		String keyString = KryoUtils.readFileString(filePath);
		byte[] data = DatatypeConverter.parseHexBinary(keyString);
		
		KeyFactory factory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(data));
		return publicKey;
	}

	public static void createFile(String path, byte[] key) throws IOException {
		FileOutputStream fileOuputStream = new FileOutputStream(path);
		fileOuputStream.write(key);
		fileOuputStream.close();
	}

	public static KeyPair getKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
		keyGenerator.initialize(1024,
				SecureRandom.getInstance("SHA1PRNG", "SUN"));
		KeyPair keyPair = keyGenerator.generateKeyPair();
		return keyPair;
	}
	
	public static PublicKey generateKeys(String path)
			throws NoSuchAlgorithmException, NoSuchProviderException,
			FileNotFoundException, UnsupportedEncodingException {
		KeyPair keyPair = getKeyPair();
		PrivateKey privateKeyNew = keyPair.getPrivate();
		PublicKey publicKeyNew = keyPair.getPublic();
		createFile(path + publicKeyName,
				DatatypeConverter.printHexBinary(publicKeyNew.getEncoded()));
		createFile(path + privateKeyName,
				DatatypeConverter.printHexBinary(privateKeyNew.getEncoded()));
		return publicKeyNew;
	}
	
	public static PublicKey generateKeysZone(String path)
			throws NoSuchAlgorithmException, NoSuchProviderException,
			FileNotFoundException, UnsupportedEncodingException {
		KeyPair keyPair = getKeyPair();
		PrivateKey privateKeyZoneNew = keyPair.getPrivate();
		PublicKey publicKeyZoneNew = keyPair.getPublic();
		createFile(path + publicKeyZoneName,
				DatatypeConverter.printHexBinary(publicKeyZoneNew.getEncoded()));
		createFile(path + privateKeyZoneName,
				DatatypeConverter.printHexBinary(privateKeyZoneNew.getEncoded()));
		return publicKeyZoneNew;
	}

	public static void generateCertificate(PrivateKey parentKey, PublicKey publicKey,
			Date date, String path, String zoneName) throws NoSuchAlgorithmException,
			InvalidKeySpecException, InvalidKeyException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, IOException {
		PrivateKey privateKey = parentKey; 
		AttributesMap attributesMap = new AttributesMap();
		attributesMap.add("publicKey", new ValueKey(publicKey));
		System.out.println("time " + date.getTime());
		attributesMap.add("time", new ValueTime(date.getTime()));
		attributesMap.add("zone", new ValueString(zoneName));
		Kryo kryo = KryoUtils.getKryo();
		Certificate certificate = new Certificate(attributesMap, privateKey,
				kryo);
		System.err.println("serialize");
		byte[] descr = KryoUtils.serialize(certificate, kryo);
		System.err.println(descr.length);
		createFile(path + certificateName, descr);
	}
	
	public static String getCcPath() {
		return ccPath;
	}
	
	public static String getClientAuthenticationName() {
		return clientAuthenticationName;
	}
}
