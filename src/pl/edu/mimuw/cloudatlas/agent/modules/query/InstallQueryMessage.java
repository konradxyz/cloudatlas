package pl.edu.mimuw.cloudatlas.agent.modules.query;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.Message;

public class InstallQueryMessage extends Message {
	private final String query;
	private final String queryName;

	public String getQuery() {
		return query;
	}

	public String getQueryName() {
		return queryName;
	}

	public InstallQueryMessage(String query, String queryName) {
		super();
		this.query = query;
		this.queryName = queryName;
	}

}
