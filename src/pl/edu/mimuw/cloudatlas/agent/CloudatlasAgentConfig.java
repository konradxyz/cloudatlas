package pl.edu.mimuw.cloudatlas.agent;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.ini4j.Ini;

import pl.edu.mimuw.cloudatlas.common.model.PathName;

public final class CloudatlasAgentConfig {
	private final PathName pathName;
	private final Inet4Address address;
	
	// Gossip:
	private int maxMessageSizeBytes;
	private int port;
	// Ignored if null.
	private final InetAddress fallbackAddress;
	private final int gossipPeriodMs;

	public CloudatlasAgentConfig(PathName pathName, Inet4Address address,
			int port, int maxMessageSizeBytes, InetAddress fallbackAddress,
			int gossipPeriodMs) {
		super();
		this.pathName = pathName;
		this.address = address;
		this.port = port;
		this.maxMessageSizeBytes = maxMessageSizeBytes;
		this.fallbackAddress = fallbackAddress;
		this.gossipPeriodMs = gossipPeriodMs;
	}

	public static CloudatlasAgentConfig fromIni(Ini file) {
		try {
			String pathName = file.get("agent", "zone_path_name");
			String interfaceName = file.get("agent", "external_interface");
			int port = Integer.parseInt(file.get("gossip", "port"));
			int maxMessageSizeBytes = Integer.parseInt(file.get("gossip", "max_message_size_bytes"));
			Inet4Address result = null;
			try {
				List<InetAddress> addresses = Collections.list(NetworkInterface
						.getByName(interfaceName).getInetAddresses());
				for (InetAddress address : addresses) {
					try {
						result = (Inet4Address) address;
					} catch (ClassCastException e) {
					}
				}
			} catch (NullPointerException e) {
				System.err.println("Unknown interface '" + interfaceName + "'");
			}
			if (result == null) {
				System.err
						.println("Could not retrieve current node IP address.");
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
			return new CloudatlasAgentConfig(new PathName(pathName), result,
					port, maxMessageSizeBytes, fallbackAddress, gossipPeriod);
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
}
