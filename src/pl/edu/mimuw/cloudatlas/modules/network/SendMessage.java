package pl.edu.mimuw.cloudatlas.modules.network;

import java.net.InetAddress;

import pl.edu.mimuw.cloudatlas.modules.framework.SimpleMessage;

public final class SendMessage extends SimpleMessage<byte[]> {
	private InetAddress target;
	private int port;

	public SendMessage(byte[] content, InetAddress target, int port) {
		super(content);
		this.target = target;
		this.port = port;
	}

	public InetAddress getTarget() {
		return target;
	}

	public int getPort() {
		return port;
	}
}
