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
		readerMap.put("--create-cc", new CreateClientCC());
	}

	public static void main(String[] args) throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException, IOException,
			InvalidKeyException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		createMap();
		System.err.println(Arrays.asList(args));
		if (args[0].startsWith("--"))
			readerMap.get(args[0]).perform("base", args);
		else {
			String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
			readerMap.get(newArgs[0]).perform(args[0], newArgs);
		}
	}
}
