package pl.edu.mimuw.cloudatlas.modules.framework;

public class MessageWrapper {
	private int target;
	private int messageType;
	private Message message;
	private int source;

	public MessageWrapper(int target, int messageType, Message message, int source) {
		this.target = target;
		this.messageType = messageType;
		this.message = message;
		this.source = source;
	}

	public int getTarget() {
		return target;
	}

	public int getMessageType() {
		return messageType;
	}

	public Message getMessage() {
		return message;
	}
	@Override
	public String toString() {
		return "" + target + " " + source + " " + messageType + " " + message.toString();
	}

	public int getSource() {
		return source;
	}
}
