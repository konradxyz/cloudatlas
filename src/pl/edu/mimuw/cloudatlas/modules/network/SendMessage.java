package pl.edu.mimuw.cloudatlas.modules.network;

import java.net.InetAddress;

import pl.edu.mimuw.cloudatlas.modules.framework.SimpleMessage;

public final class SendMessage extends SimpleMessage<byte[]> {
	private InetAddress target;

	public SendMessage(byte[] content, InetAddress target) {
		super(content);
		this.target = target;
	}

	public InetAddress getTarget() {
		return target;
	}
}
