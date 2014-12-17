package pl.edu.mimuw.cloudatlas.agent.modules.timer;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;

public final class ScheduleAlarmMessage extends Message {
	private int delay;
	private int requestId;
	private int period;

	public ScheduleAlarmMessage(int delay, int requestId, int period) {
		this.delay = delay;
		this.requestId = requestId;
		this.period = period;
	}

	public int getDelay() {
		return delay;
	}

	public int getRequestId() {
		return requestId;
	}
	
	public int getPeriod() {
		return period;
	}
}
