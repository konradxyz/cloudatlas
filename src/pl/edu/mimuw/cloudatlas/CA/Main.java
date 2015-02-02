package pl.edu.mimuw.cloudatlas.CA;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Main {
	static Map<String, CommandReader> readerMap = new HashMap<String, CommandReader>();

	public static void createMap() {
		readerMap.put("--create-zone", new CreateZone());
		readerMap.put("--create-certificate", new CreateCertificate());
	}

	public static void main(String[] args) throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException, IOException,
			InvalidKeyException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		createMap();
		System.err.println(Arrays.asList(args));
		readerMap.get(args[0]).perform(args);
	}
}