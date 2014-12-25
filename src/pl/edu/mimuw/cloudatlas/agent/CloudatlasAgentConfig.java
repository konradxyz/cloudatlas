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

	public CloudatlasAgentConfig(PathName pathName, Inet4Address address) {
		super();
		this.pathName = pathName;
		this.address = address;
	}

	public static CloudatlasAgentConfig fromIni(Ini file) {
		try {
			String pathName = file.get("agent", "zone_path_name");
			String interfaceName = file.get("agent", "external_interface");
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
			return new CloudatlasAgentConfig(new PathName(pathName), result);
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
}
