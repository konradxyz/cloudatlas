package pl.edu.mimuw.cloudatlas.agent.modules.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public final class ExecutorContext implements Context {
	private Map<Address, BlockingQueue<MessageWrapper>> modulesQueues = 
			new HashMap<Address, BlockingQueue<MessageWrapper>>();
	private List<BlockingQueue<MessageWrapper>> executorsQueues = 
			new ArrayList<BlockingQueue<MessageWrapper>>();
	
	@Override
	public void sendMessage(MessageWrapper message) {
		try {
			modulesQueues.get(message.getTarget()).add(message);
		} catch (NullPointerException e) {
			System.err.println("ERROR: Could not send message to module "
					+ message.getTarget());
			System.err.println("ERROR: Message content " + message);
			System.err.println("ERROR: Really, it should not happen.");
			throw e;
		}
	}
	
	// All Executors must be registered before first executor starts to work.
	// This method is NOT synchronized.
	public void registerExecutor(List<Address> addresses,
			BlockingQueue<MessageWrapper> queue) {
		for ( Address i : addresses ) {
			modulesQueues.put(i,  queue);
		}
		executorsQueues.add(queue);
	}
	
	public Collection<BlockingQueue<MessageWrapper>> getImmutableExecutorQueues() {
		return Collections.unmodifiableList(executorsQueues);
	}
}
