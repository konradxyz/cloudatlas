package pl.edu.mimuw.cloudatlas.agent.modules.network;

import java.net.InetAddress;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;

public class ReceivedDatagramMessage extends SimpleMessage<byte[]> {
	private InetAddress source;

	public ReceivedDatagramMessage(byte[] content, InetAddress source) {
		super(content);
		this.source = source;
	}

	public InetAddress getSource() {
		return source;
	}
}
