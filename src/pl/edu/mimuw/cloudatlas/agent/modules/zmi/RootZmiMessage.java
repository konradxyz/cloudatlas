package pl.edu.mimuw.cloudatlas.agent.modules.zmi;

import pl.edu.mimuw.cloudatlas.agent.model.SingleMachineZmiData;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.common.model.AttributesMap;

public class RootZmiMessage extends SimpleMessage<SingleMachineZmiData<AttributesMap>> {

	public RootZmiMessage(SingleMachineZmiData<AttributesMap> content) {
		super(content);
	}

}
