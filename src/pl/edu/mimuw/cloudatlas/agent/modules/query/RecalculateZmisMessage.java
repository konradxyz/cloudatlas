package pl.edu.mimuw.cloudatlas.agent.modules.query;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;
import pl.edu.mimuw.cloudatlas.common.model.PathName;
import pl.edu.mimuw.cloudatlas.common.model.ZMI;

public class RecalculateZmisMessage extends Message {
	private final ZMI rootZmi;
	private final PathName machineName;
	private final Address targetAddress;
	private final int targetMessageType;

	public RecalculateZmisMessage(ZMI rootZmi, PathName machineName,
			Address targetAddress, int targetMessageType) {
		super();
		this.rootZmi = rootZmi;
		this.machineName = machineName;
		this.targetAddress = targetAddress;
		this.targetMessageType = targetMessageType;
	}

	public Address getTargetAddress() {
		return targetAddress;
	}

	public int getTargetMessageType() {
		return targetMessageType;
	}

	public ZMI getRootZmi() {
		return rootZmi;
	}

	public PathName getMachineName() {
		return machineName;
	}
}
