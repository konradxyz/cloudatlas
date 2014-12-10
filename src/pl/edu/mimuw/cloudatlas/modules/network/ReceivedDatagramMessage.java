package pl.edu.mimuw.cloudatlas.modules.network;

import pl.edu.mimuw.cloudatlas.modules.framework.SimpleMessage;

public class ReceivedDatagramMessage extends SimpleMessage<byte[]> {

	public ReceivedDatagramMessage(byte[] content) {
		super(content);
	}

}
