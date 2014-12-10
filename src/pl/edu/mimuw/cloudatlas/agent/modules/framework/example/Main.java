package pl.edu.mimuw.cloudatlas.agent.modules.framework.example;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.ModuleInitializationException;

public class Main {

	public static void main(String[] args) throws InterruptedException, ModuleInitializationException {
		ExampleModuleFramework framework = new ExampleModuleFramework();
		framework.initAndRun(1);
	}
}
