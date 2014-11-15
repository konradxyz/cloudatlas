package pl.edu.mimuw.cloudatlas.modules.framework;

import java.util.Map;

public abstract class Module {
	private Context context;
	private Map<Integer, MessageHandler<?>> handlers;
	
	public void sendMessage(int target, int messageType, Message message) {
		context.sendMessage(new MessageWrapper(target, messageType, message, getAddress()));
	}
	
	protected abstract Map<Integer, MessageHandler<?>> generateHandlers();
	protected abstract Integer getAddress();
	
	public final void init(Context ctx) {
		context = ctx;
		handlers = generateHandlers();
		initialize();
	}
	
	public void handleMessage(MessageWrapper wrapper) {
		MessageHandler<?> handler = handlers.get(wrapper.getMessageType());
		assert(handler != null);
		wrapper.getMessage();
		handler.handleUntypedMessage(wrapper.getMessage());
	}
	
	
	public void initialize() {}
	public void shutdown(){}
}
