package pl.edu.mimuw.cloudatlas.agent.modules.network;

import java.net.DatagramPacket;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;

public class ReceivedDatagramInternalMessage extends
		SimpleMessage<DatagramPacket> {
	private final Long receivedTimestampMs;

	public ReceivedDatagramInternalMessage(DatagramPacket content,
			Long receivedTimestampMs) {
		super(content);
		this.receivedTimestampMs = receivedTimestampMs;
	}

	public Long getReceivedTimestampMs() {
		return receivedTimestampMs;
	}
}
