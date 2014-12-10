package pl.edu.mimuw.cloudatlas.agent.modules.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class Executor implements Runnable {
	private Map<Address, Module> modules = new HashMap<Address, Module>();
	private BlockingQueue<MessageWrapper> queue;

	public static final int SHUTDOWN_TYPE = -1;

	public void initialize(List<Module> modules, ExecutorContext context)
			throws ModuleInitializationException {
		List<Address> addresses = new ArrayList<Address>();
		for (Module m : modules) {
			assert (!this.modules.containsKey(m.getAddress()));
			this.modules.put(m.getAddress(), m);
			m.init(context);
			addresses.add(m.getAddress());
		}
		queue = new LinkedBlockingQueue<MessageWrapper>();
		context.registerExecutor(addresses, queue);
	}

	// Executor must be properly initialized before it is run.
	@Override
	public void run() {
		boolean finished = false;
		while (!finished) {
			MessageWrapper wrapper = null;
			try {
				wrapper = queue.take();
				System.err.println(wrapper);

				if (wrapper.getMessageType() == Executor.SHUTDOWN_TYPE) {
					finished = true;
				} else {
					modules.get(wrapper.getTarget()).handleMessage(wrapper);
				}
			} catch (Exception e) {
				System.err.println(wrapper);
				e.printStackTrace(System.err);
			}
		}
		for (Module m : modules.values()) {
			m.shutdown();
		}
		System.err.println("Executor shutdown");
	}
}
