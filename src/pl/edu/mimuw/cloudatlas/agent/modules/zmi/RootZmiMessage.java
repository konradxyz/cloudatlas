package pl.edu.mimuw.cloudatlas.agent.modules.zmi;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.common.model.ZMI;

public class RootZmiMessage extends SimpleMessage<ZMI> {

	public RootZmiMessage(ZMI content) {
		super(content);
	}

}
