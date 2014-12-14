package pl.edu.mimuw.cloudatlas.webclient;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.ini4j.Ini;

import pl.edu.mimuw.cloudatlas.agent.interpreter.MainInterpreter;
import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasAgentRmiServer;

public class Main {

	public static void main(String[] args) throws Exception {
		Ini config;
		try {
			config = new Ini(new File(args[0]));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not find, open or parse config file.");
			return;
		}
		final String rmiHost = config.get("agent", "host");
		final int rmiPort = Integer.parseInt(config.get("agent", "port"));
		final int port = Integer.parseInt(config.get("web", "port"));
		
		
		
		Server jettyServer = new Server(port);
		Handler h1 = new AbstractHandler() {

			@Override
			public void handle(String arg0, Request arg1,
					HttpServletRequest arg2, HttpServletResponse arg3)
					throws IOException, ServletException {
				Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
				CloudatlasAgentRmiServer stub;
				try {
					stub = (CloudatlasAgentRmiServer) registry
							.lookup("cloudatlas");
				} catch (NotBoundException e) {
					e.printStackTrace();
					throw new ServletException(e);
				}
				MainInterpreter.printZMIsToStream(stub.getRootZmi(), 
						new PrintStream(arg3.getOutputStream()));
				arg1.setHandled(true);
			}
		};

		jettyServer.setHandler(h1);
		jettyServer.start();
		jettyServer.join();
	}
}
