package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.gossip.ZmisFreshness;
import pl.edu.mimuw.cloudatlas.common.Certificate;

public class ZmisFreshnessAnswerCommunicate extends ZmisFreshnessCommunicate {

	public ZmisFreshnessAnswerCommunicate(ZmisFreshness content, TravelTime time, Certificate certificate, Integer gossipLevel) {
		super(content, time, certificate, gossipLevel);
	}

	@Override
	public Type getType() {
		return Type.ZMIS_FRESHNESS_ANSWER;
	}

	@Override
	public String toString() {
		return "Answer: " + super.toString();
	}
}
