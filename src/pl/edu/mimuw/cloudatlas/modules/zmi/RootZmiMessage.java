package pl.edu.mimuw.cloudatlas.modules.zmi;

import pl.edu.mimuw.cloudatlas.model.ZMI;
import pl.edu.mimuw.cloudatlas.modules.framework.SimpleMessage;

public class RootZmiMessage extends SimpleMessage<ZMI> {

	public RootZmiMessage(ZMI content) {
		super(content);
	}

}
