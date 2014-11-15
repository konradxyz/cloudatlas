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
public class ExecutorContext extends Context {
	private Map<Integer, BlockingQueue<MessageWrapper>> modulesQueues = 
			new HashMap<Integer, BlockingQueue<MessageWrapper>>();
	private List<BlockingQueue<MessageWrapper>> executorsQueues = 
			new ArrayList<BlockingQueue<MessageWrapper>>();
	
	@Override
	public void sendMessage(MessageWrapper message) {
		modulesQueues.get(message.getTarget()).add(message);
	}
	
	// All Executors must be registered before first executor starts to work.
	// This method is NOT synchronized.
	public void registerExecutor(List<Integer> addresses,
			BlockingQueue<MessageWrapper> queue) {
		for ( Integer i : addresses ) {
			modulesQueues.put(i,  queue);
		}
		executorsQueues.add(queue);
	}
	
	public Collection<BlockingQueue<MessageWrapper>> getImmutableExecutorQueues() {
		return Collections.unmodifiableList(executorsQueues);
	}
}
