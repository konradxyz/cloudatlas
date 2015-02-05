package pl.edu.mimuw.cloudatlas.agent.modules.network;

import java.net.InetAddress;
import java.security.PrivateKey;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;

public final class SendDatagramMessage extends SimpleMessage<byte[]> {
	private InetAddress target;
	private int port;
	PrivateKey privateKey;

	public SendDatagramMessage(byte[] content, InetAddress target, int port, PrivateKey privateKey) {
		super(content);
		this.target = target;
		this.port = port;
		this.privateKey = privateKey;
	}

	public InetAddress getTarget() {
		return target;
	}

	public int getPort() {
		return port;
	}
	
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
}
