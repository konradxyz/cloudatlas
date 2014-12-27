package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import java.util.List;

import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;

public class QueriesCommunicate extends GossipCommunicate {
	private final List<ValueQuery> queries;

	@Override
	public Type getType() {
		return Type.QUERIES;
	}

	public QueriesCommunicate(List<ValueQuery> queries) {
		super();
		this.queries = queries;
	}

	public List<ValueQuery> getQueries() {
		return queries;
	}

}
