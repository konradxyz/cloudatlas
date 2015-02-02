package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import java.util.List;

import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;

public class QueriesCommunicateInit extends QueriesCommunicate {

	public QueriesCommunicateInit(List<ValueQuery> queries, int gossipLevel, Certificate certificate) {
		super(queries, gossipLevel, certificate);
	}

	@Override
	public Type getType() {
		return Type.QUERIES_INIT;
	}

}
