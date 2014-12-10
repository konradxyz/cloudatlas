package pl.edu.mimuw.cloudatlas.agent.modules.network;

import java.net.InetAddress;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;

public final class SendDatagramMessage extends SimpleMessage<byte[]> {
	private InetAddress target;
	private int port;

	public SendDatagramMessage(byte[] content, InetAddress target, int port) {
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
