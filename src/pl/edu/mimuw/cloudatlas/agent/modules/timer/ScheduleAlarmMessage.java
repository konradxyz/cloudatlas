package pl.edu.mimuw.cloudatlas.agent.modules.timer;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;

public final class ScheduleAlarmMessage extends Message {
	private int delay;
	private int requestId;

	public ScheduleAlarmMessage(int delay, int requestId) {
		this.delay = delay;
		this.requestId = requestId;
	}

	public int getDelay() {
		return delay;
	}

	public int getRequestId() {
		return requestId;
	}
}
