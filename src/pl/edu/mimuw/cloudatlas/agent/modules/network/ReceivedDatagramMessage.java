package pl.edu.mimuw.cloudatlas.agent.modules.network;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;

public class ReceivedDatagramMessage extends SimpleMessage<byte[]> {

	public ReceivedDatagramMessage(byte[] content) {
		super(content);
	}

}
