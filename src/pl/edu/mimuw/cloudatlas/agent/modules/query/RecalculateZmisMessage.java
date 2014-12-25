package pl.edu.mimuw.cloudatlas.agent.modules.query;

import pl.edu.mimuw.cloudatlas.agent.model.SingleMachineZmiData;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;

public class RecalculateZmisMessage extends Message {
	private final SingleMachineZmiData<AttributesMap> zmi;
	private final Address targetAddress;
	private final int targetMessageType;

	public RecalculateZmisMessage(SingleMachineZmiData<AttributesMap> zmi,
			Address targetAddress, int targetMessageType) {
		super();
		this.zmi = zmi;
		this.targetAddress = targetAddress;
		this.targetMessageType = targetMessageType;
	}

	public Address getTargetAddress() {
		return targetAddress;
	}

	public int getTargetMessageType() {
		return targetMessageType;
	}

	public SingleMachineZmiData<AttributesMap> getRootZmi() {
		return zmi;
	}
}
