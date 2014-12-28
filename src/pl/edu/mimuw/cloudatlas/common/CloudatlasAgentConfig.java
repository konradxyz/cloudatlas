package pl.edu.mimuw.cloudatlas.common;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.ini4j.Ini;

import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.utils.IniUtils;

public final class CloudatlasAgentConfig implements Serializable {
	public enum LevelSelectionStrategyType {
		ROUND_ROBIN_CONSTANT_FREQUENCY, ROUND_ROBIN_DEC_EXP_FREQUENCY,
		RANDOM_CONSTANT_FREQUENCY, RANDOM_DEC_EXP_FREQUENCY
	}

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8557446129359783603L;
	private final PathName pathName;
	private final Inet4Address address;
	
	// Gossip:
	private int maxMessageSizeBytes;
	private int port;
	private final LevelSelectionStrategyType strategy;
	// Ignored if null.
	private final InetAddress fallbackAddress;
	private final int gossipPeriodMs;
	
	// RMI:
	private final int rmiPort;
	
	// Signer:
	private final PublicKey signerKey;
	
	// Zone expiration:
	private final int zoneExpirationMs;
	private final int zoneCleanupPeriodMs;
	
	// Gossip module data refresh time:
	private final int gossipDataRefreshTimeMs;

	public CloudatlasAgentConfig(PathName pathName, Inet4Address address,
			int port, int maxMessageSizeBytes, InetAddress fallbackAddress,
			int gossipPeriodMs, int rmiPort, PublicKey signerKey, int zoneExpirationMs, 
			int zoneCleanupPeriodMs, int gossipDataRefreshTimeMs, LevelSelectionStrategyType strategy) {
		super();
		this.pathName = pathName;
		this.address = address;
		this.port = port;
		this.maxMessageSizeBytes = maxMessageSizeBytes;
		this.fallbackAddress = fallbackAddress;
		this.gossipPeriodMs = gossipPeriodMs;
		this.rmiPort = rmiPort;
		this.signerKey = signerKey;
		this.zoneExpirationMs = zoneExpirationMs;
		this.zoneCleanupPeriodMs = zoneCleanupPeriodMs;
		this.gossipDataRefreshTimeMs = gossipDataRefreshTimeMs;
		this.strategy = strategy;
	}
	
	private static LevelSelectionStrategyType getLevelSelectionStrategyType(String descr) {
		if (descr.equals("round_robin_const_freq"))
			return LevelSelectionStrategyType.ROUND_ROBIN_CONSTANT_FREQUENCY;
		if (descr.equals("round_robin_dec_exp_freq"))
			return LevelSelectionStrategyType.ROUND_ROBIN_DEC_EXP_FREQUENCY;
		if (descr.equals("random_const_freq"))
			return LevelSelectionStrategyType.RANDOM_CONSTANT_FREQUENCY;
		if (descr.equals("random_dec_exp_freq"))
			return LevelSelectionStrategyType.RANDOM_DEC_EXP_FREQUENCY;
		throw new RuntimeException("Unknown level selection strategy type: " + descr);
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
			return new CloudatlasAgentConfig(new PathName(pathName), result,
					port, maxMessageSizeBytes, fallbackAddress, gossipPeriod, rmiPort, publicKey,
					IniUtils.readInt(file, "gossip", "zone_expiration_ms"),
					IniUtils.readInt(file, "gossip", "zone_cleanup_period_ms"),
					IniUtils.readInt(file, "gossip", "data_refresh_period_ms"),
					getLevelSelectionStrategyType(IniUtils.readString(file, "gossip", "level_selection_strategy")));
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

	public int getZoneCleanupPeriodMs() {
		return zoneCleanupPeriodMs;
	}

	public int getZoneExpirationMs() {
		return zoneExpirationMs;
	}

	public int getGossipDataRefreshTimeMs() {
		return gossipDataRefreshTimeMs;
	}

	public LevelSelectionStrategyType getStrategy() {
		return strategy;
	}
}
