package pl.edu.mimuw.cloudatlas.agent;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.ini4j.Ini;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleInitializationException;
import pl.edu.mimuw.cloudatlas.common.utils.IniUtils;

public class Main {

	public static void main(String[] args) throws UnknownHostException, SocketException {
		System.setProperty("java.security.policy", "file:./agent.policy");
		System.setProperty("java.rmi.server.hostname", "localhost");

		Ini config = IniUtils.readConfigFromArgs(args);
		if (config == null)
			return;
		CloudatlasAgentConfig agentConfig = CloudatlasAgentConfig
				.fromIni(config);
		if (agentConfig == null)
			return;
		CloudatlasAgentModulesFramework framework = new CloudatlasAgentModulesFramework(
				agentConfig);
		try {
			framework.initAndRun(4);
		} catch (InterruptedException | ModuleInitializationException e) {
			System.err.println("Unrecoverable error: '" + e.getMessage()
					+ "', shutting down.");
		}
	}
}
