package pl.edu.mimuw.cloudatlas.agent.modules.timer;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;

public final class AlarmMessage extends Message {
	private int requestId;

	public AlarmMessage(int requestId) {
		this.requestId = requestId;
	}

	public int getRequestId() {
		return requestId;
	}
}
