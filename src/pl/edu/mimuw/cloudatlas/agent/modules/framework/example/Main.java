package pl.edu.mimuw.cloudatlas.agent.modules.framework.example;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleInitializationException;

public class Main {

	public static void main(String[] args) throws InterruptedException,
			ModuleInitializationException {
		System.setProperty("java.security.policy", "file:./agent.policy");
		System.setProperty("java.rmi.server.hostname", "localhost");

		ExampleModuleFramework framework = new ExampleModuleFramework();
		framework.initAndRun(1);
	}
}
