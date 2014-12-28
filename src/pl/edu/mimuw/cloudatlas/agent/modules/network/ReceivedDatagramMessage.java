package pl.edu.mimuw.cloudatlas.agent.modules.network;

import java.net.InetAddress;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;

public class ReceivedDatagramMessage extends SimpleMessage<byte[]> {
	private final InetAddress source;
	private final Long sentTimestampMs;
	private final Long receivedTimestampMs;

	public ReceivedDatagramMessage(byte[] content, InetAddress source,
			Long sentTimestampMs, Long receivedTimestampMs) {
		super(content);
		this.source = source;
		this.sentTimestampMs = sentTimestampMs;
		this.receivedTimestampMs = receivedTimestampMs;
	}

	public InetAddress getSource() {
		return source;
	}

	public Long getSentTimestampMs() {
		return sentTimestampMs;
	}

	public Long getReceivedTimestampMs() {
		return receivedTimestampMs;
	}
}
