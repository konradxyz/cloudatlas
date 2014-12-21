package pl.edu.mimuw.cloudatlas.agent.modules.zmi;

import pl.edu.mimuw.cloudatlas.agent.model.ZmisAttributes;
import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;

public class RootZmiMessage extends SimpleMessage<ZmisAttributes> {

	public RootZmiMessage(ZmisAttributes content) {
		super(content);
	}

}
