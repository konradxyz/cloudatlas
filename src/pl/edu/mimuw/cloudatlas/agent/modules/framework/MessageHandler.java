package pl.edu.mimuw.cloudatlas.agent.modules.framework;

public abstract class MessageHandler<T extends Message> {
	public final void handleUntypedMessage(Message message) throws HandlerException {

		@SuppressWarnings("unchecked")
		T typedMessage = ((T) message);
		
		handleMessage(typedMessage);
	}
	
	public abstract void handleMessage(T message) throws HandlerException;
}
