package pl.edu.mimuw.cloudatlas.common;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.spec.X509EncodedKeySpec;

import org.ini4j.Ini;

import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.utils.IniUtils;

public final class CloudatlasAgentConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8557446129359783603L;
	private final PathName pathName;
	private final Inet4Address address;
	
	// Gossip:
	private int maxMessageSizeBytes;
	private int port;
	// Ignored if null.
	private final InetAddress fallbackAddress;
	private final int gossipPeriodMs;
	
	// RMI:
	private final int rmiPort;
	
	// Signer:
	private final PublicKey signerKey;

	public CloudatlasAgentConfig(PathName pathName, Inet4Address address,
			int port, int maxMessageSizeBytes, InetAddress fallbackAddress,
			int gossipPeriodMs, int rmiPort, PublicKey signerKey) {
		super();
		this.pathName = pathName;
		this.address = address;
		this.port = port;
		this.maxMessageSizeBytes = maxMessageSizeBytes;
		this.fallbackAddress = fallbackAddress;
		this.gossipPeriodMs = gossipPeriodMs;
		this.rmiPort = rmiPort;
		this.signerKey = signerKey;
	}

	public static CloudatlasAgentConfig fromIni(Ini file) {
		try {
			String pathName = file.get("agent", "zone_path_name");
			int port = Integer.parseInt(file.get("gossip", "port"));
			int maxMessageSizeBytes = Integer.parseInt(file.get("gossip", "max_message_size_bytes"));
			Inet4Address result = IniUtils.readAddressFromIni(file, "agent", "external_interface");
			if (result == null) {
				System.err
						.println("Could not retrieve current node IP address. Check your ini file.");
				return null;
			} else {
				System.err.println("Retrieved IP address " + result);
			}
			InetAddress fallbackAddress = null;
			try {
				String addr = file.get("gossip", "fallback");
				if ( addr != null )
					fallbackAddress = InetAddress.getByName(addr);
			} catch (Exception e) {
			}
			int gossipPeriod = Integer.parseInt(file.get("gossip", "period_ms"));
			int rmiPort  = Integer.parseInt(file.get("rmi", "port"));
			byte[] key = IniUtils.readByteArrayFromHex(file, "signer", "public_key");
			KeyFactory factory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(key));
			System.err.println(((RSAKey)publicKey).getModulus());
			return new CloudatlasAgentConfig(new PathName(pathName), result,
					port, maxMessageSizeBytes, fallbackAddress, gossipPeriod, rmiPort, publicKey);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not parse config file, cause: '"
					+ e.getMessage() + "'");
			return null;
		}
	}

	public Inet4Address getAddress() {
		return address;
	}

	public PathName getPathName() {
		return pathName;
	}

	public int getMaxMessageSizeBytes() {
		return maxMessageSizeBytes;
	}

	public int getPort() {
		return port;
	}

	public InetAddress getFallbackAddress() {
		return fallbackAddress;
	}

	public int getGossipPeriodMs() {
		return gossipPeriodMs;
	}

	public int getRmiPort() {
		return rmiPort;
	}

	public PublicKey getSignerKey() {
		return signerKey;
	}
}