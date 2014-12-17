package pl.edu.mimuw.cloudatlas.agent.modules.query;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;

public class InstallQueryMessage extends Message {
	private final String query;

	public String getQuery() {
		return query;
	}

	public InstallQueryMessage(String query) {
		super();
		this.query = query;
	}

}
