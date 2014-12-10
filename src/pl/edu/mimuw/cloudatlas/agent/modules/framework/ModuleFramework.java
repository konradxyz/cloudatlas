package pl.edu.mimuw.cloudatlas.agent.modules.framework;

import java.util.ArrayList;
import java.util.List;

public abstract class ModuleFramework {
	private ExecutorContext context = new ExecutorContext();
	private List<Executor> executors = new ArrayList<Executor>();
	private List<Thread> threads = new ArrayList<Thread>();
	private Address rootAddress;

	public abstract Module getRootModule(Address rootAddress2,
			Address shutdownModuleAddress);

	public abstract Message getInitializationMessage();

	public abstract int getInitializationMessageType();

	public final void init(int executorsCount)
			throws ModuleInitializationException {
		assert (executorsCount > 0);
		AddressGenerator addressGenerator = new AddressGenerator();
		ShutdownModule shutdownModule = new ShutdownModule(context,
				addressGenerator.getUniqueAddress());
		rootAddress = addressGenerator.getUniqueAddress();
		Module root = getRootModule(rootAddress, shutdownModule.getAddress());
		List<Module> modules = new ArrayList<Module>();
		gatherModules(shutdownModule, addressGenerator, modules);
		gatherModules(root, addressGenerator, modules);
		List<List<Module>> executorModules = new ArrayList<List<Module>>();
		// Because Math.ceil is too mainstream.
		int modulesPerExecutor = Math.max(
				(modules.size() + 1) / executorsCount, 1);
		for (int i = 0; i < executorsCount; ++i) {
			executorModules.add(modules.subList(
					Math.min(i * modulesPerExecutor, modules.size()),
					Math.min((i + 1) * modulesPerExecutor, modules.size())));
		}
		System.out.println(modules.size());
		for (List<Module> singleExecutorModules : executorModules) {
			Executor e = new Executor();
			e.initialize(singleExecutorModules, context);
			executors.add(e);
		}
	}

	private final static void gatherModules(Module current,
			AddressGenerator addressGenerator, List<Module> result) {
		result.add(current);
		List<Module> submodules = current.getSubModules(addressGenerator);
		for (Module m : submodules) {
			gatherModules(m, addressGenerator, result);
		}
	}

	public final void run() throws InterruptedException {
		for (Executor e : executors) {
			Thread t = new Thread(e);
			threads.add(t);
			t.start();
		}
		System.out.println(rootAddress);
		context.sendMessage(new MessageWrapper(rootAddress,
				getInitializationMessageType(), getInitializationMessage(),
				Address.ANY));
		for (Thread t : threads) {
			t.join();
		}
	}

	public final void initAndRun(int executorsCount)
			throws InterruptedException, ModuleInitializationException {
		init(executorsCount);
		run();
	}
}
