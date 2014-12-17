package pl.edu.mimuw.cloudatlas.agent.modules.timer;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;

public final class ScheduleAlarmMessage extends Message {
	private int delay;
	private int requestId;
	private int period;
	private Address target;
	private int gatewayMessageType;

	public ScheduleAlarmMessage(int delay, int requestId, int period, Address target, int gatewayMessageType) {
		this.delay = delay;
		this.requestId = requestId;
		this.period = period;
		this.target = target;
		this.gatewayMessageType = gatewayMessageType;
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
	
	public Address getTarget() {
		return target;
	}
	
	public int getGatewayMessageType() {
		return gatewayMessageType;
	}
}
