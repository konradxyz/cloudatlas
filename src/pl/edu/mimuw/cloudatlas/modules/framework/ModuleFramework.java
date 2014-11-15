package pl.edu.mimuw.cloudatlas.modules.framework;

import java.util.ArrayList;
import java.util.List;

public abstract class ModuleFramework {
	private ExecutorContext context = new ExecutorContext();
	private List<Executor> executors = new ArrayList<Executor>();
	private List<Thread> threads = new ArrayList<Thread>();
	
	
	public abstract List<List<Module>> getModules();
	public abstract MessageWrapper getInitializationMessage();
	
	public Module getShutdownModule() {
		return new ShutdownModule(context);
	}
	
	public void init() {
		List<List<Module>> modules = getModules();
		for ( List<Module> executorModules: modules ) {
			Executor e = new Executor();
			e.initialize(executorModules, context);
			executors.add(e);
		}
	}
	
	public void run() throws InterruptedException {
		for (Executor e : executors ) {
			Thread t  = new Thread(e);
			threads.add(t);
			t.start();
		}
		
		context.sendMessage(getInitializationMessage());
		System.err.println("Join");
		for (Thread t : threads ) {
			System.err.println("Join");
			t.join();
			System.err.println("after Join");

		}
		System.err.println("Join all");
	}
	
	public void initAndRun() throws InterruptedException {
		init();
		run();
	}
}
