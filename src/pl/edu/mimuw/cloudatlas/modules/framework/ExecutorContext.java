package pl.edu.mimuw.cloudatlas.modules.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;


// TODO: maybe some state machine to make sure that all registerExecutor calls are
// executed before first sendMessage?
public final class ExecutorContext implements Context {
	private Map<Address, BlockingQueue<MessageWrapper>> modulesQueues = 
			new HashMap<Address, BlockingQueue<MessageWrapper>>();
	private List<BlockingQueue<MessageWrapper>> executorsQueues = 
			new ArrayList<BlockingQueue<MessageWrapper>>();
	
	@Override
	public void sendMessage(MessageWrapper message) {
		modulesQueues.get(message.getTarget()).add(message);
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
