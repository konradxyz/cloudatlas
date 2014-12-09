package pl.edu.mimuw.cloudatlas.modules.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public final class ShutdownModule extends Module {
	public static final int INITIALIZE_SHUTDOWN = 0;

	private final ExecutorContext executorContext;

	private final MessageHandler<Message> shutdown_handler = new MessageHandler<Message>() {

		@Override
		public void handleMessage(Message message) {
			for (BlockingQueue<MessageWrapper> queue : executorContext
					.getImmutableExecutorQueues()) {
				queue.add(new MessageWrapper(Address.ANY,
						Executor.SHUTDOWN_TYPE, new Message(), getAddress()));
			}

		}
	};

	@Override
	protected Map<Integer, MessageHandler<?>> generateHandlers() {
		Map<Integer, MessageHandler<?>> result = new HashMap<Integer, MessageHandler<?>>();
		result.put(INITIALIZE_SHUTDOWN, shutdown_handler);
		return result;
	}

	public ShutdownModule(ExecutorContext executorContext, Address address) {
		super(address);
		this.executorContext = executorContext;
	}

}
