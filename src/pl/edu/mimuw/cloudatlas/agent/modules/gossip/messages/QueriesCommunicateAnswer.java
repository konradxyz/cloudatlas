package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import java.util.List;

import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;

public class QueriesCommunicateAnswer extends QueriesCommunicate {

	public QueriesCommunicateAnswer(List<ValueQuery> queries, int gossipLevel) {
		super(queries, gossipLevel);
	}

	@Override
	public Type getType() {
		return Type.QUERIES_ANSWER;
	}

}
