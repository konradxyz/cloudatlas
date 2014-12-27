package pl.edu.mimuw.cloudatlas.webclient;

import org.ini4j.Ini;

import pl.edu.mimuw.cloudatlas.common.utils.IniUtils;

public class Main {

	public static void main(String[] args) throws Exception {
		Ini config = IniUtils.readConfigFromArgs(args);
		final String rmiHost = config.get("agent", "host");
		final int rmiPort = Integer.parseInt(config.get("agent", "port"));
		final int refreshPeriodMs = Integer.parseInt(config.get("agent", "refresh_period_ms"));
		final int port = Integer.parseInt(config.get("web", "port"));
		//Plot
		final int plotLenghtMs = Integer.parseInt(config.get("plot", "length_ms"));
		final int plotRefreshPeriodMs = Integer.parseInt(config.get("plot", "refresh_period_ms")); 

		WebClient client = new WebClient(rmiHost, rmiPort, port, refreshPeriodMs, plotLenghtMs, plotRefreshPeriodMs);
		client.initialize();
		client.run();
	}
}
