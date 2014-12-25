package pl.edu.mimuw.cloudatlas.nodeclient;

import org.ini4j.Ini;

import pl.edu.mimuw.cloudatlas.common.utils.IniUtils;

public class Main {

	public static void main(String[] args) {
		Ini config = IniUtils.readConfigFromArgs(args);
		String host = config.get("target", "host");
		int port = Integer.parseInt(config.get("target", "port"));
		int refreshPeriodMs = Integer.parseInt(config.get("timing",
				"refresh_period_ms"));

		NodeClient nodeClient = new NodeClient(host, port, refreshPeriodMs);
		nodeClient.run();
	}

}
