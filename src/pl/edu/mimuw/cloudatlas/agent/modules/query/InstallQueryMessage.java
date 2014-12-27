package pl.edu.mimuw.cloudatlas.agent.modules.query;

import java.util.Arrays;
import java.util.List;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;

public class InstallQueryMessage extends SimpleMessage<List<ValueQuery>> {

	public InstallQueryMessage(ValueQuery content) {
		super(Arrays.asList(content));
	}

	public InstallQueryMessage(List<ValueQuery> content) {
		super(content);
	}
	
}
