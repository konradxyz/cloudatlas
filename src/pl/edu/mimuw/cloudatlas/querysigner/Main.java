package pl.edu.mimuw.cloudatlas.querysigner;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.NoSuchPaddingException;

import org.ini4j.Ini;

import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasQuerySigner;
import pl.edu.mimuw.cloudatlas.common.utils.IniUtils;
import pl.edu.mimuw.cloudatlas.common.utils.IniUtils.IniException;

public class Main {

	public static void main(String[] args) throws RemoteException,
			InterruptedException, NoSuchAlgorithmException,
			InvalidKeySpecException, InvalidKeyException,
			NoSuchPaddingException, IniException {
		Ini config = IniUtils.readConfigFromArgs(args);
		if (config == null)
			return;
		int port = IniUtils.readInt(config, "signer", "port");
		InetAddress host = IniUtils.readAddressFromIni(config, "signer",
				"external_interface");
		if (host == null)
			return;
		byte[] key = IniUtils.readByteArrayFromHex(config, "signer",
				"private_key");
		KeyFactory factory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = factory
				.generatePrivate(new PKCS8EncodedKeySpec(key));

		System.setProperty("java.security.policy", "file:./querysigner.policy");
		System.setProperty("java.rmi.server.hostname", host.getHostAddress());
		CloudatlasQuerySignerImplementation server = new CloudatlasQuerySignerImplementation(
				privateKey);
		System.err.println(((RSAKey)privateKey).getModulus());
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		CloudatlasQuerySigner stub = (CloudatlasQuerySigner) UnicastRemoteObject
				.exportObject(server, 0);
		Registry registry = LocateRegistry.createRegistry(port);
		registry.rebind("cloudatlas_signer", stub);

		while (true) {
			Thread.sleep(1000);
			System.out.println(server.getQueries());
		}

	}

}
