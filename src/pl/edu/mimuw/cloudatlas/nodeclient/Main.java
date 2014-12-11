package pl.edu.mimuw.cloudatlas.nodeclient;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

import pl.edu.mimuw.cloudatlas.common.model.ValueInt;
import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasAgentRmiServer;

public class Main {

	public static void main(String[] args) {
		Random r = new Random();
		while (true) {
			try {
				Registry registry = LocateRegistry.getRegistry("localhost",
						CloudatlasAgentRmiServer.DEFAULT_PORT);
				CloudatlasAgentRmiServer stub = (CloudatlasAgentRmiServer) registry
						.lookup("cloudatlas");
				stub.setCurrentNodeAttribute("random",
						new ValueInt(r.nextLong()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
