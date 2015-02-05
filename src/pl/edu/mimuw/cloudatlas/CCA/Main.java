package pl.edu.mimuw.cloudatlas.CCA;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;

import pl.edu.mimuw.cloudatlas.CA.ClientAuthenticationData;
import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.common.model.ValueCertificate;
import pl.edu.mimuw.cloudatlas.common.model.ValueString;
import pl.edu.mimuw.cloudatlas.common.model.ValueTime;
import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasAgentRmiServer;
import pl.edu.mimuw.cloudatlas.common.serialization.KryoUtils;
import pl.edu.mimuw.cloudatlas.common.utils.Utils;
import pl.edu.mimuw.cloudatlas.webclient.WebClient.RmiConnectionException;

public class Main {
	
	private static CloudatlasAgentRmiServer connect(String rmiHost, int rmiPort) throws RmiConnectionException {
		try {
			Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
			CloudatlasAgentRmiServer stub;
			try {
				stub = (CloudatlasAgentRmiServer) registry.lookup("cloudatlas");
			} catch (NotBoundException e) {
				e.printStackTrace();
				throw new ServletException(e);
			}
			return stub;
		} catch (Exception e) {
			throw new RmiConnectionException(e);
		}
	}
	
	public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		byte[] byteFile = KryoUtils.readFile(args[0]);
		String name = args[2];
		String query = args[3];
		ClientAuthenticationData clientAuthenticationData = KryoUtils.deserialize(byteFile, KryoUtils.getKryo(), ClientAuthenticationData.class);
		AttributesMap attributesMap = new AttributesMap();
		if (!query.startsWith("-"))
			attributesMap.add("query", new ValueString(query));
		attributesMap.add("name", new ValueString(name));
		attributesMap.add("timestamp", new ValueTime(Utils.getNowMs()));
		attributesMap.add("certificate", new ValueCertificate(clientAuthenticationData.getCertificate()));
		Certificate certificate = new Certificate(attributesMap, clientAuthenticationData.getClientAuthenticationKey(), KryoUtils.getKryo());
		CloudatlasAgentRmiServer cloudatlasAgentRmiServer = connect(args[1], 33333);
		cloudatlasAgentRmiServer.installQuery(certificate);	
	}
}
