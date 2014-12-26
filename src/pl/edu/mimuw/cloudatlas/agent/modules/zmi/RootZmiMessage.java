package pl.edu.mimuw.cloudatlas.agent.modules.zmi;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.common.single_machine_model.ZmisAttributes;

public class RootZmiMessage extends SimpleMessage<ZmisAttributes> {

	public RootZmiMessage(ZmisAttributes content) {
		super(content);
	}

}
