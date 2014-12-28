package pl.edu.mimuw.cloudatlas.agent.modules.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.modules.gossip.ZmisFreshness;

public class ZmisFreshnessAnswerCommunicate extends ZmisFreshnessCommunicate {

	public ZmisFreshnessAnswerCommunicate(ZmisFreshness content, TravelTime time) {
		super(content, time);
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
