package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import java.util.List;

import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;

public abstract class QueriesCommunicate extends GossipCommunicate {
	private final List<ValueQuery> queries;
	private final int gossipLevel;

	public QueriesCommunicate(List<ValueQuery> queries, int gossipLevel) {
		super();
		this.queries = queries;
		this.gossipLevel = gossipLevel;
	}

	public List<ValueQuery> getQueries() {
		return queries;
	}

	public int getGossipLevel() {
		return gossipLevel;
	}

}
