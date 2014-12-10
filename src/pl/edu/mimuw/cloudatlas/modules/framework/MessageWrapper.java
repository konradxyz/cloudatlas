package pl.edu.mimuw.cloudatlas.modules.framework;

public final class MessageWrapper {
	private Address target;
	private int messageType;
	private Message message;
	private Address source;

	public MessageWrapper(Address target, int messageType, Message message, Address source) {
		this.target = target;
		this.messageType = messageType;
		this.message = message;
		this.source = source;
	}

	public Address getTarget() {
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

	public Address getSource() {
		return source;
	}
}
