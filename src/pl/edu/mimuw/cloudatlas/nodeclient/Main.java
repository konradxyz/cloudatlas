package pl.edu.mimuw.cloudatlas.nodeclient;

import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasAgentRmiServer;

public class Main {

	public static void main(String[] args) {
		NodeClient nodeClient = new NodeClient("localhost",
				CloudatlasAgentRmiServer.DEFAULT_PORT, 2000);
		nodeClient.run();
	}

}
