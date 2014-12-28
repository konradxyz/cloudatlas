package pl.edu.mimuw.cloudatlas.webclient;

import java.net.InetAddress;

import org.ini4j.Ini;

import pl.edu.mimuw.cloudatlas.common.utils.IniUtils;

public class Main {

	public static void main(String[] args) throws Exception {
		Ini config = IniUtils.readConfigFromArgs(args);
		final String rmiHost = config.get("agent", "host");
		final int rmiPort = Integer.parseInt(config.get("agent", "port"));
		final int port = Integer.parseInt(config.get("web", "port"));
		//Plot
		final int plotLenghtMs = Integer.parseInt(config.get("plot", "length_ms"));
		final int plotRefreshPeriodMs = Integer.parseInt(config.get("plot", "refresh_period_ms")); 
		
		final InetAddress signerAddr = InetAddress.getByName(config.get("querysigner", "host"));
		final int signerPort = Integer.parseInt(config.get("querysigner", "port"));

		WebClient client = new WebClient(rmiHost, rmiPort, port, plotLenghtMs, plotRefreshPeriodMs,
				new QuerySignerAddress(signerAddr, signerPort));
		client.initialize();
		client.run();
	}
}
