package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import java.util.List;

import pl.edu.mimuw.cloudatlas.common.Certificate;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;

public abstract class QueriesCommunicate extends WithCertificateCommunicate {
	private final List<ValueQuery> queries;

	
	public QueriesCommunicate(List<ValueQuery> queries, int gossipLevel, Certificate certificate) {
		super(certificate, gossipLevel);
		this.queries = queries;
	}
	
	public List<ValueQuery> getQueries() {
		return queries;
	}
	
}
