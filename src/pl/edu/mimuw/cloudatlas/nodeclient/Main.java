package pl.edu.mimuw.cloudatlas.nodeclient;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;

public class Main {

	public static void main(String[] args) {
		Ini config;
		try {
			config = new Ini(new File(args[0]));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not find, open or parse config file.");
			return;
		}
		String host = config.get("target", "host");
		int port = Integer.parseInt(config.get("target", "port"));
		int refreshPeriodMs = Integer.parseInt(config.get("timing",  "refresh_period_ms"));
		
		NodeClient nodeClient = new NodeClient(host, port, refreshPeriodMs);
		nodeClient.run();
	}

}
