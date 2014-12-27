package pl.edu.mimuw.cloudatlas.agent.modules.query;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;

public class InstallQueryMessage extends SimpleMessage<ValueQuery> {

	public InstallQueryMessage(ValueQuery content) {
		super(content);
	}
	
}
