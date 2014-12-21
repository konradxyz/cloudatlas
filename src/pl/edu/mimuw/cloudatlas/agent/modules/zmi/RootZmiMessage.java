package pl.edu.mimuw.cloudatlas.agent.modules.zmi;

import pl.edu.mimuw.cloudatlas.agent.model.SingleMachineZmiAttributes;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;

public class RootZmiMessage extends SimpleMessage<SingleMachineZmiAttributes> {

	public RootZmiMessage(SingleMachineZmiAttributes content) {
		super(content);
	}

}
